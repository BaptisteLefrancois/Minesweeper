package uk.ac.brookes.lefrancois.minesweeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import uk.ac.brookes.lefrancois.minesweeper.model.SettingsParam;

public class SettingsActivity extends Activity {


    /** The string reference for this field in the the PreferenceManager */
    public static final String PLAYER_NAME = "name";
    /** The string reference for this field in the the PreferenceManager */
    public static final String PLAYER_LOOKUPKEY = "lookupkey";
    /** The string reference for this field in the the PreferenceManager */
    public static final String BORDER_SIZE = "border";
    /** The string reference for this field in the the PreferenceManager */
    public static final String TIMED_MODE_ACTIVE = "timed";
    /** The string reference for this field in the the PreferenceManager */
    public static final String SOUND_ACTIVE = "soud";
    /** The string reference for this field in the the PreferenceManager */
    public static final String BEEP_ACTIVE = "beep";
    /** The string reference for this field in the the PreferenceManager */
    public static final String AUTO_FLAG_ACTIVE = "autofalg";
    /** The string reference for the parcelable settings configuration in the bundle extras */
    public static final String EXTRA = "settings";

    private String[] sizeChoices;
    private int[] sizeValues;

    private ImageView contactImageView;
    private TextView contactTextView;
    private Spinner spinner;
    private RadioButton normalMode;
    private RadioButton timedMode;
    private CheckBox soundCheckBox;
    private CheckBox beepCheckBox;
    private CheckBox autoFlagCheckBox;

    private SettingsParam settings;

    /**
     * Load the settings from the PreferenceManager
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        initUI();


        //Loading preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        settings = new SettingsParam();
        settings.lookupkey = preferences.getString(PLAYER_LOOKUPKEY, null);

        if (settings.lookupkey != null) {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, settings.lookupkey);
            Uri person = ContactsContract.Contacts.lookupContact(getContentResolver(), lookupUri);


            InputStream photoStream =
                    ContactsContract.Contacts.openContactPhotoInputStream
                            (getContentResolver(), person);
            if (photoStream != null) {
                Bitmap photo = BitmapFactory.decodeStream(photoStream);

                contactImageView.setImageBitmap(photo);

                try {
                    photoStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                contactImageView.setImageResource(R.drawable.avatar);

            }


            Cursor cur = getApplicationContext().getContentResolver()
                    .query(person,
                            new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                                    ContactsContract.Contacts.LOOKUP_KEY}, null, null, null);

            if (cur.moveToFirst()) {
                int nameIndex = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

                settings.name = cur.getString(nameIndex);
                if (settings.name.length() == 0)
                    settings.name = "Unknown";
            }
            cur.close();

            //re init
            contactTextView.setText(settings.name);

        } else {
            settings.name = null;
        }

        int index = 1;
        int value = preferences.getInt(BORDER_SIZE, sizeValues[index]);
        for (int i = 0; i < sizeValues.length; i++)
            if (value == sizeValues[i]) {
                index = i;
                break;
            }

        spinner.setSelection(index);
        boolean result = preferences.getBoolean(TIMED_MODE_ACTIVE, false);
        normalMode.setChecked(!result);
        timedMode.setChecked(result);
        soundCheckBox.setChecked(preferences.getBoolean(SOUND_ACTIVE, true));
        beepCheckBox.setChecked(preferences.getBoolean(BEEP_ACTIVE, true));
        autoFlagCheckBox.setChecked(preferences.getBoolean(AUTO_FLAG_ACTIVE, true));

        if (soundCheckBox.isChecked() && timedMode.isChecked())
            beepCheckBox.setEnabled(true);
        else
            beepCheckBox.setEnabled(false);

    }

    /**
     * Commits the settings to the PreferenceManager, finishes the activity and return the parcelable settings
     */
    @Override
    public void onBackPressed() {
        //calling the super method will force RESULT_CODE to RESULT_CANCELED
        //super.onBackPressed();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        settings.border = sizeValues[spinner.getSelectedItemPosition()];
        settings.timed = timedMode.isChecked();
        settings.sound = soundCheckBox.isChecked();
        settings.beep = beepCheckBox.isChecked();
        settings.autoflag = autoFlagCheckBox.isChecked();

        editor.putString(PLAYER_NAME, settings.name);
        editor.putString(PLAYER_LOOKUPKEY, settings.lookupkey);
        editor.putInt(BORDER_SIZE, settings.border);
        editor.putBoolean(TIMED_MODE_ACTIVE, settings.timed);
        editor.putBoolean(SOUND_ACTIVE, settings.sound);
        editor.putBoolean(BEEP_ACTIVE, settings.beep);
        editor.putBoolean(AUTO_FLAG_ACTIVE, settings.autoflag);
        editor.commit();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA, settings);
        setResult(RESULT_OK, returnIntent);
        finish();

    }

    private void initUI() {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        if (isTablet) {
            sizeChoices = new String[]{"Small (6x6)", "Normal (12x12)", "Large (20x20)"};
            sizeValues = new int[]{6, 12, 20};
        } else {
            sizeChoices = new String[]{"Small (6x6)", "Normal (12x12)"};
            sizeValues = new int[]{6, 12};
        }

        contactImageView = (ImageView) findViewById(R.id.contactImageView);
        contactTextView = (TextView) findViewById(R.id.contactTextView);
        spinner = (Spinner) findViewById(R.id.size_choice_spinner);
        normalMode = (RadioButton) findViewById(R.id.radio_normal_mode);
        timedMode = (RadioButton) findViewById(R.id.radio_timed_mode);
        soundCheckBox = (CheckBox) findViewById(R.id.sound_checkbox);
        beepCheckBox = (CheckBox) findViewById(R.id.beep_checkbox);
        autoFlagCheckBox = (CheckBox) findViewById(R.id.autoflag_checkbox);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, sizeChoices);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);


        soundCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && timedMode.isChecked())
                    beepCheckBox.setEnabled(true);
                else
                    beepCheckBox.setEnabled(false);
            }
        });

        timedMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b && soundCheckBox.isChecked())
                    beepCheckBox.setEnabled(true);
                else
                    beepCheckBox.setEnabled(false);
            }
        });

        contactImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, 1000);
            }
        });
    }


    /** initializes in the settings the image and the name of the contact selected */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Uri person = data.getData();

            InputStream photoStream =
                    ContactsContract.Contacts.openContactPhotoInputStream
                            (getContentResolver(), person);
            if (photoStream != null) {
                Bitmap photo = BitmapFactory.decodeStream(photoStream);

                contactImageView.setImageBitmap(photo);

                try {
                    photoStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                contactImageView.setImageResource(R.drawable.avatar);

            }


            Cursor cur = getApplicationContext().getContentResolver()
                    .query(person,
                            new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                                    ContactsContract.Contacts.LOOKUP_KEY}, null, null, null);

            if (cur.moveToFirst()) {
                int nameIndex = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                int lookupIndex = cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);

                settings.name = cur.getString(nameIndex);
                settings.lookupkey = cur.getString(lookupIndex);
            }
            cur.close();

            //re init
            contactTextView.setText(settings.name);
        }

    }
}
