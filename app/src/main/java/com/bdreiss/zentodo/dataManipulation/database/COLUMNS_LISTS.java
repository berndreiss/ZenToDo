package com.bdreiss.zentodo.dataManipulation.database;

import androidx.annotation.NonNull;

public enum COLUMNS_LISTS {

    LIST_NAME_COL("list"), LIST_COLOR_COL( "color");

    private String name;

    COLUMNS_LISTS(String name){
        this.name = name;
    }

    @NonNull
    @Override
    public String toString(){
        return name;
    }

}
