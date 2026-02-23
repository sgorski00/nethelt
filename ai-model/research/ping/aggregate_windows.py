import pandas as pd
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

def run(raw_ping_filename):
    df = pd.read_csv(BASE_DIR / "output" / raw_ping_filename, parse_dates=["timestamp"])
    df["window"] = df["timestamp"].dt.floor("1min")
    grouped = df.groupby("window")

    windows = []

    for window, g in grouped:
        successful = g[g["success"] == 1]

        # odchylenie nie ma sensu przy mniejszej liczbie danych i zaburza działąnie modelu
        if len(successful) >= 3:
            ping_std = successful["ping_ms"].std()
        else:
            ping_std = float("nan")

        packet_loss = 1 - len(successful) / len(g)

        windows.append({
            "window_start": window,
            "device_group": g["device_group"].iloc[0],
            "ping_std_1m": round(ping_std, 3),
            "packet_loss_1m": round(packet_loss, 3)
        })

    out = pd.DataFrame(windows)

    out.to_csv(BASE_DIR / "output" / "ping_windows_1m.csv", index=False)

    print("Aggregated windows saved")
