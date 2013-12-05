package uk.ac.brookes.lefrancois.minesweeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import uk.ac.brookes.lefrancois.minesweeper.logic.HighScoresContentProvider;
import uk.ac.brookes.lefrancois.minesweeper.logic.HighScoresDatabase;
import uk.ac.brookes.lefrancois.minesweeper.logic.RulesManager;
import uk.ac.brookes.lefrancois.minesweeper.logic.SoundManager;
import uk.ac.brookes.lefrancois.minesweeper.model.MineFieldAdapter;
import uk.ac.brookes.lefrancois.minesweeper.model.Score;
import uk.ac.brookes.lefrancois.minesweeper.model.SettingsParam;

/**
 * Created by Baptiste on 17/06/13.
 */
public class PartyActivity extends Activity {

    /** Default interval beetween two beep */
    private static final int BEEP_INTERVAL = 30;
    /** Request code for settings activity result */
    private static final int GAME_SETTINGS_RESULT = 1001;

    private GridView minefieldGrid;
    private MineFieldAdapter minefieldAdapter;
    private ImageButton flagButton;
    private ImageButton restartButton;
    private ImageButton settingsButton;
    private TextView remainingFlagTextView;
    private TextView timerTextView;
    private Button resumeButton;
    private RelativeLayout resumeLayout;
    private LinearLayout endGameLayout;
    private TextView endGameMessage;
    private TextView endGameScore;


    /** True if the player is using the flag tool */
    private boolean isFlagMode;
    /** Equals 0 if no dialog is displayed, 1 for quit confirmation, 2 for restart confirmation */
    private int currentDialog;
    /** True if timed game mode */
    private boolean isTimeMode;
    private boolean mute;
    private boolean isBeepActive;
    /** True if the player can flag by a long press gesture*/
    private boolean isAutoFlagActive;
    /** The extended minefield */
    private int[] minefield = new int[0];
    /** Translate a grid position to its real minefield position */
    private int[] transpose = new int[0];
    private int border;
    private int extendedBorder;
    /** The current number of covered square */
    private int numCoveredSquares;
    /** The number of mines in the minefield */
    private int numMines;
    /** Counter of the logical remaining flag to place */
    private int remainingFlag;
    private Timer timer;
    /** Is true when the player already press a square once */
    private boolean isStarted;
    /** Is true if game over or player win */
    private boolean isFinished;
    /** Elements relative to the game time */
    private final TimerElements timerElements = new TimerElements(0, 0, 0);
    /** Counter of num of move since the start of the game */
    private int numMoves;
    /** True if the player won */
    private boolean win;

    private String playerName;
    private String playerLookupKey;

    /** List of normal high scores. 10 elements max expected */
    private List<Score> normalScores;
    /** List of timed high scores. 10 elements max expected */
    private List<Score> timedScores;

    /** Singleton for playing sound*/
    private SoundManager soundManager;

    /**
     * Init the graphic interface
     * Loads settings from the preferenceManager
     * Load scores from the content provider
     * Init the SoundManager
     *
     * If it is a new activity, a new game is launched
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party);

        initUI();

        loadSettings();

        loadScores();

        soundManager = SoundManager.getInstance();
        soundManager.Load(this);
        setSoundEffectsEnabled(!mute);

        if (savedInstanceState == null)
            initNewGame();


    }

    /**
     * Quit game if confirmation accepted.
     */
    @Override
    public void onBackPressed() {
        pauseTimer();
        if (isFinished || !isStarted) {
            tryRemoveTimer();
            finish();
        } else {
            //if the game is already started a confirmation is asked before quit action.
            showQuitDialog();
        }
    }

    /**
     * Pause the timer in order to have a correct final score
     */
    @Override
    protected void onPause() {
        super.onPause();

        pauseTimer();
    }

