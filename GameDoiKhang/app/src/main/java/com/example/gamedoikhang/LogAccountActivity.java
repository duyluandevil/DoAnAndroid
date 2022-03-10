package com.example.gamedoikhang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LogAccountActivity extends AppCompatActivity {


    private Socket mSocket;

    EditText txtTenNguoiChoi, txtMatKhau;
    Button btnDangNhap, btnDangKy;
    String TenNguoiChoi, matKhau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_account);

        txtTenNguoiChoi = findViewById(R.id.txtTenTaiKhoan);
        txtMatKhau = findViewById(R.id.txtMatKhau);
        btnDangNhap = findViewById(R.id.btnDangNhap);
        btnDangKy = findViewById(R.id.btnDangKy);

        //Tạo socket
        try {
            mSocket = IO.socket("https://doanandroid.herokuapp.com/");
//            mSocket = IO.socket("http://192.168.1.43:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();


        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDangNhap.setText("Đang đăng nhập...");
                btnDangNhap.setEnabled(false);
                TenNguoiChoi = txtTenNguoiChoi.getText().toString();
                matKhau = txtMatKhau.getText().toString();
                if(TextUtils.isEmpty(TenNguoiChoi)) {
                    Toast.makeText(LogAccountActivity.this, "Chưa nhập thông tin", Toast.LENGTH_SHORT).show();
                    btnDangNhap.setText("Đăng nhập");
                    btnDangNhap.setEnabled(true);
                }
                else{
                    String taikhoan = TenNguoiChoi + "@" + matKhau;
                    mSocket.emit("client-send-login-account", taikhoan);
                    mSocket.on("server-send-result-login", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject obj = (JSONObject) args[0];
                                    try {
                                        int countAccount = obj.getInt("ketQuaDangNhap");
                                        if(countAccount!=0){
                                            Toast.makeText(LogAccountActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                            btnDangNhap.setText("Đăng nhập");
                                            btnDangNhap.setEnabled(true);
                                            Intent intent = new Intent(getApplicationContext(), ListRoomActivity.class);
                                            intent.putExtra("playerName", TenNguoiChoi);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else{
                                            Toast.makeText(LogAccountActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                                            btnDangNhap.setText("Đăng nhập");
                                            btnDangNhap.setEnabled(true);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });

                }
            }
        });

        btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDangKy.setText("Đang đăng ký...");
                btnDangKy.setEnabled(false);
                TenNguoiChoi = txtTenNguoiChoi.getText().toString();
                matKhau = txtMatKhau.getText().toString();
                if(TextUtils.isEmpty(matKhau) &&TextUtils.isEmpty(TenNguoiChoi) ) {
                    Toast.makeText(LogAccountActivity.this, "Chưa nhập thông tin", Toast.LENGTH_SHORT).show();
                    btnDangKy.setText("Đăng ký");
                    btnDangKy.setEnabled(true);
                }
                else{
                    String taikhoan = TenNguoiChoi + "@" + matKhau;
                    mSocket.emit("client-send-new-account", taikhoan);
                    mSocket.on("server-send-result-register", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject obj = (JSONObject) args[0];
                                    try {
                                        int countAccount = obj.getInt("ketQuaDangNhap");
                                        if(countAccount==0)
                                        {
                                            Toast.makeText(LogAccountActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                            btnDangKy.setText("Đăng ký");
                                            btnDangKy.setEnabled(true);
                                        }else{
                                            Toast.makeText(LogAccountActivity.this, "Đã có tài khoản", Toast.LENGTH_SHORT).show();
                                            btnDangKy.setText("Đăng ký");
                                            btnDangKy.setEnabled(true);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    Toast.makeText(LogAccountActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    btnDangKy.setText("Đăng ký");
                    btnDangKy.setEnabled(true);
                }
            }
        });
    }
}