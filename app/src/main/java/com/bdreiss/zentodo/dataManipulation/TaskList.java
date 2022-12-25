package com.bdreiss.zentodo.dataManipulation;

public class TaskList {

    private int positionCount;
    private String color;

    public TaskList(int positionCount, String color){

        this.positionCount = positionCount;
        this.color = color;

    }

    public int getPositionCount(){return positionCount;}
    public String getColor(){return color;}

    public void setPositionCount(int positionCount){
        this.positionCount = positionCount;
    }

    public void setColor(String color){
        this.color = color;
    }


}
