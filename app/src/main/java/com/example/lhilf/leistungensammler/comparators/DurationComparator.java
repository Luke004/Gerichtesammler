package com.example.lhilf.leistungensammler.comparators;

import com.example.lhilf.leistungensammler.Dish;

import java.util.Comparator;

public class DurationComparator extends AbstractComparator implements Comparator<Dish> {

    public DurationComparator(boolean reverse) {
        super(reverse);
    }

    @Override
    public int compare(Dish dish1, Dish dish2) {
        if (dish1.getDishDuration() == -1 || dish2.getDishDuration() == -1) return -1;
        if (reverse)
            return Float.compare(dish2.getDishDuration(), dish1.getDishDuration());
        else
            return Float.compare(dish1.getDishDuration(), dish2.getDishDuration());
    }
}
