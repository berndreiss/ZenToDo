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
    private final ListView listView;//ListView to show lists (recyclerView is disabled before choosing)
    private final RecyclerView recyclerView;//RecyclerView for showing tasks of list chosen (listView gets disabled)
    private final ArrayList<String> lists;//Dynamically generated Array of all lists in data
    private final Data data;//Database
    private final TextView header;//header of ListView. setVisibility=GONE by default and =VISIBLE when recyclerView is shown
    private final ArrayList<Entry> listTasks = new ArrayList<>();//ArrayList that serves as a container for tasks that are in the list that has been chosen

    ListTaskListAdapter listsTaskListAdapter;//adapter for items in lists
    AllTaskListAdapter allTasksAdapter;//adapter for showing all tasks (items can't be moved and do not get removed when list is changed)
    AllTaskListAdapter noListAdapter;//adapter for showing all tasks (items can't be moved and do not get removed when list is changed) TODO implement own adapter for removing items when list is changed

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

    //get View for ListView
    @SuppressLint({"InflateParams", "NotifyDataSetChanged"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //holder for Views
        final ViewHolder holder;

        if(convertView==null){

            //create holder
            holder = new ListsListAdapter.ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //set row layout
            convertView = inflater.inflate(R.layout.lists_row, null, true);

            //assign button for selecting list
            holder.button = convertView.findViewById(R.id.button_lists_row);

            //TODO Do I need this?
            convertView.setTag(holder);

        }else{
            //TODO Do I need this?
            holder = (ListsListAdapter.ViewHolder)convertView.getTag();
        }

        //set Button-text to list name
        holder.button.setText(lists.get(position));

        //if clicked assign ListView TaskListAdapter with tasks associated to list
        holder.button.setOnClickListener(view -> {

            //set header with list name visible
            header.setVisibility(View.VISIBLE);

            //set RecyclerView VISIBLE and ListView GONE
            recyclerView.setVisibility(View.VISIBLE);

            listView.setVisibility(View.GONE);
            //fill ListView with all tasks or according list
            if (holder.button.getText().equals(context.getResources().getString(R.string.allTasks))){

                //clear ArrayList for list, add all tasks from data and notify adapter (in case they have been altered in another layout)
                listTasks.clear();
                listTasks.addAll(data.getEntriesOrderedByDate());

                //set header text
                header.setText(context.getResources().getString(R.string.allTasks));

                //initialize adapter if it is null, notifyDataSetChanged otherwise
                if (allTasksAdapter == null) {

                    //initialize and set adapter
                    allTasksAdapter = new AllTaskListAdapter(context, data, listTasks);
                    recyclerView.setAdapter(allTasksAdapter);//set adapter

                } else{

                    allTasksAdapter.notifyDataSetChanged();

                }

            } else if (holder.button.getText().equals(context.getResources().getString(R.string.noList))) {

                //clear ArrayList for list, add tasks without a list from data and notify adapter (in case they have been altered in another layout)
                listTasks.clear();
                listTasks.addAll(data.getNoList());

                //set header text
                header.setText(context.getResources().getString(R.string.noList));

                //initialize adapter if it is null, notifyDataSetChanged otherwise
                if (noListAdapter == null) {

                    //initialize and set adapter
                    noListAdapter = new AllTaskListAdapter(context, data, listTasks);
                    recyclerView.setAdapter(noListAdapter);

                }else{

                    noListAdapter.notifyDataSetChanged();

                }

            } else {

                //get list name
                String list = holder.button.getText().toString();

                //set header text
                header.setText(list);

                //clear ArrayList for list, add current tasks from data and notify adapter (in case they have been altered in another layout)
                listTasks.clear();
                listTasks.addAll(data.getList(list));

                //initialize adapter if it is null, notifyDataSetChanged otherwise
                if(listsTaskListAdapter==null){
                    //initialize and set adapter
                    listsTaskListAdapter = new ListTaskListAdapter(context, data, listTasks);
                    recyclerView.setAdapter(listsTaskListAdapter);

                    //allows items to be moved and reordered in RecyclerView
                    ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(listsTaskListAdapter);

                    //create ItemTouchHelper and assign to RecyclerView
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
