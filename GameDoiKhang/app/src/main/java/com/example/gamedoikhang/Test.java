package com.example.gamedoikhang;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Test extends AppCompatActivity {

    private Socket mSocket;
    Button btnJoin, btnJoin2, btnSend, btnDis;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        try {
            mSocket = IO.socket("http://192.168.1.18:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();

        btnJoin = findViewById(R.id.btnJoin);
        btnJoin2 = findViewById(R.id.btnJoin2);
        btnSend = findViewById(R.id.btnSend);
        btnDis = findViewById(R.id.btnDis);

        boolean isJoin = true;

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tenPhong = btnJoin.getText().toString();
                mSocket.emit("join", tenPhong);

            }
        });
        btnJoin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tenPhong = btnJoin2.getText().toString();
                mSocket.emit("join2", tenPhong);

            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chuoi= "Hello";
                mSocket.emit("send", chuoi);
                mSocket.on("sv-send-req", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject object = (JSONObject) args[0];
                                String text = null;
                                try {
                                    text = object.getString("kqsend");
                                    Toast.makeText(Test.this,text, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });
        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("disroom");
                mSocket.on("user left", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                String chuoi1 = null;
                                try {
                                    chuoi1 = obj.getString("ketquaguive");
                                    Toast.makeText(Test.this,chuoi1, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                });
            }
        });

    }
}