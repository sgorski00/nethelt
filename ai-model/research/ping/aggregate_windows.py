import pandas as pd
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

def run(raw_ping_filename):
    df = pd.read_csv(BASE_DIR / "output" / raw_ping_filename, parse_dates=["timestamp"])
    df["window"] = df["timestamp"].dt.floor("1min")
    grouped = df.groupby("window")

    windows = []
    prev_avg = None

    for window, g in grouped:
        successful = g[g["success"] == 1]

        if len(successful) > 0:
            ping_avg = successful["ping_ms"].mean()
        else:
            ping_avg = float("nan")

        # odchylenie nie ma sensu przy mniejszej liczbie danych i zaburza działąnie modelu
        if len(successful) >= 3:
            ping_std = successful["ping_ms"].std()
        else:
            ping_std = float("nan")

        packet_loss = 1 - len(successful) / len(g)
        ping_diff = 0 if prev_avg is None else ping_avg - prev_avg
        prev_avg = ping_avg

        windows.append({
            "window_start": window,
            "device_group": g["device_group"].iloc[0],
            "ping_avg_1m": round(ping_avg, 3),
            "ping_std_1m": round(ping_std, 3),
            "packet_loss_1m": round(packet_loss, 3),
            "ping_diff_1m": round(ping_diff, 3)
        })

    out = pd.DataFrame(windows)

    out["ping_avg_15m"] = out["ping_avg_1m"].rolling(15, min_periods=5).mean()
    out["ping_std_15m"] = out["ping_avg_1m"].rolling(15, min_periods=5).std()

    out["ping_zscore_15m"] = (
            (out["ping_avg_1m"] - out["ping_avg_15m"]) /
            out["ping_std_15m"]
    )
    out["ping_zscore_15m"] = round(out["ping_zscore_15m"].fillna(0.0),3)

    out = out.drop(columns=["ping_avg_15m", "ping_std_15m"])

    out.to_csv(BASE_DIR / "output" / "ping_windows_1m.csv", index=False)

    print("Aggregated windows saved")
