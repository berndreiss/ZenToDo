package com.bdreiss.zentodo.dataManipulation;

/*
*   Contains all IO-Operations with regards to the save file
*
*   Functions include:
*      -Saving a String to the file (save(String text))
*      -Loading the file contents (load()) returning a String
*/

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SaveFile{

    private final File saveFile;

    public SaveFile(Context context){
        File path = context.getFilesDir();

        //Filepath for save file
        String fileName = "Data";
        this.saveFile = new File(path.toString() + fileName);
        if(!saveFile.exists()) {
            try {
                this.saveFile.createNewFile();
            } catch (IOException e) {
                Log.e("creating file", "IOE: " + e);
            }
        }

    }

    public void save(String text){
        //Saves the String to the designated file
        try{
            FileOutputStream stream = new FileOutputStream(this.saveFile);
            try{
                stream.write(text.getBytes());
            } catch (IOException e){
                Log.e("save file", "IOException: " + e);
            }

        } catch(FileNotFoundException e){
            Log.e("save file", "FNF: " + e);
        }


    }

    public String load() {
        //Reads the designated file contents and returns it as a String
            String data = "";
            try {
            FileInputStream inputStream = new FileInputStream(saveFile.toString());

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString;
            StringBuilder stringBuilder = new StringBuilder();

            while ( (receiveString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            data = stringBuilder.toString();
            //this.textView.setText("true load");

        }
        catch (FileNotFoundException e) {
            Log.e("load file", "File not found: " + e);
        } catch (IOException e) {
            Log.e("load file", "IOException: " + e);
        }
        return data;
    }
}