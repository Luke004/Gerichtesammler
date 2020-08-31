package com.example.lhilf.leistungensammler;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface CategoryDAO {

    /**
     * Findet alle {@link Category} Objekte.
     * @return alle {@link Category} Objekte
     */
    @Query("SELECT * FROM Category")
    List<Category> findAll();

    /**
     * Sucht das {@link Category} Objekt welches mit der gegebenen id gefunden wurde und gibt
     * es als return Wert zurück.
     * @param name Category name
     *
     * @return name gefunden = Optional<Category>,
     *     nicht gefunden = Optional.empty()
     */
    @Query("SELECT * FROM Category WHERE name = :name")
    Category findByName(String name);

    /**
     * Sucht das {@link Category} Objekt welches mit der gegebenen color gefunden wurde und gibt
     * es als return Wert zurück.
     * @param color Category color
     *
     * @return color gefunden = Optional<Dish>,
     *     nicht gefunden = Optional.empty()
     */
    @Query("SELECT * FROM Category WHERE color = :color")
    Category existsColor(String color);

    /**
     * Löscht ein {@link Category} Objekt.
     * @param category {@link Category}
     */
    @Delete
    void delete(Category category);
    /**
     * Ersetzt das übergebene {@link Category} Objekt mit einem bereits
     * gespeicherten {@link Category} Objekt mit gleicher id.
     *
     * @param category Category Objekt
     * @return true = update ok
     * false = kein {@link Category} Objekt mit gleicher id im Speicher gefunden
     */
    @Update(onConflict = REPLACE)
    int update(Category category);

    /**
     * Persistiert das übergebene {@link Category} Objekt und liefert die neue id zurück.
     * @param category Category Objekt
     * @return neue category id
     */
    @Insert(onConflict = IGNORE)
    long persist(Category category);
}
