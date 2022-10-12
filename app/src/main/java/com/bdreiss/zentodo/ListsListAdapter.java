package com.bdreiss.zentodo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bdreiss.zentodo.dataManipulation.Data;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class ListsListAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> lists;
    private Data data;

    private class ViewHolder{
        private TextView textView;
        private ListView listView;
    }
    public ListsListAdapter(Context context, Data data, ArrayList<String> lists){
        super(context,R.layout.lists_row,lists);
        this.context=context;
        this.data = data;
        this.lists = lists;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView==null){
            holder = new ListsListAdapter.ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.lists_row, null, true);

            holder.textView = (TextView) convertView.findViewById(R.id.text_view_lists_row);
            holder.listView = (ListView) convertView.findViewById(R.id.list_view_lists);

            convertView.setTag(holder);

        }else{
            holder = (ListsListAdapter.ViewHolder)convertView.getTag();
        }

        holder.textView.setText(lists.get(position));

        return convertView;
    }

    }
