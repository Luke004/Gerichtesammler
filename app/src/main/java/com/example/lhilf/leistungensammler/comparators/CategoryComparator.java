package com.example.lhilf.leistungensammler.comparators;

import com.example.lhilf.leistungensammler.Dish;

import java.util.Comparator;

public class CategoryComparator extends AbstractComparator implements Comparator<Dish> {

    public CategoryComparator(boolean reverse) {
        super(reverse);
    }

    @Override
    public int compare(Dish dish1, Dish dish2) {
        if (reverse)
            return dish2.getDishType().compareTo(dish1.getDishType());
        else
            return dish1.getDishType().compareTo(dish2.getDishType());
    }
}
