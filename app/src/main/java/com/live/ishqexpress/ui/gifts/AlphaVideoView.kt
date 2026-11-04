package com.live.ishqexpress.ui.gifts

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.TextureView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Plays a "motion gift": an MP4 whose left half is the colour image and right half is the alpha
 * mask (greyscale). A GL shader recombines them so the effect plays with real transparency over the
 * live video. This is the same file layout AlphaPlayer/VAP-style tools produce, so designer exports
 * drop straight in under assets/gifts/.
 *
 * TextureView (not SurfaceView) so it composites normally inside Compose and can be transparent.
 */
class AlphaVideoView(context: Context) : TextureView(context), TextureView.SurfaceTextureListener {

    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    private var renderer: Renderer? = null
    private var pendingAsset: String? = null
    private val main = Handler(Looper.getMainLooper())

    /** Called once on the main thread when playback ends or fails. `true` = played successfully. */
    var onFinished: ((Boolean) -> Unit)? = null
    private var finished = false

    init {
        isOpaque = false
        surfaceTextureListener = this
    }

    fun play(assetPath: String) {
        pendingAsset = assetPath
        surfaceTexture?.let { startRenderer(it, width, height) }
    }

    fun release() {
        val h = handler
        val r = renderer
        renderer = null
        handler = null
        if (h != null && r != null) h.post { r.release() }
        thread?.quitSafely()
        thread = null
    }

    private fun finish(ok: Boolean) = main.post {
        if (!finished) { finished = true; onFinished?.invoke(ok) }
    }

    private fun startRenderer(st: SurfaceTexture, w: Int, h: Int) {
        val asset = pendingAsset ?: return
        if (renderer != null) return
        val t = HandlerThread("alpha-video").also { it.start() }
        thread = t
        val h2 = Handler(t.looper)
        handler = h2
        val r = Renderer(st, w, h, asset)
        renderer = r
        h2.post {
            try { r.start() } catch (e: Throwable) {
                Log.w(TAG, "Motion gift failed: $asset", e)
                r.release(); finish(false)
            }
        }
    }

