package com.parse.starter.Collation;

import android.content.Context;
import android.os.Build;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.TextView;


import com.parse.starter.R;

import java.util.ArrayList;


public class CollationAdapter extends ArrayAdapter<CollatedIdea> {

    Context context;
    ArrayList<CollatedIdea> collatedIdeas;
    LayoutInflater inflater;
    int resource;

    public CollationAdapter(@NonNull Context context, @LayoutRes int resource,
                            @NonNull ArrayList<CollatedIdea> objects) {
        super(context, resource,objects);
        this.collatedIdeas = objects;
        this.context = context;
    }

    public class ViewHolder{
        TextView description;
        TextView author;
        TextView title;
        TextView votes;
        Button upVote;
        Button downVote;
        Button chat;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CollatedIdea collatedIdea = (CollatedIdea) getItem(position);
        CollationAdapter.ViewHolder viewHolder;

        if (convertView == null){
            viewHolder = new CollationAdapter.ViewHolder();
            inflater = LayoutInflater.from(context);
            resource = R.layout.collated_idea;
            convertView = inflater.inflate(resource, parent, false);
            viewHolder.title = (TextView)convertView.findViewById(R.id.collatedTitle);
            viewHolder.description = (TextView)convertView.findViewById(R.id.collatedDescription);
            viewHolder.author = (TextView)convertView.findViewById(R.id.collatedAuthor);
            viewHolder.votes = (TextView)convertView.findViewById(R.id.collatedVotes);
            viewHolder.upVote = (Button) convertView.findViewById(R.id.upVoteCollated);
            viewHolder.downVote = (Button) convertView.findViewById(R.id.downVoteCollated);
            viewHolder.chat = (Button) convertView.findViewById(R.id.chatCollated);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CollationAdapter.ViewHolder)convertView.getTag();
        }

        viewHolder.title.setText(collatedIdea.title);
        viewHolder.description.setText(collatedIdea.description);
        viewHolder.author.setText(collatedIdea.author);
        viewHolder.upVote.setTag(collatedIdea);
        viewHolder.downVote.setTag(collatedIdea);
        viewHolder.votes.setText("+"+ collatedIdea.votes.toString());
        viewHolder.chat.setTag(collatedIdea);
        Log.i("getView", "within get view");
        collatedIdea.setVoteView(viewHolder.votes);
        collatedIdea.setButtons(viewHolder.upVote,viewHolder.downVote);
        return convertView;
    }
}
