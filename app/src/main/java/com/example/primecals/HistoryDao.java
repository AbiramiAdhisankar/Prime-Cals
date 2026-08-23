package com.example.primecals;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface HistoryDao {

    @Insert
    void insert(CalculationHistory history);

    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT 50")
    List<CalculationHistory> getAllHistory();

    @Query("DELETE FROM calculation_history")
    void clearAllHistory();
}