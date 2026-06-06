package com.example.fintechaiassistantapp.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class OCRManager {

    public interface OCRListener {
        void onSuccess(String text);
        void onFailure(Exception e);
    }

    private final TextRecognizer recognizer;

    public OCRManager() {
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void processImage(Context context, Uri uri, OCRListener listener) {
        try {
            InputImage image = InputImage.fromFilePath(context, uri);
            recognizeText(image, listener);
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }

    public void processImage(Bitmap bitmap, OCRListener listener) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        recognizeText(image, listener);
    }

    private void recognizeText(InputImage image, OCRListener listener) {
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        listener.onSuccess(visionText.getText());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure(e);
                    }
                });
    }
}
