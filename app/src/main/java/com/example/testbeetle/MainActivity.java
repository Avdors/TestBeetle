package com.example.testbeetle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    float p=0;
    private SoundPool sounds;
    private int sExplosion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(new Panel(this));

        setContentView(R.layout.activity_main);

        // Initially load the StartFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_screen, new Start())
                    .commit();
        }

    }


    public void startGame(String playerName, int numBeetles) {
        Game gameFragment = Game.newInstance(playerName, numBeetles);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_screen, gameFragment)
                .addToBackStack(null)
                .commit();
    }
}