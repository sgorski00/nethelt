import argparse
from ai_model.training.ping.train import train
from ai_model.inference.ping.predict import predict
from ai_model.analysis.score_analysis import  plot_score_distribution, generate_score_report_min

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=["train", "predict"], required=True)
    parser.add_argument("--data_path", required=True)
    parser.add_argument("--model_path", required=True)
    parser.add_argument("--analyze", action="store_true")
    parser.add_argument("--n_estimators", type=int, default=100)
    parser.add_argument("--contamination", type=float, default=0.02)
    parser.add_argument("--device_type", type=str, default="")
    args = parser.parse_args()

    contamination = float(args.contamination)
    n_estimators = int(args.n_estimators)
    if args.mode == "train":
        train(
            args.data_path,
            args.model_path,
            contamination,
            n_estimators
        )
    elif args.mode == "predict":
        df = predict(args.data_path, args.model_path)
        df.to_csv("output/predict_test.csv", index=False)
        if args.analyze:
            plot_score_distribution(df, threshold=0)
            generate_score_report_min(
                df,
                device_type=args.device_type,
                contamination=contamination,
                n_estimators=n_estimators
            )

if __name__ == "__main__":
    main()