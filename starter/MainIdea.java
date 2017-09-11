package com.parse.starter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseUser;

public class MainIdea extends ConstraintLayout {
    String title;
    String description;
    String author;
    Context mContext;
    String ideaTreeID;
    int totalVote;



    public MainIdea(Context context,String ideaTreeID) {
        super(context);
        this.ideaTreeID = ideaTreeID;
        this.mContext = context;
        author = ParseUser.getCurrentUser().getUsername();
        description = "Description";
    }

    public MainIdea(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainIdea(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }


}
