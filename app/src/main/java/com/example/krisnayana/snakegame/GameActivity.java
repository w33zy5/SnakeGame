package com.example.krisnayana.snakegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

public class GameActivity extends Activity  {


    //region Bagian Deklarasi
    /*Melakukan deklarasi canvas*/
    Canvas canvas;
    /*Deklarasi kelas SnakeView untuk kontrol kegiatan dari permainan*/
    SnakeView snakeView;

    /*Variable untuk menampung bitmap kepala, badan, ekor ular dan apel*/
    Bitmap headBitmap, bodyBitmap, tailBitmap, appleBitmap;

    /*Variabel untuk menampung pergerakan dari ular nantinya*/
    int directionOfTravel = 0;
    //0 = up, 1 = right, 2 = down, 3 = left

    /*Deklarasi tempat penyimpanan lebar, tinggi layar dan jarak antara bagian atas dengan permainan*/
    int screenWidth;
    int screenHeight;
    int topGap;

    /*Variable untuk menyimpan waktu frame sebelumnya*/
    long lastFrameTime;
    /*Variable untuk menyimpan fps, score, dan high score*/
    int fps, score, hi;

    /*Untuk menyimpan koordinat pergerakan dari ular*/
    int [] snakeX;
    int [] snakeY;
    int [] snakeH;
    /*Menyimpan panjang dari ular*/
    int snakeLength;
    /*Menyimpan koordinat letak apel*/
    int appleX;
    int appleY;

    /*Menyimpan ukuran dari arena*/
    int blockSize;
    int numBlocksWide;
    int numBlocksHigh;

    //Matrix ini dideklarasikan untuk memutar kepala dan badan dari ular
    Matrix matrix90 = new Matrix();
    Matrix matrix180 = new Matrix();
    Matrix matrix270 = new Matrix();

    /*Matrix matrixHeadFlip ini dideklarasikan untuk memutar kepala secara mirror
    * membuat kepala dapat menghadap kiri*/
    Matrix matrixHeadFlip = new Matrix();

    //endregion

    public void testSwipe(Display display){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Method untuk memasang suara*/
        /*loadSound();*/
        /*Method untuk mengatur display layar permainan*/
        configureDisplay();
        /*Deklarasi untuk pengendali utama dari permainan*/
        snakeView = new SnakeView(this);

        /*Untuk memulai/memasang method SnakeView pada layar dan memulai permainan*/
        setContentView(snakeView);
    }

