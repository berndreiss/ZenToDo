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

    private LinearLayout dropYourThoughts;
    private LinearLayout todaysFocus;
    private LinearLayout lists;

    private Button toolbarDropYourThoughts;
    private Button toolbarBrainstormAndPick;
    private Button toolbarTodaysFocus;
    private Button toolbarLists;

    private Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        data = new Data(this);


        dropYourThoughts = (LinearLayout) findViewById(R.id.layout_drop_thoughts);
        todaysFocus = (LinearLayout) findViewById(R.id.layout_todays);
        lists = (LinearLayout) findViewById(R.id.layout_lists);

        toolbarDropYourThoughts = (Button) findViewById(R.id.toolbar_drop_your_thoughts);
        toolbarBrainstormAndPick = (Button) findViewById(R.id.toolbar_brainstorm);
        toolbarTodaysFocus = (Button) findViewById(R.id.toolbar_todays_focus);
        toolbarLists = (Button) findViewById(R.id.toolbar_lists);

        initialize(this);

        initializeThoughts(this);

    }

    public void initialize(Context context){
        toolbarDropYourThoughts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialize(context);
                initializeThoughts(context);
            }
        });

        toolbarLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialize(context);
                initializeLists(context);
            }
        });

        toolbarTodaysFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialize(context);
                initializeTodays(context);

            }
        });

    }

    public void initializeTodays(Context context){
        disableLayout(dropYourThoughts);
        enableLayout(todaysFocus);
        disableLayout(lists);

        toolbarDropYourThoughts.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarBrainstormAndPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarTodaysFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getTodays(), "todays");
        ListView listView = (ListView) findViewById(R.id.list_view_todays);
        listView.setAdapter(adapter);


    }

    public void initializeLists(Context context){
        disableLayout(dropYourThoughts);
        disableLayout(todaysFocus);
        enableLayout(lists);

        toolbarDropYourThoughts.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarBrainstormAndPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarTodaysFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));

        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getEntries(), "");
        ListView listView = (ListView) findViewById(R.id.list_view_lists);
        listView.setAdapter(adapter);


    }

    public void initializeThoughts(Context context){


        enableLayout(dropYourThoughts);
        disableLayout(todaysFocus);
        disableLayout(lists);

        toolbarDropYourThoughts.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarBrainstormAndPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarTodaysFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));



        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getDropped(), "dropped");
        ListView listView = (ListView) findViewById(R.id.list_view_thoughts);
        listView.setAdapter(adapter);

        final EditText editText = (EditText) findViewById(R.id.edit_text_thoughts);
        Button buttonThoughts = (Button) findViewById(R.id.button_add_thoughts);
        buttonThoughts.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        buttonThoughts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String task = editText.getText().toString();
                editText.setText("");
                if (!task.equals("")) {
                    data.add(task);
                    initializeThoughts(context);
                }


            }
        });


    }
    public void enableLayout(LinearLayout layout){

            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setVisibility(View.VISIBLE);
            }
            layout.bringToFront();

    }

    public void disableLayout(LinearLayout layout){

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setVisibility(View.GONE);
        }
    }

}