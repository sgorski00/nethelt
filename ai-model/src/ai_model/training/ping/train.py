import pandas as pd
import joblib
from pathlib import Path
from ai_model.model import create_model
from sklearn.preprocessing import StandardScaler

BASE_DIR = Path(__file__).resolve().parents[2]

FEATURES = [
    #*"ping_avg_1m",
    "ping_std_1m",
    "packet_loss_1m",
    #"ping_diff_1m",
    #"ping_zscore_15m"
]

def train(csv_path: Path, model_out: Path):
    df = pd.read_csv(csv_path)

    X = df[FEATURES]

    model = create_model(contamination=0.02)
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    model.fit(X_scaled)

    model_out = Path(model_out)
    if model_out.suffix != ".joblib":
        model_out = model_out.with_suffix(".joblib")
    model_out.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump((model, scaler), model_out)
