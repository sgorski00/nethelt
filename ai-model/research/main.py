import argparse

from ping import generate_raw, aggregate_windows

def main():
    parser = argparse.ArgumentParser(description="Nethelt AI model fake data generator")
    parser.add_argument("--ping", action="store_true", help="Run Ping data generation with data aggregation")
    parser.add_argument("--aggregate-only", action="store_true",  help="Run only data aggregation on existing raw ping data")
    parser.add_argument("--source", help="Source raw filename. File should be in output directory")
    args = parser.parse_args()

    raw_ping_filename = args.source if args.source is not None else "raw_ping.csv"
    if args.ping:
        generate_raw.run()
        aggregate_windows.run(raw_ping_filename)
    elif args.aggregate_only:
        aggregate_windows.run(raw_ping_filename)

if __name__ == "__main__":
    main()
