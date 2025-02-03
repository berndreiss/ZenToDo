package net.berndreiss.zentodo.dataManipulation.mergeSort;

//code adapted from https://www.withexample.com/merge-sort-using-arraylist-java-example/

import net.berndreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;

public class MergeSortByListPosition extends MergeSort{
    public MergeSortByListPosition(ArrayList<Entry> inputArray){
        super(inputArray);
    }

    @Override
    public int getLeft(int leftIndex){
        return inputArray.get(leftIndex).getListPosition();
    }

    @Override
    public int getRight(int rightIndex){
        return inputArray.get(rightIndex).getListPosition();
    }

}