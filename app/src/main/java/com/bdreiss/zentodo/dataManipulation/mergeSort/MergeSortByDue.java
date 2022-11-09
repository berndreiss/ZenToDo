package com.bdreiss.zentodo.dataManipulation.mergeSort;

//code adapted from https://www.withexample.com/merge-sort-using-arraylist-java-example/

import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class MergeSortByDue extends MergeSort{

    public MergeSortByDue(ArrayList<Entry> inputArray){
        super(inputArray);
    }

    @Override
    public int getLeft(int leftIndex){
        return inputArray.get(leftIndex).getDue();
    }

    @Override
    public int getRight(int rightIndex){
        return inputArray.get(rightIndex).getDue();
    }

}