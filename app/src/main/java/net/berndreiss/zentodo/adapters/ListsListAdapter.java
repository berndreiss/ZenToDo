package net.berndreiss.zentodo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.berndreiss.zentodo.R;
import net.berndreiss.zentodo.adapters.recyclerViewHelper.CustomItemTouchHelperCallback;
import net.berndreiss.zentodo.Data.DataManager;
import net.berndreiss.zentodo.api.Entry;
import net.berndreiss.zentodo.Data.SQLiteHelper;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 *   Simple ListView adapter that shows all lists in Data as Buttons. onClick it opens a TaskListAdapter in the same ListView
 *   that shows all tasks associated with the list.
 *
 */
public class ListsListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ListView listView;//ListView to show lists (recyclerView is disabled before choosing)
    private final RecyclerView recyclerView;//RecyclerView for showing tasks of list chosen (listView gets disabled)
    private final List<String> lists;//Dynamically generated Array of all lists in data
    private final Header header;//header of ListView. setVisibility=GONE by default and =VISIBLE when recyclerView is shown
    private String headerColor;
    private final String standardColor = "#35ff0000";
    private final List<Entry> listTasks = new ArrayList<>();//ArrayList that serves as a container for tasks that are in the list that has been chosen

    ListTaskListAdapter listsTaskListAdapter;//adapter for items in lists (items can be moved and get removed when list of task is changed)
    AllTaskListAdapter allTasksAdapter;//adapter for showing all tasks (items can't be moved and do not get removed when list of task is changed)
    NoListTaskListAdapter noListAdapter;//adapter for showing all tasks (items can't be moved and do not get removed when list of task is changed) TODO implement own adapter for removing items when list is changed

    private static class ViewHolder{
        private Button button;//Button to choose list
    }

    //class containing all elements of header and setting color choosing dialog
    private class Header{
        LinearLayout layout;
        TextView headerText;
        Button colorButton;

        @SuppressLint("NotifyDataSetChanged")
        Header(Context context, LinearLayout layout, TextView headerText, Button colorButton){
            this.layout = layout;
            this.headerText = headerText;
            this.colorButton = colorButton;

            //set header invisible before list is chosen
            this.layout.setVisibility(View.GONE);
            this.headerText.setVisibility(View.GONE);
            this.colorButton.setVisibility(View.GONE);

            //set color choosing dialog
            this.colorButton.setOnClickListener(view -> ColorPickerDialogBuilder
                    .with(context)
                    .setTitle("Choose color")
                    .initialColor(Color.parseColor(headerColor))
                    //.showAlphaSlider(false)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener(selectedColor -> {
                    })
                    .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {


                        //get selected color and encode it as hex
                        String color = Integer.toHexString(selectedColor);

                        if (color.length() == 6)
                            color = "00" + color;

                        color = "#" + color;

                        headerColor = color;

                        //set header color to chosen color or default if white is chosen
                        if (color.startsWith("ffffff", 3)) {

                            this.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.header_background));

                        } else {

                            this.layout.setBackgroundColor(Color.parseColor(color));

                        }
                        DataManager.editListColor(context, headerText.getText().toString(), color);
                        listsTaskListAdapter.notifyDataSetChanged();

                    })
                    .setNegativeButton("no color", (dialog, which) -> {
                        this.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.header_background));
                        DataManager.editListColor(context, headerText.getText().toString(), ListTaskListAdapter.DEFAULT_COLOR);
                        listsTaskListAdapter.notifyDataSetChanged();

                    })
                    .build()
                    .show());

        }

    }

    public ListsListAdapter(Context context, ListView listView, RecyclerView recyclerView, LinearLayout headerLayout, TextView headerTextView, Button headerButton){
        super(context, R.layout.lists_row, DataManager.getLists(context));
        this.context=context;
        this.lists = DataManager.getLists(context);
        this.listView = listView;
        this.recyclerView = recyclerView;
        recyclerView.setVisibility(View.GONE);

        header = new Header(context, headerLayout, headerTextView, headerButton);

        this.headerColor = standardColor;
    }

    //get View for ListView
    @NonNull
    @SuppressLint({"InflateParams", "NotifyDataSetChanged"})
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

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
            header.layout.setVisibility(View.VISIBLE);
            header.headerText.setVisibility(View.VISIBLE);
            header.colorButton.setVisibility(View.VISIBLE);

            //set RecyclerView VISIBLE and ListView GONE
            recyclerView.setVisibility(View.VISIBLE);

            listView.setVisibility(View.GONE);

            //fill ListView with all tasks or according list
            if (holder.button.getText().equals(context.getResources().getString(R.string.allTasks))){

                //clear ArrayList for list, add all tasks from data and notify adapter (in case they have been altered in another layout)
                listTasks.clear();
                try (SQLiteHelper db = new SQLiteHelper(context)) {
                    listTasks.addAll(db.getEntriesOrderedByDate());
                }

                //set header text
                header.headerText.setText(context.getResources().getString(R.string.allTasks));

                header.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.header_background));

                headerColor = standardColor;

                header.colorButton.setVisibility(View.GONE);

                //initialize adapter if it is null, notifyDataSetChanged otherwise
                if (allTasksAdapter == null) {

                    //initialize and set adapter
                    allTasksAdapter = new AllTaskListAdapter(context, listTasks);
                    recyclerView.setAdapter(allTasksAdapter);//set adapter

                } else{

                    allTasksAdapter.notifyDataSetChanged();

                }

            } else if (holder.button.getText().equals(context.getResources().getString(R.string.noList))) {

                //clear ArrayList for list, add tasks without a list from data and notify adapter (in case they have been altered in another layout)
                listTasks.clear();

                try (SQLiteHelper db = new SQLiteHelper(context)) {
                    listTasks.addAll(db.getNoList());
                }

                //set header text
                header.headerText.setText(context.getResources().getString(R.string.noList));

                header.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.header_background));

                headerColor = standardColor;

                header.colorButton.setVisibility(View.GONE);

                //initialize adapter if it is null, notifyDataSetChanged otherwise
                if (noListAdapter == null) {

                    //initialize and set adapter
                    noListAdapter = new NoListTaskListAdapter(context, listTasks);
                    recyclerView.setAdapter(noListAdapter);

                }else{

                    noListAdapter.notifyDataSetChanged();

                }

            } else {

                //get list name
                String list = holder.button.getText().toString();

                //set header text
                header.headerText.setText(list);

                String color = DataManager.getListColor(context, list);
                if (color == null)
                    color = ListTaskListAdapter.DEFAULT_COLOR;
                //set color of header to default if color is white, set it to color otherwise
                if (color.startsWith("ffffff", 3)) {

                    header.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.header_background));

                    headerColor = standardColor;

                } else{

                    color = DataManager.getListColor(context, list);

                    header.layout.setBackgroundColor(Color.parseColor(color));

                    headerColor = color;

                }

                //clear ArrayList for list, add current tasks from data and notify adapter (in case they have been altered in another layout)
                listTasks.clear();
                try (SQLiteHelper db = new SQLiteHelper(context)) {
                    listTasks.addAll(db.loadList(list));
                }

                //initialize adapter if it is null, notifyDataSetChanged otherwise
                if(listsTaskListAdapter==null){
                    //initialize and set adapter
                    listsTaskListAdapter = new ListTaskListAdapter(context, listTasks);
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

    /**
     * TODO DESCRIBE
     */
    public void setHeaderGone(){
        header.layout.setVisibility(View.GONE);
        header.headerText.setVisibility(View.GONE);
        header.colorButton.setVisibility(View.GONE);
    }

    /**
     *  TODO DESCRIBE
     */
    public void reset(){
        //hide header (will be shown when list is chosen)
        setHeaderGone();

        //clear ArrayList for Lists, add current tasks from data and notify adapter (in case they have been altered in another layout)
        lists.clear();
        lists.addAll(DataManager.getLists(context));
        notifyDataSetChanged();

    }

}
