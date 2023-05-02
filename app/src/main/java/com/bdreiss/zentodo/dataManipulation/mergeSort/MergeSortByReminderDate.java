package com.bdreiss.zentodo.dataManipulation.mergeSort;

//code adapted from https://www.withexample.com/merge-sort-using-arraylist-java-example/

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.bdreiss.zentodo.dataManipulation.Entry;

import java.time.LocalDate;
import java.util.ArrayList;

public class MergeSortByReminderDate extends MergeSort{

    public MergeSortByReminderDate(ArrayList<Entry> inputArray){
        super(inputArray);
    }

    @Override
    public int getLeft(int leftIndex){
        LocalDate date = inputArray.get(leftIndex).getReminderDate();
        if (date == null)
            return 0;
        return date.getYear()*10000+ date.getMonthValue()*100+date.getDayOfMonth();    }

    @Override
    public int getRight(int rightIndex){
        LocalDate date = inputArray.get(rightIndex).getReminderDate();
        if (date == null)
            return 0;
        return date.getYear()*10000+ date.getMonthValue()*100+date.getDayOfMonth();
    }

}