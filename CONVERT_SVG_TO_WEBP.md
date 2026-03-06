# Converting SVG Files to WebP for Bottom Navigation Icons

## Why WebP?
- **Perfect color preservation**: WebP images retain all colors from your SVG files
- **No tint issues**: Unlike vector drawables, WebP doesn't get auto-tinted by Android
- **Exact rendering**: Looks exactly like your original SVG design
- **Small file size**: Efficient compression for app size

## Files Needed
Convert these SVG files to WebP (512x512 resolution):
1. `book-svgrepo-com.svg` → `ic_bottom_books.webp`
2. `receipt-part-2-svgrepo-com.svg` → `ic_bottom_receipts.webp`
3. `camera-svgrepo-com.svg` → `ic_bottom_scan.webp`
4. `analytics-chart-diagram-pie-svgrepo-com.svg` → `ic_bottom_analytics.webp`
5. `settings-svgrepo-com.svg` → `ic_bottom_settings.webp`

## Conversion Methods

### Method 1: Android Studio (Recommended)
1. Open Android Studio
2. Right-click on `app/src/main/res/drawable` folder
3. Select `New` → `Image Asset`
4. Choose `Image` type (not Launcher Icon)
5. Select your SVG file
6. Set `Resize` to 512x512
7. Set `Output Format` to WebP
8. Click `Next` → `Finish`

### Method 2: Online Converter
1. Go to https://convertio.co/svg-webp/ or similar
2. Upload each SVG file
3. Set resolution to 512x512
4. Download as WebP
5. Place in `app/src/main/res/drawable/`

### Method 3: Command Line (if you have tools)
```bash
# Using ImageMagick
magick input.svg -resize 512x512 output.webp

# Or using cwebp (from WebP tools)
rsvg-convert -w 512 -h 512 input.svg -o temp.png
cwebp -q 80 temp.png -o output.webp
```

## File Placement
Place all converted WebP files in:
```
app/src/main/res/drawable/
```

## Expected Files
After conversion, you should have:
- `app/src/main/res/drawable/ic_bottom_books.webp`
- `app/src/main/res/drawable/ic_bottom_receipts.webp`
- `app/src/main/res/drawable/ic_bottom_scan.webp`
- `app/src/main/res/drawable/ic_bottom_analytics.webp`
- `app/src/main/res/drawable/ic_bottom_settings.webp`

## Testing
After adding WebP files:
1. Build the app: `./gradlew.bat assembleDebug`
2. Install on device: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. Verify icons appear with correct colors

## Notes
- The scan icon will be larger (42.dp) as per existing design
- WebP files support transparency automatically
- No code changes needed beyond what's already implemented
- The app will use WebP if available, fallback to vector drawables if not