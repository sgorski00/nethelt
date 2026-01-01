import argparse

def main():
    parser = argparse.ArgumentParser(description="Nethelt AI model fake data generator")
    parser.add_argument("--ping", action="store_true", help="Run Ping data generation")
    parser.add_argument("--source", help="Source raw filename. File should be in output directory")
    args = parser.parse_args()

    if args.ping:
        from ping import generate_raw
        from ping import aggregate_windows
        #generate_raw.run()
        raw_ping_filename = args.source if args.source is not None else "raw_ping.csv"
        aggregate_windows.run(raw_ping_filename)

if __name__ == "__main__":
    main()
