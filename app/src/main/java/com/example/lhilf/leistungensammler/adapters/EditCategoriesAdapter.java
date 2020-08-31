package com.example.lhilf.leistungensammler.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lhilf.leistungensammler.AppDatabase;
import com.example.lhilf.leistungensammler.Category;

import com.example.lhilf.leistungensammler.Dish;
import com.example.lhilf.leistungensammler.R;

import java.util.List;

import top.defaults.colorpicker.ColorPickerPopup;

public class EditCategoriesAdapter extends ArrayAdapter<Category> {

    private Context context;
    private List<Category> categories;
    private ColoredDishesArrayAdapter dishesArrayAdapter;

    public EditCategoriesAdapter(Context context, int textViewResourceId, List<Category> categories,
                                 ColoredDishesArrayAdapter dishesArrayAdapter) {
        super(context, textViewResourceId, categories);
        this.context = context;
        this.categories = categories;
        this.dishesArrayAdapter = dishesArrayAdapter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.category_item, parent, false);
        //RelativeLayout edit_category_layout = rowView.findViewById(R.id.edit_category_layout);
        EditText category_name = rowView.findViewById(R.id.category_name);
        Button category_color_pick_btn = rowView.findViewById(R.id.category_color_pick_btn);
        ImageView category_delete_btn = rowView.findViewById(R.id.category_delete_btn);

        Category category = getItem(position);

        // if the id is 0, a new category was just added and the adapter doesn't have the info of its
        // generated id in the db
        // so let's get the correct id now:
        if (category.getId() == 0) {
            this.notifyDataSetChanged();
            Category category_with_correct_id = AppDatabase.getDb(context).categoryDAO()
                    .findByName(category.getName());    // find the correct one by name
            Toast.makeText(context, "switched with id: " + category_with_correct_id
                    .getId(), Toast.LENGTH_LONG).show();
            category.setId(category_with_correct_id.getId());   // now set the id correctly
        }

        category_color_pick_btn.setOnClickListener(view -> new ColorPickerPopup.Builder(context)
                .initialColor(Color.parseColor(category.getColor())) // set initial color
                .enableBrightness(true) // enable brightness slider
                .enableAlpha(false) // disable alpha slider
                .okTitle(context.getString(R.string.select))
                .cancelTitle(context.getString(R.string.cancel))
                .showIndicator(true)
                .showValue(false)
                .build()
                .show(view, new ColorPickerPopup.ColorPickerObserver() {
                    @Override
                    public void onColorPicked(int color) {
                        if (Color.parseColor(category.getColor()) != color) {
                            if (dishesArrayAdapter != null) {
                                dishesArrayAdapter.notifyDataSetChanged();
                            }
                            view.setBackgroundColor(color);
                            category.setColor("#" + Integer.toHexString(color));
                            AppDatabase.getDb(context).categoryDAO().update(category);
                        }
                    }
                }));

        category_delete_btn.setOnClickListener(view -> {
            if (categories.size() <= 3) {
                Toast.makeText(context, context.getString(R.string.error_category_lower_limit),
                        Toast.LENGTH_LONG).show();
                return;
            }
            // check how many dishes will be affected by this change
            List<Dish> affected_dishes = AppDatabase.getDb(context).dishDAO()
                    .findAllByCategory(category.getName());
            int affectedDishesSize = affected_dishes.size();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getString(R.string.action_delete_category)
                    // add a warning if at least 1 dish is affected
                    + (affectedDishesSize > 0 ? "\n" + context.getString(R.string
                    .warning_affected_dishes_category_change, affectedDishesSize) : ""))
                    .setTitle(category.getName())
                    .setCancelable(true)
                    .setPositiveButton(context.getString(R.string.confirm), (dialog, id) -> {
                        // remove the category
                        this.notifyDataSetChanged();
                        this.categories.remove(category);
                        AppDatabase.getDb(context).categoryDAO().delete(category);
                        // delete the category for all affected dishes
                        for (Dish affectedDish : affected_dishes) {
                            affectedDish.setDishType("");
                            AppDatabase.getDb(context).dishDAO().update(affectedDish);
                            if (dishesArrayAdapter != null) {
                                dishesArrayAdapter.deleteDishCategory(affectedDish);
                                dishesArrayAdapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        // cancel
                        dialog.dismiss();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        });

        category_name.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                category_name.clearFocus();
                if (!category_name.getText().toString().equals(category.getName())) {
                    String newCategoryName = category_name.getText().toString();
                    // capitalize first letter
                    newCategoryName = newCategoryName.substring(0, 1).toUpperCase()
                            + newCategoryName.substring(1);
                    List<Dish> affected_dishes = AppDatabase.getDb(context).dishDAO()
                            .findAllByCategory(category.getName());
                    // update the category for all affected dishes
                    for (Dish affectedDish : affected_dishes) {
                        affectedDish.setDishType(newCategoryName);
                        AppDatabase.getDb(context).dishDAO().update(affectedDish);
                        if (dishesArrayAdapter != null) {
                            dishesArrayAdapter.updateDishCategory(affectedDish);
                        }
                    }
                    // update the category itself
                    category.setName(newCategoryName);
                    AppDatabase.getDb(context).categoryDAO().update(category);
                    Toast.makeText(context, "text of id: " + category.getId() + " updateChangedDishes to "
                            + newCategoryName, Toast.LENGTH_LONG).show();
                }
                return true;
            }
            return false;
        });

        // set the category name field
        category_name.setText(category.getName());

        // set the category color field
        try {
            category_color_pick_btn.setBackgroundColor(Color.parseColor(category.getColor()));
        } catch (Exception e) {
            category_color_pick_btn.setBackgroundColor(Color.WHITE);
        }

        return rowView;
    }

}
