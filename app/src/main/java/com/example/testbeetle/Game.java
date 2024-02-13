package com.example.testbeetle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;




public class Game extends Fragment {

    private int numBeetles;
        private Panel gamePanel;
    private int currentScore;
    private TextView countdownText;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private long roundEndTime;

    public static final long ROUND_TIME = 120000;

    public static Game newInstance(String playerNameIns, int numBeetles) {
        Game fragment = new Game();
        Bundle args = new Bundle();
        args.putString("PLAYER_NAME", playerNameIns);
        args.putInt("NUM_BEETLES", numBeetles);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        numBeetles = sharedPreferences.getInt("numBeetles", 5);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_game, container, false);
        FrameLayout panelContainer = view.findViewById(R.id.panelContainer);
        countdownText = view.findViewById(R.id.countdownText);
        // Create the Panel view and add it to the FrameLayout
        gamePanel = new Panel(getActivity(), numBeetles);
        panelContainer.addView(gamePanel);
        startRound();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
    }
    private void startRound() {
        roundEndTime = System.currentTimeMillis() + ROUND_TIME;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millisRemaining = roundEndTime - System.currentTimeMillis();
                if (millisRemaining > 0) {
                    countdownText.setText("Time: " + millisRemaining / 1000);
                    timerHandler.postDelayed(this, 1000);
                } else {
                    endRound();
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void endRound() {
        // Stop the game, calculate the score, and go back to the StartFragment
        // Retrieve the score from the Panel view
        int finalScore = gamePanel.getScore();
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        int highScore = sharedPreferences.getInt("highScore", 0);
        if (finalScore > highScore) { // если игрок тот же, проверяем, выше ли результат старого рекорда
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("highScore", finalScore);
            editor.apply();
        }

        getParentFragmentManager().beginTransaction().replace(R.id.fl_screen, new Start()).commit();

    }

    @Override
    public void onResume() {
        super.onResume();
        Intent musicIntent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(musicIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent musicIntent = new Intent(getActivity(), MusicService.class);
        getActivity().stopService(musicIntent);
    }
}