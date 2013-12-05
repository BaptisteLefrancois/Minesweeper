package uk.ac.brookes.lefrancois.minesweeper.model;

import android.os.Parcel;
import android.os.Parcelable;

/** This class encapsulate the complete settings configuration*/
public class SettingsParam implements Parcelable{

    /** Name of the contact */
    public String name;
    /** Reference uri of the contact */
    public String lookupkey;
    /** Border size preference */
    public int border;
    /** Game mode if true*/
    public boolean timed;
    /** Sound effects preference */
    public boolean sound;
    /** Beep sound preference */
    public boolean beep;
    /** Long press option for flagging preference */
    public boolean autoflag;

    public SettingsParam(){}

    public SettingsParam(String name,
                         String lookupkey,
                         int border,
                         boolean timed,
                         boolean sound,
                         boolean beep,
                         boolean autoflag){

        this.name = name;
        this.lookupkey = lookupkey;
        this.border = border;
        this.timed = timed;
        this.sound = sound;
        this.beep = beep;
        this.autoflag = autoflag;
    }

    public SettingsParam(Parcel in){
        name = in.readString();
        lookupkey = in.readString();
        border = in.readInt();
        timed = in.readByte() == 1;
        sound = in.readByte() == 1;
        beep = in.readByte() == 1;
        autoflag = in.readByte() == 1;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(lookupkey);
        parcel.writeInt(border);
        parcel.writeByte((byte)(timed ? 1 : 0));
        parcel.writeByte((byte)(sound ? 1 : 0));
        parcel.writeByte((byte)(beep ? 1 : 0));
        parcel.writeByte((byte)(autoflag ? 1 : 0));
    }


    public static final Parcelable.Creator<SettingsParam> CREATOR
            = new Parcelable.Creator<SettingsParam>() {
        public SettingsParam createFromParcel(Parcel in) {
            return new SettingsParam(in);
        }

        public SettingsParam[] newArray(int size) {
            return new SettingsParam[size];
        }

    };
}
