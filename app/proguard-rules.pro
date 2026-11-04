# R8 rules for the release build. Libraries in use (Compose, CameraX, Media3, ML Kit, Play Billing,
# Coil, ZXing) ship their own consumer rules; add app-specific keeps here only when needed.

# Keep line numbers so Play Console / Android vitals crash stacks stay readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
