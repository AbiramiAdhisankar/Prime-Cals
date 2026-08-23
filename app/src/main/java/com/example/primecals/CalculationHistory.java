package com.example.primecals;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "calculation_history")
public class CalculationHistory {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String expression;
    private String result;
    private long timestamp;

    public CalculationHistory(String expression, String result, long timestamp) {
        this.expression = expression;
        this.result = result;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}