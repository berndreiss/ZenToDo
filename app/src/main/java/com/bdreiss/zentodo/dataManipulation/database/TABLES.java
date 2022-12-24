package com.bdreiss.zentodo.dataManipulation.database;

import androidx.annotation.NonNull;

public enum TABLES{
    TABLE_ENTRIES("entries"),  TABLE_LISTS("lists");

    private String name;

    TABLES(String name){
        this.name = name;
    }

    @NonNull
    @Override
    public String toString(){
        return name;
    }

}
