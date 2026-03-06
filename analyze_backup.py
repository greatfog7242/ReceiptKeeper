#!/usr/bin/env python3
import sqlite3
import sys
import os
import tempfile

def analyze_database(db_bytes):
    """Analyze SQLite database from bytes"""
    # Write bytes to temp file
    with tempfile.NamedTemporaryFile(suffix='.db', delete=False) as tmp:
        tmp.write(db_bytes)
        tmp_path = tmp.name
    
    try:
        conn = sqlite3.connect(tmp_path)
        cursor = conn.cursor()
        
        print("=== DATABASE ANALYSIS ===")
        
        # 1. Check SQLite header
        with open(tmp_path, 'rb') as f:
            header = f.read(100)
            print(f"SQLite header (first 100 bytes):")
            print(f"  Magic: {header[:16]}")
            print(f"  Page size: {int.from_bytes(header[16:18], 'big')}")
            print(f"  File format: {header[18:20].hex()}")
            print(f"  Reserved: {header[20:28].hex()}")
            print(f"  Max embedded payload: {header[28]}")
            print(f"  Min embedded payload: {header[29]}")
            print(f"  Leaf payload: {header[30]}")
            print(f"  File change counter: {int.from_bytes(header[24:28], 'big')}")
        
        # 2. Check all tables
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;")
        tables = cursor.fetchall()
        print(f"\n=== TABLES ({len(tables)}) ===")
        for table in tables:
            table_name = table[0]
            print(f"\nTable: {table_name}")
            
            # Get row count
            try:
                cursor.execute(f"SELECT COUNT(*) FROM {table_name};")
                count = cursor.fetchone()[0]
                print(f"  Rows: {count}")
                
                # Get column info
                cursor.execute(f"PRAGMA table_info({table_name});")
                columns = cursor.fetchall()
                print(f"  Columns: {len(columns)}")
                for col in columns:
                    col_id, col_name, col_type, notnull, default_val, pk = col
                    print(f"    {col_id}. {col_name} ({col_type}) {'PK' if pk else ''}")
                
                # Show sample data for receipt_entity
                if table_name == 'receipt_entity' and count > 0:
                    print(f"\n  Sample receipts (first 5):")
                    cursor.execute(f"""
                        SELECT id, vendor_id, total_amount, transaction_date, created_at, updated_at 
                        FROM {table_name} 
                        ORDER BY created_at DESC 
                        LIMIT 5;
                    """)
                    receipts = cursor.fetchall()
                    for rec in receipts:
                        print(f"    ID: {rec[0]}, Vendor: {rec[1]}, Amount: ${rec[2]:.2f}, "
                              f"Date: {rec[3]}, Created: {rec[4]}, Updated: {rec[5]}")
            
            except Exception as e:
                print(f"  Error reading table: {e}")
        
        # 3. Check database size info
        print(f"\n=== DATABASE INFO ===")
        cursor.execute("PRAGMA page_count;")
        page_count = cursor.fetchone()[0]
        cursor.execute("PRAGMA page_size;")
        page_size = cursor.fetchone()[0]
        total_size = page_count * page_size
        print(f"Page size: {page_size} bytes")
        print(f"Page count: {page_count}")
        print(f"Total size: {total_size:,} bytes")
        print(f"File size: {len(db_bytes):,} bytes")
        
        # 4. Check if database is valid
        print(f"\n=== VALIDITY CHECKS ===")
        try:
            cursor.execute("PRAGMA integrity_check;")
            integrity = cursor.fetchone()[0]
            print(f"Integrity check: {integrity}")
        except:
            print("Integrity check failed")
        
        conn.close()
        
    finally:
        # Clean up temp file
        os.unlink(tmp_path)

if __name__ == "__main__":
    print("This script needs to be run with the database file.")
    print("Trying to read from stdin...")
    
    # Try to read from stdin (for adb pipe)
    import sys
    if not sys.stdin.isatty():
        db_bytes = sys.stdin.buffer.read()
        if db_bytes:
            analyze_database(db_bytes)
        else:
            print("No data received from stdin")
    else:
        print("No database data provided")
        print("\nUsage examples:")
        print("  adb shell 'cat /path/to/database.db' | python analyze_backup.py")
        print("  python analyze_backup.py < database.db")