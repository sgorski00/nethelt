import pandas as pd
import joblib

def predict(csv_path, model_path):
    df = pd.read_csv(csv_path)
    model = joblib.load(model_path)

    x = df.drop(columns=["window_start", "device_type", "ping_zscore_15m"])
    df["anomaly"] = model.predict(x)

    return df
