package com.example.gamedoikhang;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GameOnlineActivity extends AppCompatActivity {

    Dialog dialog;

    int total = 0, score;

    private Socket mSocket;

    private boolean flagCheckJoinGame = false;

    ImageView imgTroVeGameOnline, imgChat;
    TextView txtTenPhong, txtTenChuPhong, txtTenNguoiChoi2;
    String tenNguoiChoi="", tenPhong="", tenNguoiChoi2 ="";

    Button btnJoinGame, btnThoat, btnGuiChat, btnDongChat;

    //Khoi tao Match3
    int[] icon = {
            R.drawable.khoi_time,
            R.drawable.khoi_sach,
            R.drawable.khoi_suckhoe,
            R.drawable.khoi_sucmanh,
            R.drawable.khoi_tien,
            R.drawable.khoi_tocom
    };
    int witdhOfBlock, noOfBlocks = 8, widthOfScreen;
    ArrayList<ImageView> icons = new ArrayList<>();
    int iconToBeDragged, iconToBeReplaced;
    int notIcon = R.drawable.transparent;
    Handler mHandler;
    int interval = 200;


    //Lấy từng cột và hàng sát lề cho vào mảng tương ứng
    Integer[] notValidColumnLeft = {0,8,16,24,32,40,48,56}; //Cột thứ 1 bên trái
    Integer[] notValidColumnRight = {7,15,23,31,39,47,55,63}; //Cột thứ 1 bên phải
    Integer[] notValidRowTop = {0,1,2,3,4,5,6,7}; //Hàng thứ 1 từ trên xuống dưới
    Integer[] notValidRowBottom = {56,57,58,59,60,61,62,63};//Hàng thứ 1 từ dưới lên trên
    List<Integer> listColumnLeft = Arrays.asList(notValidColumnLeft);
    List<Integer> listColumnRight = Arrays.asList(notValidColumnRight);
    List<Integer> listRowTop = Arrays.asList(notValidRowTop);
    List<Integer> listRowBottom = Arrays.asList(notValidRowBottom);
    Integer[] right = {6,7,14,15,22,23,30,31,38,39,46,47,54,55,62,63};
    List<Integer> listright = Arrays.asList(right);
    Integer[] left = {0,1,8,9,16,17,24,25,32,33,40,41,48,49,56,57};
    List<Integer> listleft = Arrays.asList(left);

    //Khai báo xử lý tấn công và mất máu
    ProgressBar prbMauNguoiChoi, prbMauNguoiChoi2;
    int mauNguoiChoi = 30, mauNguoiChoi2 = 30;
    int iconatk = R.drawable.khoi_sucmanh;
    int iconHP = R.drawable.khoi_suckhoe;
    //
    boolean move = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_online);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthOfScreen = displayMetrics.widthPixels;
        int heightOfScreen = displayMetrics.heightPixels;
        witdhOfBlock = widthOfScreen / noOfBlocks;
        dialog = new Dialog(this);
