package com.bdreiss.zentodo.dataManipulation;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bdreiss.zentodo.MainActivity;
import com.bdreiss.zentodo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SaveFile{
    //Contains all IO-Operations with regards to the save file
    //Functions include:
    //-Saving a String to the file (save(String text))
    //-Loading the file contents (load()) returning a String

    private String fileName;//Filepath for save file

    private File saveFile;

    private Context context;

    private TextView textView;//REMOVE TEXTVIEW!!!

    public SaveFile(Context context, String filepath,TextView textView){//REMOVE TEXTVIEW!!!
        File path = context.getFilesDir();
        this.textView = textView;//REMOVE TEXTVIEW!!!

        this.saveFile = new File(path.toString() + fileName);
        if(!saveFile.exists()) {
            try {
                this.saveFile.createNewFile();
                this.textView.setText("true create");
            } catch (IOException e) {
                Log.e("creating file", "IOE: " + e.toString());
                this.textView.setText("false create " + e.toString());
            }
        }else{
            textView.setText(this.load());
        }

        this.context = context;

    }

    public void save(String text){
        //Saves the String to the designated file
        try{
            FileOutputStream stream = new FileOutputStream(this.saveFile);
            try{
                stream.write(text.getBytes());
                this.textView.setText(this.load());//REMOVE TEXTVIEW!!!
            } catch (IOException e){
                Log.e("save file", "IOExcpetion: " + e.toString());
                this.textView.setText("False save IOE");//REMOVE TEXTVIEW!!!
            }

        } catch(FileNotFoundException e){
            Log.e("save file", "FNF: " + e.toString());
            this.textView.setText("false save FNF");
        }


    }

    public String load() {
        //Reads the designated file contents and returns it as a String
/*        int length = (int) this.saveFile.length();

        byte[] bytes = new byte[length];

        try {
            FileInputStream in = new FileInputStream(this.saveFile);
            try {
                in.read(bytes);
                in.close();
            } catch (IOException e) {
                this.textView.setText("False load IOE");//REMOVE TEXTVIEW!!!
                Log.e("load file", "IOE: " + e.toString());
            }

            String contents = new String(bytes);
            this.textView.setText(contents);//REMOVE TEXTVIEW!!!

            return contents;
        }catch(FileNotFoundException e){
            Log.e("load file", "FNF: " + e.toString());
            this.textView.setText("False load FNF");//REMOVE TEXTVIEW!!!

        }*/
            String data = "";
            try {
            FileInputStream inputStream = new FileInputStream(new File(saveFile.toString()));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                data = stringBuilder.toString();
                //this.textView.setText("true load");
            }
        }
        catch (FileNotFoundException e) {
            Log.e("load file", "File not found: " + e.toString());
            //this.textView.setText("false load FNF");
        } catch (IOException e) {
            Log.e("load file", "IOException: " + e.toString());
            this.textView.setText("false load IOE");
        }
        return data;
    }
}