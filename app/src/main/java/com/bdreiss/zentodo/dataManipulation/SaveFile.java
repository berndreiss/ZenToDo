package com.bdreiss.zentodo.dataManipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class SaveFile{
    //Contains all IO-Operations with regards to the save file
    //Functions include:
    //-Saving a String to the file (save(String text))
    //-Loading the file contents (load()) returning a String

    private String filepath;//Filepath for save file

    public SaveFile(String filepath){
        this.filepath=filepath;//Setting the file path
        try{//Creating the file if it does not exist

            File myObj = new File(filepath);
            myObj.createNewFile();

        } catch (IOException e){

            e.printStackTrace();

        }

    }

    public void save(String text){
        //Saves the String to the designated file

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.filepath));
            writer.write(text);
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public String load() {
        //Reads the designated file contents and returns it as a String
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.filepath));
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[10];

            try {
                while (reader.read(buffer) != -1) {
                    stringBuilder.append(new String(buffer));
                    buffer = new char[10];
                }
                reader.close();
                return stringBuilder.toString();

            } catch (IOException e){
                e.printStackTrace();
                return "IOException";
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return "FileNotFoundException";
        }
    }
}