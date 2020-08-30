package com.example.lhilf.leistungensammler;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@Database(entities = {Dish.class, Category.class}, version = 2)
@TypeConverters({Dish.Converter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract DishDAO dishDAO();
    public abstract CategoryDAO categoryDAO();

    public static AppDatabase getDb(Context ctx){
        if(INSTANCE == null){
            INSTANCE =
                    Room.databaseBuilder(ctx.getApplicationContext(), AppDatabase.class,
                            "app-database").allowMainThreadQueries()
                            .addMigrations(MIGRATION_1_2)
                            .build();
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Since we didn't alter the table, there's nothing else to do here.
            database.execSQL("CREATE TABLE IF NOT EXISTS `Category`" +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `color` TEXT)");
        }
    };

}
