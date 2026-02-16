# Receipt Organizer

A Android Studio mobile application for scanning, organizing, and analyzing receipts built with Android Stud.

## Overview

Receipt Organizer helps you digitize and manage your paper receipts. Simply scan a receipt using your device's camera, and the app will automatically extract key information like vendor name, date, and total amount using OCR (Optical Character Recognition). Organize receipts into books, categorize expenses, track payment methods, and analyze spending patterns.

## Features

### Receipt Scanning & OCR
  Core OCR Engine

- **Camera Capture** - Take photos directly within the app
- **Photo Picker** - Select existing photos from your gallery
- **Automatic Text Extraction** - Uses ML Kit OCR to extract:
  - Vendor name (from receipt header)
  - Transaction date (supports multiple date formats)
  - Total amount (recognizes "Total", "Amount Due", etc.)
  - Payment method & card last 4 digits
- **Manual Override** - Edit any extracted information
- **Receipt Image Storage** - Save original receipt photos locally

### Receipt Management
- View all receipts in one place with total spending summary
- Filter and browse receipts by book
- Swipe-to-delete functionality
- View detailed receipt information with full-size image
- Edit receipt details (amount, date, vendor, category, payment method, notes)
- Delete receipts

### Book Organization
- Create multiple "books" (folders) to organize receipts
- Each book shows:
  - Book name and description
  - Receipt count
  - Total spending amount
- Edit and delete books
- Add receipts directly to specific books

### Categories
- Pre-seeded with 8 default categories:
  - Food
  - Grocery
  - Hardware
  - Entertainment
  - Transportation
  - Utilities
  - Healthcare
  - Other
- Each category has an associated color
- Add, edit, and delete custom categories
- View spending breakdown by category

### Payment Methods
- Track payment types:
  - Cash
  - Credit Card
  - Other
- Store last 4 digits of cards for reference
- Add, edit, and delete payment methods

### Analytics & Reporting
- **Date Range Filtering** - View spending for custom date ranges
- **Total Spending Summary** - See total amount and receipt count
- **Spending Goals** - Set daily, weekly, monthly, or yearly budget goals
- **Goal Progress** - Visual progress bar showing budget usage
- **Category Breakdown** - View spending distribution by category
- **Export Functionality** - Export data to CSV format

### Settings Management
- Manage Vendors - Add, edit, delete vendor names
- Manage Categories - Customize expense categories
- Manage Payment Methods - Track payment options
- View app version information

## Pages & Screens

### Main Navigation
The app uses a custom bottom tab bar with 5 main sections:
1. **Books** - Organize receipts into folders
2. **Receipts** - View all receipts
3. **Scan** - Capture new receipts (center tab, highlighted)
4. **Analytics** - View spending insights
5. **Settings** - App configuration

### Books Page (`BooksPage`)
- Grid layout showing all books
- Each book card displays: name, description, receipt count
- Edit and delete buttons on each book
- "New Book" button to create a new book
- Pull-to-refresh functionality

### Book Detail Page (`BookDetailPage`)
- Header showing total spending and receipt count
- List of receipts in the selected book
- Swipe-to-delete receipts
- Tap to view receipt details

### Receipts Page (`ReceiptsPage`)
- Summary header with total spending
- List view of all receipts with thumbnails
- Each receipt shows: vendor, date, book, category, amount
- Swipe-to-delete functionality
- Pull-to-refresh

### Receipt Detail Page (`ReceiptDetailPage`)
- Full-size receipt image
- Editable fields:
  - Total Amount
  - Transaction Date
  - Vendor (picker)
  - Category (picker)
  - Payment Method (picker)
  - Notes
- Toggle between view and edit mode
- Save and Delete buttons in edit mode

### Scan Receipt Page (`ScanReceiptPage`)
- Image preview area
- "Take Photo" button - captures image via camera
- "Pick Photo" button - selects from gallery
- OCR processing indicator
- Form fields (auto-filled from OCR):
  - Book selection (required)
  - Vendor name(picker)
  - Total amount (required)
  - Transaction date
  - Category(picker)
  - Payment method(picker)
  - Notes
- Extracted text display (debug view)
- Save and Clear buttons

### Analytics Page (`AnalyticsPage`)
- Date range pickers (start/end)
- Refresh button to reload data
- Total spending summary card
- Spending goal card with progress bar
- Category breakdown display
- Export buttons (CSV)

### Settings Page (`SettingsPage`)
- Data Management section:
  - Vendors - manage vendor list
  - Categories - manage categories
  - Payment Methods - manage payment options
- About section:
  - App version information

### Vendors Page (`VendorsPage`)
- List of all vendors
- Swipe actions: Edit, Delete
- Add new vendor button
- Pull-to-refresh

### Categories Page (`CategoriesPage`)
- List of categories with color indicators
- Swipe actions: Edit, Delete
- Add new category button
- Pull-to-refresh

### Payment Methods Page (`PaymentMethodsPage`)
- List of payment methods with type badges
- Shows payment type and name
- Last 4 digits displayed if available
- Swipe actions: Edit, Delete
- Add new payment method button
- Pull-to-refresh