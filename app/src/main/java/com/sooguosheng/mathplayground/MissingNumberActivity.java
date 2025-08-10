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

public class MissingNumberActivity extends AppCompatActivity {

    private String difficulty;
    private UX ux;
    private final Random rng = new Random();

    private TextView txtScore, txtTimer, txtSequence;
    private Button btnA, btnB, btnC, btnHint;

    private int score = 0, timeLeft = 30;
    private int correctAnswer;
    private int streak = 0;
    private CountDownTimer timer;

    // performance
    private long roundStartMs;
    private int questions = 0, correct = 0, wrong = 0;

    // count-once guard
    private boolean answered = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_missing_number);

        difficulty = getIntent().getStringExtra("difficulty");
        ux = new UX(this);

        txtScore    = findViewById(R.id.txtScore);
        txtTimer    = findViewById(R.id.txtTimer);
        txtSequence = findViewById(R.id.txtSequence);
        btnA        = findViewById(R.id.btnA);
        btnB        = findViewById(R.id.btnB);
        btnC        = findViewById(R.id.btnC);
        btnHint     = findViewById(R.id.btnHint);

        // ensure dark text
        int black = getResources().getColor(android.R.color.black);
        txtScore.setTextColor(black);
        txtTimer.setTextColor(black);
        txtSequence.setTextColor(black);

        timeLeft = "Easy".equals(difficulty) ? 40 : 30;
        txtTimer.setText("Time: " + timeLeft);

        btnA.setOnClickListener(v -> checkAnswer(btnA.getText().toString()));
        btnB.setOnClickListener(v -> checkAnswer(btnB.getText().toString()));
        btnC.setOnClickListener(v -> checkAnswer(btnC.getText().toString()));
        btnHint.setOnClickListener(v -> ux.speak("Find the missing number. Count carefully."));

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

    private void nextQuestion(){
        answered = false;
        setButtonsEnabled(true);

        final int length = 5;
        final int LO = "Easy".equals(difficulty) ? 0 : 0;
        final int HI = "Easy".equals(difficulty) ? 10 : 99;

        // STEP is always 1 for kindergarten level (no +2, +3…)
        int step;
        if ("Easy".equals(difficulty)) {
            step = 1;                // only forward counting 0..10
        } else {
            step = rng.nextBoolean() ? 1 : -1; // Hard can be +1 or -1 (optional backward counting)
        }

        // choose a start that keeps all terms in [LO, HI]
        int minStart, maxStart;
        if (step >= 0) {
            minStart = LO;
            maxStart = HI - (length - 1) * step;
        } else {
            minStart = LO - (length - 1) * step; // step is -1 → LO + (length-1)
            maxStart = HI;
        }
        // safe pick
        int start = minStart + rng.nextInt(Math.max(1, (maxStart - minStart + 1)));

        // build sequence
        ArrayList<Integer> seq = new ArrayList<>(length);
        for (int i = 0; i < length; i++) seq.add(start + i * step);

        // pick a middle slot for "?"
        int missingIndex = 1 + rng.nextInt(length - 2);
        correctAnswer = seq.get(missingIndex);

        // show sequence
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(i == missingIndex ? "?" : seq.get(i));
            if (i < length - 1) sb.append(", ");
        }
        txtSequence.setText(sb.toString());

        // options: correct + neighbors (±1) within bounds
        ArrayList<Integer> opts = new ArrayList<>();
        opts.add(correctAnswer);

        while (opts.size() < 3) {
            int neighbor = correctAnswer + (rng.nextBoolean() ? 1 : -1);
            if (neighbor >= LO && neighbor <= HI && !opts.contains(neighbor)) {
                opts.add(neighbor);
            }
        }
        Collections.shuffle(opts);
        btnA.setText(String.valueOf(opts.get(0)));
        btnB.setText(String.valueOf(opts.get(1)));
        btnC.setText(String.valueOf(opts.get(2)));

        questions++;                         // record once per question
        ux.speak("What number is missing?");
    }

    private void checkAnswer(String picked){
        if (answered) return;                // only first tap counts
        answered = true;
        setButtonsEnabled(false);

        int val = Integer.parseInt(picked);
        if (val == correctAnswer){
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

    private void endRound(){
        int stars = score >= 90 ? 3 : score >= 60 ? 2 : 1;
        Prefs.setBestStars(this, "Missing", difficulty, stars);

        long elapsedSec = Math.max(1, (System.currentTimeMillis() - roundStartMs) / 1000);

        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("mode", "Missing");
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
