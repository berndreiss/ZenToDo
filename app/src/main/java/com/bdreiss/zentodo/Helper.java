package com.bdreiss.zentodo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;


public class Helper{

    private static class HelpListener implements View.OnClickListener {
        private final Context context;
        private final String text;

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


            textView.setText(HtmlCompat.fromHtml(text,HtmlCompat.FROM_HTML_MODE_LEGACY));

            textView.setPadding(50,50,50,50);

            textView.setTextSize(20);

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
        String help = "<p><b>This is Drop mode.</b></p>" +
                "<p>You can drop todos here and pick them later.</p>" +
                "<p>Tasks are <b>edited</b> via their menu.</p>" +
                "<p><b>Reminder date</b><br>" +
                "It is possible to set a reminder date." +
                "The task will then disappear and you will be reminded on the date set.</p>" +
                "<p><b>Repeating</b><br>" +
                "You can make tasks repeating. The reminder date will be reset when the task is ticked off.</p>" +
                "<p><b>Lists</b><br>" +
                "You can assign tasks to a list. The task is then moved to the list. <b>Tasks in lists without a reminder date will NOT be shown in Pick.</b>" +
                "This way you can have collections of items, that are not strictly todos (i.e. list of movies to watch, list of books to read etc.).</p>";

        return new HelpListener(context, help);
    }

    public static HelpListener getListListener(Context context){
        return new HelpListener(context, "LIST");
    }




}
