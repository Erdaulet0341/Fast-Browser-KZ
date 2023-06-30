package com.main.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM history")
    List<History> getAll();

    @Insert
    void insert(History history);

    @Query("DELETE FROM history WHERE id = :historyId")
    void deleteById(int historyId);

    @Query("DELETE FROM history")
    void deleteAll();
}