#!/usr/bin/env python3
import sys

def check_sqlite_header(data):
    """Check SQLite database header"""
    if len(data) < 100:
        print(f"Data too short: {len(data)} bytes")
        return False
    
    header = data[:100]
    
    # Check SQLite magic
    magic = header[:16]
    if magic != b'SQLite format 3\x00':
        print(f"Invalid SQLite magic: {magic}")
        return False
    
    print("=== SQLite Header Analysis ===")
    print(f"Magic: {magic}")
    
    # Page size (bytes 16-17, big-endian)
    page_size = int.from_bytes(header[16:18], 'big')
    print(f"Page size: {page_size} bytes")
    
    # File format write version (byte 18)
    write_version = header[18]
    print(f"Write version: {write_version} (1=legacy, 2=WAL)")
    
    # File format read version (byte 19)
    read_version = header[19]
    print(f"Read version: {read_version} (1=legacy, 2=WAL)")
    
    # Reserved space at end of each page (byte 20)
    reserved_space = header[20]
    print(f"Reserved space: {reserved_space} bytes")
    
    # Maximum embedded payload fraction (byte 21)
    max_payload = header[21]
    print(f"Max payload fraction: {max_payload}")
    
    # Minimum embedded payload fraction (byte 22)
    min_payload = header[22]
    print(f"Min payload fraction: {min_payload}")
    
    # Leaf payload fraction (byte 23)
    leaf_payload = header[23]
    print(f"Leaf payload fraction: {leaf_payload}")
    
    # File change counter (bytes 24-27, big-endian)
    change_counter = int.from_bytes(header[24:28], 'big')
    print(f"File change counter: {change_counter}")
    
    # Database size in pages (bytes 28-31, big-endian)
    page_count = int.from_bytes(header[28:32], 'big')
    print(f"Page count: {page_count}")
    
    # First freelist page (bytes 32-35, big-endian)
    freelist_page = int.from_bytes(header[32:36], 'big')
    print(f"First freelist page: {freelist_page}")
    
    # Total number of freelist pages (bytes 36-39, big-endian)
    freelist_count = int.from_bytes(header[36:40], 'big')
    print(f"Freelist pages: {freelist_count}")
    
    # Schema cookie (bytes 40-43, big-endian)
    schema_cookie = int.from_bytes(header[40:44], 'big')
    print(f"Schema cookie: {schema_cookie}")
    
    # Schema format number (bytes 44-47, big-endian)
    schema_format = int.from_bytes(header[44:48], 'big')
    print(f"Schema format: {schema_format}")
    
    # Default page cache size (bytes 48-51, big-endian)
    cache_size = int.from_bytes(header[48:52], 'big')
    print(f"Page cache size: {cache_size}")
    
    # Auto-vacuum flag (bytes 52-55, big-endian)
    auto_vacuum = int.from_bytes(header[52:56], 'big')
    print(f"Auto-vacuum: {auto_vacuum}")
    
    # Text encoding (bytes 56-59, big-endian)
    encoding = int.from_bytes(header[56:60], 'big')
    encoding_names = {1: 'UTF-8', 2: 'UTF-16le', 3: 'UTF-16be'}
    print(f"Text encoding: {encoding} ({encoding_names.get(encoding, 'unknown')})")
    
    # User version (bytes 60-63, big-endian)
    user_version = int.from_bytes(header[60:64], 'big')
    print(f"User version: {user_version}")
    
    # Incremental vacuum mode (bytes 64-67, big-endian)
    inc_vacuum = int.from_bytes(header[64:68], 'big')
    print(f"Incremental vacuum: {inc_vacuum}")
    
    # Application ID (bytes 68-71, big-endian)
    app_id = int.from_bytes(header[68:72], 'big')
    print(f"Application ID: {app_id}")
    
    # Check if file looks truncated
    expected_size = page_size * page_count
    actual_size = len(data)
    print(f"\nExpected size (page_size * page_count): {expected_size:,} bytes")
    print(f"Actual size: {actual_size:,} bytes")
    
    if actual_size < expected_size:
        print(f"WARNING: File appears truncated! Missing {expected_size - actual_size:,} bytes")
        return False
    
    if actual_size > expected_size:
        print(f"WARNING: File larger than expected! Extra {actual_size - expected_size:,} bytes")
    
    return True

if __name__ == "__main__":
    # Read from stdin
    data = sys.stdin.buffer.read()
    
    if not data:
        print("No data received")
        sys.exit(1)
    
    print(f"Total data size: {len(data):,} bytes")
    
    if check_sqlite_header(data):
        print("\n✓ SQLite header looks valid")
    else:
        print("\n✗ SQLite header has issues")