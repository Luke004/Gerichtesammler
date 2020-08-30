package com.example.lhilf.leistungensammler.comparators;

import android.content.Context;

import com.example.lhilf.leistungensammler.Dish;
import com.example.lhilf.leistungensammler.R;

import java.util.Comparator;

public class RatingComparator extends AbstractComparator implements Comparator<Dish> {

    private Context ctx;

    public RatingComparator(boolean reverse, Context ctx) {
        super(reverse);
        this.ctx = ctx;
    }

    @Override
    public int compare(Dish dish1, Dish dish2) {
        int dish_1_rating = getRatingNumber(dish1.getDishRating());
        int dish_2_rating = getRatingNumber(dish2.getDishRating());
        if (reverse) return Integer.compare(dish_1_rating, dish_2_rating);
        else return Integer.compare(dish_2_rating, dish_1_rating);
    }

    private int getRatingNumber(String rating) {
        if (rating.equals(ctx.getString(R.string.rating_4))) return 4;
        else if (rating.equals(ctx.getString(R.string.rating_3))) return 3;
        else if (rating.equals(ctx.getString(R.string.rating_2))) return 2;
        return 1;
    }

}
