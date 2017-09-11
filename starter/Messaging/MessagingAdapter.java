package com.parse.starter.Messaging;

import android.content.Context;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.starter.R;

import java.util.ArrayList;
import java.util.List;



public class MessagingAdapter extends ArrayAdapter<MessageContainer> {
    ArrayList<MessageContainer> dataSet;
    Context mContext;
    String ideaTreeId = "";
    public EditText reply;
    ViewCache viewHolder;
    LayoutInflater inflater;
    MessageContainer messageContainer;
    int index = -1;
    String replyTag;
    TextView send;
    List<Integer>  replaceIndices;
    boolean replyEnabled;



    public MessagingAdapter(@NonNull Context context,
                         ArrayList<MessageContainer> data) {
        super(context, R.layout.msg_container_redo,data);
        this.dataSet = data;
        Log.i("authorsize",Integer.toString(dataSet.size()));
        mContext = context;

//TODO perhaps delete this if not using
        //obtainReplaceIndices();
    }

    //TODO perhaps delete this if not using
    private void obtainReplaceIndices() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("CommentIndices");
        query.whereEqualTo("IdeaTreeId", ideaTreeId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (!objects.isEmpty()){
                        Log.i("obtainReplaceIndices", "objects !empty");
                        replaceIndices = objects.get(0).getList("ReplaceIndices");
                    } else {
                        Log.i("obtainReplaceIndices", "objects empty");
                        replaceIndices = new ArrayList<Integer>();
                    }
                } else {
                    Log.i("obtainReplaceIndices", e.getMessage());
                }
            }
        });
    }

    public class ViewCache{
        public TextView author;
        TextView message;
        ImageView profilePic;
        TextView replyButton;
        Button upVote;
        Button downVote;
        TextView votes;
        String id;

    }

    public void setIndex(int i){
        index = i;
    }

    public void setReplyTag(SpannableStringBuilder tag){
        replyTag = tag.toString();
    }

    public void setIdeaTreeId(String id){
        ideaTreeId = id;
    }


    public void setReplyEnabled(boolean b){
        replyEnabled = b;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        messageContainer =  getItem(position);
        messageContainer.position = position;
        inflater = LayoutInflater.from(mContext);
        Log.i("reply","position" + Integer.toString(position));
        Log.i("reply","identifier" + messageContainer.identifier);

        if (messageContainer.identifier.equals("reply")){
            Log.i("reply","reply");
            convertView = inflater.inflate(R.layout.sent_reply_layout,parent,false);
            setItemTag(convertView,parent);
        } else if (messageContainer.identifier.equals("main")){
            convertView = inflater.inflate(R.layout.message_container_reply, parent, false);
            reply = (EditText) convertView.findViewById(R.id.reply);
            send = (TextView)convertView.findViewById(R.id.send);
            send.setTag(reply);
        }

        if (position==0 || position == index){
            viewHolder = new ViewCache();
            if (position==index){
                convertView = inflater.inflate(R.layout.reply_layout,parent,false);
                Log.i("reply","position==index");
                reply = (EditText)convertView.findViewById(R.id.replyIndent);
                send = (TextView)convertView.findViewById(R.id.sendIndent);
                send.setTag(reply);
                reply.setText(replyTag);
                index = -1;
            } else {
                convertView = inflater.inflate(R.layout.message_container_reply, parent, false);
                reply = (EditText) convertView.findViewById(R.id.reply);
                send = (TextView)convertView.findViewById(R.id.send);
                send.setTag(reply);
            }

        } else {

            if (convertView == null) {
                Log.i("reply", "orange");
                convertView = inflater.inflate(com.parse.starter.R.layout.msg_container_redo, parent, false);
                setItemTag(convertView,parent);
            } else {
                if (convertView.findViewById(R.id.username) == null) {
                    Log.i("reply", "yellow");
                    convertView = inflater.inflate(com.parse.starter.R.layout.msg_container_redo, parent, false);
                    setItemTag(convertView,parent);

                } else {
                    viewHolder = (ViewCache) convertView.getTag();
                }

            }
            Log.i("setItemTag", "magenta");
            viewHolder.author.setText(messageContainer.username);
            Log.i("authorAdapter",viewHolder.author.getText().toString());
            viewHolder.replyButton.setTag(messageContainer);
            if (messageContainer.messageBuilder == null) {
                viewHolder.message.setText(messageContainer.message);
            } else {
                viewHolder.message.setText(messageContainer.messageBuilder);
                Log.i("messageBuilder","test");
            }
        }
        Log.i("setButtons", "reseting colors");
        //queryVotes();
        Log.i("zxc","finished");
        return convertView;
    }


    private void setItemTag(View convertView,ViewGroup parent){
        Log.i("setItemTag", "blue");
        viewHolder = new ViewCache();
        viewHolder.author = (TextView) convertView.findViewById(R.id.username);
        Log.i("setItemTag", "green");
        viewHolder.message = (TextView) convertView.findViewById(R.id.message);
        viewHolder.replyButton = (TextView) convertView.findViewById(R.id.reply);
        viewHolder.upVote = (Button) convertView.findViewById(R.id.upVoteMsg);
        viewHolder.downVote = (Button) convertView.findViewById(R.id.downVoteMsg);
        viewHolder.votes = (TextView) convertView.findViewById(R.id.votesMsg);
        messageContainer.setVoteView(viewHolder.votes);
        viewHolder.votes.setText(Integer.toString(messageContainer.vote));
        Log.i("setMessageContainer", "within get view");
        viewHolder.upVote.setTag(messageContainer);
        viewHolder.downVote.setTag(messageContainer);
        messageContainer.setButtons(viewHolder.upVote,viewHolder.downVote);
        convertView.setTag(viewHolder);
    }

    public void queryVotes(){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
        Log.i("reply", "messageId " + messageContainer.messageId);
        query.whereEqualTo("messageId", messageContainer.messageId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (!objects.isEmpty()){
                        List<String> voteUp =  objects.get(0).getList("voteConUp");
                        List<String> voteDwn=  objects.get(0).getList("voteConDwn");
                        Log.i("reply", "voteUpsize " + Integer.toString(voteUp.size()));

                        if (voteUp!=null ){
                            if (!voteUp.isEmpty()){
                            if (voteUp.contains(ParseUser.getCurrentUser().getUsername())){
                                Log.i("reply", "setting upvote to blue");
                                messageContainer.upVote.setBackgroundColor((Color.parseColor("#0000ff")));
                                messageContainer.upActive = true;
                            }}}
                        if (voteDwn!=null){
                            if (!voteDwn.isEmpty()){
                            if (voteDwn.contains(ParseUser.getCurrentUser().getUsername())){
                                Log.i("reply", "setting dwnvote to blue");
                                messageContainer.dwnVote.setBackgroundColor((Color.parseColor("#0000ff")));
                                messageContainer.dwnActive = true;
                            }
                        }}
                    }

                }
            }
        });
    }
}
