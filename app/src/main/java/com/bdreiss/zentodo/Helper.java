package com.bdreiss.zentodo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;


public class Helper{


    private static class HelpListener implements View.OnClickListener {
        private final Context context;
        private final String text;

        HelpListener(Context context, String text) {
            this.context = context;
            this.text = text;
        }

        @Override
        public void onClick(View v) {
            showDialog();

        }


        private void showDialog() {
            Dialog dialog = new Dialog(context);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setOnDismissListener(dialogInterface -> {

            });

            ScrollView scrollView = new ScrollView(context);
            TextView textView = new TextView(context);

            scrollView.addView(textView);

            textView.setTextColor(Color.parseColor("#000000"));

            textView.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY, s -> {

                        @SuppressLint("DiscouragedApi") int id = context.getResources().getIdentifier(s, "drawable", context.getPackageName());
                        Drawable drawable = ContextCompat.getDrawable(context, id);
                        assert drawable != null;
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        return drawable;
                    }
                    , (b, s, editable, xmlReader) -> {
                    }));

            textView.setPadding(50, 50, 50, 50);

            textView.setTextSize(15);

            dialog.addContentView(scrollView, new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            dialog.show();
            textView.setOnClickListener(vw -> dialog.dismiss());
        }
    }

    public static HelpListener getPickListener(Context context){
        String help = "<p><b>PICK MODE</b></p>" +
                "<p>All tasks you dropped and that are due today are shown here." +

                "When you press \"PICK\" all ticked tasks get sent to FOCUS.</p>" +

                "<p>Tasks can still be <b>edited</b> via their menu:</p>" +

                "<p><img src = \"ic_action_delete\"> <b>&ensp;Delete Button</b><br><br>" +
                "The task is deleted.</p>" +

                "<p><img src = \"ic_action_edit\"><b>&ensp;Edit Button</b><br><br>" +
                "Edit the text of the task.</p>" +

                "<p><img src = \"ic_action_calendar\"><b>&ensp;Reminder date Button</b><br><br>" +
                "A reminder date is set and the task will be shown again on the date given.</p>" +

                "<p><img src = \"ic_action_recurrence\"><b>&ensp;Repeat Button</b><br><br>" +
                "You can make tasks repeating. A new reminder date will be set when the task is ticked off.</p>" +

                "<p><img src = \"ic_action_list\"><b>&ensp;LISTS button</b><br><br>" +
                "You can assign tasks to a list. The task is then moved to the list. <b>Tasks in lists without a reminder date will NOT be shown in PICK.</b>" +
                "This way you can have collections of items, that are not strictly todos (i.e. list of movies to watch, list of books to read etc.).</p>";

        return new HelpListener(context, help);
    }

    public static HelpListener getFocusListener(Context context){
        String help = "<p><b>FOCUS MODE</b></p>" +
                "<p>All tasks you picked are shown here.</p>" +

                "<p>Tasks can still be <b>edited</b> via their menu:</p>" +

                "<p><img src = \"focus\"> <b>&ensp;FOCUS Button</b><br><br>" +
                "The task is being removed from this list and the reminder date is set for today.</p>" +

                "<p><img src = \"ic_action_edit\"><b>&ensp;Edit Button</b><br><br>" +
                "Edit the text of the task.</p>" +

                "<p><img src = \"ic_action_calendar\"><b>&ensp;Reminder date Button</b><br><br>" +
                "A reminder date is set, the task will disappear and be show again on the date given.</p>" +

                "<p><img src = \"ic_action_recurrence\"><b>&ensp;Repeat Button</b><br><br>" +
                "You can make tasks repeating. A new reminder date will be set when the task is ticked off.</p>" +

                "<p><img src = \"ic_action_list\"><b>&ensp;LISTS button</b><br><br>" +
                "You can assign tasks to a list. The task is then moved to the list. <b>Tasks in lists without a reminder date will NOT be shown in PICK.</b>" +
                "This way you can have collections of items, that are not strictly todos (i.e. list of movies to watch, list of books to read etc.).</p>";

        return new HelpListener(context, help);
    }

    public static HelpListener getDropListener(Context context){
        String help = "<p><b>DROP MODE</b></p>" +
                "<p>You can drop todos here and pick them later.</p>" +

                "<p>Tasks are <b>edited</b> via their menu:</p>" +

                "<p><img src = \"focus\"> <b>&ensp;FOCUS Button</b><br><br>" +
                "The task is being directly sent to FOCUS.</p>" +

                "<p><img src = \"ic_action_edit\"><b>&ensp;Edit Button</b><br><br>" +
                "Edit the text of the task.</p>" +

                "<p><img src = \"ic_action_calendar\"><b>&ensp;Reminder date Button</b><br><br>" +
                "It is possible to set a reminder date." +
                "The task will then disappear and be shown again on the date set.</p>" +

                "<p><img src = \"ic_action_recurrence\"><b>&ensp;Repeat Button</b><br><br>" +
                "You can make tasks repeating. A new reminder date will be set when the task is ticked off.</p>" +

                "<p><img src = \"ic_action_list\"><b>&ensp;LISTS button</b><br><br>" +
                "You can assign tasks to a list. The task is then moved to the list. <b>Tasks in lists without a reminder date will NOT be shown in PICK.</b>" +
                "This way you can have collections of items, that are not strictly todos (i.e. list of movies to watch, list of books to read etc.).</p>";

        return new HelpListener(context, help);
    }

    public static HelpListener getListListener(Context context) {
        String help = "<p><b>LISTS MODE</b></p>" +
                "<p>If you would like to <b>create a list</b> go to the menu of a task and assign a list. It will then be created and shown here.</p>" +
                "<p>When you remove the last task in a list the <b>list will be deleted</b> too.</p>" +
                "<p><img src = \"ic_action_color\"> When you enter a list you can assign a <b>color</b> to it via the Button shown on the top right next to the name of the list." +
                "Tasks in <b>PICK and FOCUS</b> will then be shown in the same color.</p>" +
                "<p>If a <b>task has no list</b> you can still find them here under \"No list\" where all tasks without a list are shown sorted by their reminder date.</p>" +
                "<p>Alternatively you can find every task in \"ALL TASKS\" where every task is shown sorted by their reminder date.</p>";

        return new HelpListener(context, help);
    }

    public static void showPickHelper(Context context){
        String help = "<p><b>Please categorize all tasks first.</b></p> " +
                "<p><img src = \"ic_action_checkbox\"> Tasks that are ticked are shown in DO NOW and moved to FOCUS.</p>" +
                "<p><img src = \"ic_action_calendar\"> Tasks for which a reminder date is set are shown in DO LATER and will be shown in PICK again on the given date (or if the date is in the past tomorrow).</p>" +
                "<p><img src = \"ic_action_list\"> Tasks without a reminder date and with a list are shown in MOVE TO LIST. These tasks will never be shown again in PICK. You can find them in LISTS.</p>";
        new Helper.HelpListener(context,help).showDialog();

    }

}
