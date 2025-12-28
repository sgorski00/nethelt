import pandas as pd
import joblib
from pathlib import Path
from ai_model.model import create_model

BASE_DIR = Path(__file__).resolve().parents[2]

FEATURES = [
    "ping_avg_1m",
    "ping_std_1m",
    "packet_loss_1m",
    "ping_diff_1m",
    "ping_zscore_15m"
]

def train(csv_path: Path, model_out: Path):
    df = pd.read_csv(csv_path)

    x = df[FEATURES]

    model = create_model(contamination=0.02)
    model.fit(x)

    model_out = Path(model_out)
    model_out.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(model, model_out)
