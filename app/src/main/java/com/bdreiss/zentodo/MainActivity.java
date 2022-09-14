package com.bdreiss.zentodo;

import android.content.Context;
import android.os.Bundle;

import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.SQLite;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

        setListView(this);

    }

    public void setListView(Context context){
        Data data = new Data();
        addData(data);
        final List<String> items = data.getEntriesAsString();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
        ListView listView = (ListView) findViewById(R.id.list_view_test);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                TextView textView = (TextView) findViewById(R.id.text_view_test);
                textView.setText("Test");
                data.remove(i);
                items.remove(i);
                adapter.notifyDataSetChanged();
                final List<String> items2 = data.getEntriesAsString();
                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,items2);
                ListView listView2 = (ListView) findViewById(R.id.list_view_test2);
                listView2.setAdapter(adapter);

            }
        });
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

        data.add("Waschen","Haushalt",20220908, "");

        data.add("Buegeln","Haushalt",20220909,"");

        data.add("Gleichungen ueben","Mathe",20220910,"");

        data.add("Computer einschalten","Computersysteme",20210910,"");

    }

}