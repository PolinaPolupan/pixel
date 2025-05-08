#!/usr/bin/env python3
import json
import argparse
import os
from PIL import Image
import torch
import torchvision.transforms as transforms
from torchvision.models import resnet50, ResNet50_Weights
import time

def classify_image(model, preprocess, image_path, device):
    try:
        # Load and preprocess the image
        image = Image.open(image_path).convert('RGB')
        input_tensor = preprocess(image).unsqueeze(0).to(device)

        # Run inference
        with torch.no_grad():
            start_time = time.time()
            output = model(input_tensor)
            processing_time = time.time() - start_time

        # Get top predictions
        probabilities = torch.nn.functional.softmax(output[0], dim=0)
        top_prob, top_class = torch.topk(probabilities, 1)

        return {
            "file": os.path.basename(image_path),
            "class": str(top_class.item()),
            "class_name": imagenet_classes[top_class.item()],
            "confidence": float(top_prob.item()),
            "processing_time": processing_time
        }
    except Exception as e:
        return {
            "file": os.path.basename(image_path),
            "error": str(e)
        }

def initialize_json_structure():
    """Create initial JSON structure"""
    return {
        "model": "ResNet50",
        "start_time": time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()),
        "predictions": [],
        "total_images": 0,
        "total_processing_time": 0
    }

def get_or_create_json_data(filepath):
    """Get existing JSON data or create new structure if file is empty"""
    try:
        if os.path.exists(filepath) and os.path.getsize(filepath) > 0:
            with open(filepath, 'r') as f:
                try:
                    data = json.load(f)
                    # Ensure the file has the expected structure
                    if "predictions" not in data:
                        data["predictions"] = []
                    if "total_images" not in data:
                        data["total_images"] = 0
                    if "total_processing_time" not in data:
                        data["total_processing_time"] = 0
                    return data
                except json.JSONDecodeError:
                    # File exists but isn't valid JSON
                    print("Existing file isn't valid JSON. Creating new structure.")
                    return initialize_json_structure()
        else:
            # File doesn't exist or is empty
            return initialize_json_structure()
    except Exception as e:
        print(f"Error reading JSON file: {e}")
        return initialize_json_structure()

def update_json_with_predictions(filepath, new_predictions, batch_time):
    """Update the JSON file with new predictions"""
    # Get existing data or create new structure
    data = get_or_create_json_data(filepath)

    # Update the data
    data["predictions"].extend(new_predictions)
    data["total_images"] += len(new_predictions)
    data["total_processing_time"] += batch_time
    data["last_updated"] = time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime())

    # Write updated data back to file
    with open(filepath, 'w') as f:
        json.dump(data, f, indent=2)

def main():
    parser = argparse.ArgumentParser(description='Classify images using PyTorch')
    parser.add_argument('--input', required=True, help='Comma-separated list of input files')
    parser.add_argument('--output', required=True, help='Output JSON file path')
    args = parser.parse_args()

    # Check for CUDA availability
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")

    # Load pre-trained model
    weights = ResNet50_Weights.DEFAULT
    model = resnet50(weights=weights)
    model.to(device)
    model.eval()

    preprocess = weights.transforms()

    global imagenet_classes
    try:
        with open('/app/python/imagenet_classes.txt') as f:
            imagenet_classes = [line.strip() for line in f.readlines()]
    except:
        imagenet_classes = [str(i) for i in range(1000)]

    input_files = args.input.split(',')

    start_time = time.time()
    results = []
    for file_path in input_files:
        if os.path.exists(file_path):
            result = classify_image(model, preprocess, file_path, device)
            results.append(result)
        else:
            results.append({"file": os.path.basename(file_path), "error": "File not found"})

    batch_time = time.time() - start_time

    update_json_with_predictions(args.output, results, batch_time)

    print(f"Processed {len(results)} images in this batch")
    return 0

if __name__ == "__main__":
    exit(main())