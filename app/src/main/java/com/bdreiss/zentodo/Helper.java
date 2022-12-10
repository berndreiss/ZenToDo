package com.bdreiss.zentodo;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;

import org.xml.sax.XMLReader;


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


            textView.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY, s -> {

                int id = context.getResources().getIdentifier(s,"drawable",context.getPackageName());
                @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = context.getResources().getDrawable(id);
                drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                return drawable;
            }
            , (b, s, editable, xmlReader) -> {}));

            textView.setPadding(50,50,50,50);

            textView.setTextSize(15);

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
        String help = "<p><b>This is Focus mode.</b></p>" +
                "<p>All tasks you picked are shown here.</p>" +

                "<p>Tasks can still be <b>edited</b> via their menu:</p>" +

                "<p><img src = \"focus\"> <b>&ensp;Focus Button</b><br><br>" +
                "The task is being removed from this list and the reminder date is set for today.</p>" +

                "<p><img src = \"ic_action_edit\"><b>&ensp;Edit Button</b><br><br>" +
                "Edit the text of the task.</p>" +

                "<p><img src = \"ic_action_calendar\"><b>&ensp;Reminder date Button</b><br><br>" +
                "A reminder date is set, the task will disappear and you will be reminded on the date given.</p>" +

                "<p><img src = \"ic_action_recurrence\"><b>&ensp;Repeating Button</b><br><br>" +
                "You can make tasks repeating. A new reminder date will be set when the task is ticked off.</p>" +

                "<p><img src = \"ic_action_list\"><b>&ensp;Lists button</b><br><br>" +
                "You can assign tasks to a list. The task is then moved to the list. <b>Tasks in lists without a reminder date will NOT be shown in Pick.</b>" +
                "This way you can have collections of items, that are not strictly todos (i.e. list of movies to watch, list of books to read etc.).</p>";

        return new HelpListener(context, help);
    }

    public static HelpListener getDropListener(Context context){
        String help = "<p><b>This is Drop mode.</b></p>" +
                "<p>You can drop todos here and pick them later.</p>" +

                "<p>Tasks are <b>edited</b> via their menu:</p>" +

                "<p><img src = \"focus\"> <b>&ensp;Focus Button</b><br><br>" +
                "The task is being directly sent to Focus.</p>" +

                "<p><img src = \"ic_action_edit\"><b>&ensp;Edit Button</b><br><br>" +
                "Edit the text of the task.</p>" +

                "<p><img src = \"ic_action_calendar\"><b>&ensp;Reminder date Button</b><br><br>" +
                "It is possible to set a reminder date." +
                "The task will then disappear and you will be reminded on the date set.</p>" +

                "<p><img src = \"ic_action_recurrence\"><b>&ensp;Repeating Button</b><br><br>" +
                "You can make tasks repeating. A new reminder date will be set when the task is ticked off.</p>" +

                "<p><img src = \"ic_action_list\"><b>&ensp;Lists button</b><br><br>" +
                "You can assign tasks to a list. The task is then moved to the list. <b>Tasks in lists without a reminder date will NOT be shown in Pick.</b>" +
                "This way you can have collections of items, that are not strictly todos (i.e. list of movies to watch, list of books to read etc.).</p>";

        return new HelpListener(context, help);
    }

    public static HelpListener getListListener(Context context) {
        return new HelpListener(context, "LIST");
    }




}
