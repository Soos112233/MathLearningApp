package com.sooguosheng.mathplayground;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_result);

        String mode       = getIntent().getStringExtra("mode");
        String difficulty = getIntent().getStringExtra("difficulty");
        int score        = getIntent().getIntExtra("score", 0);
        int questions    = getIntent().getIntExtra("questions", 0);
        int correct      = getIntent().getIntExtra("correct", 0);
        int wrong        = getIntent().getIntExtra("wrong", 0);

        // Grade by accuracy only (simple rubric)
        double accuracyPct = questions > 0 ? (correct * 100.0 / questions) : 0.0;
        String grade = gradeFromAccuracy(accuracyPct);

        ((TextView)findViewById(R.id.txtSummary))
                .setText(mode + " • " + difficulty);
        ((TextView)findViewById(R.id.txtScore)).setText("Score: " + score);
        ((TextView)findViewById(R.id.txtQuestions)).setText("Questions: " + questions);
        ((TextView)findViewById(R.id.txtCorrect)).setText("Correct: " + correct);
        ((TextView)findViewById(R.id.txtWrong)).setText("Wrong: " + wrong);
        ((TextView)findViewById(R.id.txtGrade)).setText("Grade: " + grade);

        Button replay = findViewById(R.id.btnReplay);
        Button menu   = findViewById(R.id.btnMenu);

        replay.setOnClickListener(v -> {
            Intent i;
            switch (mode){
                case "Counting": i = new Intent(this, CountingActivity.class); break;
                case "Number":   i = new Intent(this, NumberRecognitionActivity.class); break;
                default:         i = new Intent(this, MissingNumberActivity.class); break;
            }
            i.putExtra("difficulty", difficulty);
            startActivity(i);
            finish();
        });

        menu.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        });
    }

    private String gradeFromAccuracy(double acc){
        if (acc >= 90) return "A";
        if (acc >= 75) return "B";
        if (acc >= 60) return "C";
        return "Fail";
    }
}
