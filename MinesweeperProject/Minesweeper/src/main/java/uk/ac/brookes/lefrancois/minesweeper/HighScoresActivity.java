package uk.ac.brookes.lefrancois.minesweeper;

import android.app.ActionBar;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import uk.ac.brookes.lefrancois.minesweeper.logic.HighScoresContentProvider;
import uk.ac.brookes.lefrancois.minesweeper.logic.HighScoresDatabase;
import uk.ac.brookes.lefrancois.minesweeper.model.Score;


/**
 * Created by Baptiste on 17/06/13.
 */
public class HighScoresActivity extends Activity {

    private TextView modeLabel;
    private LinearLayout highscoresNormalLayout;
    private LinearLayout highscoresTimedLayout;
    private Button normalButton;
    private Button timedButton;

    /** The list of loaded normal scores*/
    private List<Score> normalScores;
    /** The list of loaded timed scores*/
    private List<Score> timedScores;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        loadScores();

        modeLabel = (TextView) findViewById(R.id.highscores_mode);
        modeLabel.setText(R.string.normal);
        highscoresNormalLayout = (LinearLayout) findViewById(R.id.highscores_normal_layout);
        highscoresTimedLayout = (LinearLayout) findViewById(R.id.highscores_timed_layout);
        normalButton = (Button)findViewById(R.id.highscores_normal_button);
        timedButton = (Button)findViewById(R.id.highscores_timed_button);

        normalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                highscoresNormalLayout.setVisibility(View.VISIBLE);
                highscoresTimedLayout.setVisibility(View.GONE);
                modeLabel.setText(R.string.normal);
            }
        });

        timedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                highscoresNormalLayout.setVisibility(View.GONE);
                highscoresTimedLayout.setVisibility(View.VISIBLE);
                modeLabel.setText(R.string.timed);
            }
        });




        for (int i = 0; i < normalScores.size(); i++) {

            RelativeLayout relativeLayout = new RelativeLayout(this);
            LinearLayout.LayoutParams relativeLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            relativeLayout.setLayoutParams(relativeLayoutParams);

            TextView level = new TextView(this);
            RelativeLayout.LayoutParams levelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            levelParams.addRule(RelativeLayout.CENTER_VERTICAL);
            levelParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            level.setLayoutParams(levelParams);
            level.setGravity(Gravity.CENTER);
            level.setText(String.valueOf(i + 1));
            relativeLayout.addView(level);

            ImageView image = new ImageView(this);
            int size = convertDpToPx(45);
            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(size,size);
            imageParams.addRule(RelativeLayout.CENTER_VERTICAL);
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            imageParams.setMargins(convertDpToPx(30), 0, 0, 0);
            image.setLayoutParams(imageParams);
            relativeLayout.addView(image);

            TextView name = new TextView(this);
            RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            nameParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            name.setLayoutParams(nameParams);
            name.setGravity(Gravity.CENTER);
            relativeLayout.addView(name);

            TextView score = new TextView(this);
            RelativeLayout.LayoutParams scoreParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            scoreParams.addRule(RelativeLayout.CENTER_VERTICAL);
            scoreParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            score.setLayoutParams(scoreParams);
            score.setGravity(Gravity.CENTER);
            score.setText(String.valueOf(normalScores.get(i).value));
            relativeLayout.addView(score);


            if (normalScores.get(i).lookupKey != null) {

                Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, normalScores.get(i).lookupKey);
                Uri person = ContactsContract.Contacts.lookupContact(getContentResolver(), lookupUri);


                InputStream photoStream =
                        ContactsContract.Contacts.openContactPhotoInputStream
                                (getContentResolver(), person);
                if (photoStream != null) {
                    Bitmap photo = BitmapFactory.decodeStream(photoStream);

                    image.setImageBitmap(photo);

                    try {
                        photoStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {

                    image.setImageResource(R.drawable.avatar);

                }


                Cursor cur = getApplicationContext().getContentResolver()
                        .query(person,
                                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                                        ContactsContract.Contacts.LOOKUP_KEY}, null, null, null);

                if (cur.moveToFirst()) {
                    int nameIndex = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

                    String str = cur.getString(nameIndex);
                    if (str.length() == 0)
                        str = "Unknown";
                    name.setText(str);
                }
                cur.close();
            } else {
                name.setText(normalScores.get(i).name);
                image.setImageResource(R.drawable.avatar);
            }

            highscoresNormalLayout.addView(relativeLayout);

        }


        for (int i = 0; i < timedScores.size(); i++) {

            RelativeLayout relativeLayout = new RelativeLayout(this);
            LinearLayout.LayoutParams relativeLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            relativeLayout.setLayoutParams(relativeLayoutParams);

            TextView level = new TextView(this);
            RelativeLayout.LayoutParams levelParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            levelParams.addRule(RelativeLayout.CENTER_VERTICAL);
            levelParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            level.setLayoutParams(levelParams);
            level.setGravity(Gravity.CENTER);
            level.setText(String.valueOf(i + 1));
            relativeLayout.addView(level);

            ImageView image = new ImageView(this);
            int size = convertDpToPx(45);
            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(size,size);
            imageParams.addRule(RelativeLayout.CENTER_VERTICAL);
            imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            imageParams.setMargins(convertDpToPx(30), 0, 0, 0);
            image.setLayoutParams(imageParams);
            relativeLayout.addView(image);

            TextView name = new TextView(this);
            RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            nameParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            name.setLayoutParams(nameParams);
            name.setGravity(Gravity.CENTER);
            relativeLayout.addView(name);

            TextView score = new TextView(this);
            RelativeLayout.LayoutParams scoreParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            scoreParams.addRule(RelativeLayout.CENTER_VERTICAL);
            scoreParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            score.setLayoutParams(scoreParams);
            score.setGravity(Gravity.CENTER);
            int seconds = timedScores.get(i).value;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            score.setText(String.format("%d:%02d", minutes, seconds) + " min");
            relativeLayout.addView(score);


            if (timedScores.get(i).lookupKey != null) {

                Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, timedScores.get(i).lookupKey);
                Uri person = ContactsContract.Contacts.lookupContact(getContentResolver(), lookupUri);


                InputStream photoStream =
                        ContactsContract.Contacts.openContactPhotoInputStream
                                (getContentResolver(), person);
                if (photoStream != null) {
                    Bitmap photo = BitmapFactory.decodeStream(photoStream);

                    image.setImageBitmap(photo);

                    try {
                        photoStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {

                    image.setImageResource(R.drawable.avatar);

                }


                Cursor cur = getApplicationContext().getContentResolver()
                        .query(person,
                                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                                        ContactsContract.Contacts.LOOKUP_KEY}, null, null, null);

                if (cur.moveToFirst()) {
                    int nameIndex = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

                    String str = cur.getString(nameIndex);
                    if (str.length()==0)
                        str = "Unknown";
                    name.setText(str);
                }
                cur.close();
            } else {
                name.setText(timedScores.get(i).name);
                image.setImageResource(R.drawable.avatar);
            }

            highscoresTimedLayout.addView(relativeLayout);

        }


    }

    /**
     * Loads the normal high scores and the timed high scores from the content provider. A list of maximum 10 elements is expected per table.
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
     * Converts a dp value to a pixel value
     * @param value The dp value to convert
     * @return Returns the pixel value corresponding
     */
    private int convertDpToPx(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}