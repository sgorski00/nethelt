from sklearn.ensemble import IsolationForest

RANDOM_STATE = 42

def create_model(contamination: float, n_estimators: int) -> IsolationForest:
    return IsolationForest(
        n_estimators=n_estimators,
        contamination=contamination,
        random_state=RANDOM_STATE,
    )
