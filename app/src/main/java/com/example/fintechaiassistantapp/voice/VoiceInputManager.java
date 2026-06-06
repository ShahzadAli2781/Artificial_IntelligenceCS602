package com.example.fintechaiassistantapp.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceInputManager {

    public interface VoiceInputListener {
        void onReadyForSpeech();
        void onBeginningOfSpeech();
        void onEndOfSpeech();
        void onError(String error);
        void onResults(String result);
    }

    private SpeechRecognizer speechRecognizer;
    private final VoiceInputListener listener;
    private final Context context;

    public VoiceInputManager(Context context, VoiceInputListener listener) {
        this.context = context;
        this.listener = listener;
        init();
    }

    private void init() {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Speech recognition not available on this device");
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                listener.onReadyForSpeech();
            }

            @Override
            public void onBeginningOfSpeech() {
                listener.onBeginningOfSpeech();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Optional
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Optional
            }

            @Override
            public void onEndOfSpeech() {
                listener.onEndOfSpeech();
            }

            @Override
            public void onError(int error) {

                String message;

                switch (error) {

                    case SpeechRecognizer.ERROR_AUDIO:
                        message = "Audio recording error";
                        break;

                    case SpeechRecognizer.ERROR_CLIENT:
                        message = "Client side error";
                        break;

                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        message = "Insufficient permissions";
                        break;

                    case SpeechRecognizer.ERROR_NETWORK:
                        message = "Network error";
                        break;

                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        message = "Network timeout";
                        break;

                    case SpeechRecognizer.ERROR_NO_MATCH:
                        message = "No match found";
                        break;

                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        message = "Recognition service busy";
                        break;

                    case SpeechRecognizer.ERROR_SERVER:
                        message = "Server error";
                        break;

                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        message = "No speech input";
                        break;

                    default:
                        message = "Recognition error";
                        break;
                }

                listener.onError(message);
            }

            @Override
            public void onResults(Bundle results) {

                if (results == null) {
                    listener.onError("No results received");
                    return;
                }

                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && !matches.isEmpty()) {

                    String result = matches.get(0);

                    if (result != null && !result.trim().isEmpty()) {
                        listener.onResults(result.trim());
                    } else {
                        listener.onError("Empty voice result");
                    }

                } else {
                    listener.onError("No speech recognized");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Optional
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Optional
            }
        });
    }

    public void startListening() {

        if (speechRecognizer == null) {
            listener.onError("SpeechRecognizer is null");
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault().toString()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                1
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak your expense..."
        );

        try {

            speechRecognizer.stopListening();
            speechRecognizer.cancel();

            speechRecognizer.startListening(intent);

        } catch (Exception e) {

            listener.onError(
                    "Voice recognition failed: " + e.getMessage()
            );
        }
    }

    public void stopListening() {

        if (speechRecognizer != null) {

            try {
                speechRecognizer.stopListening();
            } catch (Exception ignored) {
            }
        }
    }

    public void shutdown() {

        if (speechRecognizer != null) {

            try {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
                speechRecognizer.destroy();
            } catch (Exception ignored) {
            }

            speechRecognizer = null;
        }
    }
}