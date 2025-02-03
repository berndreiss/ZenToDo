package net.berndreiss.zentodo.dataManipulation.database.valuesV1;

import androidx.annotation.NonNull;

public enum COLUMNS_LISTS_V1 {

    LIST_NAME_COL("list"),
    LIST_COLOR_COL( "color");

    private String name;

    COLUMNS_LISTS_V1(String name){
        this.name = name;
    }

    @NonNull
    @Override
    public String toString(){
        return name;
    }

}
