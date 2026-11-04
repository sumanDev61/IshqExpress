#!/usr/bin/env python3
"""
Turn an AI-generated / stock gift clip into the app's motion-gift format:
a side-by-side MP4 (left = colour, right = alpha mask) that AlphaVideoView plays with transparency.

  python make_gift.py in.mp4 out.mp4 --key green            # chroma key (auto-detects the green)
  python make_gift.py in.mp4 out.mp4 --key black            # glow/fire clips on black: luma key
  python make_gift.py in.mp4 out.mp4 --key green --clouds   # + procedural cloud bank and intro curtain
  python make_gift.py in.mp4 out.mp4 --speed 1.25           # 24fps source -> 30fps, 25% faster

Needs ffmpeg on PATH and numpy + pillow (pip install numpy pillow).
"""
import argparse
import json
import subprocess
import sys

import numpy as np
from PIL import Image


# ----------------------------------------------------------------------------- ffmpeg io

def probe(path):
    out = subprocess.check_output([
        "ffprobe", "-v", "error", "-select_streams", "v:0",
        "-show_entries", "stream=width,height,r_frame_rate,nb_frames:format=duration",
        "-of", "json", path])
    j = json.loads(out)
    s = j["streams"][0]
    num, den = s["r_frame_rate"].split("/")
    return int(s["width"]), int(s["height"]), float(num) / float(den), float(j["format"]["duration"])


def read_frames(path, w, h):
    p = subprocess.Popen(["ffmpeg", "-v", "error", "-i", path, "-f", "rawvideo", "-pix_fmt", "rgb24", "-"],
                         stdout=subprocess.PIPE)
    n = w * h * 3
    while True:
        buf = p.stdout.read(n)
        if len(buf) < n:
            break
        yield np.frombuffer(buf, np.uint8).reshape(h, w, 3)
    p.wait()


def writer(path, w, h, fps, crf):
    return subprocess.Popen([
        "ffmpeg", "-v", "error", "-y",
        "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", f"{w * 2}x{h}", "-r", f"{fps}", "-i", "-",
        "-an", "-c:v", "libx264", "-preset", "slow", "-crf", str(crf), "-profile:v", "high",
        "-pix_fmt", "yuv420p", "-movflags", "+faststart", path], stdin=subprocess.PIPE)


# ----------------------------------------------------------------------------- keying

def detect_key(frame):
    """Median of the border pixels = the screen colour."""
    b = np.concatenate([frame[:8].reshape(-1, 3), frame[-8:].reshape(-1, 3),
                        frame[:, :8].reshape(-1, 3), frame[:, -8:].reshape(-1, 3)])
    return np.median(b, 0)


def key_green(rgb, key):
    """Returns straight colour (float 0..1) and alpha, with edge colour un-mixed from the screen."""
    c = rgb.astype(np.float32)
    d = c[..., 1] - np.maximum(c[..., 0], c[..., 2])          # how "green" each pixel is
    dk = key[1] - max(key[0], key[2])
    lo, hi = 0.12 * dk, 0.80 * dk
    a = np.clip(1.0 - (d - lo) / (hi - lo), 0.0, 1.0)
    # Un-mix: observed = a*F + (1-a)*K  ->  F = (observed - (1-a)K) / a
    k = key.astype(np.float32)
    f = (c - (1.0 - a[..., None]) * k) / np.maximum(a, 1e-3)[..., None]
    f = np.clip(f, 0, 255)
    # Despill whatever green is left on solid pixels.
    f[..., 1] = np.minimum(f[..., 1], np.maximum(f[..., 0], f[..., 2]) + 6)
    return f / 255.0, a


def key_black(rgb, _key):
    """Additive-style clips (fire, glow, fireworks) on black: brightness becomes alpha."""
    c = rgb.astype(np.float32) / 255.0
    m = c.max(-1)
    a = np.clip((m - 0.03) / 0.97, 0, 1) ** 0.85
    f = np.clip(c / np.maximum(m, 1e-3)[..., None] * np.minimum(1.0, m / np.maximum(a, 1e-3))[..., None], 0, 1)
    return f, a


