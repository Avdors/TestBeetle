package com.example.testbeetle;

import static com.example.testbeetle.Game.ROUND_TIME;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.viewmodel.CreationExtras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Start extends Fragment implements CustomPicker.PickerListener {
    private EditText editTextName;
    private TextView editTextNumBeetles;
    private TextView infoRound;
    private int highScore;

     private TextView score;

    private String lastName; // сохраняем сюда имя при открытии, чтобы узнать если имя было изменено то записать рекорд не сравнивая с предыдущим

      public Start() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        infoRound = view.findViewById(R.id.textViewInfo4);
        String infoText = infoRound.getText() + " " + String.valueOf(ROUND_TIME/1000) + " " + getString(R.string.second);
        infoRound.setText(infoText);
        editTextName = view.findViewById(R.id.editTextName);
        editTextNumBeetles = view.findViewById(R.id.editTextNumBeetles);
        score = view.findViewById(R.id.textViewScore);
        ImageButton buttonStartGame = view.findViewById(R.id.buttonStartGame);

        buttonStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

        editTextName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    onNameEditFinished();
                }
            }
        });
        editTextNumBeetles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomPicker PickerFragment = new CustomPicker();
                PickerFragment.setPickerListener(Start.this);
                PickerFragment.show(getFragmentManager(), "Picker");
            }
        });

        loadGameData();
        return view;
    }
    private void onNameEditFinished() {
        String currentName = editTextName.getText().toString();
        if (!currentName.equals(lastName)) {
            // если имя изменено сбрасываю счет и фиксирую новое имя
            highScore = 0;
            score.setText(String.valueOf(0));
            lastName = currentName;
        }
    }

    private void startGame() {
        String playerName = editTextName.getText().toString();
        int numBeetles;
        try {
            numBeetles = Integer.parseInt(editTextNumBeetles.getText().toString());
        } catch (NumberFormatException e) {
            numBeetles = 5; // Default value
        }


        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).startGame(playerName, numBeetles);
        }
        if(playerName != lastName){
            saveGameData(playerName, 0, numBeetles);
        }else saveGameData(playerName, highScore, numBeetles);

    }

    private void saveGameData(String playerName, int highScore, int numBeetles) {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("playerName", playerName);
        editor.putInt("highScore", highScore);
        editor.putInt("numBeetles", numBeetles);

        editor.apply();


    }

    private void loadGameData() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String playerName = sharedPreferences.getString("playerName", "");
        highScore = sharedPreferences.getInt("highScore", 0);
        int numBeetles = sharedPreferences.getInt("numBeetles", 5);
        if(highScore != 0) {


            String scoreText = getString(R.string.score) + " " + String.valueOf(highScore);
            score.setText(scoreText);
        }
        editTextName.setText(String.valueOf(playerName));
        editTextNumBeetles.setText(String.valueOf(numBeetles));
        lastName = playerName;

    }


    @Override
    public void numberBugSet(int numberBug) {
        editTextNumBeetles.setText(String.valueOf(numberBug));
    }

    @NonNull
    @Override
    public CreationExtras getDefaultViewModelCreationExtras() {
        return super.getDefaultViewModelCreationExtras();
    }
}