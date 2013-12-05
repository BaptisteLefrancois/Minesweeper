package uk.ac.brookes.lefrancois.minesweeper.model;

/**
 * Created by Baptiste on 06/07/13.
 */
public class Score {

    /** The primary key of the row in the respective high score table*/
    public int id;
    /** The uri of the contact who did the score*/
    public String lookupKey;
    /** The name of the contact who did the score*/
    public String name;
    /** The score value */
    public int value;


    public Score(){}

    public Score(int id, String lookupKey, String name, int value)
    {
        this.id = id;
        this.lookupKey = lookupKey;
        this.name = name;
        this.value = value;
    }
}