//        createBoard();
        //Tạo dữ liệu
        try {
            mSocket = IO.socket("https://doanandroid.herokuapp.com/");
//            mSocket = IO.socket("http://192.168.1.43:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();
        showJoinGame();
        createBoard();

        //Lấy dữ liệu
//        imgTroVeGameOnline = findViewById(R.id.imgBack);
        txtTenPhong = findViewById(R.id.txtTenPhong);
        txtTenChuPhong = findViewById(R.id.txtTenPlayer2);
        txtTenNguoiChoi2 = findViewById(R.id.txtTenPlayer1);
        //
        prbMauNguoiChoi = findViewById(R.id.prbMauPlayer1);
        prbMauNguoiChoi2 = findViewById(R.id.prbMauPlayer2);

        //Lấy dữ liệu tên người chơi
        Bundle extras = getIntent().getExtras();
        if(extras !=null){
            tenNguoiChoi = extras.getString("playerName1");
            tenPhong = extras.getString("roomName");
            tenNguoiChoi2 = extras.getString("tenNguoiChoi2");
        }

        //Gán tên phòng
        txtTenPhong.setText(tenPhong);
        //Lấy dữ liệu chủ phòng
        mSocket.emit("client-send-req-info-room-host", tenPhong);
        mSocket.on("server-send-info-host", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject obj = (JSONObject) args[0];
                        try {
                            String tenChuPhong = obj.getString("host");
                            txtTenChuPhong.setText(tenChuPhong);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        receiveTurnMove();
//        Toast.makeText(GameOnlineActivity.this, String.valueOf(move), Toast.LENGTH_SHORT).show();

        //Di chuyển
        for(final ImageView imageView : icons){
            imageView.setOnTouchListener(new OnSwipeListener(this)
            {
                @Override
                void onSwipeLeft() {

                    if (move){
                        move = false;
                        super.onSwipeLeft();
                        iconToBeDragged = imageView.getId();
                        iconToBeReplaced = iconToBeDragged - 1;
                        if (!listColumnLeft.contains(iconToBeDragged))
                            iconInterchange(1);
                        sendTurnMove(move);
                    }
                }

                @Override
                void onSwipeRight() {
                    if (move) {
                        move = false;
                        super.onSwipeRight();
                        iconToBeDragged = imageView.getId();
                        iconToBeReplaced = iconToBeDragged + 1;
                        if (!listColumnRight.contains(iconToBeDragged))
                            iconInterchange(2);
                        sendTurnMove(move);
                    }
                }

                @Override
                void onSwipeTop() {
                    if (move) {
                        move = false;
                        super.onSwipeTop();
                        iconToBeDragged = imageView.getId();
                        iconToBeReplaced = iconToBeDragged - noOfBlocks;
                        if (!listRowTop.contains(iconToBeDragged))
                            iconInterchange(3);
                        sendTurnMove(move);
                    }


                }

                @Override
                void onSwipeBottom() {


                    if (move) {
                        move = false;
                        super.onSwipeBottom();
                        iconToBeDragged = imageView.getId();
                        iconToBeReplaced = iconToBeDragged + noOfBlocks;
                        if (!listRowBottom.contains(iconToBeDragged))
                            iconInterchange(4);
                        sendTurnMove(move);
                    }



                }
            });
        }

        mHandler = new Handler();
        startRepeat();
//
        //Nhận thông tin out phòng khi host rời
        mSocket.on("server-send-player-out-room", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject obj = (JSONObject) args[0];

                        try {
                            boolean isHostOutRoom = obj.getBoolean("dataSendRequestOutRoom");
                            if(isHostOutRoom){
                                Intent i = new Intent(GameOnlineActivity.this, ListRoomActivity.class);
                                i.putExtra("playerName2", tenNguoiChoi2);
                                startActivity(i);
                                finish();
                                mSocket.emit("client-send-player2-leave-room", tenPhong);
                                Toast.makeText(GameOnlineActivity.this, "Chủ phòng vừa thoát khỏi phòng", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        imgTroVeGameOnline = findViewById(R.id.imgBack);
        imgTroVeGameOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GameOnlineActivity.this, ListRoomActivity.class);

                if(tenNguoiChoi!=null){//host
                    i.putExtra("playerName", tenNguoiChoi);
                    String info = tenPhong + "@" + tenNguoiChoi;
                    mSocket.emit("client-send-host-deleteroom", info);

                    //Khi chủ phòng thoát thì người chơi kia cũng phải thoát
                    String hostOutRoom = tenPhong;
                    mSocket.emit("client-send-player-out-room", hostOutRoom);
                    startActivity(i);
                    finish();
                }else{//player2
                    i.putExtra("playerName2", tenNguoiChoi2);
                    String info = tenPhong + "@" + tenNguoiChoi;
                    mSocket.emit("client-send-host-deleteroom", info);
                    startActivity(i);
                    finish();
                }
            }
        });

        //Tạo khung chat
        imgChat = findViewById(R.id.imgChat);
        imgChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChat();
            }
        });

    }

    //Hàm gửi thông tin lượt đi ên server
    private void sendTurnMove(boolean move){
        if(tenNguoiChoi!=null){
            String info = "host" + "@" + move + "@" + tenPhong;
            mSocket.emit("client-send-turn-move", info);
        }else{
            String info = "player2" + "@" + move + "@" + tenPhong;
            mSocket.emit("client-send-turn-move", info);
        }
    }

    //Nhận lượt đi
    private void receiveTurnMove(){
        mSocket.on("server-send-turn-move", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject obj = (JSONObject) args[0];

                        try {
                            String name = obj.getString("turnMove");
                            if(tenNguoiChoi != null){//host
                                    move = true;


                            }else{
                                    move = true;


                            }
                            Toast.makeText(GameOnlineActivity.this, "Tới lượt", Toast.LENGTH_SHORT).show();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    //Show khung chat
    private void showChat(){
        dialog.setContentView(R.layout.dialog_chat);
        btnGuiChat = dialog.findViewById(R.id.btnGuiChat);
        btnDongChat = dialog.findViewById(R.id.btnDongChat);
        final EditText edtChat = (EditText)dialog.findViewById(R.id.edtChat);

        ArrayList<String> arrayChat;
        ArrayAdapter adapterChat;
        ListView lstChat;

        lstChat = dialog.findViewById(R.id.lstChat);

        arrayChat = new ArrayList<>();
        adapterChat= new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayChat);
        lstChat.setAdapter(adapterChat);
        mSocket.on("server-send-chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject obj = (JSONObject) args[0];

                        try {
                            String noiDung = obj.getString("noiDung");
                            arrayChat.add(noiDung);
                            adapterChat.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


        //Gửi chat
        btnGuiChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noiDungChat = edtChat.getText().toString();
                edtChat.setText("");
                String tenPhongGame = txtTenPhong.getText().toString();
                //Kiểm tra ai là người gửi
                if(tenNguoiChoi2 == null){//host
                    String info = noiDungChat + "@" + tenNguoiChoi + "@" + tenPhongGame;
                    mSocket.emit("client-send-chat", info);

                }else{//player2
                    String info = noiDungChat + "@" + tenNguoiChoi2 + "@" + tenPhongGame;
                    mSocket.emit("client-send-chat", info);
                }
            }
        });

        //Đóng khung chat
        btnDongChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    //Khởi tạo
    private void showJoinGame(){
        dialog.setContentView(R.layout.dialog_join);
        btnJoinGame = dialog.findViewById(R.id.btnJoinGame);
        btnThoat = dialog.findViewById(R.id.btnThoat);

        btnJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(tenNguoiChoi2 == null){
                   move = true;
                   dialog.dismiss();
                   String info = tenPhong + "@" + tenNguoiChoi;
                   //Gửi thông tin hostroom lên server
                   mSocket.emit("client-send-info-host", info);
                   mSocket.on("server-send-host-join", new Emitter.Listener() {
                       @Override
                       public void call(Object... args) {
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   JSONObject obj = (JSONObject) args[0];
                                   try {
                                       String info = obj.getString("infoServerSend");
                                       String[] infoArr = info.split(" ");
                                       txtTenChuPhong.setText(infoArr[0]);
                                       Toast.makeText(GameOnlineActivity.this, info, Toast.LENGTH_SHORT).show();
                                   } catch (JSONException e) {
                                       e.printStackTrace();
                                   }
                               }
                           });
                       }
                   });
                   mSocket.on("server-send-player2-join", new Emitter.Listener() {
                       @Override
                       public void call(Object... args) {
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   JSONObject obj = (JSONObject) args[0];
                                   try {
                                       String info = obj.getString("infoServerSend");
                                       String[] infoArr = info.split(" ");
                                       txtTenNguoiChoi2.setText(infoArr[0]);
                                       Toast.makeText(GameOnlineActivity.this, info, Toast.LENGTH_SHORT).show();
                                   } catch (JSONException e) {
                                       e.printStackTrace();
                                   }
                               }
                           });
                       }
                   });
               }else{
                   dialog.dismiss();
                   String info = tenPhong + "@" + tenNguoiChoi2;
                   mSocket.emit("client-send-info-player2", info);
                   mSocket.on("server-send-player2-join", new Emitter.Listener() {
                       @Override
                       public void call(Object... args) {
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   JSONObject obj = (JSONObject) args[0];
                                   try {
                                       String info = obj.getString("infoServerSend");
                                       String[] infoArr = info.split(" ");
                                       txtTenNguoiChoi2.setText(infoArr[0]);
                                       Toast.makeText(GameOnlineActivity.this, info, Toast.LENGTH_SHORT).show();
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

        btnThoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GameOnlineActivity.this, ListRoomActivity.class);
                if(tenNguoiChoi!=null){//host
                    i.putExtra("playerName", tenNguoiChoi);
                    String info = tenPhong + "@" + tenNguoiChoi;
                    mSocket.emit("client-send-host-deleteroom", info);
                    startActivity(i);
                    finish();
                }else{//player2
                    i.putExtra("playerName2", tenNguoiChoi2);
                    String info = tenPhong + "@" + tenNguoiChoi;
                    mSocket.emit("client-send-host-deleteroom", info);
                    startActivity(i);
                    finish();
                }
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    //Di Chuyển icon
    private  void iconInterchange(int huong){
        ImageView i1 = icons.get(iconToBeDragged);
        ImageView i2 = icons.get(iconToBeReplaced);
        float x1 = i1.getX(), y1 = i1.getY();
        float x2 = i2.getX(), y2 = i2.getY();
        int background = (int) i1.getTag();
        int background1 = (int) i2.getTag();
        if (CheckIconInterchange(background,background1,huong)){
            i1.setImageResource(background1);
            i1.setTag(background1);
            i2.setImageResource(background);
            i2.setTag(background);
        }
        else {
            i1.animate().x(x2).y(y2).withEndAction(new Runnable() {
                @Override
                public void run() {
                    i1.animate().x(x1).y(y1);
                }
            });
            i2.animate().x(x1).y(y1).withEndAction(new Runnable() {
                @Override
                public void run() {
                    i2.animate().x(x2).y(y2);
                }
            });
        }
    }

    private boolean CheckIconInterchange(int background, int background1, int huong){
        //Bên phải
        if (huong != 1)
            if (!listright.contains(iconToBeReplaced)){
                if((int) icons.get(iconToBeReplaced + 1).getTag() == background && (int) icons.get(iconToBeReplaced + 2).getTag() == background) { return true; }
            }
        if (huong != 2)
            if (!listright.contains(iconToBeDragged)){
                if((int) icons.get(iconToBeDragged + 1).getTag() == background1 && (int) icons.get(iconToBeDragged + 2).getTag() == background1) { return true; }
            }

        //Bên trái
        if (huong != 2)
            if (!listleft.contains(iconToBeReplaced)){
                if((int) icons.get(iconToBeReplaced - 1).getTag() == background && (int) icons.get(iconToBeReplaced - 2).getTag() == background) { return true; }
            }
        if (huong != 1)
            if (!listleft.contains(iconToBeDragged)) {
                if ((int) icons.get(iconToBeDragged - 1).getTag() == background1 && (int) icons.get(iconToBeDragged - 2).getTag() == background1) { return true; }
            }

        //Bên dưới
        if (huong != 3)
            if (iconToBeReplaced < 48){
                if((int) icons.get(iconToBeReplaced + noOfBlocks).getTag() == background && (int) icons.get(iconToBeReplaced + 2 * noOfBlocks).getTag() == background) { return true; }
            }
        if (huong != 4)
            if (iconToBeDragged < 48){
                if((int) icons.get(iconToBeDragged + noOfBlocks).getTag() == background1 && (int) icons.get(iconToBeDragged + 2 * noOfBlocks).getTag() == background1) { return true; }
            }

        //Bên trên
        if (huong != 4)
            if (iconToBeReplaced >= 16){
                if((int) icons.get(iconToBeReplaced - noOfBlocks).getTag() == background && (int) icons.get(iconToBeReplaced - 2 * noOfBlocks).getTag() == background) { return true; }
            }
        if (huong != 3)
            if (iconToBeDragged >= 16){
                if((int) icons.get(iconToBeDragged - noOfBlocks).getTag() == background1 && (int) icons.get(iconToBeDragged - 2 * noOfBlocks).getTag() == background1) { return true; }
            }

        // Trái Phải
        if (huong != 1 && huong != 2){
            if (!listColumnLeft.contains(iconToBeReplaced) && !listColumnRight.contains(iconToBeReplaced)){
                if((int) icons.get(iconToBeReplaced - 1).getTag() == background && (int) icons.get(iconToBeReplaced + 1).getTag() == background) { return true; }
            }
            if (!listColumnLeft.contains(iconToBeDragged) && !listColumnRight.contains(iconToBeDragged)){
                if((int) icons.get(iconToBeDragged - 1).getTag() == background1 && (int) icons.get(iconToBeDragged + 1).getTag() == background1) { return true; }
            }
        }

        //Trên Dưới
        if (huong != 3 && huong != 4){
            if (iconToBeReplaced > 7 && iconToBeReplaced < 56){
                if((int) icons.get(iconToBeReplaced - noOfBlocks).getTag() == background && (int) icons.get(iconToBeReplaced + noOfBlocks).getTag() == background) { return true; }
            }
            if (iconToBeDragged > 7 && iconToBeDragged < 56){
                if((int) icons.get(iconToBeDragged - noOfBlocks).getTag() == background1 && (int) icons.get(iconToBeDragged + noOfBlocks).getTag() == background1) { return true; }
            }
        }


        return false;
    }

    //Tạo bàn
    private void createBoard(){
        GridLayout gridLayout = findViewById(R.id.board);
        gridLayout.setRowCount(noOfBlocks);
        gridLayout.setColumnCount(noOfBlocks);
        gridLayout.getLayoutParams().width = widthOfScreen;
        gridLayout.getLayoutParams().height = widthOfScreen;
        for(int i = 0; i < noOfBlocks * noOfBlocks; i++){
            ImageView imageView = new ImageView(this);
            imageView.setId(i);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(witdhOfBlock,witdhOfBlock));
            imageView.setMaxHeight(witdhOfBlock);
            imageView.setMaxWidth(witdhOfBlock);
            int randomicon = (int) Math.floor(Math.random() * icon.length);
            imageView.setImageResource(icon[randomicon]);
            imageView.setTag(icon[randomicon]);
            icons.add(imageView);
            gridLayout.addView(imageView);
        }
    }

    Runnable repeatChecher = new Runnable() {
        @Override
        public void run() {

            try {
                checkRowForSix();
                checkRowForFive();
                checkRowForFour();
                checkRowForThree();
                checkColumn();
                moveDownIcon();

            }
            finally {
                mHandler.postDelayed(repeatChecher,interval);
            }

        }
    };
    void startRepeat() {repeatChecher.run();}

    //Phương thức tấn công và mất máu
    //truyền vào icon để kiểm tra icon đó có phải icon nấm đấm hay ko?
    private void checkAtk(int icon){
        if (icon == iconatk){
            //Gửi dữ liệu máu đã mất lên server
            if(tenNguoiChoi!=null){//Host đánh thì thằng kia mất máu
                mauNguoiChoi = mauNguoiChoi - score;
                String info = mauNguoiChoi + "@" + "host" + "@" + tenPhong;
                mSocket.emit("client-send-hp-lost", info);
                mSocket.on("server-send-hp-lost", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                try {
                                    String hpLost = obj.getString("hpLost");
                                    prbMauNguoiChoi.setProgress(Integer.parseInt(hpLost));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                mSocket.on("server-send-hp-lost-2", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                try {
                                    String hpLost = obj.getString("hpLost");

                                    prbMauNguoiChoi2.setProgress(Integer.parseInt(hpLost));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }else{ //player2 đánh thì thằng host chảy máu
                mauNguoiChoi2 = mauNguoiChoi2 - score;
                String info = mauNguoiChoi2 + "@" + "player2" + "@" + tenPhong;
                mSocket.emit("client-send-hp-lost", info);
                mSocket.on("server-send-hp-lost", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                try {
                                    String hpLost = obj.getString("hpLost");

                                    prbMauNguoiChoi.setProgress(Integer.parseInt(hpLost));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                mSocket.on("server-send-hp-lost-2", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                try {
                                    String hpLost = obj.getString("hpLost");

                                    prbMauNguoiChoi2.setProgress(Integer.parseInt(hpLost));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }

        }
    }

    private void checkHP(int icon){
        if (icon == iconHP){
            if (tenNguoiChoi != null){
                mauNguoiChoi2 = mauNguoiChoi2 + score;
                if(mauNguoiChoi2>30) mauNguoiChoi2 = 30;
                String info = mauNguoiChoi2 + "@" + "host" + "@" + tenPhong;
                mSocket.emit("client-send-hp-plus", info);
                mSocket.on("server-send-hp-plus", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                try {
                                    String hpPlus = obj.getString("hpPlus");
                                    prbMauNguoiChoi2.setProgress(Integer.parseInt(hpPlus));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                mSocket.on("server-send-hp-plus-2", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                try {
                                    String hpPlus = obj.getString("hpPlus");

                                    prbMauNguoiChoi.setProgress(Integer.parseInt(hpPlus));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }else { // player2
                mauNguoiChoi = mauNguoiChoi + score;
                if(mauNguoiChoi>30) mauNguoiChoi = 30;
                String info = mauNguoiChoi + "@" + "player2" + "@" + tenPhong;
                mSocket.emit("client-send-hp-plus", info);
                mSocket.on("server-send-hp-plus", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                try {
                                    String hpPlus = obj.getString("hpPlus");
                                    prbMauNguoiChoi2.setProgress(Integer.parseInt(hpPlus));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                mSocket.on("server-send-hp-plus-2", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject obj = (JSONObject) args[0];
                                try {
                                    String hpPlus = obj.getString("hpPlus");

                                    prbMauNguoiChoi.setProgress(Integer.parseInt(hpPlus));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    private void checkRowForThree(){
        for (int i = 0; i <62; i++){
            score = 0;
            int chosedIcon = (int) icons.get(i).getTag();
            boolean isBlank = (int) icons.get(i).getTag() == notIcon;
            Integer[] notValid = {6,7,14,15,22,23,30,31,38,39,46,47,54,55};
            List<Integer> list = Arrays.asList(notValid);
            if(!list.contains(i)){
                int x = i;
                if((int) icons.get(x++).getTag() == chosedIcon && !isBlank && (int) icons.get(x++).getTag() == chosedIcon
                        && (int) icons.get(x).getTag() == chosedIcon)
                {
                    Column(1);
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    score = score + 3;
                    total = score + total;
                    //Kiểm tra chosedIcon
                    checkAtk(chosedIcon);
                    checkHP(chosedIcon);
                }
            }
        }
        moveDownIcon();
    }

    private void checkRowForFour(){
        for (int i = 0; i <61; i++){
            score = 0;
            int chosedIcon = (int) icons.get(i).getTag();
            boolean isBlank = (int) icons.get(i).getTag() == notIcon;
            Integer[] notValid = {5,6,7,13,14,15,21,22,23,29,30,31,37,38,39,45,46,47,53,54,55};
            List<Integer> list = Arrays.asList(notValid);
            if(!list.contains(i)){
                int x = i;
                if((int) icons.get(x++).getTag() == chosedIcon && !isBlank && (int) icons.get(x++).getTag() == chosedIcon
                        && (int) icons.get(x++).getTag() == chosedIcon && (int) icons.get(x).getTag() == chosedIcon)
                {
                    score = score + 4;
                    total = score + total;

                    checkAtk(chosedIcon);
                    checkHP(chosedIcon);

                    Column(1);
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                }
            }
        }
        moveDownIcon();
    }

    private void checkRowForFive(){
        for (int i = 0; i <60; i++){
            score = 0;
            int chosedIcon = (int) icons.get(i).getTag();
            boolean isBlank = (int) icons.get(i).getTag() == notIcon;
            Integer[] notValid = {4,5,6,7,12,13,14,15,20,21,22,23,28,29,30,31,36,37,38,39,44,45,46,47,52,53,54,55};
            List<Integer> list = Arrays.asList(notValid);
            if(!list.contains(i)){
                int x = i;
                if((int) icons.get(x++).getTag() == chosedIcon && !isBlank && (int) icons.get(x++).getTag() == chosedIcon
                        && (int) icons.get(x++).getTag() == chosedIcon && (int) icons.get(x++).getTag() == chosedIcon && (int) icons.get(x).getTag() == chosedIcon)
                {
                    score = score + 5;
                    total = score + total;

                    checkAtk(chosedIcon);
                    checkHP(chosedIcon);

                    Column(1);
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                }
            }
        }
        moveDownIcon();
    }

    private void checkRowForSix(){
        for (int i = 0; i <59; i++){
            score = 0;
            int chosedIcon = (int) icons.get(i).getTag();
            boolean isBlank = (int) icons.get(i).getTag() == notIcon;
            Integer[] notValid = {3,4,5,6,7,11,12,13,14,15,19,20,21,22,23,27,28,29,30,31,35,36,37,38,39,43,44,45,46,47,51,52,53,54,55};
            List<Integer> list = Arrays.asList(notValid);
            if(!list.contains(i)){
                int x = i;
                if((int) icons.get(x++).getTag() == chosedIcon && !isBlank && (int) icons.get(x++).getTag() == chosedIcon
                        && (int) icons.get(x++).getTag() == chosedIcon && (int) icons.get(x++).getTag() == chosedIcon
                        && (int) icons.get(x++).getTag() == chosedIcon && (int) icons.get(x).getTag() == chosedIcon)
                {
                    score = score + 6;
                    total = score + total;

                    checkHP(chosedIcon);
                    checkAtk(chosedIcon);

                    Column(1);
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x--;
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                }
            }
        }
        moveDownIcon();
    }

    private void moveDownIcon(){
        Integer[] firstRow = {0,1,2,3,4,5,6,7};
        List<Integer> list = Arrays.asList(firstRow);
        for(int i = 55; i >= 0; i--){
            if((int) icons.get(i + noOfBlocks).getTag()==notIcon){
                icons.get(i+noOfBlocks).setImageResource((int)icons.get(i).getTag());
                icons.get(i+noOfBlocks).setTag(icons.get(i).getTag());
                icons.get(i).setImageResource(notIcon);
                icons.get(i).setTag(notIcon);

                if (list.contains(i) && (int) icons.get(i).getTag() == notIcon){
                    int randomIcon = (int) Math.floor(Math.random() * icon.length);
                    icons.get(i).setImageResource(icon[randomIcon]);
                    icons.get(i).setTag(icon[randomIcon]);
                }
            }
        }
        for(int i = 0; i<8;i++){
            if((int) icons.get(i).getTag() == notIcon){
                int randomIcon = (int) Math.floor(Math.random() * icon.length);
                icons.get(i).setImageResource(icon[randomIcon]);
                icons.get(i).setTag(icon[randomIcon]);
            }
        }
    }

    private void checkColumn(){
        Column(0);
        moveDownIcon();
    }

    private void Column(int a) {
        for (int i = 0; i < 48; i++) {
            score = 0;
            int chosedIcon = (int) icons.get(i).getTag();
            boolean isBlank = (int) icons.get(i).getTag() == notIcon;
            int x = i;
            if ((int) icons.get(x).getTag() == chosedIcon && !isBlank && (int) icons.get(x + noOfBlocks).getTag() == chosedIcon && (int) icons.get(x + 2 * noOfBlocks).getTag() == chosedIcon) {
                if (i < 40 && (int) icons.get(x + 3 * noOfBlocks).getTag() == chosedIcon) {
                    if (i < 32 && (int) icons.get(x + 4 * noOfBlocks).getTag() == chosedIcon) {
                        if (i < 24 && (int) icons.get(x + 5 * noOfBlocks).getTag() == chosedIcon) {
                            if (i < 16 && (int) icons.get(x + 6 * noOfBlocks).getTag() == chosedIcon) {
                                if (i < 8 && (int) icons.get(x + 7 * noOfBlocks).getTag() == chosedIcon) {
                                    icons.get(x).setImageResource(notIcon);
                                    icons.get(x).setTag(notIcon);
                                    x = x + noOfBlocks;
                                    score = score + 1;
                                }
                                icons.get(x).setImageResource(notIcon);
                                icons.get(x).setTag(notIcon);
                                x = x + noOfBlocks;
                                score = score + 1;
                            }
                            icons.get(x).setImageResource(notIcon);
                            icons.get(x).setTag(notIcon);
                            x = x + noOfBlocks;
                            score = score + 1;
                        }
                        icons.get(x).setImageResource(notIcon);
                        icons.get(x).setTag(notIcon);
                        x = x + noOfBlocks;
                        score = score + 1;
                    }
                    icons.get(x).setImageResource(notIcon);
                    icons.get(x).setTag(notIcon);
                    x = x + noOfBlocks;
                    score = score + 1;
                }
                icons.get(x).setImageResource(notIcon);
                icons.get(x).setTag(notIcon);
                x = x + noOfBlocks;
                icons.get(x).setImageResource(notIcon);
                icons.get(x).setTag(notIcon);
                x = x + noOfBlocks;
                icons.get(x).setImageResource(notIcon);
                icons.get(x).setTag(notIcon);
                score = score + 3;
                if (a == 1) score = score - 1;
                total = score + total;

                checkAtk(chosedIcon);
                checkHP(chosedIcon);
            }
        }
    }

}