# ----------------------------------------------------------------------------- noise / clouds

def fbm(h, w, rng, base=96, octaves=5, billow=0.5):
    """Fractal value noise, 0..1. `billow` mixes in |noise| creases for puffy cloud edges."""
    total = np.zeros((h, w), np.float32)
    amp, norm, cell = 1.0, 0.0, base
    for _ in range(octaves):
        gh, gw = max(2, h // cell + 3), max(2, w // cell + 3)
        g = rng.random((gh, gw)).astype(np.float32)
        img = Image.fromarray(g, "F").resize((gw * cell, gh * cell), Image.BICUBIC)
        n = np.asarray(img)[:h, :w]
        n = (1 - billow) * n + billow * (1 - np.abs(2 * n - 1))
        total += amp * n
        norm += amp
        amp *= 0.5
        cell = max(2, cell // 2)
    return np.clip(total / norm, 0, 1)


def smooth(e0, e1, x):
    t = np.clip((x - e0) / (e1 - e0), 0, 1)
    return t * t * (3 - 2 * t)


def ease_out(t):
    t = np.clip(t, 0, 1)
    return 1 - (1 - t) ** 3


def render_puffs(w, h, puffs, rng, shadow, light, tex_amp=22.0, ambient=0.25, blur=6):
    """
    Cumulus-style cloud mass from overlapping spheres: a height map (max of hemispheres) gives
    normals for 3D lighting, and fractal noise roughens the silhouette so edges read as fluffy.
    Returns straight colour (h, w, 3) and alpha (h, w).
    """
    H = np.full((h, w), -1e3, np.float32)
    for (cx, cy, r) in puffs:
        x0, x1 = int(max(0, cx - r)), int(min(w, cx + r + 1))
        y0, y1 = int(max(0, cy - r)), int(min(h, cy + r + 1))
        if x0 >= x1 or y0 >= y1:
            continue
        xs = np.arange(x0, x1, dtype=np.float32)[None, :] - cx
        ys = np.arange(y0, y1, dtype=np.float32)[:, None] - cy
        d2 = 1.0 - (xs * xs + ys * ys) / (r * r)
        hz = np.where(d2 > 0, r * np.sqrt(np.maximum(d2, 0)), -1e3)
        H[y0:y1, x0:x1] = np.maximum(H[y0:y1, x0:x1], hz)
    tex = fbm(h, w, rng, base=72, octaves=4, billow=0.55)
    Hc = np.where(H > -1e2, H, -40.0)
    Ht = Hc + (tex - 0.5) * tex_amp * 2
    # Soft, wispy silhouette: noise only erodes the edge, it doesn't crumple the body.
    alpha = smooth(-4.0, 34.0, Ht)
    # Lighting from upper-left on a blurred height map, so the body shades like soft vapour.
    hs = np.maximum(Hc, 0)
    small = Image.fromarray(hs, "F").resize((max(2, w // blur), max(2, h // blur)), Image.BILINEAR)
    hs = np.asarray(small.resize((w, h), Image.BICUBIC)) * 0.8 + (tex - 0.5) * 10
    gy, gx = np.gradient(hs)
    nx, ny, nz = -gx, -gy, np.ones_like(gx)
    inv = 1.0 / np.sqrt(nx * nx + ny * ny + nz * nz)
    L = np.array([-0.35, -0.80, 0.50], np.float32)
    L /= np.linalg.norm(L)
    lam = np.clip((nx * L[0] + ny * L[1] + nz * L[2]) * inv, 0, 1)
    k = np.clip(ambient + (1 - ambient) * lam, 0, 1) * (0.93 + 0.14 * (tex - 0.5))
    k = np.clip(k, 0, 1)[..., None]
    col = shadow * (1 - k) + light * k
    return col.astype(np.float32), alpha.astype(np.float32)


class Clouds:
    """
    Cloud set for gifts that stand on something:
      - back + front cumulus banks drifting in opposite directions, with translucent wispy tops
      - a soft haze where the banks meet the air, so the top edge never reads as a row of balls
      - an intro of see-through mist that parts from the centre and evaporates
    The canvas can be taller than the subject (pad_bottom): the subject sits at the top, the banks
    carry on below it and fade out, which lifts the subject off the screen's bottom edge.
    """

    def __init__(self, w, subject_h, canvas_h, duration, seed=7):
        rng = np.random.default_rng(seed)
        h = canvas_h
        self.w, self.h, self.sh, self.dur = w, h, subject_h, duration
        sh = subject_h
        self.pad = pad = 360
        W = w + pad
        peach = np.array([0.97, 0.76, 0.66], np.float32)
        rose = np.array([0.98, 0.86, 0.83], np.float32)
        cream = np.array([1.0, 0.97, 0.94], np.float32)
        white = np.array([1.0, 0.99, 0.98], np.float32)
        self.ys = np.arange(h, dtype=np.float32)[:, None]
        self.xs = np.arange(w, dtype=np.float32)[None, :]
        ysW = self.ys

        def bank(base_y, rmin, rmax, n, rows):
            puffs = []
            for row in range(rows):
                for i in range(n):
                    cx = (i + rng.random() * 0.8) * W / n
                    cy = base_y + row * rmax * 0.55 + rng.normal(0, rmin * 0.25)
                    puffs.append((cx, cy, rng.uniform(rmin, rmax) * (1 + 0.25 * row)))
            return puffs

        def soften(layer, top_y):
            c, a = layer
            # Upper part of the bank is thinner vapour; it thickens with depth.
            depth = smooth(top_y - 30, top_y + 130, ysW)
            wisp = fbm(h, W, rng, base=40, octaves=4, billow=0.3)
            a = a * (0.72 + 0.28 * depth) * (0.96 + 0.12 * (wisp - 0.5))
            # Carry on below the subject, then fade out softly at the canvas bottom.
            a = a * (1 - smooth(sh + 30, h - 4, ysW))
            return c, np.clip(a, 0, 1)

        self.back = soften(render_puffs(W, h, bank(sh * 0.80, 60, 125, 16, 4), rng, peach, cream), sh * 0.76)
        fc, fa = render_puffs(W, h, bank(sh * 0.99, 45, 95, 20, 4), rng, rose, white, ambient=0.35)
        fc, fa = soften((fc, fa), sh * 0.92)
        # The subject's frame ends at sh (a pedestal is cut there): keep the bank solid across that line.
        band = smooth(sh * 0.955, sh * 0.99, ysW) * (1 - smooth(sh + 40, h - 4, ysW))
        self.front = (fc, np.maximum(fa, band * np.ones_like(fa)))

        # Haze over the bank tops.
        hz = fbm(h, W, rng, base=130, octaves=4, billow=0.2)
        prof = np.exp(-((ysW - sh * 0.80) / (sh * 0.09)) ** 2)
        self.haze = (np.broadcast_to(cream, (h, W, 3)).copy(), np.clip(smooth(0.35, 0.8, hz) * prof * 0.55, 0, 1))

        # Intro mist: large soft fbm, not spheres, so it reads as translucent vapour.
        MW = w + 2 * pad
        big = fbm(h, MW, rng, base=230, octaves=4, billow=0.0)
        det = fbm(h, MW, rng, base=48, octaves=3, billow=0.4)
        self.mist_d = (big * 0.85 + det * 0.15).astype(np.float32)
        lit = np.clip(0.75 + (det - 0.5) * 0.6 + (big - 0.5) * 0.4, 0, 1)[..., None]
        self.mist_c = (np.array([0.90, 0.84, 0.88], np.float32) * (1 - lit) + white * lit).astype(np.float32)

    def _slice(self, layer, off):
        c, a = layer
        return c[:, off:off + self.w], a[:, off:off + self.w]

    def layers(self, t):
        """Returns (back, haze, front, mist) as (colour, alpha) pairs for time t (seconds)."""
        rise = ease_out(t / 1.3)
        # Fade out together with the subject's sparkle dissolve (no sinking: it would expose the cut pedestal).
        env = (1 - smooth(self.dur - 1.05, self.dur - 0.1, t))
        speed = 22.0
        bo = int(np.clip(t * speed, 0, self.pad - 1))
        fo = int(np.clip(self.pad - 1 - t * speed * 1.7, 0, self.pad - 1))
        ho = int(np.clip(t * speed * 0.6, 0, self.pad - 1))
        bc, ba = self._slice(self.back, bo)
        fc, fa = self._slice(self.front, fo)
        hc, ha = self._slice(self.haze, ho)
        sink = int((1 - rise) * self.sh * 0.16)
        if sink:
            bc, ba = np.roll(bc, sink, 0), np.roll(ba, sink, 0)
            fc, fa = np.roll(fc, sink, 0), np.roll(fa, sink, 0)
            hc, ha = np.roll(hc, sink, 0), np.roll(ha, sink, 0)
            ba[:sink] = 0
            fa[:sink] = 0
            ha[:sink] = 0
        ba = ba * env * 0.95
        fa = fa * env
        ha = ha * env * rise

        mist = (None, None)
        p = float(smooth(0.0, 2.1, t))
        if p < 1:
            half = self.w / 2
            shift = int(p * self.pad * 0.95)
            d = self.mist_d
            left = d[:, self.pad + shift:self.pad + shift + self.w]
            right = d[:, self.pad - shift:self.pad - shift + self.w]
            lc = self.mist_c[:, self.pad + shift:self.pad + shift + self.w]
            rc = self.mist_c[:, self.pad - shift:self.pad - shift + self.w]
            # Cross-fade the two halves over the centre so there is no seam.
            wl = 1 - smooth(half - 110, half + 110, self.xs)
            md = wl * left + (1 - wl) * right
            mc = wl[..., None] * lc + (1 - wl[..., None]) * rc
            # Opens from the centre outwards...
            dist = np.abs(self.xs - half) / half
            e = p * 1.45 - 0.4          # starts fully closed, ends past the screen edges
            opening = smooth(e - 0.2, e + 0.05, dist + (md - 0.5) * 0.6)
            # ...while the vapour thins out (higher threshold eats the thin parts first).
            th = 0.02 + p * 0.85
            ma = smooth(th, th + 0.28, md) * 0.86 * opening
            # Slight upward drift.
            up = int(t * 50)
            if up:
                ma = np.roll(ma, -up, 0)
                mc = np.roll(mc, -up, 0)
            mist = (mc, ma)
        return (bc, ba), (hc, ha), (fc, fa), mist

    def glow(self, t):
        env = smooth(0.6, 1.6, t) * (1 - smooth(self.dur - 1.6, self.dur - 0.3, t))
        cx, cy, r = self.w * 0.5, self.sh * 0.6, self.w * 0.85
        d = np.sqrt((self.xs - cx) ** 2 + ((self.ys - cy) * 0.8) ** 2) / r
        a = np.clip(1 - d, 0, 1) ** 2 * 0.40 * env
        col = np.array([1.0, 0.72, 0.80], np.float32)
        return np.broadcast_to(col, (self.h, self.w, 3)), a


def over(dst_c, dst_a, src_c, src_a):
    """Straight-alpha 'source over destination'."""
    sa = src_a[..., None]
    da = dst_a[..., None]
    out_a = sa + da * (1 - sa)
    out_c = (src_c * sa + dst_c * da * (1 - sa)) / np.maximum(out_a, 1e-4)
    return out_c, out_a[..., 0]


def bleed(c, a, k=8):
    """Fill fully transparent pixels with nearby colour so YUV420 chroma doesn't drag dark fringes in."""
    h, w = a.shape
    hh, ww = h // k * k, w // k * k
    pc = (c * a[..., None])[:hh, :ww].reshape(hh // k, k, ww // k, k, 3).mean((1, 3))
    pa = a[:hh, :ww].reshape(hh // k, k, ww // k, k).mean((1, 3))
    fill = pc / np.maximum(pa, 1e-4)[..., None]
    fill = np.repeat(np.repeat(fill, k, 0), k, 1)
    out = c.copy()
    m = a[:hh, :ww] < 0.03
    out[:hh, :ww][m] = fill[m]
    return out


# ----------------------------------------------------------------------------- main

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("src")
    ap.add_argument("dst")
    ap.add_argument("--key", choices=["green", "black"], default="green")
    ap.add_argument("--clouds", action="store_true", help="add cloud banks + intro curtain")
    ap.add_argument("--speed", type=float, default=1.0, help="playback speed; output fps = source fps * speed")
    ap.add_argument("--crf", type=int, default=17)
    ap.add_argument("--pad-bottom", type=int, default=0,
                    help="extra transparent rows under the subject (lifts it off the screen bottom); clouds fill them")
    ap.add_argument("--preview", help="also write a PNG contact sheet over a dark room-like background")
    args = ap.parse_args()

    w, h, fps, dur = probe(args.src)
    out_fps = fps * args.speed
    out_dur = dur / args.speed
    keyer = key_green if args.key == "green" else key_black
    H = h + args.pad_bottom
    H += H % 2
    clouds = Clouds(w, h, H, out_dur) if args.clouds else None
    enc = writer(args.dst, w, H, out_fps, args.crf)
    key = None
    previews = []
    n_total = int(dur * fps + 0.5)

    for i, frame in enumerate(read_frames(args.src, w, h)):
        if key is None:
            key = detect_key(frame) if args.key == "green" else np.zeros(3)
            print(f"{w}x{h} @ {fps:g}fps -> {out_fps:g}fps, {out_dur:.2f}s, key {key}", file=sys.stderr)
        t = i / out_fps
        c, a = keyer(frame, key)
        if H != h:
            c = np.concatenate([c, np.zeros((H - h, w, 3), np.float32)], 0)
            a = np.concatenate([a, np.zeros((H - h, w), np.float32)], 0)
        if clouds:
            gc, ga = clouds.glow(t)
            (bc, ba), (hc, ha), (fc, fa), (mc, ma) = clouds.layers(t)
            oc, oa = over(gc, ga, bc, ba)
            oc, oa = over(oc, oa, hc, ha)
            oc, oa = over(oc, oa, c, a)
            oc, oa = over(oc, oa, fc, fa)
            if mc is not None:
                oc, oa = over(oc, oa, mc, ma)
            c, a = oc, oa
        # Last few frames fade out so nothing pops off when the player clears.
        a = a * (1 - smooth(out_dur - 0.35, out_dur - 0.03, t))
        c = bleed(c, a)
        left = (np.clip(c, 0, 1) * 255 + 0.5).astype(np.uint8)
        right = np.repeat((np.clip(a, 0, 1) * 255 + 0.5).astype(np.uint8)[..., None], 3, -1)
        enc.stdin.write(np.concatenate([left, right], 1).tobytes())
        if args.preview and i % max(1, int(out_fps / 2)) == 0:
            bg = np.zeros((H, w, 3), np.float32) + np.linspace([0.30, 0.12, 0.45], [0.16, 0.06, 0.28], H)[:, None, :]
            comp = c * a[..., None] + bg * (1 - a[..., None])
            previews.append(Image.fromarray((comp * 255).astype(np.uint8)).resize((w // 4, H // 4)))
        if i % 24 == 0:
            print(f"  frame {i}/{n_total}", file=sys.stderr)

    enc.stdin.close()
    enc.wait()
    if args.preview and previews:
        cols = 9
        rows = (len(previews) + cols - 1) // cols
        pw, ph = previews[0].size
        sheet = Image.new("RGB", (pw * cols, ph * rows))
        for j, im in enumerate(previews):
            sheet.paste(im, ((j % cols) * pw, (j // cols) * ph))
        sheet.save(args.preview)
    print("done:", args.dst, file=sys.stderr)


if __name__ == "__main__":
    main()
