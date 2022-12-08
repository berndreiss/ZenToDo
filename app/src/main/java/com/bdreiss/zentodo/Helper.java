package com.bdreiss.zentodo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bdreiss.zentodo.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Helper{

    private static final String PICK_FILE = "PickHelp";
    private static final String FOCUS_FILE = "FocusHelp";
    private static final String DROP_FILE = "DropHelp";
    private static final String LIST_FILE = "ListHelp";

    private static class HelpListener implements View.OnClickListener {
        private Context context;
        private String text;

        HelpListener (Context context, String text){
            this.context = context;
            this.text = text;
        }

        @Override
        public void onClick(View v){
            Dialog dialog = new Dialog(context);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.WHITE));

            dialog.setOnDismissListener(dialogInterface -> {
                //nothing;
            });

            TextView textView = new TextView(context);


            textView.setText(text);

            dialog.addContentView(textView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            dialog.show();
            textView.setOnClickListener(vw -> dialog.dismiss());

        }
    }

    public static HelpListener getPickListener(Context context){
        return new HelpListener(context, "PICK");
    }

    public static HelpListener getFocusListener(Context context){
        return new HelpListener(context, "FOCUS");
    }

    public static HelpListener getDropListener(Context context){
        return new HelpListener(context, "DROP");
    }

    public static HelpListener getListListener(Context context){
        return new HelpListener(context, "LIST");
    }




}
