package com.parse.starter.Messaging;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.parse.starter.R;

import java.util.List;

public class PopUp extends ConstraintLayout {
    Context context;
    public ListView listView;
    public View popUpView;
    public static final int widthDp = 212;
    public static final int heightDp = 344;
    LayoutInflater layoutInflater;
    public AlertDialog dialog;

    public PopUp(Context context) {
        super(context);
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        popUpView = layoutInflater.inflate(R.layout.messaging, null);
        listView = (ListView) popUpView.findViewById(R.id.commentsSection);
        disableParentScrolling();
    }

    private void disableParentScrolling() {
        listView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    public void activatePopUp() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popUpView);
        dialog = builder.create();

        dialog.show();
    }

}
