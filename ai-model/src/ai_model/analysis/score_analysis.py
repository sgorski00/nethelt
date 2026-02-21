from pathlib import Path

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

from ai_model.training.ping.train import FEATURES

SCORE_COLUMN = "score"

def plot_score_distribution(
    data: pd.DataFrame,
    threshold: float | None = None,
    save_path: str = "output/score_distribution.png"
):
    normal = data[data["anomaly"] == 1][SCORE_COLUMN]
    anomalies = data[data["anomaly"] == -1][SCORE_COLUMN]

    fig, axes = plt.subplots(1, 2, figsize=(13, 5))

    # ===== 1. History plot =====
    sns.histplot(normal, ax=axes[0], label="Normalny (1)", bins=30, stat="density", alpha=0.5)
    sns.histplot(anomalies, ax=axes[0], label="Anomalia (-1)", bins=30, stat="density", alpha=0.5)

    if threshold is not None:
        axes[0].axvline(threshold, linestyle="--", linewidth=2, label="Próg decyzyjny")

    axes[0].set_title("Rozkład score - histogram")
    axes[0].set_xlabel("Anomaly score (wyższy = bardziej normalny)")
    axes[0].set_ylabel("Gęstość")
    axes[0].legend()

    # ===== 2. Violin plot =====
    sns.violinplot(
        data=data,
        x="anomaly",
        y=SCORE_COLUMN,
        ax=axes[1],
        inner="quartile",
        bw_method=0.2
    )

    axes[1].set_title("Rozkład score wg klasy")
    axes[1].set_xlabel("Predykcja")
    axes[1].set_ylabel("Anomaly score")

    plt.tight_layout()
    save_path = Path(save_path)
    save_path.parent.mkdir(parents=True, exist_ok=True)
    plt.savefig(save_path, dpi=150)
    plt.close()

def generate_score_report_min(
    data: pd.DataFrame,
    device_type: str = "",
    contamination: float | None = None,
    n_estimators: int | None = None,
    save_path: str = "output/score_report.csv"
):
    normal = data[data["anomaly"] == 1][SCORE_COLUMN]
    anomalies = data[data["anomaly"] == -1][SCORE_COLUMN]

    n_total = len(data)
    n_anom = len(anomalies)

    min_normal = normal.min()
    max_anomaly = anomalies.max()

    separation_margin = min_normal - max_anomaly
    overlap = separation_margin < 0

    report = {
        "device": device_type,
        "n_samples": n_total,
        "n_anomaly": n_anom,
        "anomaly_%": round((n_anom / n_total) * 100, 3),

        "max_anomaly_score": max_anomaly,
        "min_normal_score": min_normal,
        "separation_margin": separation_margin,
        "overlap": overlap,

        "contamination": contamination,
        "n_estimators": n_estimators,
        "features": FEATURES
    }

    df_report = pd.DataFrame([report])

    save_path = Path(save_path)
    save_path.parent.mkdir(parents=True, exist_ok=True)
    if save_path.exists():
        existing = pd.read_csv(save_path)
        df_report = pd.concat([existing, df_report], ignore_index=True)
    df_report.to_csv(save_path, index=False)

    return df_report