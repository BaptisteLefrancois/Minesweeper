package uk.ac.brookes.lefrancois.minesweeper.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import uk.ac.brookes.lefrancois.minesweeper.R;
import uk.ac.brookes.lefrancois.minesweeper.view.SquareImageView;

/**
 * Created by Baptiste on 30/06/13.
 */
public class MineFieldAdapter extends BaseAdapter {

    /**
     *  Permits to access directly to the image resource without using a switch case statement
     */
    private static int[] imageResources = {
            0,
            R.drawable.one,
            R.drawable.two,
            R.drawable.three,
            R.drawable.four,
            R.drawable.five,
            R.drawable.six,
            R.drawable.seven,
            R.drawable.height,
            R.drawable.flag
    };

    /** Keep a reference of the activity where this instance is used */
    private Context context;
    /** The state of the extended minefield */
    private int[] minefield;
    /** Permits to translate the view position to the real position in the extended minefield*/
    private int[] transpose;

    public MineFieldAdapter(Context c, int[] minefield, int[] transpose) {
        context = c;
        this.minefield = minefield;
        this.transpose = transpose;
    }

    @Override
    public int getCount() {
        return transpose.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        if (view == null) {
            imageView = new SquareImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(2, 2, 2, 2);
        } else {
            imageView = (ImageView) view;
        }

        int value = minefield[transpose[i]];

        if (value < 10)
            imageView.setImageResource(imageResources[value]);
        else if (value == 59)
            imageView.setImageResource((R.drawable.flag));
        else if (value == 50)
            imageView.setImageResource(R.drawable.mine);
        else
            imageView.setImageResource(R.color.holo_blue_light_like);

        return imageView;

    }

    /** Permits to redefine the resource to display*/
    public void resetResources( int[] minefield, int[] transpose){
        this.minefield = minefield;
        this.transpose = transpose;
    }
}
