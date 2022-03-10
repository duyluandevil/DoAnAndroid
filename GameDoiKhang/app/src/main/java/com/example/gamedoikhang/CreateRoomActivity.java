package com.example.gamedoikhang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class CreateRoomActivity extends AppCompatActivity {

    private Socket mSocket;

    Button btnXacNhanTaoPhong;
    EditText txtTenPhong;

    String tenPhong="";
    String tenNguoiChoi1 ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        //Tạo server
        try {
            mSocket = IO.socket("https://doanandroid.herokuapp.com/");
//            mSocket = IO.socket("http://192.168.1.43:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();


        txtTenPhong= findViewById(R.id.txtTenPhong);

        Bundle extras = getIntent().getExtras();
        if(extras !=null){
            tenNguoiChoi1 = extras.getString("playerName");
        }

        btnXacNhanTaoPhong = findViewById(R.id.btnXacNhanTaoPhong);
        btnXacNhanTaoPhong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tenPhong = txtTenPhong.getText().toString();
                btnXacNhanTaoPhong.setText("Đang tạo phòng...");
                btnXacNhanTaoPhong.setEnabled(false);
                if(TextUtils.isEmpty(tenPhong))
                {
                    Toast.makeText(CreateRoomActivity.this, "Bạn chưa nhập tên phòng", Toast.LENGTH_SHORT).show();
                    btnXacNhanTaoPhong.setEnabled(true);
                    btnXacNhanTaoPhong.setText("Tạo phòng");
                }
                else{
                    String infoRoom = tenPhong+"@"+tenNguoiChoi1;
                    mSocket.emit("client-send-new-room", infoRoom);
                    mSocket.on("server-send-result-add-room", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    JSONObject obj = (JSONObject) args[0];
                                    try {
                                        int ketQuaTaoPhong = obj.getInt("ketQuaTaoPhong");
                                        if(ketQuaTaoPhong == 0){
                                            Toast.makeText(CreateRoomActivity.this, "Bạn tạo phòng thành công", Toast.LENGTH_SHORT).show();
                                            btnXacNhanTaoPhong.setEnabled(true);
                                            btnXacNhanTaoPhong.setText("Tạo phòng");
                                            Intent intent = new Intent(CreateRoomActivity.this, GameOnlineActivity.class);
                                            intent.putExtra("playerName1", tenNguoiChoi1);
                                            intent.putExtra("roomName", tenPhong);
                                            startActivity(intent);
                                            finish();
                                        }else{
                                            Toast.makeText(CreateRoomActivity.this, "Phòng đã tồn tại", Toast.LENGTH_SHORT).show();
                                            btnXacNhanTaoPhong.setEnabled(true);
                                            btnXacNhanTaoPhong.setText("Tạo phòng");
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
    }
}