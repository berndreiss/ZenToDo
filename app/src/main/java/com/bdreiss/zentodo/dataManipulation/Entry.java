package com.bdreiss.zentodo.dataManipulation;

public class Entry{
//this class represents the entry of a task
//it consists of the following fields:

    private int id;//id generated in data.java
    private String task;//a description of the task that has to be done
    private Boolean today;//true if task has been chosen today
    private Boolean dropped;//true if task has been dropped and not been used in brainstorm and pick
    private String list;//a list to which the task belongs to
    private int listPosition;//position in according list
    private int due;//a date, when the task is due -> "yyyyMMdd"
    private String recurrence;//consisting of a String in the form of "y/m/w/d0-90-9" where the
                              //two digit number defines the offset in years (y), months(m),
                              //weeks (w) or days(d) when the task is going to reoccur

    public Entry(int id, String task, Boolean today, String list, int listPosition, int due, String recurrence){
        //creates a new instance and initializes the fields of the entry
        this.id = id;
        this.today = false;
        this.dropped = true;
        this.task=task;
        this.list=list;
        this.listPosition=listPosition;
        this.due=due;
        this.recurrence=recurrence;
    }

    //the following functions simply return the different fields of the entry
    public int getId(){return this.id;}

    public String getTask(){
        return task;
    }

    public Boolean getToday(){return today;}

    public Boolean getDropped(){return dropped;}

    public String getList(){
        return list;
    }

    public int getListPosition(){return listPosition;}

    public int getDue(){
        return due;
    }
    
    public String getRecurrence(){
        return recurrence;
    }
    
    //The following functions are to update the different fields
    public void setTask(String task){
        this.task=task;
    }

    public void setToday(Boolean today){this.today = today;}

    public void setDropped(Boolean dropped){this.dropped = dropped;}

    public void setList(String list){
        this.list=list;
    }

    public void setListPosition(int listPosition){this.listPosition=listPosition;}

    public void setDue(int due){
        this.due=due;
    }

    public void setRecurrence(String recurrence){
        this.recurrence=recurrence;
    }



}
