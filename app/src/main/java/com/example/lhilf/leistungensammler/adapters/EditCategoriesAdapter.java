package com.example.lhilf.leistungensammler.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lhilf.leistungensammler.AppDatabase;
import com.example.lhilf.leistungensammler.Category;

import com.example.lhilf.leistungensammler.Dish;
import com.example.lhilf.leistungensammler.Helper;
import com.example.lhilf.leistungensammler.R;

import java.io.File;
import java.util.ArrayList;
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
                            view.setBackgroundColor(color);
                            category.setColor("#" + Integer.toHexString(color));
                            AppDatabase.getDb(context).categoryDAO().update(category);
                            dishesArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }));

        category_delete_btn.setOnClickListener(view -> {
            if (categories.size() <= 3) {
                Toast.makeText(context, context.getString(R.string.error_category_lower_limit),
                        Toast.LENGTH_LONG).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.action_delete_category)
                    .setTitle(category.getName())
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, id) -> {
                        // remove the category
                        this.notifyDataSetChanged();
                        this.categories.remove(category);
                        if (!category.getName().equals(context.getString(R.string.my_category))) {
                            // only remove if it really exists in db (name is not = string.my_category)
                            AppDatabase.getDb(context).categoryDAO().delete(category);
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
                    String newDishType = category_name.getText().toString();
                    // capitalize first letter
                    newDishType = newDishType.substring(0, 1).toUpperCase() + newDishType.substring(1);
                    List<Dish> dishes = AppDatabase.getDb(context).dishDAO()
                            .findAllByCategory(category.getName());
                    // update the category for all affected dishes
                    for (Dish dish : dishes) {
                        dish.setDishType(newDishType);
                        AppDatabase.getDb(context).dishDAO().update(dish);
                    }
                    category.setName(newDishType);
                    if (dishes.size() > 0) {    // category was renamed
                        // update the category itself
                        AppDatabase.getDb(context).categoryDAO().update(category);
                        // update dishesArrayAdapter
                        dishesArrayAdapter.clear();
                        dishes = AppDatabase.getDb(context).dishDAO().findAll();
                        Helper.sortDishesBySortingMethod(context, dishes, false);
                        dishesArrayAdapter.addAll(dishes);
                    } else {    // new category was created
                        // color check to prevent double creation by editing name multiple times
                        Category exists = AppDatabase.getDb(context).categoryDAO()
                                .existsColor(category.getColor());
                        if (exists == null) {
                            AppDatabase.getDb(context).categoryDAO().persist(category);
                        } else {    // double creation -> only update the name
                            exists.setName(newDishType);
                            AppDatabase.getDb(context).categoryDAO().update(exists);
                        }
                    }
                }
                return true;
            }
            return false;
        });

        category_name.setText(category.getName());

        try {
            category_color_pick_btn.setBackgroundColor(Color.parseColor(category.getColor()));
        } catch (Exception e) {
            category_color_pick_btn.setBackgroundColor(Color.WHITE);
        }


        return rowView;
    }

}
