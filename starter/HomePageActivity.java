package com.parse.starter;


import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomePageActivity extends AppCompatActivity {

    public static final float collateLimit = 0.2f;
    public static final float voteRateDuration =3600000;
    public static final float voteThreshhold = 0.8f;
    public int maxVote;
    public float minVote;
    Map<String,Integer> voteMap;
    ArrayList<ParseObject> voteList;
    ArrayList<VoteMap> vms;
    ArrayList<VoteMap> topVotes;
    ArrayList<MainIdea> list;
    GridView gridView;
    TopIdeaAdapter topIdeaAdapter;

    public void libraryOnClick(View view){
        Intent intent = new Intent(HomePageActivity.this, IdeaTreeLibraryActivity.class);
        startActivity(intent);
    }

    public void profileOnClick(View view){
        Log.i("onclick","profile");
    }

    public void networkOnClick(View view){
        Log.i("onclick","network");
    }

    public void settingsOnClick(View view){

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        list = new ArrayList<>();
        gridView=(GridView)findViewById(R.id.gridView);
        topIdeaAdapter = new TopIdeaAdapter(this, R.layout.list_view,list);
        gridView.setAdapter(topIdeaAdapter);
        voteList = new ArrayList<>();

        updatePopularIdeas();
    }


    private void updatePopularIdeas() {
        //TODO amend for top voted and category
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Votes");
        query.whereGreaterThanOrEqualTo("creation",TimeSetter.getTime()-voteRateDuration);
        query.whereLessThanOrEqualTo("creation",TimeSetter.getTime());
        Log.i("creation","start " + Float.toString(TimeSetter.getTime()-voteRateDuration));
        Log.i("creation","end " + Float.toString(TimeSetter.getTime()));

        query.findInBackground(new FindCallback<ParseObject>() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (!objects.isEmpty()) {
                        Log.i("objects", objects.get(0).getString("IdeaTreeId"));
                        for (ParseObject object : objects) {
                            voteList.add(object);
                            //createIdea(object);
                            }
                          Log.i("votelsitsize", Integer.toString(voteList.size()));
                            sortVotes();
                            //calcVoteRate(object.getNumber("voteMainTotal").intValue());
                            setClickListener();
                        } else {
                        Log.i("objects","empty");
                    }
                    }else {
                    Log.i("objects",e.getMessage());
                }

            }
        });
    }

    private void sortVotes() {
        vms = new ArrayList<>();
        ArrayList<String> voteIdHolder = new ArrayList<>();
        for (int i = 0;i<voteList.size();i++){
            if (vms.size()>0) {
               for (int l = 0;l<vms.size();l++) {
                    if (!voteList.get(i).getString("IdeaTreeId").equals(vms.get(l).voteId)) {
                        Log.i("voteList", "test");
                        if (voteIdHolder.size() > 0){
                            Log.i("voteIdHolder", Integer.toString(voteIdHolder.size()));
                            if (!voteIdHolder.contains(voteList.get(i).getString("IdeaTreeId"))){
                            Log.i("voteMap1","test");
                            VoteMap voteMap1 = new VoteMap();
                            voteMap1.voteId = voteList.get(i).getString("IdeaTreeId");
                            voteMap1.voteTotal = 0;
                            for (int j = 0; j < voteList.size(); j++) {
                                if (voteList.get(i).getString("IdeaTreeId").
                                        equals(voteList.get(j).getString("IdeaTreeId"))) {
                                    voteMap1.voteTotal++;
                                }
                            }
                            voteIdHolder.add(voteMap1.voteId);
                            vms.add(voteMap1);
                        } } else {
                                VoteMap voteMap1 = new VoteMap();
                                voteMap1.voteId = voteList.get(i).getString("IdeaTreeId");
                                voteMap1.voteTotal = 0;
                                for (int j = 0; j < voteList.size(); j++) {
                                    if (voteList.get(i).getString("IdeaTreeId").
                                            equals(voteList.get(j).getString("IdeaTreeId"))) {
                                        voteMap1.voteTotal++;
                                    }
                                }
                                voteIdHolder.add(voteMap1.voteId);
                                vms.add(voteMap1);
                        }


                    }
                }

            } else {
                VoteMap voteMap1 = new VoteMap();
                voteMap1.voteId = voteList.get(i).getString("IdeaTreeId");
                voteMap1.voteTotal = 0;
                for (int j = 0; j < voteList.size(); j++) {
                    if (voteList.get(i).getString("IdeaTreeId").
                            equals(voteList.get(j).getString("IdeaTreeId"))) {
                        voteMap1.voteTotal++;
                    }
                }
                vms.add(voteMap1);
            }
            Log.i("sortvotes", "vms size " + Integer.toString(vms.size()));
            Log.i("voteMap1", Integer.toString(vms.get(0).voteTotal));
        }
        thresholdCalc();
    }

    private void thresholdCalc() {
        ArrayList<Integer> voteStore = new ArrayList<>();
        topVotes = new ArrayList<>();
        for (VoteMap vm : vms){
            voteStore.add(vm.voteTotal);
        }
        Collections.sort(voteStore, new Comparator<Integer>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1, o2);
            }
        });
        Log.i("voteStore", voteStore.toString());
        float voteMin = voteStore.get(voteStore.size()-1)*voteThreshhold;
        for (VoteMap topVoteMap:vms){
            if (topVoteMap.voteTotal >= voteMin) {
                topVotes.add(topVoteMap);
            }
        }
        parseQuery();
        Log.i("topVotes", topVotes.toString());

    }

    private void parseQuery() {
        for (int i = 0; i < topVotes.size(); i++) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Idea");
            query.whereEqualTo("IdeaTree", topVotes.get(i).voteId);
            query.whereEqualTo("Identifier", "Main");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e==null){
                        if(!objects.isEmpty()){
                            for (ParseObject obj:objects) {
                                createIdea(obj);
                            }
                        }
                    }
                }
            });
        }
    }


    private void createIdea(ParseObject object) {
                    MainIdea idea = new MainIdea(HomePageActivity.this, object.getString("IdeaTree"));
                    if (object.getString("Description") != null) {
                        idea.setDescription(object.getString("Description"));
                    }
                    if (object.getString("Title") != null) {
                        idea.setTitle(object.getString("Title"));
                    }
                    if (object.getString("Author") != null) {
                        idea.author = object.getString("Author");
                    }
                    if (object.getNumber("voteMainTotal") != null) {
                         idea.totalVote = object.getNumber("voteMainTotal").intValue();
                    }
                    list.add(idea);
                    topIdeaAdapter.notifyDataSetChanged();
                }



    private void calcVoteRate(int voteTotal) {
        float voteRate = voteTotal/TimeSetter.getTime()-(TimeSetter.getTime()-voteRateDuration);
    }

    private void setClickListener() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClickIdeaTree(list.get(position).ideaTreeID);
            }
        });
    }

    private void onClickIdeaTree(String ideaTreeID) {
        Intent intent = new Intent(HomePageActivity.this, IdeaTreeActivity.class);
        intent.putExtra("IdeaTreeID", ideaTreeID);
        Log.i("onClickIdeaTree",ideaTreeID);
        startActivity(intent);
    }

    public void onClickBookMark(View view){
        MainIdea mainIdea = (MainIdea)view.getTag();
        List<String> bookmarks = ParseUser.getCurrentUser().getList("BookmarkID");
        Log.i("onClickBookMark",mainIdea.ideaTreeID);
        if ( bookmarks == null){
            ParseUser.getCurrentUser().put("BookmarkID",new ArrayList<>());
        }
        bookmarks.add(mainIdea.ideaTreeID);
        ParseUser.getCurrentUser().put("BookmarkID",bookmarks);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    Toast.makeText(HomePageActivity.this, "Mindmap successfully added to your bookmarks within your library.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public float calculateLimit(){
        minVote = maxVote*(1-collateLimit);
        return  minVote;
    }

}



