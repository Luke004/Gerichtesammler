package com.example.lhilf.leistungensammler.comparators;

import com.example.lhilf.leistungensammler.Dish;

import java.util.Comparator;

public class LastCookedComparator extends AbstractComparator implements Comparator<Dish> {

    public LastCookedComparator(boolean reverse) {
        super(reverse);
    }

    @Override
    public int compare(Dish dish1, Dish dish2) {
        if (dish1.getLastCookingDate() == null) return reverse ? -1 : 1;
        if (dish2.getLastCookingDate() == null) return reverse ? 1 : -1;
        if (reverse)
            return dish2.getLastCookingDate().compareTo(dish1.getLastCookingDate());
        else
            return dish1.getLastCookingDate().compareTo(dish2.getLastCookingDate());
    }

}