    private void configureDisplay() {
        /*Mengetahui ukuran dari layar
        * Deklarasi Display dan dinamakan dengan display
        * setelah itu menyimpan nilai display dengan getWindowManager().getDefaultDisplay()*/
        Display display = getWindowManager().getDefaultDisplay();
        /*Deklarasi Point untuk menyimpan nilai display*/
        Point size = new Point();
        /*display akan mengambil nilai dalam variabel size yang khusus untuk menyimpan point*/
        display.getSize(size);
        /*14:10:50.792 31296-31296/com.example.krisnayana.snakegame E/size,: Point(720, 1280)*/
        Log.e("size, ", String.valueOf(size));
        /*size.x akan disimpan dalam screenWidth*/
        screenWidth = size.x;
        /*size.y akan disimpan dalam screenHeight*/
        screenHeight = size.y;
        /*topGap untuk menaruh teks dibuat dengan membagi screenHeight dengan 14*/
        topGap = screenHeight/14;

        /*Menentukan ukuran block yang diinginkan untuk memenuhi tinggi dan lebar layar*/
        blockSize = screenWidth/40;
        /*apabila di atas 40, maka dibawah juga 40*/
        /*Menentuk seberapa banyak block game yang akan memenuhi tinggi dan lebar permainan*/
        numBlocksWide = 40;
        /*menentukan tinggi arena, dikurangi topGap untuk menaruh teks ke depannya kemudian dibagi blockSize
        * untuk mendapatkan hasil akhir*/
        //Untuk minus 20 yang ada di paling kiri untuk mengurangi tinggi dari arena
        numBlocksHigh = (((screenHeight - topGap))/blockSize) - 16;
        Log.d("GAME_VALUE", "numBlocksHigh" + numBlocksHigh);

        /*Mendeklarasikan bitmap dalam satu variabel*/
        headBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head);
        bodyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.body);
        tailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tail);
        appleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.apple);

        /*Mengubah skala dari bitmap untuk menyesuaikan dengan ukuran block/blockSize agar sesuai dengan arena permainan*/
        headBitmap = Bitmap.createScaledBitmap(headBitmap, blockSize, blockSize, false);
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap, blockSize, blockSize, false);
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap, blockSize, blockSize, false);
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap, blockSize, blockSize, false);

        //Menyimpan nilai rotasi untuk digunakan memutar kepala dan badan
        matrix90.postRotate(90);
        matrix180.postRotate(180);
        matrix270.postRotate(270);

        //Menyimpan nilai untuk mengubah kepala ular sehingga menghadap kiri
        matrixHeadFlip.setScale(-1,1);
        matrixHeadFlip.postTranslate(headBitmap.getWidth(),0);
    }

    @Override
    protected void onStop(){
        super.onStop();
        while (true){
            snakeView.pause();
            break;
        }
        finish();
    }
    @Override
    protected void onResume(){
        super.onResume();
        snakeView.resume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        snakeView.pause();
    }

    /*Method untuk menerima input back dari smartphone android*/
    public boolean onKeyDown(int keyCode, KeyEvent event){
        /*Apabila tombol back ditekan*/
        if(keyCode == KeyEvent.KEYCODE_BACK){
            /*SnakeView di pause*/
            snakeView.pause();

            /*Intent i dideklarasikan dengan class MainActivity*/
            Intent i = new Intent(this, MainActivity.class);
            /*Dan memulai main menu*/
            startActivity(i);
            /*dan menyelesaikannya*/
            finish();
            return true;
        }
        return false;
    }


    /*Sama seperti kelas MainActivity*/
    class SnakeView extends SurfaceView implements Runnable{
        /*Mendeklarasikan Thread untuk menjalankan, meng-animasikan dan menyimpan seluruh kegiatan*/
        Thread ourThread = null;
        /*Mendeklarasikan holder untuk menampilkan gambar permainan*/
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        /*Method untuk meng-animasikan dan menjalankan permainan*/
        public SnakeView(Context context) {
            super(context);
            /*Memanggil holder*/
            ourHolder = getHolder();
            paint = new Paint();

            /*Menyimpan panjang, pergerakan, dan rotasi dari ular*/
            snakeX = new int[200];
            snakeY = new int[200];
            snakeH = new int[200];

            /*Memanggil method untuk memanggil ular*/
            getSnake();
            /*Memanggil method untuk memanggil apel*/
            getApple();
        }

        /*Method untuk memanggil apel*/
        private void getApple(){
            /*Mendeklarasikan sebuah random untuk membuat sebuah pengacakan*/
            Random random = new Random();
            /*Random akan mengembalikan nilai secara acak berdasarkan batas numBlocks*/
            appleX = random.nextInt(numBlocksWide);
            appleY = random.nextInt(numBlocksHigh);
        }

        /*Method untuk memunculkan ular di tengah layar smartphone*/
        private void getSnake() {
            /*Deklarasi panjang dari ular*/
            snakeLength = 3;

            /*Memasang koordinat spawn dari ular dengan cara membagi setengah dari ukuran layar
            * yang terdapat pada numBlocksWide(lebar) dan numBlocksHigh(panjang)*/
            snakeX[0] = numBlocksWide/2;
            snakeY[0] = numBlocksHigh/2;

            /*Memasang koordinat untuk menaruh badan ular dan menyimpannya pada snakeX dan snakeY*/
            snakeX[1] = snakeX[0]-1;
            snakeY[1] = snakeY[0];

            /*Memasang koordinat untuk menaruh ekor ular dan menyimpannya pada snakeX dan snakeY*/
            snakeX[2] = snakeX[1]-1;
            snakeY[2] = snakeY[0];
        }

        /*Menjalankan Thread/method run selama playingSnake atau permainan masih berlangsung*/
        @Override
        public void run() {
            while(playingSnake){
                /*Memanggil method untuk melakukan update game*/
                updateGame();
                /*Memanggil method untuk Memasang gambar ular dan apel*/
                drawGame();
                /*Mengatur seberapa cepat permainan*/
                controlFPS();
            }
        }

        /*Method ini dipanggil dan akan mengubah playingSnake menjadi false
        * sehingga menunda beberapa method dalam permainan yang akan menyebabkan
        * permainan dipause*/
        public void pause(){
            playingSnake = false;
            try{
                /*.join() untuk menunggu Thread untuk mati/selesai dijalankan/tertutup*/
                ourThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /*Method untuk memulai game kembali dan mengubah playingSnake menjadi true
        * dan memanggil Thread yang sebelumnya dipause dan memulainya*/
        public void resume(){
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        /*Method ini digunakan untuk mengendalikan seberapa cepat permainan berjalan.
        * berapa frame per second frame akan diganti*/
        private void controlFPS() {
            /*Mengambil waktu yang sekarang dikurang waktu yang dikurang sebelumnya*/
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            /*Variabel untuk menampung waktu jeda per frame semakin besar waktu yang
            * harus dikurangi oleh timeThisFrame, maka semakin lama jeda antar frame*/
            long timeToSleep = 200 - timeThisFrame;
            /*Menghitung fps*/
            if(timeThisFrame > 0){
                Log.e("fps: ", String.valueOf(fps));
                fps = (int)(1000/timeThisFrame);
            }
            if(timeToSleep > 0){
                try {
                    /*Mengambil waktu untuk jeda dengan waktu berdasarkan variable timeToSleep*/
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            /*Mengambil waktu yang sekarang*/
            lastFrameTime = System.currentTimeMillis();
        }

        /*Method untuk memasang gambar dari ular dan memasang arena
        * Method ini dipanggil berkali kali bersama dengan update dan controlFPS untuk menghasilkan
        * gerakan dinamis permainan.*/
        private void drawGame() {
            if(ourHolder.getSurface().isValid()){
                /*Lock canvas sebelum di post dalam aplikasi*/
                canvas = ourHolder.lockCanvas();
                /*Membuat layar hitam*/
                canvas.drawColor(Color.BLACK);
                /*Set warna garis yang akan ditampilkan (Putih)*/
                paint.setColor(Color.argb(255,255,255,255));
                /*Memasang ukuran dari font setengah dari space layar*/
                paint.setTextSize(topGap/2);
                /*Set posisi teks dengan x sedikit menjauh dari ujung layar dan kurang dari bagian atas arena permainan (x:10, y:topGap-6)*/
                canvas.drawText("Score: "+ score + " Hi: "+hi, 10, topGap-6, paint);

                /*Set tebal garis pembatas untuk digambar nantinya*/
                paint.setStrokeWidth(3);

                /* Bagian ini untuk membuat garis pembatas dengan canvas.drawLine(), apa yang diterima line ini sebagai berikut
                canvas.drawLine(float startX, float startY, float stopX, float stopY, Paint paint)*/
                /*Log.e("topGap, ", String.valueOf(topGap));
                Log.e("numBlocksHigh, ", String.valueOf(numBlocksHigh));
                Log.e("numBlocksWide", String.valueOf(numBlocksWide));
                Log.e("screenWidth, ", String.valueOf(screenWidth));*/

                /*Garis atas*/
                canvas.drawLine(1, topGap, screenWidth-1, topGap, paint);
                /*Garis kanan*/
                canvas.drawLine(screenWidth-1, topGap, screenWidth-1,topGap+(numBlocksHigh*blockSize),paint);
                /*Garis bawah*/
                canvas.drawLine(screenWidth-1, topGap+(numBlocksHigh*blockSize),1,topGap+(numBlocksHigh*blockSize),paint);
                /*Garis kiri*/
                canvas.drawLine(1,topGap,1,topGap+(numBlocksHigh*blockSize),paint);

                //Menggambar garis atas button
                canvas.drawLine(1,1000, 720, 1000, paint);
                //Menggambar garis bawah button
                canvas.drawLine(1,1200, 720, 1200, paint);

                //Menggambar garis 1 yang berada paling kiri yang berada pada koordinat x 0
                canvas.drawLine(1, 1000, 1, 1200, paint);
                //Menggambar garis 2 setelah garis 1 yang berada pada koordinat x 180
                canvas.drawLine(180, 1000, 180, 1200, paint);
                //Menggambar garis 3 setelah garis 2 yang berada pada koordinat x 360
                canvas.drawLine(360, 1000, 360, 1200, paint);
                //Menggambar garis 4 setelah garis 3 yang berada pada koordinat x 540
                canvas.drawLine(540, 1000, 540, 1200, paint);
                //Menggambar garis 5 setelah garis 4 yang berada pada koordinat x 720
                canvas.drawLine(720, 1000, 720, 1200, paint);

                //Memasang teks untuk button dalam permainan ular
                canvas.drawText("Atas", 10, 1110, paint);
                canvas.drawText("Bawah", 190,1110, paint);
                canvas.drawText("Kiri", 370,1110, paint);
                canvas.drawText("Kanan", 550, 1110, paint);

                //Menyimpan bitmap kepala, badan, dan ekor untuk diputar
                Bitmap rotatedBitmap;

                /*Menggambar kepala dari ular dan bersamaan dengan rotasinya*/
                rotatedBitmap = headBitmap;
                switch (snakeH[0]){
                    case 0://up, mengubah rotasi kepala menjadi 270 derajat dengan matrix270
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), matrix270, true);
                        break;
                    case 1://right
                        break;
                    case 2://down
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap .getWidth(), rotatedBitmap .getHeight(), matrix90, true);
                        break;
                    case 3://left
                        //matrixHeadFlip digunakan untuk melakukan mirror pada kepala sehingga kepala dapat menghadap kiri
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap .getWidth(), rotatedBitmap .getHeight(), matrixHeadFlip, true);
                        break;
                }
                /*Menggambar snake[0] berdasarkan koordinat yang telah diinput dan ukuran dari arena permainan
                * dan menggunakan bitmap yang telah diputar*/
                canvas.drawBitmap(rotatedBitmap, snakeX[0]*blockSize, (snakeY[0]*blockSize)+topGap, paint);

                rotatedBitmap = bodyBitmap;
                /*Perulangan ini digunakan untuk menggambar badan dari ular bersama dengan rotasinya*/
                for(int i = 1; i < snakeLength-1;i++){
                    switch (snakeH[i]){
                        case 0://up
                            rotatedBitmap = Bitmap.createBitmap(bodyBitmap , 0, 0, bodyBitmap .getWidth(), bodyBitmap .getHeight(), matrix270, true);
                            break;
                        case 1://right
                            break;
                        case 2://down
                            rotatedBitmap = Bitmap.createBitmap(bodyBitmap , 0, 0, bodyBitmap .getWidth(), bodyBitmap .getHeight(), matrix90, true);
                            break;
                        case 3://left
                            rotatedBitmap = Bitmap.createBitmap(bodyBitmap , 0, 0, bodyBitmap .getWidth(), bodyBitmap .getHeight(), matrix180, true);
                            break;
                    }
                    /*Menggambar badan dari ular*/
                    canvas.drawBitmap(rotatedBitmap, snakeX[i]*blockSize, (snakeY[i]*blockSize)+topGap, paint);
                }

                rotatedBitmap = tailBitmap;
                /*Menggambar ekor dari ular dan merubah rotasinya sesuai dengan directionoftravel*/
                switch (snakeH[snakeLength-1]){
                    case 0://up
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap .getWidth(), rotatedBitmap .getHeight(), matrix270, true);
                        break;
                    case 1://right
                        break;
                    case 2://down
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap .getWidth(), rotatedBitmap .getHeight(), matrix90, true);
                        break;
                    case 3://left
                        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap , 0, 0, rotatedBitmap .getWidth(), rotatedBitmap .getHeight(), matrix180, true);
                        break;
                }
                canvas.drawBitmap(rotatedBitmap, snakeX[snakeLength-1]*blockSize, (snakeY[snakeLength-1]*blockSize)+topGap, paint);

                /*Menggambar snake[0] berdasarkan koordinat yang telah diinput dan ukuran dari arena permainan*//*
                canvas.drawBitmap(headBitmap, snakeX[0]*blockSize,(snakeY[0]*blockSize)+topGap, paint);

                *//*Menggambar badan dari ular*//*
                for(int i=1; i<snakeLength-1; i++){
                    canvas.drawBitmap(bodyBitmap, snakeX[i]*blockSize, (snakeY[i]*blockSize)+topGap, paint);
                }

                *//*Menggambar ekor dari ular*//*
                canvas.drawBitmap(tailBitmap, snakeX[snakeLength - 1]*blockSize, (snakeY[snakeLength - 1]*blockSize)+topGap,paint);*/

                /*Menggambar apel*/
                canvas.drawBitmap(appleBitmap, appleX * blockSize, (appleY*blockSize) + topGap, paint);

                /*Unlock canvas dan mem-posting nya dalam screen*/
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        /*Method update game*/
        private void updateGame() {
            /*Apabila koordinat kepala x dan y sama dengan koordinat x dan y apple
            * maka snakeLength atau panjang dari ular akan bertambah dan posisi apel yang baru akan berubah
            * karena method getApple() dipanggil, score bertambah berdasarkan panjang dari ular, dan memulai sound*/
            if(snakeX[0] == appleX && snakeY[0] == appleY){
                snakeLength++;
                getApple();
                score = score + snakeLength;
            }

            /*Menggerakkan ular dari bagian belakang ular berdasarkan koordinat posisi bagian belakang ular
            * Karena i menggunakan snakeLength yang akan mengambil panjang dari ular dan menguranginya 1 untuk mengubah
            * koordinat posisi snakeX dan snakeY*/
            for(int i = snakeLength; i > 0; i--){
                snakeX[i] = snakeX[i - 1];
                snakeY[i] = snakeY[i - 1];

                /*Mengubah rotasi dari kepala ular*/
                snakeH[i] = snakeH[i-1];
            }

            /*Menggerakkan kepala dari ular dengan mengubah directionOfTravel
            * directionOfTravel diambil dari method onTouchEvent yang mengatur penambahan dan pengurangan nilai directionOfTravel
            * snakeX[0] dan snakeY[0] adalah kepala dari ular,*/
            switch (directionOfTravel){
                case 0:
                    /*Menggerakkan ular ke atas*/
                    snakeY[0] --;
                    /*Mengubah arah kepala ular menghadap atas*/
                    snakeH[0] = 0;
                    Log.d("snakeXY[0]", "X : " + snakeX[0] + " Y : " + snakeY[0]);
                    Log.e("snakeY[0] -- :", String.valueOf(snakeY[0]));
                    //Log.e("Case 0: ", String.valueOf(directionOfTravel));
                    break;

                case 1:
                    /*Menggerakkan ular ke kanan*/
                    snakeX[0] ++;
                    /*Mengubah arah kepala ular menghadap kanan*/
                    snakeH[0] = 1;
                    Log.d("snakeXY[0]", "X : " + snakeX[0] + " Y : " + snakeY[0]);
                    Log.e("snakeX[0] ++ :", String.valueOf(snakeX[0]));
                    //Log.e("Case 1: ", String.valueOf(directionOfTravel));
                    break;

                case 2:
                    /*Menggerakkan ular ke bawah*/
                    snakeY[0] ++;
                    /*MEngubah arah kepala ular menghadap bawah*/
                    snakeH[0] = 2;
                    Log.d("snakeXY[0]", "X : " + snakeX[0] + " Y : " + snakeY[0]);
                    Log.e("snakeY[0] ++ :", String.valueOf(snakeY[0]));
                    //Log.e("Case 2: ", String.valueOf(directionOfTravel));
                    break;

                case 3:
                    /*Menggerakkan ular ke kiri*/
                    snakeX[0] --;
                    /*Mengubah arah kepala ular menghadap kiri*/
                    snakeH[0] = 3;
                    Log.d("snakeXY[0]", "X : " + snakeX[0] + " Y : " + snakeY[0]);
                    Log.e("snakeX[0] -- :", String.valueOf(snakeX[0]));
                    //Log.e("Case 3: ", String.valueOf(directionOfTravel));
                    break;
            }
            /*boolean dead false ini merupakan variabel yang digunakan untuk melihat apakah
            * ular keluar dari batas arena permainan atau tidak*/
            boolean dead = false;
            /*Menyentuh pembatas kiri*/
            if(snakeX[0] == -1)dead = true;
            /*Menyentuh pembatas kanan*/
            if(snakeX[0] >= numBlocksWide)dead = true;
            /*Menyentuh pembatas atas*/
            if(snakeY[0] == -1)dead = true;
            /*Menyentuh pembatas bawah*/
            if(snakeY[0] == numBlocksHigh)dead = true;

            /*Apabila ular menabrak diri sendiri,*/
            for(int i = snakeLength - 1; i>0; i--){
                /*Apabila panjang dari ular telah lebih dari 2(kenapa 2? karena pemain tidak akan bisa menabrak badan sendiri ketika panjang badan masih kecil)
                * dan apabila koordinat kepala ular menyentuh koordinat posisi dari badan ular, yaitu snakeX dan snakeY*/
                if((i > 2) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])){
                    /*Dead akan menjadi true*/
                    dead = true;
                }
            }

            /*Jika dead true*/
            if(dead){
                /*score kembali menjadi 0*/
                score = 0;
                /*dan mengubah posisi ular dengan memanggil method getSnake()*/
                getSnake();
            }
        }

        /*Method untuk menerima input pemain*/
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            /*Switch menerima action dari pemain dengan motionEvent.getAction() dan MotionEvent.ACTION_MASK*/
            /*Disini switch menggunakan bitwise operator & yang artinya 'bitwise and'
             * motionEvent.getAction() untuk mengembalikan nilai bit yang dilakukan, MotionEvent.ACTION_MASK juga mengambalikan bit*/
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                /*Apabila layar smartphone ditekan dan setelah itu diangkat
                * Apabila telah selesai menekan layar smartphone dan mengangkat jari*/
                case MotionEvent.ACTION_UP:
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    Log.d("COORDINATE", "X : " + x + " Y : " + y);

                    //float terms1 = (motionEvent.getX() - snakeX[0])/2;
                    float terms2 = (motionEvent.getX() - snakeX[0]);
                    //float terms3 = (motionEvent.getX() * 0)+snakeX[0];
                    //float terms4 = (terms2 + screenWidth)/2;
                    float terms5 = (screenWidth - snakeX[0])/2;
                    float target = (snakeX[0]/2);

                    //Log.d("VTERMS", "terms1 : " + terms1);
                    Log.d("VTERMS", "terms2 : " + terms2);
                    Log.d("VTERMS", "terms5 : " + terms5);
                    //Log.d("VTERMS", "terms4 : " + terms4);
                    Log.d("VTERMS", "target : " + target);
                    Log.d("MOVE", "x : " + String.valueOf(x));
                    Log.d("MOVE", "y : " + String.valueOf(y));
                    //1# if(motionEvent.getX() >= screenWidth/2)
                    //2# if(snakeX[0] >= snakeX[0]/2)
                    //3# if((terms) >= (target))
                    if((x >= 0 && x <= 180)  && (y > 1000 && y < 1200)){
                        //Up
                        if(directionOfTravel != 2)
                        directionOfTravel = 0;
                        Log.d("DoT", "0:Up " + directionOfTravel);
                        /*Log.d("VTERMS_COND", "DoT 1 : " + true);
                        *//*Apabila directionOfTravel menjadi 4, maka directionOfTravel diubah kembali menjadi 0 agar tidak keluar dari kendali sentuh pemain*//*
                        if(directionOfTravel == 4){
                            directionOfTravel = 0;
                        }*/
                    }else if((x > 180 && x <= 360) && (y > 1000 && y < 1200)){
                        //Down
                        if(directionOfTravel != 0)
                        directionOfTravel = 2;
                        Log.d("DoT", "1:Right " + directionOfTravel);
                        /*directionOfTravel--;
                        Log.d("VTERMS_COND", "DoT 2 : " + true);
                        *//*Apabila directionOfTravel menjadi -1, maka directionOfTravel diubah kembali menjadi 3 agar tidak keluar dari kendali sentuh pemain*//*
                        if(directionOfTravel == -1){
                            directionOfTravel = 3;
                        }*/
                    }else if((x > 360 && x <= 540) && (y > 1000 && y < 1200)){
                        //Left
                        if(directionOfTravel != 1)
                        directionOfTravel = 3;
                        Log.d("DoT", "2:Down " + directionOfTravel);
                    }else if((x > 540 && x <= 720) && (y > 1000 && y < 1200)){
                        //Right
                        if(directionOfTravel != 3)
                            directionOfTravel = 1;
                        Log.d("DoT", "3:Left " + directionOfTravel);
                    }
            }
            return true;
        }
    }
}
