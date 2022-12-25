package com.bdreiss.zentodo.dataManipulation.database.valuesV1;

import androidx.annotation.NonNull;

public enum COLUMNS_ENTRIES_V1 {
    ID_COL("id"),
    TASK_COL ("task"),
    FOCUS_COL("focus"),
    DROPPED_COL("dropped"),
    LIST_COL("list"),
    LIST_POSITION_COL("listPosition"),
    REMINDER_DATE_COL ("due"),
    RECURRENCE_COL("recurrence"),
    POSITION_COL("position");

    private String name;

    COLUMNS_ENTRIES_V1(String name){
        this.name = name;
    }

    @NonNull
    @Override
    public String toString(){
        return name;
    }

}
