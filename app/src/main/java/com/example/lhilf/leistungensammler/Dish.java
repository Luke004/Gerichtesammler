package com.example.lhilf.leistungensammler;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

@Entity
public class Dish implements Serializable {

    private @PrimaryKey(autoGenerate = true)
    int id;
    private String dishName, dishType, dishRating, dishRecipe;
    private Date lastCookingDate;
    private float dishDuration;
    private ArrayList<String> recipeImagePaths;

    public Dish(String dishName, String dishType, float dishDuration, String dishRating, String dishRecipe) {
        this.dishName = dishName;
        this.dishType = dishType;
        this.dishDuration = dishDuration;
        this.dishRating = dishRating;
        this.dishRecipe = dishRecipe;
        this.recipeImagePaths = new ArrayList<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    public String getDishType() {
        return dishType;
    }

    public void setDishRecipe(String dishRecipe) {
        this.dishRecipe = dishRecipe;
    }

    public String getDishRecipe() {
        return dishRecipe;
    }

    public void setDishDuration(float dishDuration) {
        this.dishDuration = dishDuration;
    }

    public float getDishDuration() {
        return dishDuration;
    }

    public void setDishRating(String dishRating) {
        this.dishRating = dishRating;
    }

    public String getDishRating() {
        return dishRating;
    }

    public void setLastCookingDate(Date lastCookingDate) {
        this.lastCookingDate = lastCookingDate;
    }

    public Date getLastCookingDate() {
        return this.lastCookingDate;
    }

    public boolean addRecipeImagePath(String path) {
        if (recipeImagePaths.size() > 5) return false;
        else recipeImagePaths.add(path);
        return true;
    }

    public void setRecipeImagePaths(ArrayList<String> recipeImagePaths) {
        this.recipeImagePaths = recipeImagePaths;
    }

    public ArrayList<String> getRecipeImagePaths() {
        return recipeImagePaths;
    }

    @Override
    public String toString() {
        String reportDate = "";

        if (lastCookingDate == null)
            reportDate = "NA";
        else
            reportDate = Helper.getDateStringFromDate(lastCookingDate);

        return dishName + "\nDauer: "
                + (dishDuration == -1 ? "NA " : dishDuration + " Std ") + "| Note: " + dishRating + " | Zuletzt: " + reportDate;
    }

    public static class Converter {
        @TypeConverter
        public static ArrayList<String> fromString(String value) {
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromArrayLisr(ArrayList<String> list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            return json;
        }

        @TypeConverter
        public static Date fromTimestamp(Long value) {
            return value == null ? null : new Date(value);
        }

        @TypeConverter
        public static Long dateToTimestamp(Date date) {
            return date == null ? null : date.getTime();
        }
    }
}




