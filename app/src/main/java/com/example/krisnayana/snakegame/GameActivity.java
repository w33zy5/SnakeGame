package com.example.krisnayana.snakegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

public class GameActivity extends Activity {


    //region Bagian Deklarasi
    /*Melakukan deklarasi canvas*/
    Canvas canvas;
    /*Deklarasi kelas SnakeView untuk kontrol kegiatan dari permainan*/
    SnakeView snakeView;

    /*Variable untuk menampung bitmap kepala, badan, ekor ular dan apel*/
    Bitmap headBitmap, bodyBitmap, tailBitmap, appleBitmap;

    /*Variable untuk menampung suara*/
    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;

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
    /*Menyimpan panjang dari ular*/
    int snakeLength;
    /*Menyimpan koordinat letak apel*/
    int appleX;
    int appleY;

    /*Menyimpan ukuran dari arena*/
    int blockSize;
    int numBlocksWide;
    int numBlocksHigh;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Method untuk memasang suara*/
        loadSound();
        /*Method untuk mengatur display layar permainan*/
        configureDisplay();
        /*Deklarasi untuk pengendali utama dari permainan*/
        snakeView = new SnakeView(this);
        /*Untuk memulai/memasang method SnakeView pada layar dan memulai permainan*/
        setContentView(snakeView);
    }

    private void configureDisplay() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        topGap = screenHeight/14;

        blockSize = screenWidth/40;

        numBlocksWide = 40;
        numBlocksHigh = ((screenHeight - topGap))/blockSize;

        headBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head);
        bodyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.body);
        tailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tail);
        appleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.apple);

        headBitmap = Bitmap.createScaledBitmap(headBitmap, blockSize, blockSize, false);
        bodyBitmap = Bitmap.createScaledBitmap(bodyBitmap, blockSize, blockSize, false);
        tailBitmap = Bitmap.createScaledBitmap(tailBitmap, blockSize, blockSize, false);
        appleBitmap = Bitmap.createScaledBitmap(appleBitmap, blockSize, blockSize, false);

    }

    private void loadSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try{
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("sample1.ogg");
            sample1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample2.ogg");
            sample2 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample3.ogg");
            sample3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("sample4.ogg");
            sample4 = soundPool.load(descriptor, 0);
        }catch (IOException e){
            Log.e("Error", "Failed to load sound files");
        }
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

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            snakeView.pause();

            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return false;
    }

    /*Sama seperti kelas MainActivity*/
    class SnakeView extends SurfaceView implements Runnable{
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playingSnake;
        Paint paint;

        public SnakeView(Context context) {
            super(context);
            /*Memanggil holder*/
            ourHolder = getHolder();
            paint = new Paint();

            /*Menyimpan panjang dari ular dan untuk menyimpan pergerakan dari ular*/
            snakeX = new int[200];
            snakeY = new int[200];

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

        /*Method untuk memunculkan ular*/
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
            long timeToSleep = 100 - timeThisFrame;
            if(timeThisFrame > 0){
                /*Menghitung fps*/
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

        private void drawGame() {
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.argb(255,255,255,255));
                paint.setTextSize(topGap/2);
                canvas.drawText("Score: "+ score + " Hi: "+hi, 10, topGap-6, paint);

                paint.setStrokeWidth(3);
                canvas.drawLine(1, topGap, screenWidth-1, topGap, paint);
                canvas.drawLine(screenWidth-1, topGap, screenWidth-1,topGap+(numBlocksHigh*blockSize),paint);
                canvas.drawLine(screenWidth-1, topGap+(numBlocksHigh*blockSize),1,topGap+(numBlocksHigh*blockSize),paint);
                canvas.drawLine(1,topGap,1,topGap+(numBlocksHigh*blockSize),paint);

                canvas.drawBitmap(headBitmap, snakeX[0]*blockSize,(snakeY[0]*blockSize)+topGap, paint);

                for(int i=1; i<snakeLength-1; i++){
                    canvas.drawBitmap(bodyBitmap, snakeX[i]*blockSize, (snakeY[i]*blockSize)+topGap, paint);
                }

                canvas.drawBitmap(tailBitmap, snakeX[snakeLength - 1]*blockSize, (snakeY[snakeLength - 1]*blockSize)+topGap,paint);

                canvas.drawBitmap(appleBitmap, appleX * blockSize, (appleY*blockSize) + topGap, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        private void updateGame() {
            if(snakeX[0] == appleX && snakeY[0] == appleY){
                snakeLength++;
                getApple();
                score = score + snakeLength;
                soundPool.play(sample1, 1,1,0,0, 1);
            }

            for(int i = snakeLength; i > 0; i--){
                snakeX[i] = snakeX[i - 1];
                snakeY[i] = snakeY[i - 1];
            }

            switch (directionOfTravel){
                case 0:
                    snakeY[0] --;
                    break;

                case 1:
                    snakeX[0] ++;
                    break;

                case 2:
                    snakeY[0] ++;
                    break;

                case 3:
                    snakeX[0] --;
                    break;
            }
            boolean dead = false;
            if(snakeX[0] == -1)dead = true;
            if(snakeX[0] >= numBlocksWide)dead = true;
            if(snakeY[0] == -1)dead = true;
            if(snakeY[0] == numBlocksHigh)dead = true;

            for(int i = snakeLength - 1; i>0; i--){
                if((i > 4) && (snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])){
                    dead = true;
                }
            }

            if(dead){
                soundPool.play(sample4,1,1,0,0,1);
                score = 0;
                getSnake();
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                    if(motionEvent.getX() >= screenWidth/2){
                        directionOfTravel ++;

                        if(directionOfTravel == 4){
                            directionOfTravel = 0;
                        }
                    }else{
                        directionOfTravel--;
                        if(directionOfTravel == -1){
                            directionOfTravel = 3;
                        }
                    }
            }
            return true;
        }
    }
}
