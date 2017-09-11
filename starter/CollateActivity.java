package com.parse.starter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.Collation.CollatedIdea;
import com.parse.starter.Collation.Collation;
import com.parse.starter.Collation.CollationAdapter;
import com.parse.starter.Collation.SavedList;
import com.parse.starter.Messaging.MessageContainer;
import com.parse.starter.Messaging.MessagingAdapter;
import com.parse.starter.Messaging.PopUp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollateActivity extends AppCompatActivity {

    Collation collation;
    TextView listTitle;
    CollationAdapter arrayAdapter;
    ListView listView;
    String ideaTreeId;
    String mainTitle;
    boolean voteDisabled;
    String mainAuthor;
    Intent intent;
    boolean userInChat;
    String commentId;
    PopUp popUp;
    ArrayList<MessageContainer> messageContainers;
    MessagingAdapter adapter;
    SpannableString initialTag;
    boolean replyEnabled;
    String replyTotal;
    String reply;
    int index = -1;
    MessageContainer messageContainer;
    CollatedIdea collatedIdea;
    ArrayList<CollatedIdea> topIdeas;
    MessageContainer replyContainer;
    TextView tv;
    String msg;
    String mainID;
    int replaceIndex = -1;
    List<String> replyIDs;
    boolean replyComplete;
    ArrayList<MessageContainer> mainContainers;
    Map<String,MessageContainer> replyContainers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collate);
        intent = getIntent();
        listView = (ListView)findViewById(R.id.listViewCollate);
        listTitle = (TextView)findViewById(R.id.listTitle);
        listTitle.setText("Top Collated Ideas from: "+intent.getStringExtra("title"));
        ideaTreeId = intent.getStringExtra("id");

        mainTitle = intent.getStringExtra("title");
        mainAuthor = intent.getStringExtra("author");
        if (intent.getStringArrayListExtra("savedIds")==null) {
            queryTopIdeas(ideaTreeId);
        } else {
            Log.i("CollateActivity", "list sent through");
            queryCollatedList();
        }
    }

    private void queryCollatedList() {
        final ArrayList<String> topIdeaIDs = intent.getStringArrayListExtra("savedIds");
        Log.i("queryCollatedList", topIdeaIDs.toString());
        collation = new Collation();
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Idea");
                query.whereContainedIn("randomId", topIdeaIDs);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            if (!objects.isEmpty()) {
                                Log.i("topIdeaIDs", "size " + Integer.toString(collation.topIdeaObjects.size()));
                                collation.topIdeaObjects.addAll(objects);
                                collation.parseToIdeas();
                                activateAdapter();
                            } else {
                                Log.i("queryCollatedList", "objects empty");
                            }
                        } else {
                            Log.i("queryCollatedList", e.getMessage());
                        }
                    }
                });
            }




    private void queryTopIdeas(final String id){
        topIdeas = new ArrayList<>();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Idea");
        query.whereEqualTo("IdeaTree", id);
        query.orderByDescending("voteTotal");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null) {
                    if (!objects.isEmpty()) {
                        Log.i("queryTopIdeas", objects.get(0).getNumber("voteTotal").toString());
                        activateAdapter();
                        collation = new Collation(objects.get(0).getNumber("voteTotal").intValue(), id,
                                CollateActivity.this);
                        Log.i("calculateLimit", Float.toString(collation.calculateLimit()));
                        float voteLimit = collation.calculateLimit();
                        for (int i = 0; i < objects.size(); i++) {
                            if (objects.get(i).getNumber("voteTotal").intValue() >= voteLimit
                                    && objects.get(i).getNumber("voteTotal").intValue() != 0) {
                                collation.topIdeaObjects.add(objects.get(i));
                                Log.i("topIdeaObjects", collation.topIdeaObjects.get(i)
                                        .getString("randomId"));
                            }
                        }
                    }

                    for (final ParseObject topIdeaObj : collation.topIdeaObjects) {
                        final CollatedIdea collatedIdea = new CollatedIdea(topIdeaObj.getString("Title"),
                                topIdeaObj.getString("Description"), topIdeaObj.getString("Author"));
                        collatedIdea.setVotes(topIdeaObj.getNumber("voteTotal").intValue());
                        collatedIdea.setId(topIdeaObj.getString("randomId"));
                        topIdeas.add(collatedIdea);
                        arrayAdapter.notifyDataSetChanged();

                        //THIS  QUERY IS JUST A HACK TO DELAY THE THREAD AS FOR SOME REASON GETVIEW IS BEING CALLED
                        //AFTER collatedIdea.upVote.setBackgroundColor THEREFORE NPE IS OCCURING.
                        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Test");
                        query.whereEqualTo("objectId", "MBmQgGhHBK");
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                List<String> voteUp = topIdeaObj.getList("voteConUp");
                                List<String> voteDwn = topIdeaObj.getList("voteConDwn");
                                if (voteUp != null) {
                                    if (voteUp.contains(ParseUser.getCurrentUser().getUsername())) {
                                        Log.i("getView", "after notifyDataSetChanged");
                                        collatedIdea.upVote.setBackgroundColor((Color.parseColor("#0000ff")));
                                        collatedIdea.upActive = true;
                                    }
                                }
                                if (voteDwn != null) {
                                    if (voteDwn.contains(ParseUser.getCurrentUser().getUsername())) {
                                        collatedIdea.dwnVote.setBackgroundColor((Color.parseColor("#0000ff")));
                                        collatedIdea.dwnActive = true;
                                    }
                                }
                            }
                        });
                    }
                }}
        });
    }


    private void activateAdapter(){
        Log.i("activateAdapter", "red");
        arrayAdapter = new CollationAdapter(this,R.layout.collated_idea,topIdeas);
        Log.i("activateAdapter", "green");
        listView.setAdapter(arrayAdapter);
    }

    public void saveList(View view){
        collation.extractIds();
        Log.i("extractIds",collation.topIdeasIds.toString());
        SavedList savedList = new SavedList(collation.topIdeasIds,ideaTreeId);
        savedList.setMainTitle(mainTitle);
        savedList.setAuthor(mainAuthor);
        Gson gson = new Gson();
        String savedIDJSON = gson.toJson(savedList);

        if ( ParseUser.getCurrentUser().getList("CollatedLists")==null) {
            ParseUser.getCurrentUser().put("CollatedLists",new ArrayList<>());
        }
        ParseUser.getCurrentUser().add("CollatedLists",savedIDJSON);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Toast.makeText(CollateActivity.this, "List successfully added to your lists " +
                            "within your library", Toast.LENGTH_LONG).show();
                }
            }
        });
        }

    public void onClickupVoteCollated(View view){
        collatedIdea = (CollatedIdea)view.getTag();
        if (!collatedIdea.dwnActive) {
            voteClick(1, view);
        }
    }

    public void onClickdownVoteCollated(View view){
        collatedIdea = (CollatedIdea)view.getTag();
        if (!collatedIdea.upActive) {
            voteClick(-1, view);
        }
    }

    private void voteClick(final int n, final View view){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Idea");
        query.whereEqualTo("randomId", collatedIdea.id);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (!objects.isEmpty()){
                        String voteColumn;
                        final String oppositeKey;
                        final List<String> oppositeColumn;
                        if (n>0){
                            voteColumn = "voteConUp";
                            collatedIdea.resetButtonColors();
                            //view.setBackgroundColor((Color.parseColor("#0000ff")));
                            oppositeColumn = objects.get(0).getList("voteConDwn");
                            oppositeKey = "voteConDwn";
                        } else {
                            voteColumn = "voteConDwn";
                            collatedIdea.resetButtonColors();
                            //view.setBackgroundColor((Color.parseColor("#0000ff")));
                            oppositeColumn = objects.get(0).getList("voteConUp");
                            oppositeKey = "voteConUp";
                        }
                        collatedIdea.disableButton(voteColumn);
                        if(objects.get(0).getList(voteColumn)!= null) {
                            List<String> voteContributors = objects.get(0).getList(voteColumn);
                            if (!voteContributors.contains(ParseUser.getCurrentUser().getUsername())) {
                                collatedIdea.setVotes(collatedIdea.votes + n);
                                objects.get(0).put("voteTotal", collatedIdea.votes);
                                voteContributors.add(ParseUser.getCurrentUser().getUsername());
                                objects.get(0).put(voteColumn, voteContributors);
                                objects.get(0).saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            if (oppositeColumn != null) {
                                                if (oppositeColumn.contains(ParseUser.getCurrentUser().getUsername())) {

                                                    oppositeColumn.remove(oppositeColumn.indexOf(ParseUser.getCurrentUser().
                                                            getUsername()));
                                                    objects.get(0).put(oppositeKey, oppositeColumn);
                                                    objects.get(0).saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if (e==null){
                                                                Log.i("commentVoteClick","saved");
                                                            } else {
                                                                Log.i("commentVoteClick",e.getMessage());
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        } else {
                                            //     messageContainer.vote.setTypeface(null, Typeface.BOLD);
                                            //  messageContainer.vote.setTextSize(16f);
                                            Log.i("vote", e.getMessage());
                                        }
                                    }
                                });
                            }else {
                                view.setBackgroundResource(android.R.drawable.btn_default);
                                voteContributors.remove(voteContributors.indexOf(ParseUser.getCurrentUser().
                                        getUsername()));
                                int s = n;
                                if (n>0){
                                    s = -1;
                                } else {
                                    s = 1;
                                }
                                collatedIdea.setVotes(collatedIdea.votes + s);
                                objects.get(0).put("voteTotal", collatedIdea.votes);
                                objects.get(0).put(voteColumn, voteContributors);
                                objects.get(0).saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e==null){
                                            Log.i("commentVoteClick","saved");
                                            collatedIdea.resetButtons();
                                        } else {
                                            Log.i("commentVoteClick",e.getMessage());
                                        }
                                    }
                                });
                            }
                        } else {
                            objects.get(0).put(voteColumn, new ArrayList<>());
                            objects.get(0).add(voteColumn,ParseUser.
                                    getCurrentUser().getUsername());
                            collatedIdea.setVotes(collatedIdea.votes + n);
                            objects.get(0).put("voteTotal",collatedIdea.votes);
                            objects.get(0).saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e==null){
                                        if (oppositeColumn != null) {
                                            Log.i("oppositeColumn","opposite not null");
                                            if (oppositeColumn.contains(ParseUser.getCurrentUser().getUsername())) {
                                                oppositeColumn.remove(oppositeColumn.indexOf(ParseUser.getCurrentUser().
                                                        getUsername()));
                                                objects.get(0).put(oppositeKey, oppositeColumn);
                                                objects.get(0).saveInBackground();
                                            }}
                                    } else {
                                        //   idea.totalVote.setTypeface(null, Typeface.BOLD);
                                        Log.i("vote",e.getMessage());
                                        //  idea.totalVote.setTextSize(16f);
                                    }
                                }
                            });
                        }

                    } else {
                        Log.i("vote","objects empty");
                    }
                } else {
                    Log.i("vote",e.getMessage());
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void commentsOnClick(final View view) {
        Log.i("commentsOnClick","test");
        userInChat = true;
        final CollatedIdea idea = (CollatedIdea) view.getTag();
        commentId = idea.id;
        popUp = new PopUp(CollateActivity.this);
        messageContainers = new ArrayList<MessageContainer>();
        mainContainers = new ArrayList<MessageContainer>();
        replyContainers = new HashMap<>();
        messageContainers.add(new MessageContainer());
        setMessagingAdapter();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
        query.whereEqualTo("randomId", commentId);
        query.orderByDescending("votes");
        query.findInBackground(new FindCallback<ParseObject>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (!objects.isEmpty()) {
                        for (int i =0;i<objects.size();i++) {
                            setMessageContainer(objects.get(i),false);
                        }
                        generateReplies();
                       /// sortReplies();
                    }
                    popUp.activatePopUp();
                    setDismissListener();
                } else {
                    Log.i("commentsclick", e.getMessage());
                }
            }
        });
    }

    private void setMessageContainer(final ParseObject object,boolean isReply) {
        final MessageContainer mContainer = new MessageContainer(
                object.getString("message"), object.getString("Author"));
        mContainer.setId(object.getString("messageId"));
        if (object.getNumber("votes")!=null) {
            mContainer.setVote(object.getNumber("votes").intValue());
        }
        if (object.getString("replyTag") != null) {
            String replyTag = "@"+object.getString("replyTag");
            mContainer.setInitialTag(new SpannableString(replyTag));
            mContainer.stringBuilder();
        }

        if (object.getNumber("replaceIndex")!=null){
            mContainer.setReplaceIndex(object.getNumber("replaceIndex").intValue());
        }

        if (object.getList("replyIDs")!=null){
            List<String> replyIDs = object.getList("replyIDs");
            if (!replyIDs.isEmpty()){
                mContainer.setReplyIDs(replyIDs);
                Log.i("replyIDs", mContainer.replyIDs.toString());
            }
        }

        if (object.getString("Identifier") !=null){
            mContainer.setIdentifier(object.getString("Identifier"));
            if (mContainer.identifier.equals("main")){
                mainContainers.add(mContainer);
            } else {
                replyContainers.put(mContainer.messageId,mContainer);
            }
        }
        Log.i("msgId", object.getString("messageId") + "has been added");


        }

    private void generateReplies() {
        if (!mainContainers.isEmpty()){
            for (int j=0;j<mainContainers.size();j++) {
                messageContainers.add(mainContainers.get(j));
                adapter.notifyDataSetChanged();
                if (messageContainers.size()<5) {
                    queryVotes(mainContainers.get(j));
                }
                Log.i("messageId", mainContainers.get(j).messageId);
                if (mainContainers.get(j).replyIDs!= null) {
                    for (int i = 0; i < mainContainers.get(j).replyIDs.size(); i++) {
                        if (!replyContainers.isEmpty()) {
                            Log.i("generateReplies", mainContainers.get(j).replyIDs.get(i));
                            messageContainers.add(replyContainers.get(mainContainers.get(j).replyIDs.get(i)));
                            adapter.notifyDataSetChanged();
                            if (messageContainers.size()<5) {
                                queryVotes(replyContainers.get(mainContainers.get(j).replyIDs.get(i)));
                            }
                        }
                    }
                }
            }
        }
    }


    public void queryVotes(final MessageContainer mContainer){
        Log.i("mContainer", mContainer.messageId);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
        query.whereEqualTo("messageId", mContainer.messageId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (!objects.isEmpty()){
                        List<String> voteUp =  objects.get(0).getList("voteConUp");
                        List<String> voteDwn=  objects.get(0).getList("voteConDwn");
                        if (voteUp!=null){
                            if (voteUp.contains(ParseUser.getCurrentUser().getUsername())){
                                    Log.i("setButtons", "changing color for upvote button for " + mContainer.messageId);
                                  //  mContainer.upVote.setBackgroundColor((Color.parseColor("#0000ff")));
                                    mContainer.disableButton("voteConUp");

                            }}
                        if (voteDwn!=null){
                            if (voteDwn.contains(ParseUser.getCurrentUser().getUsername())){
                                    Log.i("setButtons", "changing color for dwnvote button for " + mContainer.messageId);
                                  //  mContainer.dwnVote.setBackgroundColor((Color.parseColor("#0000ff")));
                                    mContainer.disableButton("voteConDwn");
                            }
                        }

                    }

                }
            }
        });
    }

    private void sortReplies() {
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
                query.whereEqualTo("Identifier","reply");
                query.orderByAscending("Index");
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            if (!objects.isEmpty()) {
                                for (ParseObject obj : objects) {
                                    Log.i("sortReplies", obj.getNumber("Index").toString());
                                    setMessageContainer(obj,true);
                                }

                            }
                        }
                    }
                });
            }




    private void setDismissListener() {
        popUp.dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialog != null){
                    userInChat = false;
                    Log.i("onDismiss", Boolean.toString(userInChat));
                }
            }
        });
    }

    private void setMessagingAdapter(){
        adapter = new MessagingAdapter(CollateActivity.this
                ,messageContainers);
        adapter.setIdeaTreeId(commentId);
        popUp.listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    public void onClickReply(View view){
        MessageContainer mc = (MessageContainer) view.getTag();
        initialTag = new SpannableString("@"+mc.username);
        index = mc.position+1;
        replyEnabled = true;
        replyContainer = new MessageContainer();
        replyContainer.setReplaceIndex(index);
        messageContainers.add(index,replyContainer);
        adapter.setIndex(index);
        adapter.setReplyTag(stringBuilder(initialTag));
        adapter.notifyDataSetChanged();
        mainID = mc.messageId;
        Log.i("onClickReply", stringBuilder(initialTag).toString());

    }

    public void onClickSend(View view){
        EditText et = (EditText)view.getTag();
        msg = et.getText().toString();
        addMessageToParse();
    }

    public void onClickSendIndent(View view){
        tv = (TextView) view.getTag();
        replyTotal = tv.getText().toString();
        reply = obtainText(replyTotal,initialTag.length());
        replyEnabled = true;
        addMessageToParse();

    }

    private String obtainText(String s, int start) {
        return s.substring(start,s.length());
    }

    private void adjustIndex(MessageContainer mContainer) {
        int startingIndex = messageContainers.indexOf(mContainer) +1;
        if (startingIndex<messageContainers.size()) {
            for (int i = startingIndex; i < messageContainers.size(); i++) {
                if (messageContainers.get(i).identifier.equals("reply")) {
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
                    query.whereEqualTo("messageId", messageContainers.get(i).messageId);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                if (!objects.isEmpty()) {
                                    if (objects.get(0).getNumber("Index") != null) {
                                        Log.i("adjustIndex", objects.get(0).getString("messageId"));
                                        int objectIndex = objects.get(0).getNumber("Index").intValue();
                                        objects.get(0).put("Index", objectIndex + 1);
                                        objects.get(0).saveInBackground();
                                    }

                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private void addMessageToParse() {
        final String msgId = new IDGenerator().generateID();
        MessageContainer mContainer;

        if (replyEnabled){
            Log.i("giri", "replyTotal "+replyTotal);
            Log.i("giri", "initialTag "+initialTag.toString());
            if (!replyTotal.equals(initialTag.toString())){
                Log.i("giri", " but setting it anyway?");
                mContainer = new MessageContainer(reply, ParseUser.getCurrentUser()
                        .getUsername());
                mContainer.setId(msgId);
                mContainer.setInitialTag(initialTag);
                mContainer.stringBuilder();
                mContainer.setIdentifier("reply");
                replaceIndex = messageContainers.indexOf(replyContainer);
                mContainer.setReplaceIndex(replaceIndex);
                adapter.setReplyEnabled(true);
                Log.i("replaceIndex", Integer.toString(replaceIndex));
                messageContainers.remove(replaceIndex);
                Log.i("size", "after " + Integer.toString(messageContainers.size()));
                messageContainers.add(replaceIndex, mContainer);
                addToParse(mContainer,msgId);
            } else {
                adapter.notifyDataSetChanged();
                replyEnabled = false;
                messageContainers.remove(replyContainer.replaceIndex);
            }
        } else {
            Log.i("reply", "reply disenabled");
            if (!msg.equals("")) {
                mContainer = new MessageContainer(msg, ParseUser.getCurrentUser()
                        .getUsername());
                mContainer.setIdentifier("main");
                mContainer.setId(msgId);
                messageContainers.add(1,mContainer);
                Log.i("mContainer", mContainer.getMessage());
                addToParse(mContainer,msgId);
            }
        }

        Log.i("addMessageToParse",Integer.toString(messageContainers.size()));


    }

    private void addToParse(final MessageContainer mContainer, final String msgId) {
        adapter.notifyDataSetChanged();
        ParseObject parseObject = new ParseObject("IdeaComments");
        parseObject.put("randomId", commentId);
        parseObject.put("messageId", msgId);
        parseObject.put("update", new TimeSetter().presentTime);
        if (replyEnabled) {
            //TODO once youve sorted out the profile inbox, need to set a system whereby
            //TODO repliers that are saved in "replyTag" on parse need to be queried and sent
            //TODO an alert that such and such has replied.
            parseObject.put("replyTag", obtainText(initialTag.toString(),1));
            Log.i("put", "e");
            parseObject.put("message", reply);
            parseObject.put("Identifier","reply");
            parseObject.put("replaceIndex",mContainer.replaceIndex);
            replyEnabled = false;
            parseObject.put("Author", ParseUser.getCurrentUser().getUsername());
            parseObject.put("Index",messageContainers.indexOf(mContainer));
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null){
                        adjustIndex(mContainer);
                        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
                        query.whereEqualTo("messageId",mainID);
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                if (e==null){
                                    if (!objects.isEmpty()){
                                        replyIDs = objects.get(0).getList("replyIDs");
                                        if (replyIDs== null){
                                            replyIDs = new ArrayList<String>();
                                            objects.get(0).put("replyIDs",replyIDs);
                                        }
                                        replyIDs.add(msgId);
                                        objects.get(0).put("replyIDs", replyIDs);
                                        objects.get(0).saveInBackground();
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else {
            adjustIndex(mContainer);
            parseObject.put("Identifier","main");
            parseObject.put("message", msg);
            parseObject.put("Author", ParseUser.getCurrentUser().getUsername());
            parseObject.saveInBackground();
        }


    }

    private SpannableStringBuilder stringBuilder(SpannableString text){
        SpannableStringBuilder sp = new SpannableStringBuilder();
        text.setSpan(new ForegroundColorSpan(Color.parseColor("#335ce5")),0, text.length(),0);
        sp.append(text);
        return sp;
    }


    public void onClickUpVoteMsg(View view){
            Log.i("onClickUpVoteMsg", "click");
            messageContainer = (MessageContainer) view.getTag();
            if (!messageContainer.dwnActive) {
                messageContainer.resetButtons();
                commentVoteClick(1, view);
            }
        }

    public void onClickDownVoteMsg(View view){
            messageContainer = (MessageContainer) view.getTag();
            Log.i("onClickUpVoteMsg", Boolean.toString(messageContainer.upActive));
            if (!messageContainer.upActive) {
                messageContainer.resetButtons();
                commentVoteClick(-1, view);
            }
        }


  private void commentVoteClick(final int n, final View view){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
        Log.i("messageId",messageContainer.messageId);
        query.whereEqualTo("messageId", messageContainer.messageId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (!objects.isEmpty()){
                        String voteColumn;
                        final String oppositeKey;
                        final List<String> oppositeColumn;
                        if (n>0){
                            voteColumn = "voteConUp";
                            messageContainer.resetButtonColors();
                           // view.setBackgroundColor((Color.parseColor("#0000ff")));
                            oppositeColumn = objects.get(0).getList("voteConDwn");
                            oppositeKey = "voteConDwn";
                        } else {
                            voteColumn = "voteConDwn";
                            messageContainer.resetButtonColors();
                          //  view.setBackgroundColor((Color.parseColor("#0000ff")));
                            oppositeColumn = objects.get(0).getList("voteConUp");
                            oppositeKey = "voteConUp";
                        }
                        messageContainer.disableButton(voteColumn);
                        if(objects.get(0).getList(voteColumn)!= null) {
                            List<String> voteContributors = objects.get(0).getList(voteColumn);
                            if (!voteContributors.contains(ParseUser.getCurrentUser().getUsername())) {
                                messageContainer.setVote(messageContainer.vote + n);
                                objects.get(0).put("votes", messageContainer.vote);
                                voteContributors.add(ParseUser.getCurrentUser().getUsername());
                                objects.get(0).put(voteColumn, voteContributors);
                                objects.get(0).saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            if (oppositeColumn != null) {
                                                if (oppositeColumn.contains(ParseUser.getCurrentUser().getUsername())) {
                                                    oppositeColumn.remove(oppositeColumn.indexOf(ParseUser.getCurrentUser().
                                                            getUsername()));
                                                    objects.get(0).put(oppositeKey, oppositeColumn);
                                                    objects.get(0).saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if (e==null){
                                                                Log.i("commentVoteClick","saved");
                                                            } else {
                                                                Log.i("commentVoteClick",e.getMessage());
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        } else {
                                       //     messageContainer.vote.setTypeface(null, Typeface.BOLD);
                                          //  messageContainer.vote.setTextSize(16f);
                                            Log.i("vote", e.getMessage());
                                        }
                                    }
                                });
                            }else {
                                view.setBackgroundResource(android.R.drawable.btn_default);
                                voteContributors.remove(voteContributors.indexOf(ParseUser.getCurrentUser().
                                        getUsername()));
                                int s = n;
                                if (n>0){
                                    s = -1;
                                } else {
                                    s = 1;
                                }
                                messageContainer.setVote(messageContainer.vote + s);
                                objects.get(0).put("votes", messageContainer.vote);
                                objects.get(0).put(voteColumn, voteContributors);
                                objects.get(0).saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e==null){
                                            Log.i("commentVoteClick","saved");

                                        } else {
                                            Log.i("commentVoteClick",e.getMessage());
                                        }
                                    }
                                });
                            }
                        } else {
                            objects.get(0).put(voteColumn, new ArrayList<>());
                            objects.get(0).add(voteColumn,ParseUser.
                                    getCurrentUser().getUsername());
                            messageContainer.setVote(messageContainer.vote + n);
                            objects.get(0).put("votes",messageContainer.vote);
                            objects.get(0).saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e==null){
                                        if (oppositeColumn != null) {
                                            Log.i("oppositeColumn","opposite not null");
                                        if (oppositeColumn.contains(ParseUser.getCurrentUser().getUsername())) {
                                            oppositeColumn.remove(oppositeColumn.indexOf(ParseUser.getCurrentUser().
                                                    getUsername()));
                                            objects.get(0).put(oppositeKey, oppositeColumn);
                                            objects.get(0).saveInBackground();
                                        }}
                                    } else {
                                     //   idea.totalVote.setTypeface(null, Typeface.BOLD);
                                        Log.i("vote",e.getMessage());
                                      //  idea.totalVote.setTextSize(16f);
                                    }
                                }
                            });
                        }

                    } else {
                        Log.i("vote","objects empty");
                    }

                } else {
                    Log.i("vote",e.getMessage());
                }
                voteDisabled = false;
            }
        });
    }



    }


