package com.example.krisnayana.snakegame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    //region Deklarasi
    /*Melakukan deklarasi pada canvas untuk layout dari permainan*/
    Canvas canvas;

    /*Menjalankan animasi untuk main menu*/
    SnakeAnimView snakeAnimView;

    /*Menampung bitmap frame animasi kepala*/
    Bitmap headAnimBitmap;

    /*Untuk menggambarkan porsi bitmap yang akan ditampilkan di main menu (bitmap kepala)*/
    Rect rectToBeDrawn;

    /*Dimensi dari satu frame untuk animasi kepala*/
    int frameHeight = 64;
    int frameWidth = 64;
    int numFrames = 6;
    int frameNumber;

    /*Menyimpan ukuran lebar dan panjang layar*/
    int screenWidth;
    int screenHeight;

    /*Menyimpan nilai frame per second dan menentukan seberapa cepat animasi berjalan*/
    long lastFrameTime;
    int fps;

    /*Menyimpan nilai*/
    int hi;

    /*Untuk mengirim pemain pada kelas GameActivity*/
    Intent i;

    //endregion

    /*Note: method onCreate dieksekusi ketika sebuah activity dibuat*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Mengambil dimensi layar dengan mendeklarasi Display menamakannya display
        * kemudian memanggil window manager untuk mengambil ukuran display layar smartphone*/
        Display display = getWindowManager().getDefaultDisplay();
        /*Menedeklarasi point x dan y untuk variabel size*/
        Point size = new Point();
        /*display akan mengambil size dari screen berdasarkan point variabel size*/
        display.getSize(size);
        /*size dari layar akan disimpan dalam variabel screenWidth dengan size.x dan screenHeight dengan size.y*/
        screenWidth = size.x;
        screenHeight = size.y;

        /*Mendeklarasikan headAnimBitmap dengan gambar ular yang telah ada*/
        headAnimBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.head_sprite_sheet);

        /*Mendeklarasikan method untuk menampilkan animasi dari ular*/
        snakeAnimView = new SnakeAnimView(this);
        setContentView(snakeAnimView);

        /*Untuk menyimpan intent atau bisa dibilang activity dari permainan, yaitu GameActivity*/
        i = new Intent(this, GameActivity.class);
    }

    /*Kenapa extends SurfaceView? Dikarenakan pada permainan ini, atau pada main menu diperlukan
    * sebuah update GUI atau rendering yang sangat banyak atau sangat cepat.
    * Intinya, SurfaceView digunakan untuk melakukan update GUI dan rendering yang cepat*/

    /*Kenapa implements Runnable? Dikarenakan untuk menjalankan beberapa kegiatan secara langsung
    * Runnable memiliki kekuatan yang hampir sama seperti HandlerThread, AsyncTask, dan IntentService
    * Pada permainan ini, Thread dan Runnable digunakan untuk melakukan method onPause dan onStart*/
    class SnakeAnimView extends SurfaceView implements Runnable{
        /*Dikarenakan ada Runnable maka dapat dideklarasikan Thread*/
        Thread ourThread = null;
        /*SurfaceHolder digunakan untuk menampung surface yang digunakan untuk melakukan animasi dari kepala ular*/
        SurfaceHolder ourHolder;
        /*Kenapa volatile? Dikarenakan playingSnake akan digunakan pada dua method yang berbeda/dua Thread yang berbeda
        * Fungsi dari volatile ini adalah untuk memberitahu compiler untuk lebih teliti dalam mengatasi variable ini*/
        volatile boolean playingSnake;
        Paint paint;

        public SnakeAnimView(Context context) {
            super(context);
            /*Untuk mengambil holder*/
            ourHolder = getHolder();
            /*Untuk melakukan pengaturan edit teks*/
            paint = new Paint();
            /*Bitmap lebar kepala ular dipotong berdasarkan jumlah frame yang telah ditentukan, yaitu 6
            * Ini dilakukan untuk memanggil bitmap dan memasang nilai width dan height pada varible frame yang akan dipakai nantinya*/
            frameWidth = headAnimBitmap.getWidth()/numFrames;
            frameHeight = headAnimBitmap.getHeight();
        }

        /*Method run ini akan menjalankan method update, draw, dan controlFPS satu setelah yang lainnya*/
        @Override
        public void run() {
            while (playingSnake){
                update();
                draw();
                controlFPS();
            }
        }

        private void pause(){
            playingSnake = false;
            try{
                ourThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void resume(){
            playingSnake = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        public boolean onTouchEvent(MotionEvent motionEvent){
            startActivity(i);
            return true;
        }

        /*Mengendalikan frame per second yang ditampilkan di screen*/
        private void controlFPS() {
            /*Menyimpan waktu sekarang dikurang waktu frame sebelumnya*/
            long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
            /*Untuk menyimpan frame pada saat aplikasi di keluarkan sementara*/
            long timeToSleep = 500 - timeThisFrame;
            /*Menyimpan fps*/
            if(timeThisFrame > 0){
                fps = (int)(1000/timeThisFrame);
            }
            /*Jika aplikasi di pause*/
            if(timeToSleep > 0){
                try{
                    ourThread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            /*Menyimpan waktu frame sebelumnya*/
            lastFrameTime = System.currentTimeMillis();
        }

        private void draw() {
            /*Apabila ourHolder terdeteksi*/
            if(ourHolder.getSurface().isValid()){
                /*canvas akan mengambil tampilan dari ourHolder dan melakukan lockCanvas() pada holder*/
                canvas = ourHolder.lockCanvas();
                /*Warna dari canvas*/
                canvas.drawColor(Color.BLACK);
                paint.setColor(Color.argb(255,255,255,255));
                /*Set ukuran dari teks "Snake"*/
                paint.setTextSize(150);
                /*Set apa isi teks dan tempat nya*/
                canvas.drawText("Snake", 10, 150, paint);
                paint.setTextSize(25);
                /*Set teks high score, untuk koordinat y, screenHeight - 50*/
                canvas.drawText("Hi Score: " + hi, 10, screenHeight-50, paint);

                /*Memasang nilai untuk destRect untuk mengatur tata letak dari gambar*/
                Rect destRect = new Rect(screenWidth/2-100, screenHeight/2-100, screenWidth/2+100, screenHeight/2+100);
                /*Memasang bitmap kepala ular beserta dengan mengambil gambar kepala ular yang berbeda dari rectToBeDrawn
                * dari update(), destRect untuk lokasi pemasangan kepala di screen, dan paint untuk menampilkan teks*/
                canvas.drawBitmap(headAnimBitmap, rectToBeDrawn, destRect, paint);

                /*Untuk memng-unlock canvas dan mem-post nya di screen*/
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        private void update() {
            /*Bitmap kepala dianimasikan dengan melacak dan memilih nomor frame yang harus ditampilkan
            * Setiap update method yang dijalankan, method ini akan meng-kalkulasi sprite yang digunakan dengan variabel
            * frameWidth, frameHeight, dan frameNumber*/
            rectToBeDrawn = new Rect((frameNumber * frameWidth)- 1,
                    0, (frameNumber * frameWidth + frameWidth)-1,
                    frameHeight);

            /*Menjalankan frame selanjutnya*/
            frameNumber++;

            /*Apabila frame sudah mencapai frame terakhir, maka kembali ke frame awal*/
            if(frameNumber == numFrames){
                frameNumber = 0;
            }
        }
    }

    /*Note: method onStop termasuk dalam fase berhenti
    * Dalam fase ini kita melakukan undo semua yang dilakukan pada method onCreate
    * Jika mencapai fase ini, activity dapat selesai beberapa waktu kemudian*/

    @Override
    protected void onStop(){
        super.onStop();

        while(true){
            snakeAnimView.pause();
            break;
        }
        finish();
    }

    /*Note: onResume merupakan method yang dapat dimasuki dan berjalan setelah onStart
    * Juga method ini dapat dimasuki ketika sebelumnya aplikasi telah dipause dan memasuki
    * kondisi permainan yang sebelumnya*/

    @Override
    protected void onResume(){
        super.onResume();
        snakeAnimView.resume();
    }

    /*Note: onPause berjalan apabila aplikasi dipause. Dalam method ini dapat melakukan
    * penyimpanan kondisi permainan yang sekarang*/

    @Override
    protected void onPause(){
        super.onPause();
        snakeAnimView.pause();
    }

    /*Mengambil tombol back dari android, onKeyDown untuk mengambil*/
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            snakeAnimView.pause();
            finish();
            return true;
        }
        return false;
    }
}