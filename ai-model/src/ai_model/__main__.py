import argparse
from ai_model.training.ping.train import train
from ai_model.inference.ping.predict import predict

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=["train", "predict"], required=True)
    parser.add_argument("--data_path", required=True)
    parser.add_argument("--model_path", required=True)
    args = parser.parse_args()
    if args.mode == "train":
        train(args.data_path, args.model_path)
    elif args.mode == "predict":
        df = predict(args.data_path, args.model_path)
        df.to_csv("predict_test.csv", index=False)

if __name__ == "__main__":
    main()