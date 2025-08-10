package com.sooguosheng.mathplayground;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class NumberRecognitionActivity extends AppCompatActivity {
    private String difficulty;
    private UX ux;
    private final Random rng = new Random();

    private TextView txtScore, txtTimer, txtNumber;
    private Button btnA, btnB, btnC, btnHint;

    private int score = 0, timeLeft = 30;
    private String correctWord;
    private int correctNumber;
    private int streak = 0;
    private CountDownTimer timer;

    // Performance
    private long roundStartMs;
    private int questions = 0, correct = 0, wrong = 0;

    // Count-once guard
    private boolean answered = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_number_recognition);

        difficulty = getIntent().getStringExtra("difficulty");
        ux = new UX(this);
        setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);

        txtScore  = findViewById(R.id.txtScore);
        txtTimer  = findViewById(R.id.txtTimer);
        txtNumber = findViewById(R.id.txtNumber);
        btnA      = findViewById(R.id.btnA);
        btnB      = findViewById(R.id.btnB);
        btnC      = findViewById(R.id.btnC);
        btnHint   = findViewById(R.id.btnHint);

        timeLeft = "Easy".equals(difficulty) ? 40 : 30;
        txtTimer.setText("Time: " + timeLeft);

        btnA.setOnClickListener(v -> checkAnswer(btnA.getText().toString()));
        btnB.setOnClickListener(v -> checkAnswer(btnB.getText().toString()));
        btnC.setOnClickListener(v -> checkAnswer(btnC.getText().toString()));
        btnHint.setOnClickListener(v -> speakHint());

        roundStartMs = System.currentTimeMillis();
        startTimer();
        nextQuestion();
    }

    private void startTimer(){
        timer = new CountDownTimer(timeLeft * 1000L, 1000) {
            public void onTick(long ms){ txtTimer.setText("Time: " + (ms/1000)); }
            public void onFinish(){ endRound(); }
        }.start();
    }

    private int rangeMax(){
        int base = "Easy".equals(difficulty) ? 10 : 20 + rng.nextInt(80);
        if ("Easy".equals(difficulty)) base = 10 + Math.min(5, streak);
        return Math.min(99, base);
    }

    private void nextQuestion(){
        answered = false;
        setButtonsEnabled(true);

        int max = rangeMax();
        correctNumber = "Easy".equals(difficulty) ? rng.nextInt(11) : rng.nextInt(max + 1);
        correctWord = Words.numToWord(correctNumber);

        txtNumber.setText(String.valueOf(correctNumber)); // big black digits (layout sets color)

        ArrayList<String> opts = new ArrayList<>();
        opts.add(correctWord);
        while (opts.size() < 3){
            String w = Words.numToWord(rng.nextInt(100));
            if (!opts.contains(w)) opts.add(w);
        }
        Collections.shuffle(opts);
        btnA.setText(opts.get(0));
        btnB.setText(opts.get(1));
        btnC.setText(opts.get(2));

        questions++;                           // count this question once
        ux.speak("Find the word for number");
    }

    private void checkAnswer(String picked){
        if (answered) return;                   // only first tap counts
        answered = true;
        setButtonsEnabled(false);

        if (picked.equals(correctWord)){
            correct++;
            score += 10;
            streak++;
            txtScore.setText("Score: " + score);
            ux.speak("Correct");
        } else {
            wrong++;
            streak = Math.max(0, streak - 1);
            score = Math.max(0, score - 5);
            txtScore.setText("Score: " + score);
            ux.buzzShort();
            ux.speak("Wrong");
        }

        txtScore.postDelayed(this::nextQuestion, 600);
    }

    private void setButtonsEnabled(boolean en){
        btnA.setEnabled(en); btnB.setEnabled(en); btnC.setEnabled(en);
    }

    private void speakHint() {
        // Partial hint only: initial letter + letter count (never reveal answer)
        String word = correctWord;
        String firstPart = word.contains("-") ? word.substring(0, word.indexOf('-')) : word;
        char initial = Character.toUpperCase(firstPart.charAt(0));
        int letters = word.replace("-", "").length();
        ux.speak("Hint: starts with " + initial + " and has " + letters + " letters.");
    }

    private void endRound(){
        int stars = score >= 90 ? 3 : score >= 60 ? 2 : 1;
        Prefs.setBestStars(this, "Number", difficulty, stars);

        long elapsedSec = Math.max(1, (System.currentTimeMillis() - roundStartMs) / 1000);

        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("mode", "Number");
        i.putExtra("difficulty", difficulty);
        i.putExtra("score", score);
        i.putExtra("stars", stars);
        i.putExtra("questions", questions);
        i.putExtra("correct", correct);
        i.putExtra("wrong", wrong);
        i.putExtra("elapsedSec", elapsedSec);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        if (ux != null) ux.shutdown();
    }
}
