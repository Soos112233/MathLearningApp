package com.sooguosheng.mathplayground;

import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CountingActivity extends AppCompatActivity {

    private String difficulty;
    private UX ux;
    private final Random rng = new Random();

    // UI
    private TextView txtScore, txtTimer;
    private FrameLayout canvas;
    private Button btnA, btnB, btnC, btnHint;

    // Game state
    private int score = 0, timeLeft = 30;
    private int correctAnswer = 0;
    private int streak = 0;
    private CountDownTimer timer;
    private int canvasW = 0, canvasH = 0;

    // Performance report
    private long roundStartMs = 0L;
    private boolean started = false;
    private int questions = 0;
    private int correct = 0;
    private int wrong = 0;

    // NEW: ensures each question is counted ONCE
    private boolean answered = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_counting);

        difficulty = getIntent().getStringExtra("difficulty");
        ux = new UX(this);

        txtScore = findViewById(R.id.txtScore);
        txtTimer = findViewById(R.id.txtTimer);
        canvas   = findViewById(R.id.canvas);
        btnA     = findViewById(R.id.btnA);
        btnB     = findViewById(R.id.btnB);
        btnC     = findViewById(R.id.btnC);
        btnHint  = findViewById(R.id.btnHint);

        timeLeft = "Easy".equals(difficulty) ? 40 : 30;
        txtTimer.setText("Time: " + timeLeft);

        btnA.setOnClickListener(v -> checkAnswer(((Button)v).getText().toString()));
        btnB.setOnClickListener(v -> checkAnswer(((Button)v).getText().toString()));
        btnC.setOnClickListener(v -> checkAnswer(((Button)v).getText().toString()));
        btnHint.setOnClickListener(v -> ux.speak("Count the stars and choose " + correctAnswer));

        // Wait for canvas size before starting
        canvas.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
                canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                canvasW = canvas.getWidth();
                canvasH = canvas.getHeight();

                if (!started) {
                    started = true;
                    roundStartMs = System.currentTimeMillis();
                    startTimer();
                    nextQuestion();
                }
            }
        });
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
        if (canvasW == 0 || canvasH == 0) return;

        // NEW: reopen question (count only once)
        answered = false;
        setButtonsEnabled(true);

        int max = rangeMax();
        correctAnswer = "Easy".equals(difficulty) ? rng.nextInt(11) : rng.nextInt(max + 1);

        placeRandomStars(correctAnswer);

        ArrayList<Integer> opts = new ArrayList<>();
        opts.add(correctAnswer);
        while (opts.size() < 3){
            int w = Math.max(0, correctAnswer + rng.nextInt(7) - 3);
            if (!opts.contains(w)) opts.add(w);
        }
        Collections.shuffle(opts);
        btnA.setText(String.valueOf(opts.get(0)));
        btnB.setText(String.valueOf(opts.get(1)));
        btnC.setText(String.valueOf(opts.get(2)));

        // Count this question ONCE here
        questions++;

        ux.speak("How many stars?");
    }

    private void checkAnswer(String pickedText){
        // If already answered, ignore extra taps
        if (answered) return;
        answered = true;
        setButtonsEnabled(false);

        int val = Integer.parseInt(pickedText);

        if (val == correctAnswer){
            // Count CORRECT only if first try was correct
            correct++;
            score += 10;
            streak++;
            txtScore.setText("Score: " + score);
            ux.speak("Correct. " + correctAnswer);
        } else {
            // First try was wrong: record WRONG and move on (no second chance counted)
            wrong++;
            streak = Math.max(0, streak - 1);
            score = Math.max(0, score - 5);
            txtScore.setText("Score: " + score);
            ux.buzzShort();
            ux.speak("Wrong");
        }

        // Brief pause, then next question
        canvas.postDelayed(this::nextQuestion, 600);
    }

    private void setButtonsEnabled(boolean enabled){
        btnA.setEnabled(enabled);
        btnB.setEnabled(enabled);
        btnC.setEnabled(enabled);
    }

    private void endRound(){
        int stars = score >= 90 ? 3 : score >= 60 ? 2 : 1;
        Prefs.setBestStars(this, "Counting", difficulty, stars);

        long elapsedSec = Math.max(1, (System.currentTimeMillis() - roundStartMs) / 1000);

        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("mode", "Counting");
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

    private int dp(int d){
        return (int) (d * getResources().getDisplayMetrics().density);
    }

    /** Randomly place 'count' stars without overlap. */
    private void placeRandomStars(int count){
        canvas.removeAllViews();

        int sizePx = dp(44);
        int gapPx  = dp(8);

        ArrayList<RectF> placed = new ArrayList<>();
        Drawable icon = ContextCompat.getDrawable(this, R.drawable.star);
        if (icon == null) icon = ContextCompat.getDrawable(this, android.R.drawable.btn_star_big_on);

        int maxX = Math.max(0, canvasW - sizePx);
        int maxY = Math.max(0, canvasH - sizePx);

        for (int i = 0; i < count; i++){
            boolean ok = false;
            for (int tries = 0; tries < 300 && !ok; tries++){
                int x = rng.nextInt(Math.max(1, maxX + 1));
                int y = rng.nextInt(Math.max(1, maxY + 1));
                RectF rect = new RectF(x, y, x + sizePx, y + sizePx);

                boolean overlaps = false;
                for (RectF r : placed){
                    RectF inf = new RectF(r.left - gapPx, r.top - gapPx, r.right + gapPx, r.bottom + gapPx);
                    if (RectF.intersects(inf, rect)) { overlaps = true; break; }
                }
                if (overlaps) continue;

                placed.add(rect);
                ImageView star = new ImageView(this);
                star.setImageDrawable(icon);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizePx, sizePx);
                lp.leftMargin = x; lp.topMargin = y;
                star.setLayoutParams(lp);
                star.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                star.setRotation(rng.nextInt(21) - 10);
                canvas.addView(star);
                ok = true;
            }
            if (!ok) break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        if (ux != null) ux.shutdown();
    }
}
