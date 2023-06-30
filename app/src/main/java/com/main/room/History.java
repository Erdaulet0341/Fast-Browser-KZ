package com.main.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class History {
    @PrimaryKey(autoGenerate = true)
    public Integer id;

    @ColumnInfo(name = "url")
    public String url;

    @ColumnInfo(name = "time")
    public String time;
}