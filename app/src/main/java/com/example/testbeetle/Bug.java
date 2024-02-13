package com.example.testbeetle;

import static android.opengl.ETC1.getHeight;
import static android.opengl.ETC1.getWidth;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

public class Bug {
    // Поля для отладки нажатия по жуку
    public boolean visible;

    private long lastHitTime;
    private Bitmap splashImage;
    private static final long INVISIBLE_TIME = 2000; // 5 seconds
    private static final long SPLASH_TIME = 200; // 1 seconds

    ///////////////////////////////////////////////////////////////////
    float x, y; // Current position
    int xSpeed, ySpeed, overallSpeed; // Speed in x and y direction
    Bitmap bitmap; // Божья коровка



    int screenWidthS, screenHeightS;

    public Bug(Bitmap bitmap, int startX, int startY, int xSpeed, int ySpeed, int overallSpeed, Bitmap splashImage) {
        this.bitmap = bitmap;
        this.x = startX;
        this.y = startY;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.visible = true;
        this.splashImage = splashImage;
    }
    public void updatePosition(int screenWidth, int screenHeight) {
        screenWidthS = screenWidth;
        screenHeightS = screenHeight;
        long currentTime = System.currentTimeMillis();
        if (!visible && currentTime - lastHitTime > INVISIBLE_TIME) {
            xSpeed = xSpeed * -1;
            ySpeed = ySpeed * -1;
            visible = true; // Make the beetle visible again after 5 seconds
        }

        if (visible || currentTime - lastHitTime > SPLASH_TIME) {
            if (x + bitmap.getWidth() >= screenWidth && xSpeed > 0) {
                xSpeed = -xSpeed; // reverse direction with the same speed
            }
            if (x <= 0 && xSpeed < 0) {

                xSpeed = xSpeed * -1; // reverse direction with the same speed
            }

            // Check for vertical bounds and reverse direction if necessary
            if (y + bitmap.getHeight() >= screenHeight && ySpeed > 0) {
                ySpeed = -ySpeed; // reverse direction with the same speed
            }
            if (y <= 0 && ySpeed < 0) {
                ySpeed = ySpeed * -1; // reverse direction with the same speed
            }
            x = x + xSpeed;
            y = y + ySpeed;
        }
    }

    public boolean isCollition(float x2, float y2) {



        return x2 > x && x2 < x + bitmap.getWidth() && y2 > y && y2 < y + bitmap.getHeight();
    }

    public synchronized void hit() {
        if (visible) {
            visible = false;
            lastHitTime = System.currentTimeMillis();

        }
    }
    public void draw(Canvas canvas) {
        if (!visible && System.currentTimeMillis() - lastHitTime <= SPLASH_TIME) {
            canvas.drawBitmap(splashImage, x, y, null);
        } else if (visible) {
            canvas.drawBitmap(bitmap, x, y, null);

        }
    }
}