    override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) = startRenderer(st, w, h)
    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) { handler?.post { renderer?.resize(w, h) } }
    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean { release(); return true }
    override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

    /* ------------------------------------------------------------------------------------------ */

    private inner class Renderer(
        private val output: SurfaceTexture,
        private var viewW: Int,
        private var viewH: Int,
        private val asset: String,
    ) : SurfaceTexture.OnFrameAvailableListener {

        private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
        private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
        private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
        private var program = 0
        private var oesTex = 0
        private var videoTexture: SurfaceTexture? = null
        private var videoSurface: Surface? = null
        private var player: MediaPlayer? = null
        private var videoW = 0
        private var videoH = 0
        private val stMatrix = FloatArray(16)
        private val quad: FloatBuffer = ByteBuffer.allocateDirect(QUAD.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(QUAD); position(0) }
        private var released = false

        fun start() {
            setupEgl()
            program = buildProgram()
            oesTex = IntArray(1).also { GLES20.glGenTextures(1, it, 0) }[0]
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTex)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            val vt = SurfaceTexture(oesTex).also { it.setOnFrameAvailableListener(this, handler) }
            videoTexture = vt
            videoSurface = Surface(vt)

            // Clear to fully transparent so nothing flashes before the first frame.
            clear(); EGL14.eglSwapBuffers(display, eglSurface)

            val afd = context.assets.openFd(asset)
            player = MediaPlayer().apply {
                afd.use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
                setSurface(videoSurface)
                setOnVideoSizeChangedListener { _, w, h -> videoW = w; videoH = h }
                setOnCompletionListener { handler?.post { clear(); EGL14.eglSwapBuffers(display, eglSurface) }; finish(true) }
                setOnErrorListener { _, what, extra -> Log.w(TAG, "MediaPlayer error $what/$extra"); finish(false); true }
                prepare()
                start()
            }
        }

        fun resize(w: Int, h: Int) { viewW = w; viewH = h }

        override fun onFrameAvailable(st: SurfaceTexture) {
            if (released) return
            st.updateTexImage()
            st.getTransformMatrix(stMatrix)
            draw()
        }

        private fun clear() {
            GLES20.glViewport(0, 0, viewW, viewH)
            GLES20.glClearColor(0f, 0f, 0f, 0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        }

        private fun draw() {
            clear()
            if (videoW > 0 && videoH > 0) {
                // Fit the colour half to the full view width and anchor it to the bottom (GL y = 0),
                // so gifts that stand on clouds/ground sit at the screen's bottom edge on any aspect
                // ratio; taller phones get empty space above, shorter ones crop the top.
                val halfW = videoW / 2f
                val drawH = (viewW * videoH / halfW).toInt()
                GLES20.glViewport(0, 0, viewW, drawH)
            }
            GLES20.glUseProgram(program)
            val pos = GLES20.glGetAttribLocation(program, "aPos")
            val uv = GLES20.glGetAttribLocation(program, "aUv")
            quad.position(0)
            GLES20.glVertexAttribPointer(pos, 2, GLES20.GL_FLOAT, false, 16, quad)
            GLES20.glEnableVertexAttribArray(pos)
            quad.position(2)
            GLES20.glVertexAttribPointer(uv, 2, GLES20.GL_FLOAT, false, 16, quad)
            GLES20.glEnableVertexAttribArray(uv)
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uSt"), 1, false, stMatrix, 0)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTex)
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTex"), 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            EGL14.eglSwapBuffers(display, eglSurface)
        }

        private fun setupEgl() {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val ver = IntArray(2)
            check(EGL14.eglInitialize(display, ver, 0, ver, 1)) { "eglInitialize failed" }
            val attrs = intArrayOf(
                EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val num = IntArray(1)
            check(EGL14.eglChooseConfig(display, attrs, 0, configs, 0, 1, num, 0) && num[0] > 0) { "No RGBA8888 EGL config" }
            eglContext = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0)
            eglSurface = EGL14.eglCreateWindowSurface(display, configs[0], output, intArrayOf(EGL14.EGL_NONE), 0)
            check(EGL14.eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) { "eglMakeCurrent failed" }
        }

        private fun buildProgram(): Int {
            fun shader(type: Int, src: String) = GLES20.glCreateShader(type).also {
                GLES20.glShaderSource(it, src); GLES20.glCompileShader(it)
                val ok = IntArray(1); GLES20.glGetShaderiv(it, GLES20.GL_COMPILE_STATUS, ok, 0)
                check(ok[0] != 0) { "Shader: " + GLES20.glGetShaderInfoLog(it) }
            }
            return GLES20.glCreateProgram().also {
                GLES20.glAttachShader(it, shader(GLES20.GL_VERTEX_SHADER, VERTEX))
                GLES20.glAttachShader(it, shader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT))
                GLES20.glLinkProgram(it)
            }
        }

        fun release() {
            if (released) return
            released = true
            runCatching { player?.stop() }
            player?.release(); player = null
            videoSurface?.release(); videoSurface = null
            videoTexture?.release(); videoTexture = null
            if (display != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                EGL14.eglDestroySurface(display, eglSurface)
                EGL14.eglDestroyContext(display, eglContext)
                EGL14.eglTerminate(display)
            }
        }
    }

    companion object {
        private const val TAG = "AlphaVideoView"

        // x, y, u, v — (0,0) at bottom-left; the SurfaceTexture matrix handles orientation.
        private val QUAD = floatArrayOf(
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            -1f, 1f, 0f, 1f,
            1f, 1f, 1f, 1f,
        )

        private const val VERTEX = """
            attribute vec2 aPos;
            attribute vec2 aUv;
            varying vec2 vUv;
            void main() { vUv = aUv; gl_Position = vec4(aPos, 0.0, 1.0); }
        """

        /** Left half = colour, right half = alpha mask. Output is premultiplied for TextureView. */
        const val FRAGMENT = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vUv;
            uniform samplerExternalOES uTex;
            uniform mat4 uSt;
            void main() {
                vec2 cUv = (uSt * vec4(vUv.x * 0.5, vUv.y, 0.0, 1.0)).xy;
                vec2 aUv = (uSt * vec4(vUv.x * 0.5 + 0.5, vUv.y, 0.0, 1.0)).xy;
                vec3 rgb = texture2D(uTex, cUv).rgb;
                float a = texture2D(uTex, aUv).r;
                gl_FragColor = vec4(rgb * a, a);
            }
        """
    }
}
