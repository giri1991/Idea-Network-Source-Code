package com.parse.starter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.FindCallback;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

//TODO redundant class potentially delete
public class ParseIdeaTree extends ParseQuery implements FindCallback {
    ArrayList<Idea> treeIdeas = new ArrayList<>();
    ParseQuery<ParseObject> query;
    private int idPosition;
    public String ideaTreeID = "";
    Context mContext;


    public ParseIdeaTree(Class subclass) {
        super(subclass);
    }

    public ParseIdeaTree(String theClassName, String ideaTreeID,Context context) {
        super(theClassName);
        this.ideaTreeID = ideaTreeID;
        mContext = context;
        addIdeaTreeToParse(theClassName);
    }

    private void addIdeaTreeToParse(String theClassName) {
        ParseObject parseIdeaTree = new ParseObject(theClassName);
        parseIdeaTree.put("IdeaTreeID",ideaTreeID);
        parseIdeaTree.saveInBackground(new SaveCallback() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void done(ParseException e) {
                if (e==null){
                    //adding the main idea to the tree
                    Idea idea = new Idea(mContext,null,ideaTreeID, "Main");
                    idea.setTopMargin(0);
                    idea.setLeftMargin(5000);
                    idea.setVectorCoordinates(idea, IdeaTreeActivity.IdeaAge.OLD);
                    addMainIdeaToParse(idea);
                } else {
                    Log.i("addIdeaerror",e.getMessage());
                }
            }
        });

    }

    private void addMainIdeaToParse(Idea idea) {
        ParseObject parseObjectMainIdea = new ParseObject("Idea");
        parseObjectMainIdea.put("x", idea.topLeftVector.x);
        parseObjectMainIdea.put("y", idea.topLeftVector.y);
        parseObjectMainIdea.put("IdeaTree",idea.getIdeaTreeID());
        parseObjectMainIdea.put("Identifier",idea.getIdentifier());
        parseObjectMainIdea.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("added", "Main idea successfully added to parse");
                } else {
                    Log.i("added", "Main idea unsuccessfully added" + " " +e.getMessage());
                }
            }
        });
    }


    public ParseIdeaTree(ParseQuery query,int idPosition,Context context) {
        super(query);
        this.query = query;
        mContext = context;
        this.idPosition = idPosition;
        grabIdeaTreeIDs();

    }

    @Override
    public void done(Object o, Throwable throwable) {

    }

    @Override
    public void done(List objects, ParseException e) {
        if (e==null && !objects.isEmpty()){
            addIdeas(objects);
        }
    }

    private void addIdeas(List<ParseObject> objects) {
        for (int i = 0; i < objects.size(); i++) {
            //TODO here get all the data of the main idea and put it into an Idea so that
            //TODO it can be displayed within library activity grid view.
            //String ideaTreeID = objects.get(i).getString("IdeaTreeI");
            //Idea idea = new Idea(mContext,,)
        }
    }

   /* public ArrayList<Idea> getDownloadedIdeas(){
        return ideas;
    }*/

    public void grabIdeaTreeIDs() {
        if (ParseUser.getCurrentUser() != null) {
            //TODO get the index of ideatree positioning in library grid and use to find its cousin within id list in parseuser.

            //TODO add date ordering
            query.whereEqualTo("IdeaTreeID", ideaTreeID);
            query.findInBackground(this);


        }
    }
        public String getIdeaTreeID(){
        return ideaTreeID;
    }
    }

