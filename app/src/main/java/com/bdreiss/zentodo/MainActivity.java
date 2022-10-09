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

    private LinearLayout drop;
    private LinearLayout focus;
    private LinearLayout lists;
    private LinearLayout pick;

    private Button toolbarDrop;
    private Button toolbarPick;
    private Button toolbarFocus;
    private Button toolbarLists;

    private Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        data = new Data(this);


        drop = (LinearLayout) findViewById(R.id.layout_drop);
        focus = (LinearLayout) findViewById(R.id.layout_focus);
        lists = (LinearLayout) findViewById(R.id.layout_lists);
        pick = (LinearLayout) findViewById(R.id.layout_pick);

        toolbarDrop = (Button) findViewById(R.id.toolbar_drop);
        toolbarPick = (Button) findViewById(R.id.toolbar_pick);
        toolbarFocus = (Button) findViewById(R.id.toolbar_focus);
        toolbarLists = (Button) findViewById(R.id.toolbar_lists);

        initialize(this);

        initializeThoughts(this);

    }

    public void initialize(Context context){
        toolbarDrop.setOnClickListener(new View.OnClickListener() {
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

        toolbarFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialize(context);
                initializeFocus(context);

            }
        });

        toolbarPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initialize(context);
                initializeBrainstorm(context);
            }
        });

    }

    public void initializeFocus(Context context){
        disableLayout(drop);
        enableLayout(focus);
        disableLayout(lists);
        disableLayout(pick);

        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));

        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getFocus(), "focus");
        ListView listView = (ListView) findViewById(R.id.list_view_focus);
        listView.setAdapter(adapter);


    }

    public void initializeLists(Context context){
        disableLayout(drop);
        disableLayout(focus);
        enableLayout(lists);
        disableLayout(pick);

        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));

        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getEntries(), "");
        ListView listView = (ListView) findViewById(R.id.list_view_lists);
        listView.setAdapter(adapter);


    }

    public void initializeBrainstorm(Context context){
        enableLayout(pick);
        disableLayout(drop);
        disableLayout(focus);
        disableLayout(lists);

        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));


        TaskListAdapter adapter = new TaskListAdapter(this, data, data.getPotentials(), "potentials");
        ListView listView = (ListView) findViewById(R.id.list_view_pick);
        listView.setAdapter(adapter);

        Button pick = (Button) findViewById(R.id.button_pick);

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Integer> checked= adapter.getIdsChecked();

                for (int id : checked){
                    data.setFocus(id, true);
                    data.setDropped(id, false);
                }

                ArrayList<Integer> notChecked = adapter.getIdsNotChecked();

                for (int id: notChecked){
                    data.setFocus(id, false);
                }

                initialize(context);
                initializeFocus(context);

            }
        });

    }

    public void initializeThoughts(Context context){


        enableLayout(drop);
        disableLayout(focus);
        disableLayout(lists);
        disableLayout(pick);

        toolbarDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar_chosen));
        toolbarPick.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarFocus.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        toolbarLists.setBackgroundColor(getResources().getColor(R.color.button_toolbar));



        TaskListAdapter adapter = new TaskListAdapter(this,data,data.getDropped(), "dropped");
        ListView listView = (ListView) findViewById(R.id.list_view_thoughts);
        listView.setAdapter(adapter);

        final EditText editText = (EditText) findViewById(R.id.edit_text_drop);
        Button buttonDrop = (Button) findViewById(R.id.button_drop);
        buttonDrop.setBackgroundColor(getResources().getColor(R.color.button_toolbar));
        buttonDrop.setOnClickListener(new View.OnClickListener() {
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