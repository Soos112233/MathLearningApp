package com.sooguosheng.mathplayground;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main_menu);

        CheckBox chk = findViewById(R.id.chkSound);
        if (chk != null) {
            chk.setChecked(Prefs.isSoundOn(this));
            chk.setOnCheckedChangeListener((v, on) -> Prefs.setSoundOn(this, on));
        }

        Button btnCounting = findViewById(R.id.btnCounting);
        Button btnNumber   = findViewById(R.id.btnNumber);
        Button btnMissing  = findViewById(R.id.btnMissing);

        btnCounting.setOnClickListener(v -> openDifficulty("Counting NUMBER"));
        btnNumber.setOnClickListener(v   -> openDifficulty("Number RECOGNITION"));
        btnMissing.setOnClickListener(v  -> openDifficulty("Missing NUMBER"));
    }

    private void openDifficulty(String mode){
        Intent i = new Intent(this, DifficultyActivity.class);
        i.putExtra("mode", mode);
        startActivity(i);
    }
}
