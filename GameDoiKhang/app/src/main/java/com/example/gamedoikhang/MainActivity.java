package com.example.gamedoikhang;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnChoiTrucTuyen, btnChoiDon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChoiTrucTuyen = findViewById(R.id.btnXungBaThienHa);
        btnChoiDon = findViewById(R.id.btnZoLuon);
        //Chuyển activitiy
        btnChoiTrucTuyen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogAccountActivity.class);
                startActivity(intent);
            }
        });

        btnChoiDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameOfflineActivity.class);
                startActivity(intent);
            }
        });
    }

    public void ThoatGame(){
        MainActivity.this.finish();
        System.exit(0);
    }


}