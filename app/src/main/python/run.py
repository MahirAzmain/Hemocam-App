import numpy as np
import joblib
import json
import cv2
import os
from os.path import dirname, join

filename = join(dirname(__file__), "model.pkl")

def load_model():
    """Load the pre-trained model from assets."""
    with open(filename, "rb") as f:
        model = joblib.load(f)
    return model

def cut_image(img, low=0.2, high=0.8):
    """Crop the image to focus on a specific region."""
    h, w = img.shape[:2]
    return img[int(low * h):int(high * h), int(low * w):int(high * w), :]

def calculate_features(img, white_ref, percentile_levels=[5, 15, 25, 50, 75, 85, 95], low=0.2, high=0.8):
    """
    Extract color-based features from the image using percentiles and normalize with the white reference.
    Assumes that both img and white_ref are in RGB.
    """
    features_dict = {}

    # Convert to float for calculations
    img = img.astype(np.float32)
    white_ref = white_ref.astype(np.float32)

    # Crop the image once
    cropped_img = cut_image(img, low=low, high=high)

    # Iterate over channels in RGB order
    for color_chan_id, color in enumerate("RGB"):
        # Compute the white reference median for this channel
        white_median = np.median(white_ref[:, :, color_chan_id])
        # If white_median is 0, use a small epsilon to avoid division by zero
        if white_median == 0:
            white_median = 1e-6

        # Get the channel data from the cropped image
        channel_data = cropped_img[:, :, color_chan_id].ravel()

        # Compute each percentile feature and normalize it
        for percentile_level in percentile_levels:
            feature_value = np.percentile(channel_data, percentile_level)
            feature_name = f"{color}_p={percentile_level}"
            features_dict[feature_name] = feature_value / white_median

    return features_dict

def predict_hb(model, white_ref, skin_imgs, nail_imgs):
    """
    Predict Hb levels for 3 test cases.
    For each test case, compute features from the skin and nail images,
    prefix the feature names, and then assemble the feature vector in the same order as during training.
    """
    features_list = []

    for i in range(3):  # There are 3 test cases
        skin_features = calculate_features(skin_imgs[i], white_ref)
        nail_features = calculate_features(nail_imgs[i], white_ref)

        # Add prefixes so that features are labeled similarly to training (e.g., SKIN_R_p=5)
        skin_features = {f"SKIN_{k}": v for k, v in skin_features.items()}
        nail_features = {f"NAIL_{k}": v for k, v in nail_features.items()}

        # Combine the two dictionaries
        combined_features = {**skin_features, **nail_features}
        features_list.append(combined_features)

    # Explicit feature order as expected by the model:
    rgb_order = [5, 15, 25, 50, 75, 85, 95]
    feature_order = (
        [f"SKIN_{color}_p={p}" for color in "RGB" for p in rgb_order] +
        [f"NAIL_{color}_p={p}" for color in "RGB" for p in rgb_order]
    )

    # Assemble the feature matrix X ensuring the same order for every sample
    X = np.array([[features[k] for k in feature_order] for features in features_list])

    # Predict Hb levels using the loaded model
    hb_levels = model.predict(X)

    return hb_levels.tolist()

def main(white_img_path, skin_img_paths, nail_img_paths):
    """
    Main function: Convert image paths to OpenCV images, ensuring they are in RGB,
    load the model, compute the features, and return predictions.
    """
    def path_to_cv2_image(img_path):
        """Convert image file path to OpenCV image and convert from BGR to RGB."""
        # Read image from file path
        img = cv2.imread(img_path)
        if img is None:
            print(f"Failed to load image at {img_path}")
            return None

        img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        return img_rgb

    # Convert file paths to images (now in RGB)
    white_ref = path_to_cv2_image(white_img_path)
    skin_imgs = [path_to_cv2_image(img) for img in skin_img_paths]
    nail_imgs = [path_to_cv2_image(img) for img in nail_img_paths]

    # Load the trained model
    model = load_model()

    # Compute and return Hb level predictions as JSON
    hb_levels = predict_hb(model, white_ref, skin_imgs, nail_imgs)
    return json.dumps(hb_levels)
