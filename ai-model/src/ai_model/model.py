from sklearn.ensemble import IsolationForest

N_ESTIMATORS = 200 # liczba drzew losowych - im więcej tym lepsze wyniki - kosztem wydajności
RANDOM_STATE = 42

def create_model(contamination : float= 0.02):
    return IsolationForest(
        n_estimators=N_ESTIMATORS,
        contamination=contamination,
        random_state=RANDOM_STATE,
    )
