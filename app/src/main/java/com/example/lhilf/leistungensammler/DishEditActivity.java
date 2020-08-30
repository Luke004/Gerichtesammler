package com.example.lhilf.leistungensammler;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DishEditActivity extends AppCompatActivity {

    private EditText dish_name, dish_recipe, dish_duration;
    private ArrayList<String> recipeImagesPaths;
    private ArrayList<File> imagesToDelete;
    private LinearLayout recipeImageViewHolder;
    private Spinner dish_rating, dish_type;
    private Date lastCookingDate;
    private int temp_id;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 24385;
    private static final int CAMERA_REQUEST = 1887;
    private static final int PHOTO_PICK_REQUEST = 1889;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MAX_RECIPE_PICTURES = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_edit);

        getSupportActionBar().setTitle(R.string.edit_dish);

        dish_name = findViewById(R.id.dish_edit_input_editText);
        dish_rating = findViewById(R.id.dish_rating_spinner2);
        dish_type = findViewById(R.id.dish_type_spinner2);
        dish_recipe = findViewById(R.id.dish_recipe_editText2);
        dish_duration = findViewById(R.id.dish_duration_editText2);

        recipeImageViewHolder = findViewById(R.id.recipeImageViewHolder);

        // add items to ratings spinner
        String[] ratingsList = getResources().getStringArray(R.array.dish_ratings);

        ArrayAdapter<String> ratings_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ratingsList);
        ratings_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        dish_rating.setAdapter(ratings_adapter);

        // add categories to dish type spinner
        List<Category> categories = AppDatabase.getDb(this).categoryDAO().findAll();
        String[] categoryNames = new String[categories.size()];
        for (int idx = 0; idx < categoryNames.length; ++idx) {
            categoryNames[idx] = categories.get(idx).getName();
        }

        ArrayAdapter<String> dish_types_adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        dish_types_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        dish_type.setAdapter(dish_types_adapter);


        // prompt all the information given by the record that's being edited
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            Dish dish = (Dish) extras.get("dish");
            dish_name.setText(dish.getDishName());
            dish_recipe.setText(dish.getDishRecipe());
            if (dish.getDishDuration() != -1f)
                dish_duration.setText(Float.toString(dish.getDishDuration()));
            lastCookingDate = dish.getLastCookingDate();

            for (int idx = 0; idx < ratingsList.length; ++idx) {
                if (ratingsList[idx].equals(dish.getDishRating())) {
                    dish_rating.setSelection(idx);
                    break;
                }
            }
            for (int idx = 0; idx < categoryNames.length; ++idx) {
                if (categoryNames[idx].equals(dish.getDishType())) {
                    dish_type.setSelection(idx);
                    break;
                }
            }

            recipeImagesPaths = dish.getRecipeImagePaths();

            for (String recipe : recipeImagesPaths) {
                ImageView imageView = createImageView(this);

                File imgFile = new File(recipe);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                // calculate inSampleSize
                options.inSampleSize = Helper.calculateInSampleSize(options, 720, 1280);
                // decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap photo = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                imageView.setImageBitmap(photo);
                recipeImageViewHolder.addView(imageView);
            }
            temp_id = dish.getId();
        }
    }

    // take or select a photo from library
    public void onSelectPhoto(View view) {
        // request permission for getOrientation()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }


        if (recipeImagesPaths.size() >= MAX_RECIPE_PICTURES) {
            //if (recipeIndex > 4) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.action_max_recipe_photos_reached)
                    .setCancelable(true)
                    .setPositiveButton(R.string.delete, (dialog, id) -> {
                        recipeImageViewHolder.removeAllViews();
                        for (String recipe : recipeImagesPaths) {
                            File imgFile = new File(recipe);
                            if (imgFile.exists()) {
                                if (imagesToDelete == null) imagesToDelete = new ArrayList<>();
                                imagesToDelete.add(imgFile);
                            }
                        }
                        recipeImagesPaths.clear();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        dialog.dismiss();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.action_chose_or_take_photo)
                    .setCancelable(true)
                    .setPositiveButton(R.string.action_chose_photo, (dialog, id) -> {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, PHOTO_PICK_REQUEST);
                    })
                    .setNegativeButton(R.string.action_take_photo, (dialog, id) -> {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                                    MY_CAMERA_PERMISSION_CODE);
                        } else {
                            Intent intent = new Intent(this, CameraActivity.class);
                            startActivityForResult(intent, CAMERA_REQUEST);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
/*
    private String getRealPathFromURI(Uri contentUri) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);

        if (cursor == null) {
            return contentUri.getPath();
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

 */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (recipeImagesPaths.size() < MAX_RECIPE_PICTURES) {
                        String photo_path = data.getExtras().getString("photo_path");
                        File imgFile = new File(photo_path);

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                        // calculate inSampleSize
                        options.inSampleSize = Helper.calculateInSampleSize(options, 720, 1280);
                        // decode bitmap with inSampleSize set
                        options.inJustDecodeBounds = false;
                        Bitmap photo = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

                        // add another image view to layout
                        ImageView imageView = createImageView(this);
                        imageView.setImageBitmap(photo);
                        recipeImageViewHolder.addView(imageView);

                        recipeImagesPaths.add(photo_path);
                    }
                }
                break;
            case PHOTO_PICK_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (recipeImagesPaths.size() < MAX_RECIPE_PICTURES) {
                        Uri selectedImage = data.getData();
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(DishEditActivity.this.getContentResolver(), selectedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        File photoFile = null;
                        String photo_path = null;
                        try {
                            photoFile = Helper.createImageFile(this);
                            photo_path = photoFile.getAbsolutePath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // copy the photo to internal app storage
                        try {
                            FileOutputStream out = new FileOutputStream(photoFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // add another image view
                        ImageView imageView = createImageView(this);
                        imageView.setImageBitmap(bitmap);
                        recipeImageViewHolder.addView(imageView);

                        recipeImagesPaths.add(photo_path);
                    }
                }
                break;
        }
    }

    // submit input when save button is activated
    public void onSave(View view) {
        View focusView = null;
        boolean cancel = false;
        if (dish_name.getText().toString().isEmpty()) {
            dish_name.setError(getString(R.string.error_field_required));
            focusView = dish_name;
            cancel = true;
            dish_name.requestFocus();
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            // is valid:
            Float m_dishDuration;
            if (dish_duration.getText().toString().isEmpty()) {
                m_dishDuration = -1f;
            } else {
                m_dishDuration = Float.parseFloat(dish_duration.getText().toString());
                DecimalFormat df = new DecimalFormat("0.00");
                df.setMaximumFractionDigits(2);
                String holder = df.format(m_dishDuration).replaceAll(",", ".");
                m_dishDuration = Float.parseFloat(holder);
            }
            String m_dish_name = dish_name.getText().toString().trim();
            m_dish_name = m_dish_name.substring(0, 1).toUpperCase()
                    + m_dish_name.substring(1);

            Dish dish = new Dish(m_dish_name,
                    dish_type.getSelectedItem().toString(),
                    m_dishDuration,
                    dish_rating.getSelectedItem().toString(),
                    dish_recipe.getText().toString()
            );
            dish.setId(temp_id);
            dish.setLastCookingDate(lastCookingDate);

            // delete all images the user wants to delete
            if (imagesToDelete != null) {
                for (File file_img : imagesToDelete) {
                    if (file_img.exists()) file_img.delete();
                }
            }

            // every existing images(visible) path is put on dish database
            dish.setRecipeImagePaths(recipeImagesPaths);

            AppDatabase.getDb(this).dishDAO().update(dish);
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ImageView createImageView(Context ctx) {
        ImageView imageView = new ImageView(ctx);
        imageView.setId(View.generateViewId());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Helper.convertDpToPixel(50f, ctx),
                Helper.convertDpToPixel(100f, ctx),
                0.2f);
        imageView.setLayoutParams(params);

        imageView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.action_remove_pic_from_selection)
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, id) -> {
                        // remove the image view from layout
                        int view_index = ((ViewGroup) v.getParent()).indexOfChild(v);
                        recipeImageViewHolder.removeView(v);

                        String img_path = recipeImagesPaths.get(view_index);
                        File imgFile = new File(img_path);
                        if (imgFile.exists()) {
                            if (imagesToDelete == null) imagesToDelete = new ArrayList<>();
                            imagesToDelete.add(imgFile);
                        }
                        // remove the path according to the view index
                        recipeImagesPaths.remove(view_index);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                        // cancel
                        dialog.dismiss();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        });

        return imageView;
    }

}