    /**
     * A resume button is displayed if in timer game mode.
     * Resume button is not needed if a dialog box is displayed.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (isTimeMode && isStarted && !isFinished && currentDialog == 0) {
            resumeLayout.setVisibility(View.VISIBLE);
            minefieldGrid.setEnabled(false);
        } else
            resumeTimer();

    }

    /**
     * Ensures no timer task will run after quitting the game
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        tryRemoveTimer();
    }

    /**
     * Save the state of the game and its critical parameters in order to be able to continue the game later.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        pauseTimer();

        outState.putBoolean("isFlagMode", isFlagMode);
        outState.putInt("currentDialog", currentDialog);
        outState.putIntArray("minefield", minefield);
        outState.putIntArray("transpose", transpose);
        outState.putInt("border", border);
        outState.putInt("numCoveredSquares", numCoveredSquares);
        outState.putInt("numMines", numMines);
        outState.putInt("remainingFlag", remainingFlag);
        outState.putBoolean("isStarted", isStarted);
        outState.putBoolean("isFinished", isFinished);
        outState.putLong("duration", timerElements.duration);
        outState.putInt("elapsedSeconds", timerElements.elapsedSeconds);
        outState.putInt("numMoves", numMoves);
        outState.putBoolean("win", win);

    }

    /**
     * Restore the critical games values mandatory to continue the game
     * @param savedInstanceState The bundle containing the games values saved in emergency.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isFlagMode = savedInstanceState.getBoolean("isFlagMode");
        currentDialog = savedInstanceState.getInt("currentDialog");
        minefield = savedInstanceState.getIntArray("minefield");
        transpose = savedInstanceState.getIntArray("transpose");
        border = savedInstanceState.getInt("border");
        extendedBorder = border + 2;
        numCoveredSquares = savedInstanceState.getInt("numCoveredSquares");
        numMines = savedInstanceState.getInt("numMines");
        remainingFlag = savedInstanceState.getInt("remainingFlag");
        isStarted = savedInstanceState.getBoolean("isStarted");
        isFinished = savedInstanceState.getBoolean("isFinished");
        timerElements.duration = savedInstanceState.getLong("duration");
        timerElements.elapsedSeconds = savedInstanceState.getInt("elapsedSeconds");
        numMoves = savedInstanceState.getInt("numMoves");
        win = savedInstanceState.getBoolean("win");

        refreshGlobalDisplay();

        if (currentDialog == 1) {
            showQuitDialog();
        } else if (currentDialog == 2) {
            showRestartDialog();
        }

    }

    /**
     * Listening the result of the settings activity if this one is launch during the game.
     * The new settings will be applied to the current game except the border size preference.
     * @param requestCode The activity requestCode sent. Only {@link #GAME_SETTINGS_RESULT} is managed.
     * @param resultCode Permits to know if the action was done properly
     * @param data Contains the game settings data changes by the Settings Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GAME_SETTINGS_RESULT) {

            if (resultCode == RESULT_OK) {
                Bundle b = data.getExtras();
                SettingsParam settings = b.getParcelable(SettingsActivity.EXTRA);

                playerName = settings.name;
                playerLookupKey = settings.lookupkey;
                isTimeMode = settings.timed;
                setSoundEffectsEnabled(settings.sound);
                isBeepActive = settings.beep;
                isAutoFlagActive = settings.autoflag;
                refreshScoresDisplay();

            }
        }
    }

    /**
     * Execute all the game logic after a square is pressed
     * @param position the position of the square in the grid view
     */
    private void actionPressMinefield(int position) {

        if (!isFinished) {

            if (!isStarted) {
                isStarted = true;
                startTimer();
            }

            int index = transpose[position];
            int value = minefield[index];
            boolean IsCoverNoFlag = value == 10 || value == 60;

            if (value > 8) {

                //Flag or unflag depending the previous state
                if (isFlagMode) {

                    soundManager.play(SoundManager.FLAG_SOUND);

                    if (IsCoverNoFlag) {
                        minefield[index]--;
                        remainingFlag--;
                        numMoves++;
                        if (!isTimeMode)
                            timerTextView.setText(String.valueOf(numMoves));
                    } else {
                        minefield[index]++;
                        remainingFlag++;
                    }

                    remainingFlagTextView.setText(String.valueOf(remainingFlag));
                    minefieldAdapter.notifyDataSetChanged();
                }


                //attempt to sweep only if it is a covered square with no flag on it on sweep mode.
                // simple covered => 10, with hidden mine => 60
                else if (IsCoverNoFlag) {

                    numMoves++;

                    if (!isTimeMode)
                        timerTextView.setText(String.valueOf(numMoves));

                    numCoveredSquares = RulesManager.uncoverSquare(minefield, index, extendedBorder, numCoveredSquares);

                    if (numCoveredSquares == -1) {
                        for (int j = 0; j < minefield.length; j++)
                            if (minefield[j] > 50)
                                minefield[j] = 50;
                        minefieldAdapter.notifyDataSetChanged();
                        endGame(false);
                    } else {
                        minefieldAdapter.notifyDataSetChanged();
                        if (numCoveredSquares <= numMines)
                            endGame(true);

                    }

                }

            }
        }
    }

