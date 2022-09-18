package com.bdreiss.zentodo.dataManipulation;

public class Entry{
//this class represents the entry of a task
//it consists of the following fields:

    private int id;//the id, which is generated upon loading the save file (see Data.java)
    private String task;//a description of the task that has to be done
    private String list;//a list to which the task belongs to
    private int due;//a date, when the task is due -> "yyyyMMdd"
    private String recurrence;//consisting of a String in the form of "y/m/w/d0-90-9" where the
                              //two digit number defines the offset in years (y), months(m),
                              //weeks (w) or days(d) when the task is going to reoccur

    public Entry(int id,String task, String list, int due, String recurrence){
        //creates a new instance and initializes the fields of the entry

        this.id=id;
        this.task=task;
        this.list=list;
        this.due=due;
        this.recurrence=recurrence;
    }

    //the following functions simply return the different fields of the entry
    public int getID(){
        return this.id;
    }

    public String getTask(){
        return this.task;
    }

    public String getList(){
        return this.list;
    }

    public int getDue(){
        return this.due;
    }
    public String getRecurrence(){
        return this.recurrence;
    }

    public void setID(int id){
        this.id=id;
    }

    //The following functions are to update the different fields
    public void setTask(String task){
        this.task=task;
    }

    public void setList(String list){
        this.list=list;
    }

    public void setDue(int due){
        this.due=due;
    }

    public void setRecurrence(String recurrence){
        this.recurrence=recurrence;
    }



}
