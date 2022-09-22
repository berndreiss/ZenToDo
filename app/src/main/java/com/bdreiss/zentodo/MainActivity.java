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

import com.bdreiss.zentodo.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //      setSupportActionBar(binding.toolbar);

        //      NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        //      appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        //      NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    /*    binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        setViews(this);

    }

    public void setViews(Context context){

        TextView textView = (TextView) findViewById(R.id.text_view_test);
        TextView textView2 = (TextView) findViewById(R.id.text_view_test2);
        Data data = new Data(context);
        //      addData(data);//adds TestData
        final ArrayList<Entry> items = data.getEntries();
        TaskListAdapter adapter = new TaskListAdapter(this,data);
        ListView listView = (ListView) findViewById(R.id.list_view_test);
        listView.setAdapter(adapter);

        final EditText editText = (EditText) findViewById(R.id.edit_text_test);
        Button button = (Button) findViewById(R.id.button_test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String task = editText.getText().toString();
                editText.setText("");
                if (!task.equals("")) {
                    Data data = new Data(context);
                    data.add(task, " ", data.getDate(), " ");

                }
                setViews(context);

            }
        });

    }

    public void openDatePickerDialog(Context context) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {

            }
        }, year, month, day);
        datePickerDialog.show();
    }

    public void setListView(Context context){



    }
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }*/

    public static void addData(Data data){

        data.add("Waschen","Haushalt",20220908, " ");

        data.add("Buegeln","Haushalt",20220909," ");

        data.add("Gleichungen ueben","Mathe",20220910," ");

        data.add("Computer einschalten","Computersysteme",20210910," ");

    }

}