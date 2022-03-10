package com.example.gamedoikhang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ListRoomActivity extends AppCompatActivity {

    Button btnTaoPhong;
    ListView lstPhong;

    private Socket mSocket;


    String tenPhong="";
    String tenNguoiChoi = "", tenP1="", tenP2="", tenNguoiChoi2 = "";

    ArrayList<String> arrayDsPhong;
    ArrayAdapter adapterDsPhong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_room);

        try {
            mSocket = IO.socket("https://doanandroid.herokuapp.com/");
//            mSocket = IO.socket("http://192.168.1.43:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();

        //Nhận dữ liệu phòng
        mSocket.on("server-send-room", nhanDanhSachPhong);

        //Nhận dữ liệu từ activity trước
        Bundle extras = getIntent().getExtras();
        if(extras !=null){
            tenNguoiChoi = extras.getString("playerName");
            tenP2 = extras.getString("playerName");
            tenNguoiChoi2 = extras.getString("playerName2");
        }

        //Nhận dữ liệu từ server

        lstPhong = findViewById(R.id.lstPhong);
        lstPhong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListRoomActivity.this, GameOnlineActivity.class);
                String tenPhongClick = (String) parent.getItemAtPosition(position);
                if(tenNguoiChoi2!=null){//Từ gameonline trả về
                    intent.putExtra("roomName", tenPhongClick);
                    intent.putExtra("tenNguoiChoi2", tenNguoiChoi2);
                    startActivity(intent);
                    finish();
                }else{
                    intent.putExtra("roomName", tenPhongClick);
                    intent.putExtra("tenNguoiChoi2", tenP2);
                    startActivity(intent);
                    finish();
                }

            }
        });

        btnTaoPhong = findViewById(R.id.btnTaoPhong);
        btnTaoPhong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListRoomActivity.this, CreateRoomActivity.class);
                intent.putExtra("playerName", tenNguoiChoi);
                intent.putExtra("roomName", tenPhong);
                startActivity(intent);
            }
        });

        //Hiển thị danh sách phòng từ server
        arrayDsPhong = new ArrayList<>();
        adapterDsPhong = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayDsPhong);
        lstPhong.setAdapter(adapterDsPhong);
    }

    private Emitter.Listener nhanUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject obj = (JSONObject) args[0];

                    try {
                        Intent intent = new Intent(ListRoomActivity.this, GameOnlineActivity.class);
                        String penP1 = obj.getString("tenNguoichoi");
                        intent.putExtra("tenPlayer1", penP1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener nhanDanhSachPhong = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject obj = (JSONObject) args[0];

                    try {
                        JSONArray arr = obj.getJSONArray("danhsach");
                        arrayDsPhong.clear();
                        for(int i=0;i<arr.length();i++){
                            String tenPhong = arr.getString(i);
                            arrayDsPhong.add(tenPhong);
                        }
                        adapterDsPhong.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

}