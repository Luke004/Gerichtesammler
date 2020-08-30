package com.example.lhilf.leistungensammler.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.lhilf.leistungensammler.AppDatabase;
import com.example.lhilf.leistungensammler.Category;
import com.example.lhilf.leistungensammler.R;
import com.example.lhilf.leistungensammler.adapters.EditCategoriesAdapter;

import java.lang.reflect.Array;
import java.util.List;

public class EditCategoriesDialog extends Dialog {

    private ListView categoriesListView;
    private EditCategoriesAdapter editCategoriesAdapter;
    private List<Category> categories;

    public Activity ctx;
    public Dialog d;

    public EditCategoriesDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.ctx = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_categories_dialog);
        categoriesListView = findViewById(R.id.categories_list);
        registerForContextMenu(categoriesListView);
        categories = AppDatabase.getDb(ctx).categoryDAO().findAll();
        editCategoriesAdapter = new EditCategoriesAdapter(ctx, R.layout.category_item, categories);
        categoriesListView.setAdapter(editCategoriesAdapter);




    }

}
