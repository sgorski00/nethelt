import pandas as pd
import joblib
from pathlib import Path
from ai_model.training.ping.train import FEATURES


def predict(csv_path, model_path_str):
    df = pd.read_csv(csv_path)
    model_path = Path(model_path_str)
    if  model_path.suffix != ".joblib":
        model_path= model_path.with_suffix(".joblib")
    model, feature_scaler = joblib.load(model_path)

    X = df[FEATURES]
    X_scaled = feature_scaler.transform(X)
    df["anomaly"] = model.predict(X_scaled)
    df["score"] = model.decision_function(X_scaled)

    return df
