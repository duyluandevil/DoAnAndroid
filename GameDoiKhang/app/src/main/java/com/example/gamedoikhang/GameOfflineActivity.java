package com.example.gamedoikhang;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameOfflineActivity extends AppCompatActivity {

    //Khai báo Dialog
    Dialog dialog;
    Button btnCoBip, btnKhongBip, btnChoiLai, btnTroVe;

    //Tính điểm cao nhất
    TextView txtDiemCaoNhat;
    String HighScore;

    //Khai bao
    Boolean move = false;

    //Thowif gian
    int icontime = R.drawable.khoi_time;
    int time = 0;

    GridLayout gridLayout;

    //Khởi tạo điểm, thời gian
    TextView txtTotal, txtTime;

    //Khởi tạo nút công cụ
    TextView txtBack;
    ImageView imgRefresh;

    private CountDownTimer countDownTimer;
    private long Milliseconds=60000;

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

    int total = 0, score;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_offline);

        txtTime = findViewById(R.id.time);
        txtTotal = findViewById(R.id.total);
        gridLayout = findViewById(R.id.board);

        txtDiemCaoNhat = findViewById(R.id.txtDiemCaoNhat);

        //đọc trạng thái ứng dụng: Điểm cao nhất
        SharedPreferences preferences = getSharedPreferences("GameM3",MODE_PRIVATE);
        HighScore = preferences.getString("DiemCaoNhat","0");
        txtDiemCaoNhat.setText(HighScore);

        //create Board
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthOfScreen = displayMetrics.widthPixels;
        int heightOfScreen = displayMetrics.heightPixels;
        witdhOfBlock = widthOfScreen / noOfBlocks;
        createBoard();

        //Di chuyển
        for(final ImageView imageView : icons){
            imageView.setOnTouchListener(new OnSwipeListener(this)
            {
                @Override
                void onSwipeLeft() {

                    super.onSwipeLeft();
                    iconToBeDragged = imageView.getId();
                    iconToBeReplaced = iconToBeDragged - 1;
                    if (!listColumnLeft.contains(iconToBeDragged))
                        iconInterchange(1);
                }

                @Override
                void onSwipeRight() {

                    super.onSwipeRight();
                    iconToBeDragged = imageView.getId();
                    iconToBeReplaced = iconToBeDragged + 1;
                    if (!listColumnRight.contains(iconToBeDragged))
                        iconInterchange(2);
                }

                @Override
                void onSwipeTop() {

                    super.onSwipeTop();
                    iconToBeDragged = imageView.getId();
                    iconToBeReplaced = iconToBeDragged - noOfBlocks;
                    if (!listRowTop.contains(iconToBeDragged))
                        iconInterchange(3);
                }

                @Override
                void onSwipeBottom() {

                    super.onSwipeBottom();
                    iconToBeDragged = imageView.getId();
                    iconToBeReplaced = iconToBeDragged + noOfBlocks;
                    if (!listRowBottom.contains(iconToBeDragged))
                        iconInterchange(4);
                }
            });
        }

        mHandler = new Handler();
        startRepeat();

        //Hiển thị thời gian
        startTimer();
        updateTimer();

        //Trở về màn hình chính
        txtBack = findViewById(R.id.txtBack);
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameOfflineActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        dialog = new Dialog(this);

        //Reload lại game
        imgRefresh = findViewById(R.id.txtRefresh);
        imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRefresh();
            }
        });

    }

    @Override
    protected void onDestroy() {
        countDownTimer.cancel();
        super.onDestroy();
        Toast.makeText(GameOfflineActivity.this, "Mày thoát mẹ game rồi", Toast.LENGTH_SHORT).show();
    }

    //Điểm hiện tại lớn hơn điểm cao nhất thì lưu lại
    private void CheckScore(){
        int s = Integer.parseInt(HighScore);
        if (total > s){
            SharedPreferences preferences = getSharedPreferences("GameM3",MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("DiemCaoNhat", String.valueOf(total));
            edit.apply();
            txtDiemCaoNhat.setText(String.valueOf(total));
        }
    }

    //Tăng thời gian
    public void AddTime(int x){
        if(x == icontime) {
            time = score * 1000;
            Milliseconds = Milliseconds + time;
            countDownTimer.cancel();
            startTimer();

        }
    }

    //Reset bàn chơi
    private void resetBoard(){
        for(int i = 0; i < noOfBlocks * noOfBlocks; i++){
            icons.get(i).setImageResource(notIcon);
            icons.get(i).setTag(notIcon);
        }
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
                    txtTotal.setText(String.valueOf(total));
                    CheckScore();
                }
                finally {
                    mHandler.postDelayed(repeatChecher,interval);
                }


        }
    };

    void startRepeat() {repeatChecher.run();}

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
                    AddTime(chosedIcon);
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
                    AddTime(chosedIcon);
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
                    AddTime(chosedIcon);
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
                    AddTime(chosedIcon);
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
                AddTime(chosedIcon);
            }
        }
    }

    //Tạo thời gian đếm ngược
    public void startTimer(){
        countDownTimer = new CountDownTimer(Milliseconds,1000) {
            @Override
            public void onTick(long l) {
                Milliseconds = l;
                updateTimer();
            }
            @Override
            public void onFinish(){showThatBai();}
        }.start();
    }

    public void updateTimer() {
        int minutes = (int) Milliseconds / 60000;
        int seconds = (int) Milliseconds % 60000 / 1000;

        String timeLeftText;
        timeLeftText = "" + minutes;
        timeLeftText += ":";
        if(seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;
        txtTime.setText(timeLeftText);
    }

    private void showRefresh(){
        dialog.setContentView(R.layout.dialog_finish);
        btnCoBip = dialog.findViewById(R.id.btnCo);
        btnKhongBip = dialog.findViewById(R.id.btnKhong);



        //Dừng thời gian
//        Long time = Milliseconds;
        countDownTimer.cancel();

        btnCoBip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBoard();
                total = 0;
                txtTotal.setText("0");
                Milliseconds = 60000;
                startTimer();
                dialog.dismiss();
            }
        });
        btnKhongBip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
//                Milliseconds = time;

                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }


    public void showThatBai(){
        total =  0;
        dialog.setContentView(R.layout.dialog_timeout);
        btnTroVe = dialog.findViewById(R.id.btnTroVe);
        btnChoiLai = dialog.findViewById(R.id.btnChoiLai);
        btnChoiLai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBoard();
                total = 0;
                txtTotal.setText("0");
                Milliseconds = 60000;
                startTimer();
                dialog.dismiss();
            }
        });
        btnTroVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameOfflineActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}