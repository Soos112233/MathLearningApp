package com.sooguosheng.mathplayground;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity {
    private String mode;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_difficulty);

        mode = getIntent().getStringExtra("mode");
        ((TextView)findViewById(R.id.txtMode)).setText(mode.toUpperCase());

        findViewById(R.id.btnEasy).setOnClickListener(v -> go("Easy"));
        findViewById(R.id.btnHard).setOnClickListener(v -> go("Hard"));
    }

    private void go(String diff){
        Intent i;
        switch (mode){
            case "Counting NUMBER": i = new Intent(this, CountingActivity.class); break;
            case "Number RECOGNITION":   i = new Intent(this, NumberRecognitionActivity.class); break;
            default:         i = new Intent(this, MissingNumberActivity.class); break;
        }
        i.putExtra("difficulty", diff);
        startActivity(i);
    }
}
