package com.bdreiss.zentodo;

//TODO add comments


import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;
import com.bdreiss.zentodo.dataManipulation.SaveFile;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.bdreiss.zentodo.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    /*    binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        data = new Data(this);

        initialize(this);

    }

    public void initialize(Context context){

        initializeThougts(context);

    }

    public void initializeThougts(Context context){
        LinearLayout dropYourThoughts = (LinearLayout) findViewById(R.id.layout_drop_thoughts);

        enableLayout(dropYourThoughts);



        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getDropped(), "dropped");
        ListView listView = (ListView) findViewById(R.id.list_view_thoughts);
        listView.setAdapter(adapter);

        final EditText editText = (EditText) findViewById(R.id.edit_text_thoughts);
        Button button = (Button) findViewById(R.id.button_add_thoughts);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String task = editText.getText().toString();
                editText.setText("");
                if (!task.equals("")) {
                    data.add(task);
                    initializeThougts(context);
                }


            }
        });


    }
    public void enableLayout(LinearLayout layout){

            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setEnabled(true);
                child.setAlpha(1);
            }


    }

    public void disableLayout(LinearLayout layout){

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
            child.setAlpha(0);
        }


    }

}