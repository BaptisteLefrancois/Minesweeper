package uk.ac.brookes.lefrancois.minesweeper.logic;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Baptiste on 15/06/13.
 */
public class RulesManager {

    /**
     * Gives the relative position of the adjacent up left square
     */
    private static int upLeft;
    /**
     * Gives the relative position of the adjacent up  square
     */
    private static int up;
    /**
     * Gives the relative position of the adjacent up right square
     */
    private static int upRight;
    /**
     * Gives the relative position of the adjacent left square
     */
    private static int left;
    /**
     * Gives the relative position of the adjacent right square
     */
    private static int right;
    /**
     * Gives the relative position of the adjacent down left square
     */
    private static int downLeft;
    /**
     * Gives the relative position of the adjacent down square
     */
    private static int down;
    /**
     * Gives the relative position of the adjacent down right square
     */
    private static int downRight;

    /*
     * The minefield is an array of Integers.
     *
     * The array concentrate many different state
     * final minefield array code =>    0   : 0 adjacent mine
     *                                  1   : 1 adjacent mine
     *                                  2   : 2 adjacent mines
     *                                  3   : 3 adjacent mines
     *                                  4   : 4 adjacent mines
     *                                  5   : 5 adjacent mines
     *                                  6   : 6 adjacent mines
     *                                  7   : 7 adjacent mines
     *                                  8   : 8 adjacent mines
     *
     *                                  9  : flagged square
     *                                  10  : covered square
     *
     *                                  +50 : mine
     *
     * This system permits to sum and have only unique combinations
     */


    /**
     * @param extendedMinefield the minefield with extra borders
     * @param position          the square to uncover
     * @return false if the player uncover a mine square
     */
    public static int uncoverSquare(int[] extendedMinefield, int position, int extendedBorder, int squaresCoveredCounter) {

        Queue<Integer> queue = new LinkedList<Integer>();

        //check if the player did not uncover a mine => gameover
        if (extendedMinefield[position] >= 50)
            return -1;
        if (extendedMinefield[position] == 9)
            return squaresCoveredCounter;
        if(squaresCoveredCounter < 0)
            return squaresCoveredCounter;

        queue.add(position);

        int current;
        int count;
        while (!queue.isEmpty()) {

            //setting the filter of the adjacent squares
            current = queue.remove();
            upLeft = current - extendedBorder - 1;
            up = upLeft + 1;
            upRight = up + 1;
            right = upRight + extendedBorder;
            downRight = right + extendedBorder;
            down = downRight - 1;
            downLeft = down - 1;
            left = downLeft - extendedBorder;

            //count the number of mine adjacent to the square
            count = 0;
            if (extendedMinefield[upLeft] >= 50) count++;

            if (extendedMinefield[up] >= 50) count++;

            if (extendedMinefield[upRight] >= 50) count++;

            if (extendedMinefield[left] >= 50) count++;

            if (extendedMinefield[right] >= 50) count++;

            if (extendedMinefield[downLeft] >= 50) count++;

            if (extendedMinefield[down] >= 50) count++;

            if (extendedMinefield[downRight] >= 50) count++;

            //change the extendedMinefield (the number will be displayed)
            //the algorithm supposes not flag is dig.
            if ((extendedMinefield[current] += 10) != 9)
                extendedMinefield[current] = count;

            //decrement to know how many more squares need to be uncovered to win
            squaresCoveredCounter--;

            if (count == 0) {
                //recursion if cover squares (flag square are also take in account)
                if (extendedMinefield[upLeft] > 8) {
                    extendedMinefield[upLeft] -= 10;
                    queue.add(upLeft);
                }
                if (extendedMinefield[up] > 8) {
                    extendedMinefield[up] -= 10;
                    queue.add(up);
                }
                if (extendedMinefield[upRight] > 8) {
                    extendedMinefield[upRight] -= 10;
                    queue.add(upRight);
                }
                if (extendedMinefield[left] > 8) {
                    extendedMinefield[left] -= 10;
                    queue.add(left);
                }
                if (extendedMinefield[right] > 8) {
                    extendedMinefield[right] -= 10;
                    queue.add(right);
                }
                if (extendedMinefield[downLeft] > 8) {
                    extendedMinefield[downLeft] -= 10;
                    queue.add(downLeft);
                }
                if (extendedMinefield[down] > 8) {
                    extendedMinefield[down] -= 10;
                    queue.add(down);
                }
                if (extendedMinefield[downRight] > 8) {
                    extendedMinefield[downRight] -= 10;
                    queue.add(downRight);
                }
            }
        }


        return squaresCoveredCounter;
    }


}