    /**
     * Adds flag to the grid position if empty square.
     * Removes flag if the square is already flagged.
     * @param position the position of the square in the grid view
     */
    private void actionLongPressMinefield(int position) {

        soundManager.play(SoundManager.FLAG_SOUND);
        int index = transpose[position];
        int value = minefield[index];
        boolean IsCoverNoFlag = value == 10 || value == 60;
        if (value > 8) {
            //Flag or unflag depending the previous state
            if (IsCoverNoFlag) {
                minefield[index]--;
                remainingFlag--;
                numMoves++;
                if (!isTimeMode)
                    timerTextView.setText(String.valueOf(numMoves));
            } else {
                minefield[index]++;
                remainingFlag++;
            }
            remainingFlagTextView.setText(String.valueOf(remainingFlag));
            minefieldAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Refresh the score label. Set time text if time mode otherwise set the number game moves.
     */
    private void refreshScoresDisplay() {
        if (isTimeMode) {
            long millis = timerElements.duration;
            int seconds = (int) (millis * 0.001f);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));
        } else
            timerTextView.setText(String.valueOf(numMoves));
    }

    /** Permits to define the proper image to the flag tool button */
    private void refreshFlagButtonDisplay() {
        if (isFlagMode)
            flagButton.setImageResource(R.drawable.flag);
        else
            flagButton.setImageResource(R.drawable.shovel);
    }

    /**
     * Permits to restore the display after a restore game state process
     */
    private void refreshGlobalDisplay() {

        if (isFinished) {
            if (win) {
                soundManager.play(SoundManager.FANFARE_SOUND);
                isFinished = true;
                endGameMessage.setText(R.string.youwin_message);
                if (isTimeMode) {

                    long millis = timerElements.duration;
                    int seconds = (int) (millis * 0.001f);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;

                    endGameScore.setText(String.format("%d:%02d", minutes, seconds) + " min");
                } else {
                    endGameScore.setText(String.valueOf(numMoves) + " moves");
                }
                endGameScore.setVisibility(View.VISIBLE);


            } else {
                soundManager.play(SoundManager.EXPLOSION_SOUND);
                isFinished = true;
                endGameMessage.setText(R.string.gameover_message);
                endGameScore.setVisibility(View.GONE);
            }

            endGameLayout.setVisibility(View.VISIBLE);
        }

        refreshScoresDisplay();
        refreshFlagButtonDisplay();
        remainingFlagTextView.setText(String.valueOf(remainingFlag));
        minefieldGrid.setNumColumns(border);
        minefieldAdapter.resetResources(minefield, transpose);
        minefieldAdapter.notifyDataSetChanged();
    }

