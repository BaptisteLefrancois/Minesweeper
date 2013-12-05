package uk.ac.brookes.lefrancois.minesweeper.logic;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.content.ContentUris;
import android.text.TextUtils;

/**
 * Created by Baptiste on 04/07/13.
 */
public class HighScoresContentProvider extends ContentProvider {

    private HighScoresDatabase scoresDb;
    private SQLiteDatabase sqlDB;

    /**
     * Define the name of the authority
     */
    public static final String AUTHORITY = "uk.ac.brookes.lefrancois.minesweeper.logic.HighScoresContentProvider";
    /**
     * Uri matcher value for the list of the high scores for normal mode
     */
    private static final int NORMAL_SCORES = 1;
    /**
     * Uri matcher value for an element of the high scores for normal mode
     */
    private static final int NORMAL_SCORE_ID = 2;
    /**
     * Uri matcher value for the list of the high scores for timed mode
     */
    private static final int TIMED_SCORES = 3;
    /**
     * Uri matcher value for an element of the high scores for normal mode
     */
    private static final int TIMED_SCORE_ID = 4;

    //define access to item or set of items
    private static final UriMatcher uriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "normalscores", NORMAL_SCORES);
        uriMatcher.addURI(AUTHORITY, "normalscores/#", NORMAL_SCORE_ID);
        uriMatcher.addURI(AUTHORITY, "timedscores", TIMED_SCORES);
        uriMatcher.addURI(AUTHORITY, "timedscores/#", TIMED_SCORE_ID);
    }

    /**
     * Define the full uri in order to access to the normal high scores table
     */
    public static final Uri CONTENT_URI_NORMAL_SCORES = Uri.parse("content://" + AUTHORITY
            + "/" + HighScoresDatabase.NORMAL_SCORES_TABLE);

    /**
     * Define the full uri in order to access to the timed high scores table
     */
    public static final Uri CONTENT_URI_TIMED_SCORES = Uri.parse("content://" + AUTHORITY
            + "/" + HighScoresDatabase.TIMED_SCORES_TABLE);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        scoresDb = new HighScoresDatabase(getContext());
        sqlDB = scoresDb.getWritableDatabase();
        return true;
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();


        int uriType = uriMatcher.match(uri);
        switch (uriType) {

            case NORMAL_SCORES:
                // no filter
                builder.setTables(HighScoresDatabase.NORMAL_SCORES_TABLE);
                break;

            case NORMAL_SCORE_ID:
                builder.setTables(HighScoresDatabase.NORMAL_SCORES_TABLE);
                builder.appendWhere(HighScoresDatabase.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;

            case TIMED_SCORES:
                // no filter
                builder.setTables(HighScoresDatabase.TIMED_SCORES_TABLE);
                break;

            case TIMED_SCORE_ID:
                builder.setTables(HighScoresDatabase.TIMED_SCORES_TABLE);
                builder.appendWhere(HighScoresDatabase.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = builder.query(scoresDb.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * @return Returns null, method useless in this application conception
     * */
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long rowID;
        Uri newuri;
        int uriType = uriMatcher.match(uri);

        if (uriType == NORMAL_SCORES) {
            rowID = sqlDB.insert(HighScoresDatabase.NORMAL_SCORES_TABLE, HighScoresDatabase.CONTACT_URI, contentValues);
            if (rowID != -1) {
                newuri = ContentUris.withAppendedId(CONTENT_URI_NORMAL_SCORES, rowID);
                getContext().getContentResolver().notifyChange(newuri, null);
                return newuri;
            }

        } else if (uriType == TIMED_SCORES) {

            rowID = sqlDB.insert(HighScoresDatabase.TIMED_SCORES_TABLE, HighScoresDatabase.CONTACT_URI, contentValues);
            if (rowID != -1) {
                newuri = ContentUris.withAppendedId(CONTENT_URI_TIMED_SCORES, rowID);
                getContext().getContentResolver().notifyChange(newuri, null);
                return newuri;
            } else
                throw new SQLException("Failed to insert row into " + uri);
        }

        throw new IllegalArgumentException("Unknown URI");
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int matchResult = uriMatcher.match(uri);
        String newWhere = makeNewWhere(selection, uri, matchResult);

        int count;
        switch (matchResult) {
            case NORMAL_SCORES:
            case NORMAL_SCORE_ID:
                count = sqlDB.delete(HighScoresDatabase.NORMAL_SCORES_TABLE, newWhere,
                        selectionArgs);

                break;
            case TIMED_SCORES:
            case TIMED_SCORE_ID:
                count = sqlDB.delete(HighScoresDatabase.TIMED_SCORES_TABLE, newWhere,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int matchResult = uriMatcher.match(uri);
        String newWhere = makeNewWhere(selection, uri, matchResult);

        int count;
        switch (matchResult) {
            case NORMAL_SCORES:
            case NORMAL_SCORE_ID:
                count = sqlDB.update(HighScoresDatabase.NORMAL_SCORES_TABLE, contentValues, newWhere,
                        selectionArgs);
                break;
            case TIMED_SCORES:
            case TIMED_SCORE_ID:
                count = sqlDB.delete(HighScoresDatabase.TIMED_SCORES_TABLE, newWhere,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    private String makeNewWhere(String where, Uri uri, int matchResult) {

        if (matchResult != NORMAL_SCORE_ID && matchResult != TIMED_SCORE_ID) {
            return where;
        } else {
            String newWhereSoFar = HighScoresDatabase.COLUMN_ID + "="
                    + uri.getPathSegments().get(1);
            if (TextUtils.isEmpty(where))
                return newWhereSoFar;
            else
                return newWhereSoFar + " AND (" + where + ')';
        }
    }
}
