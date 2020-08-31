package com.example.lhilf.leistungensammler.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lhilf.leistungensammler.AppDatabase;
import com.example.lhilf.leistungensammler.Category;
import com.example.lhilf.leistungensammler.Dish;
import com.example.lhilf.leistungensammler.Helper;
import com.example.lhilf.leistungensammler.R;

import java.util.Date;
import java.util.List;

public class ColoredDishesArrayAdapter extends ArrayAdapter<Dish> {

    private Context context;
    private List<Dish> dishes;

    public ColoredDishesArrayAdapter(Context context, int textViewResourceId, List<Dish> dishes) {
        super(context, textViewResourceId, dishes);
        this.context = context;
        this.dishes = dishes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dish_item, parent, false);
        RelativeLayout dish_layout = rowView.findViewById(R.id.dish_layout);
        LinearLayout ratings_layout = rowView.findViewById(R.id.ratings_layout);
        ImageView star_image_view = rowView.findViewById(R.id.star_image_view);
        TextView dish_name = rowView.findViewById(R.id.dish_name);
        TextView dish_duration = rowView.findViewById(R.id.dish_duration);
        TextView dish_last_cooked = rowView.findViewById(R.id.dish_last_cooked);

        Dish dish = getItem(position);

        dish_name.setText(dish.getDishName());

        String dish_duration_text = context.getString(R.string.dish_duration_short) + ": ";
        float dishDuration = dish.getDishDuration();
        if (dishDuration == -1) {   // no duration given
            dish_duration.setText(dish_duration_text + context.getString(R.string.not_available));
        } else if (dishDuration % 1 != 0) {     // duration has mantissa (e.g. 1.30)
            float dishMinutes = Helper.getMantissaMinutesOfDishDuration(dishDuration);
            float dishHours = (float) Math.floor(dishDuration);
            dish_duration.setText(dish_duration_text
                    + ((dishHours > 0) ? ((int) dishHours + " "
                    + context.getString(R.string.hours_short)) + " " : (""))
                    + (int) dishMinutes + " "
                    + context.getString(R.string.minutes_short));
        } else {    // duration has no mantissa (e.g. 3)
            dish_duration.setText(dish_duration_text + (int) dishDuration + " "
                    + context.getString(R.string.hours_short));
        }

        Date lastCookingDate = dish.getLastCookingDate();
        String reportDate;
        if (lastCookingDate == null) {
            reportDate = context.getString(R.string.not_available);
        } else {
            reportDate = Helper.getDateStringFromDate(lastCookingDate);
        }
        dish_last_cooked.setText(context.getString(R.string.last_cooked) + ": " + reportDate);

        String dish_type = getItem(position).getDishType();

        Category category = AppDatabase.getDb(context).categoryDAO().findByName(dish_type);

        if (category != null) {
            try {
                dish_layout.setBackgroundColor(Color.parseColor(category.getColor()));
            } catch (Exception e) {
                dish_layout.setBackgroundColor(Color.WHITE);
            }
        } else {
            // category was deleted -> use white
            dish_layout.setBackgroundColor(Color.WHITE);
        }

        int stars;

        if (dish.getDishRating().equals(context.getString(R.string.rating_4))) {
            stars = 4;
        } else if (dish.getDishRating().equals(context.getString(R.string.rating_3))) {
            stars = 3;
        } else if (dish.getDishRating().equals(context.getString(R.string.rating_2))) {
            stars = 2;
        } else {
            stars = 1;
        }

        while (--stars > 0) {
            ImageView newStar = addNewStar(star_image_view, context);
            ratings_layout.addView(newStar);
        }

        return rowView;
    }

    private ImageView addNewStar(ImageView star_image_view, Context ctx) {
        ImageView imageView;
        imageView = new ImageView(ctx);
        imageView.setId(View.generateViewId());
        imageView.setImageDrawable(star_image_view.getDrawable());
        imageView.setScaleType(star_image_view.getScaleType());
        imageView.setLayoutParams(star_image_view.getLayoutParams());
        return imageView;
    }

    void updateDishCategory(Dish affectedDish) {
        for (Dish dish : dishes) {
            if (dish.getId() == affectedDish.getId()) {
                dish.setDishType(affectedDish.getDishType());
                break;
            }
        }
    }

    void deleteDishCategory(Dish affectedDish) {
        for (Dish dish : dishes) {
            if (dish.getId() == affectedDish.getId()) {
                dish.setDishType("");
                break;
            }
        }
    }

}
