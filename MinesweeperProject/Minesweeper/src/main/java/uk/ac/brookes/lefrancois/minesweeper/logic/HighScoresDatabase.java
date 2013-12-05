package uk.ac.brookes.lefrancois.minesweeper.logic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Baptiste on 04/07/13.
 */
public class HighScoresDatabase extends SQLiteOpenHelper {

    /**
     * The table name of the highscores for normal mode game
     */
    public static final String NORMAL_SCORES_TABLE = "normalscores";
    /**
     * The table name of the highscores for timed mode game
     */
    public static final String TIMED_SCORES_TABLE = "timedscores";
    /**
     * The column relative to the primary key
     */
    public static final String COLUMN_ID = "id";
    /**
     * The column name relative to the contact Lookup key
     */
    public static final String CONTACT_URI = "uri";
    /**
     * the column name relative to the contact name
     */
    public static final String CONTACT_NAME = "name";
    /**
     * the column name relative to the score
     */
    public static final String SCORE_VALUE = "scorevalue";

    /**
     * the name of the application database
     */
    public static final String DATABASE_NAME = "minesweeper.db";
    /**
     * the current version of the database
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * The database sql creation statement for table storing the normal game mode scores
     */
    private static final String DATABASE_CREATE_NORMAL_SCORES =

            "create table "
                    + NORMAL_SCORES_TABLE + "(" + COLUMN_ID
                    + " integer primary key autoincrement, " + CONTACT_URI + " text, " + CONTACT_NAME + " text not null, "
                    + SCORE_VALUE + " integer not null)";

    /**
     * The database sql creation statement for table storing the timed game mode scores
     */
    private static final String DATABASE_CREATE_TIMED_SCORES =

            "create table "
                    + TIMED_SCORES_TABLE + "(" + COLUMN_ID
                    + " integer primary key autoincrement, " + CONTACT_URI + " text, " + CONTACT_NAME + " text not null, "
                    + SCORE_VALUE + " integer not null);";

    public HighScoresDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Execute the request {@link #DATABASE_CREATE_NORMAL_SCORES} and the request {@link #DATABASE_CREATE_TIMED_SCORES}
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE_NORMAL_SCORES);
        sqLiteDatabase.execSQL(DATABASE_CREATE_TIMED_SCORES);
    }

    /**
     * Upgrade database
     *
     * @param sqLiteDatabase The database source for the upgrade
     * @param oldVersion     The old version of the database
     * @param newVersion     The new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(HighScoresDatabase.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(sqLiteDatabase);
    }
}
