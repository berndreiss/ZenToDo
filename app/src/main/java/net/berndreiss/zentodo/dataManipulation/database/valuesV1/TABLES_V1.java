package net.berndreiss.zentodo.dataManipulation.database.valuesV1;

import androidx.annotation.NonNull;

public enum TABLES_V1 {
    TABLE_ENTRIES("entries"),
    TABLE_LISTS("lists");

    private final String name;

    TABLES_V1(String name){
        this.name = name;
    }

    @NonNull
    @Override
    public String toString(){
        return name;
    }

}
