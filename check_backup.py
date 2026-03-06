#!/usr/bin/env python3
import sqlite3
import sys
import os

def check_backup_db(db_path):
    if not os.path.exists(db_path):
        print(f"Database file not found: {db_path}")
        return
    
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # Check tables
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
        tables = cursor.fetchall()
        print("Tables in backup database:")
        for table in tables:
            print(f"  - {table[0]}")
        
        # Check receipt count
        cursor.execute("SELECT COUNT(*) FROM receipt_entity;")
        receipt_count = cursor.fetchone()[0]
        print(f"\nTotal receipts in backup: {receipt_count}")
        
        # Check latest receipt timestamps
        cursor.execute("""
            SELECT id, vendor_id, total_amount, transaction_date, created_at, updated_at 
            FROM receipt_entity 
            ORDER BY created_at DESC 
            LIMIT 5;
        """)
        latest_receipts = cursor.fetchall()
        
        print("\nLatest 5 receipts in backup:")
        for receipt in latest_receipts:
            print(f"  ID: {receipt[0]}, Vendor ID: {receipt[1]}, Amount: {receipt[2]}, "
                  f"Transaction: {receipt[3]}, Created: {receipt[4]}, Updated: {receipt[5]}")
        
        # Check if there are any receipts
        if receipt_count > 0:
            cursor.execute("SELECT MIN(created_at), MAX(created_at) FROM receipt_entity;")
            min_max = cursor.fetchone()
            print(f"\nReceipt date range: {min_max[0]} to {min_max[1]}")
        
        conn.close()
        
    except Exception as e:
        print(f"Error examining database: {e}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        check_backup_db(sys.argv[1])
    else:
        print("Usage: python check_backup.py <database_file>")
        print("Trying to find backup file...")
        
        # Try to find the backup file
        possible_paths = [
            "backup.db",
            "receipt_keeper_backup.db",
            "/sdcard/Download/雪松堡账本/receipt_keeper_backup.db"
        ]
        
        for path in possible_paths:
            if os.path.exists(path):
                check_backup_db(path)
                break
        else:
            print("No backup database file found.")