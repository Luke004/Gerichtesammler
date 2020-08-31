package com.example.lhilf.leistungensammler.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.lhilf.leistungensammler.AppDatabase;
import com.example.lhilf.leistungensammler.Category;
import com.example.lhilf.leistungensammler.R;
import com.example.lhilf.leistungensammler.adapters.ColoredDishesArrayAdapter;
import com.example.lhilf.leistungensammler.adapters.EditCategoriesAdapter;

import java.util.List;
import java.util.Random;

public class EditCategoriesDialog extends Dialog {

    private ListView categoriesListView;
    private EditCategoriesAdapter editCategoriesAdapter;
    private List<Category> categories;
    private ImageView addCategoryBtn;
    private Context ctx;
    private ColoredDishesArrayAdapter dishesAdapter;
    private Random random;

    public EditCategoriesDialog(Context ctx, ColoredDishesArrayAdapter dishesAdapter) {
        super(ctx);
        this.ctx = ctx;
        random = new Random();
        this.dishesAdapter = dishesAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_categories_dialog);
        categoriesListView = findViewById(R.id.categories_list);
        addCategoryBtn = findViewById(R.id.edit_categories_add_btn);
        registerForContextMenu(categoriesListView);
        categories = AppDatabase.getDb(ctx).categoryDAO().findAll();
        editCategoriesAdapter = new EditCategoriesAdapter(ctx, R.layout.category_item, categories,
                dishesAdapter);
        categoriesListView.setAdapter(editCategoriesAdapter);

        addCategoryBtn.setOnClickListener(view -> {
            // Generate random color code:
            // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
            int nextInt = random.nextInt(0xffffff + 1);
            // format it as hexadecimal string (with hash tag and leading zeros)
            String colorCode = String.format("#%06x", nextInt);

            Category category = new Category(ctx.getString(R.string.my_category), colorCode);

            //AppDatabase.getDb(ctx).categoryDAO().


        });

    }

}
