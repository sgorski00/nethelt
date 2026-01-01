import csv
import datetime
import yaml
import random
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

def run():
    with open(BASE_DIR / "config.yaml") as f:
        config = yaml.safe_load(f)

    device_id = config["device_id"]
    device_group = config["device_group"]
    interval = config["interval_seconds"]
    duration = config["duration_minutes"]

    normal = config["normal"]
    degradation = config["degradation"]
    failure = config["failure"]

    start_time = datetime.datetime.now()
    current_time = start_time

    rows = []
    loss_state = 0.0 # narastający packet loss
    total_pings = int(duration * 60.0 / interval)

    for i in range(total_pings):
        minute = (i * interval) / 60.0
        ping_ms = None

        if degradation["start_minute"] <= minute <= degradation["end_minute"]:
            loss_state = min(loss_state + 0.02, degradation.get("max_loss", 0.2))
        elif minute >= failure["start_minute"]:
            loss_state = min(loss_state + 0.05, failure["packet_loss_rate"])
        else:
            loss_state = max(loss_state - 0.01, 0.0)
        success = 1 if random.random() > loss_state else 0

        if minute >= failure["start_minute"]:
            if random.random() < failure["packet_loss_rate"]:
                success = 0
            else:
                ping_ms = random.uniform(
                    failure["min_ms"],
                    failure["max_ms"]
                )
        elif degradation["start_minute"] <= minute <= degradation["end_minute"]:
            progress = (
                    (minute - degradation["start_minute"]) /
                    (degradation["end_minute"] - degradation["start_minute"])
            )
            mean = normal["mean_ms"] + progress * (
                    degradation["final_mean_ms"] - normal["mean_ms"]
            )
            base_ping = random.gauss(mean, normal["std_ms"])
            jitter = abs(random.gauss(0, normal["jitter_ms"]))
            ping_ms = base_ping + jitter

        else:
            base_ping = random.gauss(normal["mean_ms"], normal["std_ms"])
            jitter = abs(random.gauss(0, normal["jitter_ms"]))
            ping_ms = max(base_ping + jitter, 0.1)

        rows.append({
            "device_id": device_id,
            "device_group": device_group,
            "timestamp": current_time.isoformat(),
            "ping_ms": None if success == 0 else round(ping_ms, 2),
            "success": success
        })

        current_time += datetime.timedelta(seconds=interval)

    with open(BASE_DIR / "output/raw_ping.csv", "w", newline="") as f:
        writer = csv.DictWriter(
            f,
            fieldnames=["device_id", "device_type", "timestamp", "ping_ms", "success"]
        )
        writer.writeheader()
        writer.writerows(rows)

    print(f"Generated {len(rows)} raw ping samples")
