package uk.ac.brookes.lefrancois.minesweeper.logic;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import uk.ac.brookes.lefrancois.minesweeper.R;

/**
 * Created by Baptiste on 06/07/13.
 */
public class SoundManager {

    /** The internal value corresponding to the beep sound*/
    public static int BEEP_SOUND;
    /** The internal value corresponding to the explosion sound*/
    public static int EXPLOSION_SOUND;
    /** The internal value corresponding to the fanfare sound*/
    public static int FANFARE_SOUND;
    /** The internal value corresponding to the flag sound*/
    public static int FLAG_SOUND;
    /** The internal value corresponding to the win sound*/
    public static int WIN_SOUND;

    /** The sound manager will not play sound if this field is set to true */
    public boolean mute;

    private static SoundManager instance;
    private SoundPool soundPool;

    private SoundManager(){}

    public static SoundManager getInstance(){
        if(instance == null)
            instance = new SoundManager();
        return instance;
    }

    /**
     * Loads predefined sounds for the mine sweeper game
     * @param context the activity context in order to localize the resources
     */
    public void Load(Context context){
        //flag sound have been added to avoid confusion for long press gesture (to short press -> Game Over !)
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        BEEP_SOUND = soundPool.load(context, R.raw.beep, 1);
        EXPLOSION_SOUND = soundPool.load(context, R.raw.explosion, 1);
        FANFARE_SOUND = soundPool.load(context, R.raw.fanfare, 1);
        FLAG_SOUND = soundPool.load(context, R.raw.flag, 1);
        WIN_SOUND = soundPool.load(context, R.raw.win, 1);

    }

    /**
     * Allows the Garbage Collector to release this instance if no reference still exists
     */
    public void ReleaseSingleton(){
        instance = null;
    }

    /**
     * Plays a predefined sound
     * @param soundIndex the sound reference. Should received the value {@link #BEEP_SOUND},{@link #EXPLOSION_SOUND},{@link #FANFARE_SOUND},{@link #FLAG_SOUND} or {@link #WIN_SOUND}.
     */
    public void play(int soundIndex){
        if(!mute)
            soundPool.play(soundIndex, 1, 1, 0, 0, 1);
    }
}
