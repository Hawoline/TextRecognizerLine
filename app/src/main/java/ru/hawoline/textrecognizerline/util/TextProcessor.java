package ru.hawoline.textrecognizerline.util;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

public class TextProcessor {

    public static String process(FirebaseVisionText firebaseVisionText) {
        StringBuilder resultText = new StringBuilder();

        for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
            for (FirebaseVisionText.Line line: block.getLines()) {
                resultText.append(line.getText());
            }

            resultText.append("\n").append("\n");
        }

        return resultText.toString().trim();
    }
}
