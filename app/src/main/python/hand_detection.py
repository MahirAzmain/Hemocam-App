import numpy as np
import tflite_runtime.interpreter as tflite
from PIL import Image
import os

class HandDetectionError(Exception):
    """Custom exception for hand detection errors"""
    pass

def load_tflite_model(model_path):
    if not os.path.exists(model_path):
        raise HandDetectionError(f"Model file not found: {model_path}")
    try:
        interpreter = tflite.Interpreter(model_path=model_path, num_threads=1)
        interpreter.allocate_tensors()
        return interpreter
    except Exception as e:
        raise HandDetectionError(f"Error loading model: {str(e)}")

def preprocess_image(image_path):
    if not os.path.exists(image_path):
        raise HandDetectionError(f"Image file not found: {image_path}")
    try:
        img = Image.open(image_path).convert("RGB")
        img = img.resize((300, 300))
        img_array = np.expand_dims(np.array(img, dtype=np.float32) / 255.0, axis=0)
        return img_array
    except Exception as e:
        raise HandDetectionError(f"Error processing image: {str(e)}")

def predict(image_path, model_path):
    try:
        # Load model
        interpreter = load_tflite_model(model_path)

        # Get input and output tensors
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()

        # Preprocess the image
        img_array = preprocess_image(image_path)

        # Ensure the input shape matches the model requirements
        input_shape = input_details[0]['shape']
        if list(img_array.shape) != list(input_shape):
            img_array = np.reshape(img_array, input_shape)

        # Match the data type
        if img_array.dtype != input_details[0]['dtype']:
            img_array = img_array.astype(input_details[0]['dtype'])

        # Set the input tensor
        interpreter.set_tensor(input_details[0]['index'], img_array)

        # Run inference
        interpreter.invoke()

        # Get the output tensor
        output_data = interpreter.get_tensor(output_details[0]['index'])

        # Handle 3D output tensor
        if output_data.ndim == 3 and output_data.shape[0] == 1:
            predictions = output_data[0]  # Remove batch dimension

            # Example: Check the first prediction's score (assuming it's in the 4th column)
            first_prediction = predictions[0]
            score = first_prediction[-1]  # Adjust index based on your model's structure

            # Return classification result based on the score
            return "hand" if score > 0.5 else "non-hand"

        else:
            raise HandDetectionError(f"Unexpected output tensor shape: {output_data.shape}")

    except Exception as e:
        raise HandDetectionError(f"Prediction error: {str(e)}")
