import pandas as pd
import joblib
from pathlib import Path

def predict(csv_path, model_path_str):
    df = pd.read_csv(csv_path)
    model_path = Path(model_path_str)
    if  model_path.suffix != ".joblib":
        model_path= model_path.with_suffix(".joblib")
    model, scaler = joblib.load(model_path)

    X = df.drop(columns=[
        "window_start",
        "device_group",
        "ping_diff_1m",
        #"ping_std_1m",
        "ping_zscore_15m",
        "ping_avg_1m",
    ])
    X_scaled = scaler.transform(X)
    df["anomaly"] = model.predict(X_scaled)

    return df
