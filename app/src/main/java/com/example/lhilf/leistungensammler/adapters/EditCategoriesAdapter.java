package com.example.lhilf.leistungensammler.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lhilf.leistungensammler.Category;

import com.example.lhilf.leistungensammler.R;

import java.util.List;

public class EditCategoriesAdapter extends ArrayAdapter<Category> {

    private Context context;

    public EditCategoriesAdapter(Context context, int textViewResourceId, List<Category> categories) {
        super(context, textViewResourceId, categories);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.category_item, parent, false);
        RelativeLayout edit_category_layout = rowView.findViewById(R.id.edit_category_layout);
        TextView category_name = rowView.findViewById(R.id.category_name);
        Button category_color_pick_btn = rowView.findViewById(R.id.category_color_pick_btn);
        ImageView category_delete_btn = rowView.findViewById(R.id.category_delete_btn);



        Category category = getItem(position);

        category_name.setText(category.getName());

        category_color_pick_btn.setBackgroundColor(Color.parseColor(category.getColor()));

        return rowView;
    }

}
