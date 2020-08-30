package com.example.lhilf.leistungensammler.comparators;

import com.example.lhilf.leistungensammler.Dish;

import java.util.Comparator;

public class NameComparator extends AbstractComparator implements Comparator<Dish> {

    public NameComparator(boolean reverse) {
        super(reverse);
    }

    @Override
    public int compare(Dish dish1, Dish dish2) {
        if (reverse)
            return dish2.getDishName().compareTo(dish1.getDishName());
        else
            return dish1.getDishName().compareTo(dish2.getDishName());
    }
}
