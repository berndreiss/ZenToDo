package com.bdreiss.zentodo.dataManipulation;

public class Entry{
//this class represents the entry of a task
//it consists of the following fields:

    private int id;//id generated in data.java
    private String task;//a description of the task that has to be done
    private Boolean today;//stores the information if task has been chosen today
    private String list;//a list to which the task belongs to
    private int listPosition;//position in according list
    private int due;//a date, when the task is due -> "yyyyMMdd"
    private String recurrence;//consisting of a String in the form of "y/m/w/d0-90-9" where the
                              //two digit number defines the offset in years (y), months(m),
                              //weeks (w) or days(d) when the task is going to reoccur

    public Entry(int id, String task, Boolean today, String list, int listPosition, int due, String recurrence){
        //creates a new instance and initializes the fields of the entry
        this.id = id;
        this.today = today;
        this.task=task;
        this.list=list;
        this.listPosition=listPosition;
        this.due=due;
        this.recurrence=recurrence;
    }

    //the following functions simply return the different fields of the entry
    public int getId(){return this.id;}

    public String getTask(){
        return this.task;
    }

    public Boolean getToday(){return this.today;}

    public String getList(){
        return this.list;
    }

    public int getListPosition(){return this.listPosition;}

    public int getDue(){
        return this.due;
    }
    
    public String getRecurrence(){
        return this.recurrence;
    }
    
    //The following functions are to update the different fields
    public void setTask(String task){
        this.task=task;
    }

    public void setToday(Boolean today){this.today = today;}

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
