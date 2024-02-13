package com.example.testbeetle;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Panel extends SurfaceView {

    // создаею потоконезависмую коллекцию, чтобы не получить
    //ConcurrentModificationException, если коллекция будет изменена во время итерации.
    private CopyOnWriteArrayList<Bug> bugs = new CopyOnWriteArrayList<>();
    //использую  ThreadPoolExecutor с фиксированным количеством потоков
    // так как мне нужно обрабатывать быстрые касания нет возможности создать очередь
    // в одном потоке, тогда я буду контролировать количество потоков, чтобы избежать перегруженности
    private ExecutorService touchEventExecutor;
    private Random random = new Random();
    private Bitmap bmp;
    private SurfaceHolder holder;
    private GameManager gameLoopThread;
    /** Координата движения по Х=0*/
    private int x = 0;
    private int y = 0;
    /**Скорость изображения = 1*/
    private int xSpeed = 10;
    private int ySpeed = 10;
    private int overallSpeed = 5;
    private SoundPool sounds;
    private int soundId;

    private int number_of_bug; // количество жуков
    private int score;
    private long lastHitTime;
    private static final long SIMULTANEOUS_HIT_THRESHOLD = 100; // милисекунды
    private int soundIdwow; //при двойном убийстве жука, другой звук и дополнительная анимация
    private Bitmap animationBitmap;
    private boolean showAnimation = false;
    private long animationStartTime;

    public Panel(Context context, int numberOfBugs)
    {
        super(context);
        touchEventExecutor = Executors.newFixedThreadPool(6);
        initAnimation();
        number_of_bug = numberOfBugs;
        gameLoopThread = new GameManager(this);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback()
        {
            /** Уничтожение области рисования */
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                boolean retry = true;
                gameLoopThread.setRunning(false);
                while (retry) {
                    try {
                        gameLoopThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }

            /** Создание области рисования */
            public void surfaceCreated(SurfaceHolder holder)
            {
                createBugs();
                gameLoopThread.setRunning(true);
                gameLoopThread.start();
            }

            /** Изменение области рисования */
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
            }
        });
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ladybagsmal);
    }

    private void createBugs() {
        bugs.clear(); // Clear existing bugs if any
        Bitmap bugBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ladybagsmal);
        Bitmap splashImage = BitmapFactory.decodeResource(getResources(), R.drawable.blood); // Replace with your splash image
        Bitmap muhImage = BitmapFactory.decodeResource(getResources(), R.drawable.muhe);
        sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundId = sounds.load(getContext(), R.raw.carrotcrunch, 1);
        soundIdwow = sounds.load(getContext(), R.raw.wow, 1);
        for (int i = 0; i < number_of_bug; i++) {
            int startX = random.nextInt(getWidth() - bugBitmap.getWidth());
            int startY = random.nextInt(getHeight() - bugBitmap.getHeight());
            int xSpeed = overallSpeed; // Random speed between -10 and 10
            int ySpeed = overallSpeed;
            int choiceBug = random.nextInt(2);
            if(choiceBug == 1) {
                bugs.add(new Bug(bugBitmap, startX, startY, xSpeed, ySpeed, overallSpeed, splashImage));
            } else {
                bugs.add(new Bug(muhImage, startX, startY, xSpeed, ySpeed, overallSpeed, splashImage));
            }
        }
    }

    private void initAnimation() {
        animationBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wowimg);
    }
        private void startAnimation() {
        showAnimation = true;
        animationStartTime = System.currentTimeMillis();
        postInvalidate(); // перерисовка UI
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int color_green = ContextCompat.getColor(getContext(), R.color.green_medium_light);
        canvas.drawColor(color_green);

        for (Bug bug : bugs) {
            //рисую анимацию
            if (showAnimation && System.currentTimeMillis() - animationStartTime <= 1000) {
                int xPosition = getWidth() - animationBitmap.getWidth();
                int yPosition = getHeight() - animationBitmap.getHeight();
                canvas.drawBitmap(animationBitmap, xPosition, yPosition, null);
            } else {
                showAnimation = false;
            }

            bug.updatePosition(getWidth(), getHeight());

            bug.draw(canvas);
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();
// в новом потоке проверяем попадание по жуку и его состояние
            touchEventExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    processTouchEvent(touchX, touchY);
                }
            });
        }
        return true;
    }
    private void processTouchEvent(float touchX, float touchY) {
        boolean hitRegistered = false;
        for (Bug bug : bugs) {
            if (bug.isCollition(touchX, touchY)) {
                bug.hit();
                score++;
                hitRegistered = true;
                bug.visible = false;
                sounds.play(soundId, 1, 1, 0, 0, 1);

                //проверяю здесь было ли убито два жука одним нажатием
                if (System.currentTimeMillis() - lastHitTime <= SIMULTANEOUS_HIT_THRESHOLD) {
                    startAnimation();
                    score++; // Bonus point for simultaneous hit
                    sounds.play(soundIdwow, 1, 1, 0, 0, 1);

                } else {
                    sounds.play(soundId, 1, 1, 0, 0, 1);
                }
                lastHitTime = System.currentTimeMillis();
            }
        }
        if (!hitRegistered) {
            lastHitTime = 0;
        }
          postInvalidate(); // перерисовка UI
    }

    public int getScore() {
        return score;
    }
    public void stopGame() {
        if (gameLoopThread != null) {
            gameLoopThread.setRunning(false);
        }
        touchEventExecutor.shutdownNow(); // остановка других потоков
    }
}