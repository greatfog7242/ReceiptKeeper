# ReceiptKeeper - User Manual

**Version:** 1.0.0

ReceiptKeeper is a mobile application for scanning, organizing, and analyzing receipts. It uses OCR (Optical Character Recognition) to automatically extract information from receipt images, helping you track spending across different categories, vendors, and time periods.

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Navigation Overview](#navigation-overview)
3. [Books](#books)
4. [Receipts](#receipts)
5. [Scanning Receipts](#scanning-receipts)
6. [Analytics](#analytics)
7. [Settings](#settings)
8. [Exporting Data](#exporting-data)
9. [Tips & Tricks](#tips--tricks)

---

## Getting Started

### First Launch

When you first open ReceiptKeeper:

1. The app will create a local database on your device
2. Default categories will be automatically set up (Food, Grocery, Hardware, Entertainment, Transportation, Utilities, Healthcare, Other)
3. You're ready to start adding receipts!

### Adding Your First Receipt

You can add receipts in two ways:

1. **Manual Entry**: Tap the + button on the Receipts screen
2. **Scan a Receipt**: Use the Scan tab to photograph a receipt

---

## Navigation Overview

The app uses a bottom navigation bar with 5 main sections:

| Tab | Icon | Description |
|-----|------|-------------|
| Books | :books: | Organize receipts into collections |
| Receipts | :receipt: | View all receipts |
| Scan | :camera: | Scan receipts with camera |
| Analytics | :chart: | View spending insights |
| Settings | :gear: | App configuration |

---

## Books

Books help you organize receipts into separate collections (e.g., "Personal", "Business", "Travel").

### Creating a Book

1. Navigate to the **Books** tab
2. Tap the **+** button
3. Enter a **name** for the book (e.g., "Business Expenses")
4. Optionally add a **description**
5. Tap **Save**

### Managing Books

- **View receipts in a book**: Tap on a book card to see all receipts within it
- **Edit a book**: Tap the edit icon on the book card
- **Delete a book**: Swipe left on the book card, then tap Delete
  - Note: Deleting a book will also delete all receipts within it

### Book Information

Each book card displays:
- Book name
- Total spending amount
- Number of receipts

---

## Receipts

The Receipts screen shows all your receipts organized by date.

### Viewing Receipts

- Receipts are grouped by date (e.g., "Today", "Yesterday", "January 2025")
- Tap a date header to expand/collapse that group
- Each receipt shows: vendor name, category icon, amount, and thumbnail image

### Filtering by Book

Use the dropdown menu at the top to filter receipts by:
- All Books
- A specific book

### Adding a Receipt Manually

1. Tap the **+** (FAB) button on the Receipts screen
2. Fill in the receipt details:
   - **Vendor**: Select from existing vendors or create a new one
   - **Amount**: Enter the total amount
   - **Date**: Select the transaction date
   - **Book**: Choose which book to categorize the receipt
   - **Category**: Select a category (Food, Grocery, etc.)
   - **Payment Method** (optional): Select how you paid
   - **Notes** (optional): Add any additional notes
   - **Image** (optional): Attach a receipt photo from your gallery
3. Tap **Save**

### Editing a Receipt

1. Tap on a receipt to open its details
2. Tap the **Edit** button
3. Modify any fields
4. Tap **Save**

### Deleting a Receipt

- **Swipe left** on a receipt in the list, then tap Delete
- Or tap the receipt, then tap the Delete button

### Viewing Receipt Details

Tap on any receipt to see:
- Full-size receipt image (tap to enlarge)
- Vendor name and icon
- Category
- Book
- Payment method
- Transaction date
- Amount
- Notes
- OCR extracted text (if scanned)

### Downloading Receipt Image

In the receipt detail view:
1. Tap the receipt image to open full-screen view
2. Tap the **download icon** to save the image to your device's Downloads folder

---

## Scanning Receipts

The Scan feature uses OCR (Optical Character Recognition) to automatically extract information from receipt photos.

### How to Scan a Receipt

1. Navigate to the **Scan** tab
2. Point your camera at the receipt
3. Tap the **capture button** to take a photo
4. Review the captured image
5. If satisfied, tap **Use Photo** (or retake)
6. Wait for OCR processing to complete
7. Review and edit the extracted information:
   - **Vendor**: Usually auto-detected
   - **Amount**: Total amount is extracted
   - **Date**: Transaction date is extracted
8. Fill in any remaining fields:
   - Select a **Book**
   - Select a **Category**
   - Select **Payment Method** (optional)
   - Add **Notes** (optional)
9. Tap **Save Receipt**

### OCR Tips

- Ensure good lighting for better recognition
- Keep the receipt flat and in focus
- The entire receipt should be visible in the frame
- You can always manually correct any auto-extracted values

### Viewing Raw OCR Text

If you want to see the exact text extracted by OCR:

1. After scanning, tap **Show OCR Text** to expand
2. This shows all text recognized from the image

### Manual Entry from Scan

Even if OCR doesn't recognize everything correctly, you can:
- Manually correct any field
- Use the image as a reference
- Add or edit any information

---

## Analytics

The Analytics screen provides insights into your spending patterns.

### Date Range

At the top of the screen, select a time period:

- **This Week**
- **This Month**
- **This Year**
- **Last 7 Days**
- **Last 30 Days**
- **Last 90 Days**
- **Custom Range**

### Total Spending Card

The top card displays:
- **Total spending** for the selected period
- **Number of receipts**

### Category Breakdown

The category chart shows spending by category:
- **Pie Chart**: Visual representation of proportions
- **Stacked Bar Chart**: Compare categories side by side
- **Treemap**: Hierarchical view of spending

Tap on the chart type buttons to switch views.

Each category shows:
- Category icon and name
- Amount spent
- Percentage of total

### Vendor Breakdown

Similar to category breakdown, this chart shows spending by vendor (store/merchant).

### Spending Goals

If you've set spending goals in Settings, they'll appear in Analytics:
- Shows your current spending vs. goal amount
- Visual progress bar
- Color indicator (green = on track, red = over budget)

---

## Settings

The Settings screen allows you to manage app configuration and data.

### Vendors

Manage the list of vendors (stores/merchants) that appear when adding receipts.

**Adding a Vendor:**
1. Go to **Settings** > **Vendors**
2. Tap the **+** button
3. Enter the **vendor name**
4. Optionally select a **brand logo** (built-in or custom)
5. Tap **Save**

**Brand Logos:**
- The app includes logos for popular stores (Walmart, Target, McDonald's, etc.)
- You can also upload custom brand images

**Editing/Deleting Vendors:**
- Tap on a vendor to edit
- Swipe left to delete

### Categories

Manage expense categories for organizing receipts.

**Default Categories (pre-installed):**
- Food
- Grocery
- Hardware
- Entertainment
- Transportation
- Utilities
- Healthcare
- Other

**Adding a Custom Category:**
1. Go to **Settings** > **Categories**
2. Tap the **+** button
3. Enter the **category name**
4. Choose a **color** (20 predefined colors)
5. Choose an **icon** from Material icons
6. Tap **Save**

### Payment Methods

Track how you pay for purchases.

**Adding a Payment Method:**
1. Go to **Settings** > **Payment Methods**
2. Tap the **+** button
3. Enter the **name** (e.g., "Visa", "Cash", "Bank Account")
4. Select the **type** (Cash, Credit Card, Debit Card, Other)
5. Optionally enter **last 4 digits** for card tracking
6. Tap **Save**

### Spending Goals

Set budget targets to track your spending.

**Creating a Goal:**
1. Go to **Settings** > **Spending Goals**
2. Tap the **+** button
3. Enter the **budget amount**
4. Select the **period**:
   - Daily
   - Weekly
   - Monthly
   - Yearly
5. Optionally select a **category** (leave empty for overall goal)
6. Tap **Save**

**Viewing Progress:**
- Goals appear in the Analytics screen
- Shows current spending vs. target with a progress bar

### About

View app information:
- **Version**: The app version number
- **Build**: The specific build identifier

---

## Exporting Data

You can export your receipt data to CSV format for use in spreadsheets or backup.

### How to Export

1. Navigate to the **Analytics** tab
2. Select your desired **date range**
3. Tap the **Export** button

### Export Output

The export creates a folder in your device's Downloads folder:
- **Folder name**: `ReceiptKeeper_YYYYMMDD_HHMMSS` (e.g., `ReceiptKeeper_20260227_143022`)

Contents:
- `receipts.csv` - CSV file with all receipt data
- `images/` - Subfolder containing all receipt images

### CSV Format

The CSV file includes these columns:
- Date
- Vendor
- Category
- Book
- Payment Method
- Amount
- Notes
- Image Filename

---

## Tips & Tricks

### Efficient Receipt Scanning

1. **Good Lighting**: Scan in well-lit areas for better OCR accuracy
2. **Flat Receipt**: Ensure the receipt is flat, not crumpled
3. **Full Frame**: Fill the camera view with the receipt
4. **Steady Hand**: Hold steady or use a tripod for clear photos

### Organizing Tips

1. **Use Books**: Create separate books for different purposes (Business, Personal, Travel)
2. **Consistent Categories**: Stick to consistent categories for accurate analytics
3. **Regular Scanning**: Scan receipts immediately after purchases to avoid forgetting

### Data Management

1. **Regular Exports**: Export your data periodically for backup
2. **Review Analytics**: Check your spending patterns weekly/monthly
3. **Set Goals**: Use spending goals to stay on budget

### Image Handling

- Receipt images are stored locally on your device
- You can download any receipt image to your gallery
- Images are included in CSV exports

---

## Troubleshooting

### OCR Not Working Well

- Ensure the receipt is clearly visible
- Try adjusting the lighting
- Manually correct any misread values

### App Not Responding

- Try closing and reopening the app
- Restart your device
- Check for app updates

### Export Issues

- Ensure you have storage permissions
- Check that your Downloads folder has available space

---

## Data Privacy

- All data is stored **locally** on your device
- No data is sent to external servers
- Receipt images stay on your device
- You have full control over your data

---

## Technical Information

- **Database**: Local SQLite database (Room)
- **OCR**: Google ML Kit Text Recognition
- **Architecture**: MVVM with Jetpack Compose
- **Minimum Android Version**: Android 8.0 (API 26)

---

*Last Updated: February 2026*
