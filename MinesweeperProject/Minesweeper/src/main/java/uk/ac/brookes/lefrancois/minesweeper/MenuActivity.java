package uk.ac.brookes.lefrancois.minesweeper;

import uk.ac.brookes.lefrancois.minesweeper.logic.HighScoresDatabase;
import uk.ac.brookes.lefrancois.minesweeper.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


public class MenuActivity extends Activity {

    private Button playButton;
    private Button highScoresButton;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        playButton = (Button)findViewById(R.id.play_button);
        highScoresButton = (Button)findViewById(R.id.highscores_button);
        settingsButton = (Button)findViewById(R.id.settings_button);

        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity( new Intent(MenuActivity.this, PartyActivity.class));
            }
        });

        highScoresButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, HighScoresActivity.class));
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               startActivity(new Intent(MenuActivity.this, SettingsActivity.class));

            }
        });


    }


}
