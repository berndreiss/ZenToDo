package com.bdreiss.zentodo.adapters;

/*
*   Simple ListView adapter that shows all lists in Data as Buttons. onClick it opens a TaskListAdapter in the same ListView
*   that shows all tasks associated with the list.
*
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bdreiss.zentodo.R;
import com.bdreiss.zentodo.adapters.recyclerViewHelper.CustomItemTouchHelperCallback;
import com.bdreiss.zentodo.dataManipulation.Data;
import com.bdreiss.zentodo.dataManipulation.Entry;

import java.util.ArrayList;


public class ListsListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final RecyclerView recyclerView;//ListView to show lists/tasks
    private final ListView listView;
    private final ArrayList<String> lists;//Dynamically generated Array of all lists in data
    private final Data data;//Database
    private final TextView header;//header of ListView. setVisibility=GONE by default and =VISIBLE when TaskListAdapter is initialized.
    private final ArrayList<Entry> listTasks = new ArrayList<>();

    ListTaskListAdapter listsTaskListAdapter;
    AllTaskListAdapter allTasksAdapter;
    AllTaskListAdapter noListAdapter;

    private static class ViewHolder{
        private Button button;//Button to choose list
    }

    public ListsListAdapter(Context context, ListView listView, RecyclerView recyclerView, TextView header, Data data, ArrayList<String> lists){
        super(context, R.layout.lists_row,lists);
        this.context=context;
        this.data = data;
        this.lists = lists;
        this.header = header;
        this.listView = listView;
        this.recyclerView = recyclerView;
        recyclerView.setVisibility(View.GONE);

    }

    @SuppressLint({"InflateParams", "NotifyDataSetChanged"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView==null){
            holder = new ListsListAdapter.ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.lists_row, null, true);

            holder.button = convertView.findViewById(R.id.button_lists_row);

            convertView.setTag(holder);

        }else{
            holder = (ListsListAdapter.ViewHolder)convertView.getTag();
        }

        //set Button-text to list name
        holder.button.setText(lists.get(position));

        //if clicked assign ListView TaskListAdapter with tasks associated to list
        holder.button.setOnClickListener(view -> {

            //set header with list name visible
            header.setVisibility(View.VISIBLE);

            recyclerView.setVisibility(View.VISIBLE);

            listView.setVisibility(View.GONE);
            //fill ListView with all tasks or according list
            if (holder.button.getText().equals(context.getResources().getString(R.string.allTasks))){

                listTasks.clear();
                listTasks.addAll(data.getEntriesOrderedByDate());
                header.setText(context.getResources().getString(R.string.allTasks));//set header text
                //initialize adapter
                if (allTasksAdapter == null) {
                    allTasksAdapter = new AllTaskListAdapter(context, data, listTasks);
                    recyclerView.setAdapter(allTasksAdapter);//set adapter
                } else{
                    allTasksAdapter.notifyDataSetChanged();
                }
            } else if (holder.button.getText().equals(context.getResources().getString(R.string.noList))) {

                listTasks.clear();
                listTasks.addAll(data.getNoList());
                header.setText(context.getResources().getString(R.string.noList));//set header text
                //initialize adapter
                if (noListAdapter == null) {
                    noListAdapter = new AllTaskListAdapter(context, data, listTasks);
                    recyclerView.setAdapter(noListAdapter);//set adapter
                }else{
                    noListAdapter.notifyDataSetChanged();
                }

            } else {

                String list = holder.button.getText().toString();//get list name
                header.setText(list);//set header text

                listTasks.clear();

                listTasks.addAll(data.getList(list));
                //initialize adapter

                if(listsTaskListAdapter==null){
                    listsTaskListAdapter = new ListTaskListAdapter(context, data, listTasks);
                    recyclerView.setAdapter(listsTaskListAdapter);//set adapter
                    ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(listsTaskListAdapter);


                    ItemTouchHelper iTouchHelper = new ItemTouchHelper(callback);

                    iTouchHelper.attachToRecyclerView(recyclerView);

                }
                else{
                    listsTaskListAdapter.notifyDataSetChanged();
                }



            }
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

        });

        return convertView;
    }

    }
