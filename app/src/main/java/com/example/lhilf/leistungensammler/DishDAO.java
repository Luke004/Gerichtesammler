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
public interface DishDAO {

    /**
     * Findet alle {@link Dish} Objekte.
     * @return alle {@link Dish} Objekte
     */
    @Query("SELECT * FROM Dish")
    List<Dish> findAll();

    /**
     * Sucht das {@link Dish} Objekt welches mit der gegebenen id gefunden wurde und gibt
     * es als return Wert zurück.
     * @param id Dish id
     *
     * @return id gefunden = Optional<Dish>,
     *     nicht gefunden = Optional.empty()
     */
    @Query("SELECT * FROM Dish WHERE id = :id")
    Dish findById(int id);

    /**
     * Löscht ein {@link Dish} Objekt.
     * @param dish {@link Dish}
     */
    @Delete
    void delete(Dish dish);
    /**
     * Ersetzt das übergebene {@link Dish} Objekt mit einem bereits
     * gespeicherten {@link Dish} Objekt mit gleicher id.
     *
     * @param dish Dish Objekt
     * @return true = update ok
     * false = kein {@link Dish} Objekt mit gleicher id im Speicher gefunden
     */
    @Update(onConflict = REPLACE)
    int update(Dish dish);

    /**
     * Persistiert das übergebene {@link Dish} Objekt und liefert die neue id zurück.
     * @param dish Dish Objekt
     * @return neue dish id
     */
    @Insert(onConflict = IGNORE)
    long persist(Dish dish);
}
