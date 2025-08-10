package com.sooguosheng.mathplayground;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class UX {
    private final Context ctx;
    private TextToSpeech tts;

    // Defaults — change here or via the public setters below
    private float ttsVolume = 10.0f; // 0.0f..1.0f (multiplies device media volume)
    private float ttsRate   = 0.75f; // 1.0f = normal speed
    private float ttsPitch  = 1.0f;  // 1.0f = normal pitch

    public UX(Context c){
        ctx = c.getApplicationContext();

        // Make hardware volume keys adjust MEDIA volume inside this screen
        if (c instanceof Activity) {
            ((Activity) c).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

        tts = new TextToSpeech(ctx, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);
                tts.setSpeechRate(ttsRate);
                tts.setPitch(ttsPitch);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build());
                }
            }
        });
    }

    // ---- Public controls you can call from Activities ----
    public void setTtsRate(float rate){          // 0.5f .. 2.0f typical
        ttsRate = clamp(rate, 0.3f, 3.0f);
        if (tts != null) tts.setSpeechRate(ttsRate);
    }

    public void setTtsPitch(float pitch){        // 0.5f .. 2.0f typical
        ttsPitch = clamp(pitch, 0.3f, 3.0f);
        if (tts != null) tts.setPitch(ttsPitch);
    }

    public void setTtsVolume(float volume){      // 0.0f .. 1.0f
        ttsVolume = clamp(volume, 0f, 1f);
    }

    /** Speak text at current volume/rate/pitch (respects Prefs sound toggle). */
    public void speak(String text){
        if (text == null || text.isEmpty() || !Prefs.isSoundOn(ctx) || tts == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, ttsVolume);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "tts1");
        } else {
            // Old API fallback (no per-utterance volume control)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /** Queue speech (doesn't interrupt current utterance). */
    public void speakQueue(String text){
        if (text == null || text.isEmpty() || !Prefs.isSoundOn(ctx) || tts == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, ttsVolume);
            tts.speak(text, TextToSpeech.QUEUE_ADD, params, "ttsQ");
        } else {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void buzzShort(){
        Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= 26)
            v.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
        else v.vibrate(60);
    }

    public void shutdown(){
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    private static float clamp(float x, float lo, float hi){
        return Math.max(lo, Math.min(hi, x));
    }
}
