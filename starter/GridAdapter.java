package com.parse.starter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class GridAdapter extends ArrayAdapter<MainIdea> {
    Context context;
    LayoutInflater inflater;
    ArrayList<MainIdea> mainideas;
    int resource;

    public GridAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<MainIdea> mainideas) {
        super(context, resource,mainideas);
        this.mainideas = mainideas;
        this.context = context;

    }

    public class ViewHolder{
        TextView description;
        TextView author;
        TextView title;
        ImageView deleteView;
        ImageView bookmark;
        TextView collate;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MainIdea mainIdea = (MainIdea) getItem(position);
        ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            inflater = LayoutInflater.from(context);
            resource = R.layout.mainidea;
            convertView = inflater.inflate(resource, parent, false);
            viewHolder.title = (TextView)convertView.findViewById(R.id.title);
            viewHolder.description = (TextView)convertView.findViewById(R.id.description);
            viewHolder.author = (TextView)convertView.findViewById(R.id.author);
            viewHolder.deleteView = (ImageView) convertView.findViewById(R.id.deleteView);
            viewHolder.bookmark = (ImageView) convertView.findViewById(R.id.bookmark);
            viewHolder.collate = (TextView) convertView.findViewById(R.id.collate);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
            viewHolder.collate.setTag(mainIdea);
            viewHolder.deleteView.setTag(mainIdea);
            viewHolder.bookmark.setTag(mainIdea);
            viewHolder.title.setText(mainIdea.title);
        viewHolder.description.setText(mainIdea.description);
        viewHolder.description.setTag(mainIdea.ideaTreeID);
        viewHolder.author.setText(mainIdea.author);
        viewHolder.author.setTag(mainIdea.ideaTreeID);
        return convertView;
    }

}

