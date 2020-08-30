package com.example.lhilf.leistungensammler;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lhilf.leistungensammler.adapters.ColoredDishesArrayAdapter;
import com.example.lhilf.leistungensammler.comparators.CategoryComparator;
import com.example.lhilf.leistungensammler.comparators.DurationComparator;
import com.example.lhilf.leistungensammler.comparators.LastCookedComparator;
import com.example.lhilf.leistungensammler.comparators.NameComparator;
import com.example.lhilf.leistungensammler.comparators.RatingComparator;
import com.example.lhilf.leistungensammler.dialogs.EditCategoriesDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class DishesActivity extends AppCompatActivity {

    private ListView dishesListView;
    private ColoredDishesArrayAdapter dishesAdapter;
    private List<Dish> dishes;
    private boolean onStartFlag;
    private boolean onCreateOrEdit;
    private boolean firstTime;
    private SharedPreferences sharedPref;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 24385;
    private static final String AUTHORITY = "com.example.lhilf.leistungensammler.fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        dishesListView = findViewById(R.id.records_list);
        registerForContextMenu(dishesListView);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        if (sharedPref.getBoolean("first_run_ever", true)) {
            // the app is being launched for first time, do something
            // add the five categories from resources to db
            String[] dish_categories = getResources().getStringArray(R.array.dish_categories);
            String[] dish_colors = getResources().getStringArray(R.array.dish_categories_colors);

            for (int i = 0; i < dish_categories.length; ++i) {
                AppDatabase.getDb(this).categoryDAO().persist(
                        new Category(dish_categories[i], dish_colors[i]));
            }

            // record the fact that the app has been started at least once
            sharedPref.edit().putBoolean("first_run_ever", false).apply();
        }

        dishesListView.setOnItemClickListener((parent, view, position, id) -> {
            // request permission for getOrientation()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
            ArrayAdapter<Dish> record_adapter = (ArrayAdapter<Dish>) dishesListView.getAdapter();
            Dish dish = record_adapter.getItem(position);
            int lastId = -1;
            firstTime = false;
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.recipe_dialog);

            ArrayList<String> recipeImagePaths = dish.getRecipeImagePaths();

            for (String recipe : recipeImagePaths) {
                TextView recipe_dialog_header = dialog.findViewById(R.id.recipe_dialog_header);
                recipe_dialog_header.setText(dish.getDishName());

                ImageView imageView = new ImageView(this);
                imageView.setId(View.generateViewId());

                // request permission for getOrientation()
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }

                File imgFile = new File(recipe);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                // calculate inSampleSize
                options.inSampleSize = Helper.calculateInSampleSize(options, 300, 300);

                // decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap photo = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                imageView.setImageBitmap(photo);

                // view full size picture in other app and zoom on picture click
                imageView.setOnClickListener(v -> {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri photoURI = FileProvider.getUriForFile(this,
                            AUTHORITY,
                            imgFile);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                });

                RelativeLayout relativeLayout = dialog.findViewById(R.id.recipe_dialog_relative_layout);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                if (lastId != -1)
                    layoutParams.addRule(RelativeLayout.BELOW, lastId);
                else
                    layoutParams.addRule(RelativeLayout.BELOW, R.id.recipe_dialog_text);

                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.bottomMargin = 20;
                lastId = imageView.getId();
                relativeLayout.addView(imageView, layoutParams);
            }

            TextView text = dialog.findViewById(R.id.recipe_dialog_text);
            if (!dish.getDishRecipe().equals(""))
                text.setText(dish.getDishRecipe());
            else if (lastId == -1)
                text.setText(R.string.no_dish_recipe_available);

            dialog.show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        onStartFlag = true;
        // refresh record adapter on record change or edit
        dishes = AppDatabase.getDb(this).dishDAO().findAll();
        if (onCreateOrEdit) {
            if (firstTime) {
                sortBySortingMethod(false);
                dishesAdapter = new ColoredDishesArrayAdapter(this, R.layout.dish_item, dishes);
                dishesListView.setAdapter(dishesAdapter);
                // change layout size from '0dp' to 'match_parent'
                ViewGroup.LayoutParams param = dishesListView.getLayoutParams();
                param.height = ListView.LayoutParams.MATCH_PARENT;
                param.width = ListView.LayoutParams.MATCH_PARENT;
                dishesListView.setLayoutParams(param);
                dishesListView.requestLayout();
                firstTime = false;
            } else {
                sortBySortingMethod(false);
                dishesAdapter.clear();
                dishesAdapter.addAll(dishes);
            }
        } else {
            firstTime = true;
            if (dishes.size() > 0) {
                sortBySortingMethod(false);
                dishesAdapter = new ColoredDishesArrayAdapter(this, R.layout.dish_item, dishes);
                dishesListView.setAdapter(dishesAdapter);
                // change layout size from '0dp' to 'match_parent'
                ViewGroup.LayoutParams param = dishesListView.getLayoutParams();
                param.height = ListView.LayoutParams.MATCH_PARENT;
                param.width = ListView.LayoutParams.MATCH_PARENT;
                dishesListView.setLayoutParams(param);
                dishesListView.requestLayout();
                firstTime = false;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (onStartFlag) {
            onStartFlag = false;
        } else {
            dishes = AppDatabase.getDb(this).dishDAO().findAll();
            if (onCreateOrEdit) {
                if (firstTime) {
                    sortBySortingMethod(false);
                    dishesAdapter = new ColoredDishesArrayAdapter(this, R.layout.dish_item, dishes);
                    dishesListView.setAdapter(dishesAdapter);
                    // change layout size from '0dp' to 'match_parent'
                    ViewGroup.LayoutParams param = dishesListView.getLayoutParams();
                    param.height = ListView.LayoutParams.MATCH_PARENT;
                    param.width = ListView.LayoutParams.MATCH_PARENT;
                    dishesListView.setLayoutParams(param);
                    dishesListView.requestLayout();
                    firstTime = false;
                } else {
                    sortBySortingMethod(false);
                    dishesAdapter.clear();
                    dishesAdapter.addAll(dishes);

                }
            } else {
                firstTime = true;
                if (dishes.size() > 0) {
                    sortBySortingMethod(false);
                    dishesAdapter = new ColoredDishesArrayAdapter(this, R.layout.dish_item, dishes);
                    dishesListView.setAdapter(dishesAdapter);
                    // change layout size from '0dp' to 'match_parent'
                    ViewGroup.LayoutParams param = dishesListView.getLayoutParams();
                    param.height = ListView.LayoutParams.MATCH_PARENT;
                    param.width = ListView.LayoutParams.MATCH_PARENT;
                    dishesListView.setLayoutParams(param);
                    dishesListView.requestLayout();
                    firstTime = false;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_new_dish:
                // ADD NEW DISH HERE
                Intent intent = new Intent(this, DishFormActivity.class);
                startActivityForResult(intent, 1);
                return true;
            case R.id.action_appearance_settings:
                showAppearanceSettingsMenu(findViewById(R.id.action_appearance_settings));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAppearanceSettingsMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.sort:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.sort_by))
                            .setItems(R.array.sort_types, (dialog, which) -> {
                                String[] sortTypesArray = getResources().getStringArray(R.array.sort_types);
                                String sortingMethod = sortTypesArray[which];
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("sortingMethod", sortingMethod);
                                editor.apply();
                                // check the previous sorting method
                                String sortingMethod_prev = sharedPref
                                        .getString("sortingMethod_prev",
                                                getString(R.string.sort_type_name));
                                // reverse sort when the prev sorting method equals the current
                                boolean reversed = sortingMethod_prev.equals(sortingMethod);
                                boolean isAlreadyReversed = sharedPref
                                        .getBoolean("sortingMethod_reversed", false);
                                // reverse again if it is already reversed
                                if (reversed && isAlreadyReversed) {
                                    reversed = false;
                                }
                                // finally sort
                                sortBySortingMethod(reversed);

                                if (onCreateOrEdit) {
                                    dishesAdapter.clear();
                                    dishesAdapter.addAll(dishes);
                                } else {
                                    dishesAdapter.notifyDataSetChanged();
                                }
                            });
                    builder.create();
                    builder.show();
                    return true;
                case R.id.filter:
                    Toast.makeText(DishesActivity.this, "filter",
                            Toast.LENGTH_LONG).show();
                    return true;
                case R.id.edit_categories:
                    Toast.makeText(DishesActivity.this, "edit categories",
                            Toast.LENGTH_LONG).show();

                    EditCategoriesDialog dialog = new EditCategoriesDialog(DishesActivity.this);
                    dialog.show();
                    return true;
            }
            return false;
        });
        popupMenu.inflate(R.menu.appearance_settings_menu);
        popupMenu.show();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        ArrayAdapter<Dish> dishes_adapter;
        Dish dish;

        switch (item.getItemId()) {
            case R.id.action_delete_selected_dish:
                AlertDialog.Builder builder = new AlertDialog.Builder(DishesActivity.this);
                builder.setTitle(R.string.action_delete);
                builder.setMessage(R.string.action_delete_confirm_msg);
                builder.setNeutralButton(R.string.confirm, (dialog, which) -> {
                    // delete selected item
                    ArrayAdapter<Dish> record_adapter2 = (ArrayAdapter<Dish>) dishesListView.getAdapter();
                    Dish dish2 = record_adapter2.getItem(index);
                    // delete all pictures from app associated with this dish
                    for (String imagePath : dish2.getRecipeImagePaths()) {
                        File imgFile = new File(imagePath);
                        if (imgFile.exists()) {
                            imgFile.delete();
                        }
                    }
                    // delete dish from database
                    AppDatabase.getDb(DishesActivity.this).dishDAO()
                            .delete(dish2);
                    // refresh DishesActivity
                    dishes = AppDatabase.getDb(DishesActivity.this).dishDAO().findAll();
                    dishesAdapter.clear();
                    sortBySortingMethod(false);
                    dishesAdapter.addAll(dishes);
                    onCreateOrEdit = true;
                });
                builder.show();
                return true;

            case R.id.action_edit_selected_dish:
                // edit selected item
                dishes_adapter = (ArrayAdapter<Dish>) dishesListView.getAdapter();
                dish = dishes_adapter.getItem(index);
                Intent intent = new Intent(this, DishEditActivity.class);
                intent.putExtra("dish", dish);
                startActivityForResult(intent, 1);
                return true;

            case R.id.action_mark_as_cooked:
                dishes_adapter = (ArrayAdapter<Dish>) dishesListView.getAdapter();
                dish = dishes_adapter.getItem(index);
                Dish foundDish = AppDatabase.getDb(DishesActivity.this)
                        .dishDAO()
                        .findById(dish.getId());
                Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
                foundDish.setLastCookingDate(gmtCal.getTime());
                AppDatabase.getDb(DishesActivity.this).dishDAO().update(foundDish);
                // refresh DishesActivity
                dishes = AppDatabase.getDb(DishesActivity.this).dishDAO().findAll();
                sortBySortingMethod(false);
                dishesAdapter.clear();
                dishesAdapter.addAll(dishes);

                return true;

            case R.id.action_send_mail_dish:
                dishes_adapter = (ArrayAdapter<Dish>) dishesListView.getAdapter();
                dish = dishes_adapter.getItem(index);

                boolean hasRecipe, hasDuration, hasImages;

                ArrayList<String> recipeImagePaths = dish.getRecipeImagePaths();
                hasImages = recipeImagePaths.size() != 0;
                hasRecipe = !dish.getDishRecipe().isEmpty();
                hasDuration = dish.getDishDuration() != -1.f;

                // CREATE SUBJECT
                String subject = DishesActivity.this.getString(R.string.my_dish)
                        + ": " + dish.getDishName();

                // CREATE TEXT
                String text;
                // Name
                text = dish.getDishName();
                if (hasRecipe || hasDuration) text += "\n\n";

                // Recipe
                if (hasRecipe) {
                    text += getString(R.string.cooking) + ":\n";
                    text += dish.getDishRecipe();
                    if (hasDuration) text += "\n\n";
                }

                // Duration
                if (hasDuration) {
                    text += getString(R.string.dish_duration) + ":\n" + getString(R.string.circa)
                            + " " + dish.getDishDuration()
                            + " " + getString(R.string.hours);
                }

                // create the intent and add the extras (subject, text and photos)
                Intent intent2 = new Intent(Intent.ACTION_SEND);

                if (hasImages) intent2.setType("image/png");
                else intent2.setType("text/html");

                // SUBJECT
                intent2.putExtra(Intent.EXTRA_SUBJECT, subject);
                // TEXT
                intent2.putExtra(Intent.EXTRA_TEXT, text);
                // PHOTOS
                if (hasImages) {
                    // add all the images as extras
                    for (String recipe : recipeImagePaths) {
                        ImageView imageView = new ImageView(this);
                        imageView.setId(View.generateViewId());
                        intent2.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                                AUTHORITY,
                                new File(recipe)));
                    }
                }
                startActivity(Intent.createChooser(intent2, DishesActivity.this.getString(
                        R.string.send_email)
                ));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            onCreateOrEdit = resultCode == Activity.RESULT_OK;
        }
    }

    private void sortBySortingMethod(boolean reversed) {
        String sortingMethod = sharedPref
                .getString("sortingMethod", getString(R.string.sort_type_name));

        Toast.makeText(DishesActivity.this, "sortingMethod: " + sortingMethod + "\n"
                + "reversed: " + reversed, Toast.LENGTH_LONG).show();


        if (sortingMethod.equals(getString(R.string.sort_type_name))) {
            Collections.sort(dishes, new NameComparator(reversed));
        } else if (sortingMethod.equals(getString(R.string.sort_type_rating))) {
            Collections.sort(dishes, new RatingComparator(reversed, DishesActivity.this));
        } else if (sortingMethod.equals(getString(R.string.sort_type_category))) {
            Collections.sort(dishes, new CategoryComparator(reversed));
        } else if (sortingMethod.equals(getString(R.string.sort_type_duration))) {
            Collections.sort(dishes, new DurationComparator(reversed));
        } else if (sortingMethod.equals(getString(R.string.sort_type_last_cooked))) {
            Collections.sort(dishes, new LastCookedComparator(reversed));
        }
        // save previous sorting method
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("sortingMethod_prev", sortingMethod);
        // save reversed info
        editor.putBoolean("sortingMethod_reversed", reversed);
        editor.apply();
    }

}
