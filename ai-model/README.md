# AI Model

Python module for network anomaly detection using Isolation Forest algorithm.

## Requirements

- Python 3.12

## Installation

```bash
pip install poetry
poetry install
```

## Running

This module allows you to train and use an Isolation Forest model for anomaly detection in network data.

You will need the `poetry` and `python` to run this module.

### Training the Model

To train the model, you can run the following command:

```shell
poetry run python -m ai_model --mode train \
    --data_path path/to/your/training/data.csv \
    --model_path path/to/save/your/model \
    --n_estimators 100 \
    --contamination 0.1
```

Description of the parameters:
- `data_path`: Path to the training data in CSV format.
- `model_path`: Path where the trained model will be saved.
- `n_estimators`: (optional) Number of trees in the Isolation Forest (default: 200).
- `contamination`: (optional) Proportion of outliers in the data (default: 0.2).

After training this model can be used to predict anomalies in new data.

### Predicting Anomalies

> This feature will be replaced with the HTTP API in the future.

To predict anomalies using the trained model, you can run the following command:

```shell
poetry run python -m ai_model --mode predict \
    --data_path path/to/your/test/data.csv \
    --model_path path/to/your/trained/model \
    --analyze \
    --n_estimators 100 \
    --contamination 0.1 \
    --device_type lan_client 
```

Description of the parameters:
- `data_path`: Path to the test data in CSV format.
- `model_path`: Path to the trained model.
- `analyze`: (optional) Flag to indicate whether to perform analysis on the predictions.
- `n_estimators`: (optional) Number of trees in the Isolation Forest (used only for the analyze purpose)
- `contamination`: (optional) Proportion of outliers in the data (used only for the analyze purpose)
- `device_type`: (optional) Type of device for analysis (used only for the analyze purpose).

When prediction is run with --analyze mode it will provide 2 additional files in the output directory:
- `score_report.csv`: A CSV file containing the analysis of the anomalies detected in the test data, including the anomaly scores and other relevant information.
- `score_distribution.png`: A PNG image visualizing the analysis of the anomalies detected in the test data.