    /**
     * Display quit confirmation dialog. Pause the timer if needed.
     */
    private void showQuitDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.quit_title)
                .setMessage(R.string.quit_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tryRemoveTimer();
                        currentDialog = 0;
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        currentDialog = 0;
                        resumeTimer();
                    }
                })
                .show();
        currentDialog = 1;
    }


    /**
     * Display restart confirmation dialog. Pause the timer if needed.
     */
    private void showRestartDialog() {
        new AlertDialog.Builder(PartyActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.restart_title)
                .setMessage(R.string.restart_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        currentDialog = 0;
                        playAgain();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        currentDialog = 0;
                        resumeTimer();
                    }
                })
                .show();
        currentDialog = 2;
    }

    /** Manages end of game and the score saving particularly
     *
     * @param successful If true the score will be compared to the high scores list and added if needed
     */
    private void endGame(boolean successful) {

        pauseTimer();
        win = successful;

        if (successful) {
            isFinished = true;
            endGameMessage.setText(R.string.youwin_message);
            if (isTimeMode) {

                long millis = timerElements.duration;
                int seconds = (int) (millis * 0.001f);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                endGameScore.setText(String.format("%d:%02d", minutes, seconds) + " min");
            } else {
                endGameScore.setText(String.valueOf(numMoves) + " moves");
            }
            endGameScore.setVisibility(View.VISIBLE);

            if (submitScore())
                soundManager.play(SoundManager.FANFARE_SOUND);
            else
                soundManager.play(SoundManager.WIN_SOUND);


        } else {
            soundManager.play(SoundManager.EXPLOSION_SOUND);
            isFinished = true;
            endGameMessage.setText(R.string.gameover_message);
            endGameScore.setVisibility(View.GONE);
        }

        endGameLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Permits to restart a game after killing timer task process.
     */
    private void playAgain() {

        tryRemoveTimer();
        initNewGame();
    }


    /**
     * Initialize a new game by resetting the games parameters to their default values.
     * Read the border size preference (due to the postponed strategy) before creating the minefield.
     */
    private void initNewGame() {

        //TODO load settings
        int borderSize = PreferenceManager.getDefaultSharedPreferences(this).getInt("border", 12);
        initMinefieldGame(borderSize);

        timerElements.duration = 0;
        timerElements.base = 0;
        timerElements.elapsedSeconds = 0;

        isStarted = false;
        isFinished = false;
        win = false;

        //Permits to display the number of mine expected.
        remainingFlag = numMines;
        numCoveredSquares = transpose.length;
        numMoves = 0;
        isFlagMode = false;
        currentDialog = 0;

        refreshGlobalDisplay();

        endGameLayout.setVisibility(View.GONE);
        resumeLayout.setVisibility(View.GONE);
        minefieldGrid.setEnabled(true);
    }

    /**
     * Create a new minefield with mines. Affect the fields {@link #minefield}, {@link #transpose}, {@link #border} and {@link #extendedBorder}
     * @param borderSize the border size wanted for the new minefield
     */
    private void initMinefieldGame(int borderSize) {

        border = borderSize;
        //the minefield is extended (6x6 becomes 8x8)
        extendedBorder = borderSize + 2;
        int size = extendedBorder * extendedBorder;
        int limit = size - extendedBorder;

        minefield = new int[size];

        //Convert the position of the GridView squares to their positions in the extended minefield.
        transpose = new int[borderSize * borderSize];

        //the good square are covered. The adjacent mine indexes are calculated during the uncover process
        //Only the strict final minefield is initialized with cover squares.
        //The extended minefield permits to stop the search algorithm without testing the border position (powerful !)
        /*
            0   0   0   0   0   0
            0   10  10  10  10  0
            0   10  10  10  10  0
            0   10  10  10  10  0
            0   10  10  10  10  0
            0   0   0   0   0   0
         */
        int count = borderSize + 3;
        int index = 0;
        for (int i = 0; i < count; i++) {
            minefield[i] = 0;
        }
        while (count < limit) {
            for (int i = 0; i < borderSize; i++) {
                minefield[count] = 10;
                transpose[index] = count;
                count++;
                index++;
            }
            minefield[count++] = 0;
            minefield[count++] = 0;
        }
        for (; count < size; count++)
            minefield[count] = 0;


        //Define the mine density (comparable to original game density)
        if (borderSize < 7)
            numMines = 5;
        else if (borderSize < 13)
            numMines = 25;
        else if (borderSize < 21)
            numMines = 70;

        //add mines at random position (if not already mined).
        Random rand = new Random();
        int randPosition;
        int maxRange = size - 2 * borderSize - 6;
        for (int i = 0; i < numMines; i++) {
            do {
                randPosition = rand.nextInt(maxRange) + borderSize + 3;
            } while (minefield[randPosition] != 10);
            minefield[randPosition] += 50;
        }

    }

    /**
     * Init the graphical user interface and notably the OnclickListeners actions.
     */
    private void initUI() {

        //Give an indication concerning the number of mine.
        //Negative values for flag counter is accepted like the original game if you flag to many squares
        remainingFlagTextView = (TextView) findViewById(R.id.remaining_flag_textview);
        timerTextView = (TextView) findViewById(R.id.timer_textview);
        minefieldGrid = (GridView) findViewById(R.id.minefield);
        flagButton = (ImageButton) findViewById(R.id.flag_button);
        restartButton = (ImageButton) findViewById(R.id.restart_button);
        settingsButton = (ImageButton) findViewById(R.id.settings_button);
        resumeLayout = (RelativeLayout) findViewById(R.id.resumeLayout);
        resumeButton = (Button) findViewById(R.id.resumeButton);
        endGameLayout = (LinearLayout) findViewById(R.id.end_game_layout);
        endGameMessage = (TextView) findViewById(R.id.end_game_message);
        endGameScore = (TextView) findViewById(R.id.end_game_score);

        minefieldAdapter = new MineFieldAdapter(this, minefield, transpose);
        minefieldGrid.setAdapter(minefieldAdapter);

        minefieldGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                actionPressMinefield(i);
            }
        });

        //Flagging is permitted by a long press gesture
        minefieldGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isAutoFlagActive)
                    return false;

                actionLongPressMinefield(i);
                return true;
            }
        });

        flagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFlagMode = !isFlagMode;
                refreshFlagButtonDisplay();
            }
        });

        restartButton.setImageResource(R.drawable.restart);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTimer();
                if (isFinished || !isStarted) {
                    playAgain();
                } else {
                    //if the game is already started a confirmation is asked before restart action.
                    showRestartDialog();
                }
            }
        });

        settingsButton.setImageResource(R.drawable.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PartyActivity.this, SettingsActivity.class);
                startActivityForResult(intent, GAME_SETTINGS_RESULT);
            }
        });


        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resumeLayout.setVisibility(View.GONE);
                minefieldGrid.setEnabled(true);
                resumeTimer();
            }
        });

    }

    /**
     * Load settings from the PreferenceManager except the border size values in order to not destroy the current game.
     */
    private void loadSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        playerName = preferences.getString(SettingsActivity.PLAYER_NAME, "Unknown");
        playerLookupKey = preferences.getString(SettingsActivity.PLAYER_LOOKUPKEY, null);
        isTimeMode = preferences.getBoolean(SettingsActivity.TIMED_MODE_ACTIVE, false);
        mute = !preferences.getBoolean(SettingsActivity.SOUND_ACTIVE, true);
        isBeepActive = preferences.getBoolean(SettingsActivity.BEEP_ACTIVE, true);
        isAutoFlagActive = preferences.getBoolean(SettingsActivity.AUTO_FLAG_ACTIVE, true);


    }

    /**
     * Loads the scores lists {@link #normalScores} and {@link #timedScores} from the HighScoresContentProvider.
     */
    private void loadScores() {

        Cursor normalScoresCursor = getContentResolver().query(HighScoresContentProvider.CONTENT_URI_NORMAL_SCORES, null, null, null, HighScoresDatabase.SCORE_VALUE + " ASC ");
        normalScores = new ArrayList<Score>();

        for (normalScoresCursor.moveToFirst(); !normalScoresCursor.isAfterLast(); normalScoresCursor.moveToNext()) {
            Score score = new Score();
            score.id = Integer.parseInt(normalScoresCursor.getString(normalScoresCursor.getColumnIndex(HighScoresDatabase.COLUMN_ID)));
            score.lookupKey = normalScoresCursor.getString(normalScoresCursor.getColumnIndex(HighScoresDatabase.CONTACT_URI));
            score.name = normalScoresCursor.getString(normalScoresCursor.getColumnIndex(HighScoresDatabase.CONTACT_NAME));
            score.value = Integer.parseInt(normalScoresCursor.getString(normalScoresCursor.getColumnIndex(HighScoresDatabase.SCORE_VALUE)));

            normalScores.add(score);
        }

        Cursor timedScoresCursor = getContentResolver().query(HighScoresContentProvider.CONTENT_URI_TIMED_SCORES, null, null, null, HighScoresDatabase.SCORE_VALUE + " ASC ");
        timedScores = new ArrayList<Score>();

        for (timedScoresCursor.moveToFirst(); !timedScoresCursor.isAfterLast(); timedScoresCursor.moveToNext()) {
            Score score = new Score();
            score.id = Integer.parseInt(timedScoresCursor.getString(timedScoresCursor.getColumnIndex(HighScoresDatabase.COLUMN_ID)));
            score.lookupKey = timedScoresCursor.getString(timedScoresCursor.getColumnIndex(HighScoresDatabase.CONTACT_URI));
            score.name = timedScoresCursor.getString(timedScoresCursor.getColumnIndex(HighScoresDatabase.CONTACT_NAME));
            score.value = Integer.parseInt(timedScoresCursor.getString(timedScoresCursor.getColumnIndex(HighScoresDatabase.SCORE_VALUE)));

            timedScores.add(score);
        }


    }

    /**
     * Check if the score is eligible to be added to the appropriate HighScores table.
     * If OK the score is saved in the database thanks the contentProvider
     * @return Return true if the score is in the top 10 of the appropriate high scores list.
     */
    private boolean submitScore() {


        Uri contentUri;
        int actualScore;
        List<Score> scores;

        if (isTimeMode) {
            actualScore = (int) (timerElements.duration * 0.001f);
            contentUri = HighScoresContentProvider.CONTENT_URI_TIMED_SCORES;
            scores = timedScores;
        } else {
            actualScore = numMoves;
            contentUri = HighScoresContentProvider.CONTENT_URI_NORMAL_SCORES;
            scores = normalScores;
        }

        ContentResolver contentResolver = getContentResolver();

        if (scores.size() >= 10) {
            if (actualScore < scores.get(9).value) {
                String[] args = {String.valueOf(scores.get(9).id)};
                contentResolver.delete(contentUri, HighScoresDatabase.COLUMN_ID + "=?", args);

            } else
                return false;
        }

        ContentValues values = new ContentValues();
        values.put(HighScoresDatabase.CONTACT_NAME, playerName);
        values.put(HighScoresDatabase.CONTACT_URI, playerLookupKey);
        values.put(HighScoresDatabase.SCORE_VALUE, actualScore);
        getContentResolver().insert(contentUri, values);
        loadScores();

        return true;

    }

    /**
     * Permits to enable or disable all sounds in the game .
     * @param soundEffectsEnabled Should be false if the game need to be muted.
     */
    private void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        mute = !soundEffectsEnabled;
        soundManager.mute = !soundEffectsEnabled;
        minefieldGrid.setSoundEffectsEnabled(soundEffectsEnabled);
        restartButton.setSoundEffectsEnabled(soundEffectsEnabled);
        flagButton.setSoundEffectsEnabled(soundEffectsEnabled);
        settingsButton.setSoundEffectsEnabled(soundEffectsEnabled);
        resumeButton.setSoundEffectsEnabled(soundEffectsEnabled);
    }


    ///------------------------- Timer section --------------------------


    /** Start a new timer process with the Timertask {@link BeepAndTimerTextTask} */
    private void startTimer() {

        tryRemoveTimer();
        timer = new Timer();
        timerElements.base = SystemClock.elapsedRealtime();
        timer.scheduleAtFixedRate(new BeepAndTimerTextTask(), 0, 1000);
    }

    /** Manage pause mechanism when the app is interrupted */
    private void pauseTimer() {

        if (!isStarted || isFinished || timer == null)
            return;

        timerElements.duration = SystemClock.elapsedRealtime() - timerElements.base;
        timer.cancel();
        timer.purge();
        timer = null;
    }

    /** Manage resume mechanism when the app is resumed */
    private void resumeTimer() {

        tryRemoveTimer();
        if (!isStarted || isFinished)
            return;
        /*
        The next algorithm permits to resolve a paradox, for example if the timer is stop at 5.380 sec
        we have to manage a delay of 620ms before the execution of the next timerTask to have the exact
        time and to avoid to "accelerate" the timer by many pause mechanism.
         */

        //the base is recalculated after a pause
        timerElements.base = SystemClock.elapsedRealtime() - timerElements.duration;
        long delay = timerElements.duration % 1000;

        if (delay < 1000) {
            delay = 1000 - delay;
            timer = new Timer();
            timer.scheduleAtFixedRate(new BeepAndTimerTextTask(), delay, 1000);
        } else
            timer.scheduleAtFixedRate(new BeepAndTimerTextTask(), 0, 1000);

    }

    /** Release useless resources (once canceled a timer cannot schedule tasks anymore). */
    private void tryRemoveTimer() {

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * It is the task launched by the timer :
     *  - every second refresh the display of the timer
     *  - every 30 second,  beep sound request is sent to the SoundManager.
     */
    private class BeepAndTimerTextTask extends TimerTask {

        @Override
        public void run() {

            //Ensures access to the UI before modification
            PartyActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    timerElements.duration = SystemClock.elapsedRealtime() - timerElements.base;

                    if (isTimeMode) {

                        long millis = timerElements.duration;

                        //divide by 1000 thanks to multiplication operation (performance)
                        int seconds = (int) (millis * 0.001f);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;

                        timerTextView.setText(String.format("%d:%02d", minutes, seconds));

                        //Beep every 30 seconds, specific counter is more efficient than a modulo operation
                        if (timerElements.elapsedSeconds >= BEEP_INTERVAL) {
                            timerElements.elapsedSeconds = 0;
                            if (isBeepActive)
                                soundManager.play(SoundManager.BEEP_SOUND);
                        }
                        timerElements.elapsedSeconds++;
                    }
                }
            });
        }
    }

    /** This class is useful to store the fields relative to the actual time in the game */
    private class TimerElements {

        public long duration;
        public long base;
        public int elapsedSeconds;

        public TimerElements(long startTime, long base, int elapsedSeconds) {
            this.base = base;
            this.duration = startTime;
            this.elapsedSeconds = elapsedSeconds;
        }
    }
}