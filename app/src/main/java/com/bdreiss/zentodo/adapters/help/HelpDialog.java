package com.bdreiss.zentodo.adapters.help;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bdreiss.zentodo.R;

public class HelpDialog extends Dialog{

    private String text;

    private Context context;

    public HelpDialog(@NonNull Context context, String text) {
        super(context);
        this.text = text;
        this.context = context;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.WHITE));

        setOnDismissListener(dialogInterface -> {
            //nothing;
        });

        TextView textView = new TextView(context);


        textView.setText(text);

        addContentView(textView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        show();
        textView.setOnClickListener(v -> dismiss());

    }

}
