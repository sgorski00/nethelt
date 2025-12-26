import argparse

def main():
    parser = argparse.ArgumentParser(description="Nethelt AI model fake data generator")
    parser.add_argument("--ping", action="store_true", help="Run Ping data generation")
    args = parser.parse_args()

    if args.ping:
        from ping import generate_raw
        from ping import aggregate_windows
        generate_raw.run()
        aggregate_windows.run()

if __name__ == "__main__":
    main()
