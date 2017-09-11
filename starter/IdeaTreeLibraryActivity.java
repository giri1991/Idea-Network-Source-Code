package com.parse.starter;


import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;

import android.widget.GridView;

import android.widget.TextView;
import android.widget.Toast;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.Collation.CollatedListsActivity;
import com.parse.starter.Collation.Collation;


import java.util.ArrayList;
import java.util.List;

public class IdeaTreeLibraryActivity extends AppCompatActivity {

    GridView gridLayout;
    String ideaTreeID;
    List<String> ideaTrees;
    List<String> ideaTreeIDs;
    ArrayList<MainIdea> allIdeasStore;
    ArrayList<MainIdea> allIdeasStoreTmp;
    ArrayList<MainIdea> bookmarkIdeasStore;
    ArrayList<MainIdea> bookmarkIdeasStoreTmp;
    ArrayList<String> bookmarkIdeasStoreIDs;
    ArrayList<String> bookmarkIdeasStoreTmpIDs;
    ArrayList<MainIdea> personalIdeasStore;
    ArrayList<MainIdea> personalIdeasStoreTmp;
    ArrayList<String> personalIdeasStoreIDs;
    ArrayList<String> personalIdeasStoreTmpIDs;
    ArrayList<String> lastClicks;
    List<String> bookmarkIDs;
    Button createButton;
    EditText title;
    EditText description;
    GridAdapter arrayAdapter;
    AlertDialog dialog;
    MainIdea mainIdea;
    Collation collation;
    String allIdeas = "IdeaTreeID";
    String bookmarkIdeas = "BookmarkID";
    String personalIdeas = "PersonalID";
    String collatedLists = "CollatedLists";

    Button allIdeasButton;
    Button bookmarkButton;
    Button personalButton;
    Button collatedListsButton;
    TextView noIdeasMsg;

    boolean isAllIdeas;
    boolean isYourIdeas;
    boolean isCollatedLists;
    boolean isBookmarkIdeas;
    boolean ideasAvailable;
    boolean popupActive;

    public void createTree(View view) {

        if (view.getTag() == null) {
            ideaTreeID = new IDGenerator().generateID();
            view.setTag(ideaTreeID);
            if (ParseUser.getCurrentUser().getList("IdeaTreeID") == null) {
                ParseUser.getCurrentUser().put("IdeaTreeID", new ArrayList<>());
            }
            Log.i("createTree", ideaTreeID);
            ParseUser.getCurrentUser().add("IdeaTreeID", ideaTreeID);
            if (isBookmarkIdeas) {
                Log.i("createTree", "isBookmarkIdeas");
                if (ParseUser.getCurrentUser().getList("BookmarkID") == null) {
                    ParseUser.getCurrentUser().put("BookmarkID", new ArrayList<>());
                }
                ParseUser.getCurrentUser().add("BookmarkID", ideaTreeID);
            }
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        ideaTreeIDs.add(ideaTreeID);
                        ideaTrees.add(ideaTreeID);
                        mainIdea = new MainIdea(IdeaTreeLibraryActivity.this, ideaTreeID);
                        mainIdea.setTitle(title.getText().toString());
                        mainIdea.setDescription(description.getText().toString());
                        if (!allIdeasStore.isEmpty()) {
                            Log.i("allIdeasStore", "allIdeasStore not empty");
                            allIdeasStore.add(mainIdea);
                        } else if (!allIdeasStoreTmp.isEmpty()) {
                            Log.i("allIdeasStoretmp", "allIdeasStore tmp not empty");
                            allIdeasStoreTmp.add(mainIdea);
                        } else if (allIdeasStoreTmp.isEmpty() && allIdeasStore.isEmpty()) {
                            allIdeasStore.add(mainIdea);
                        }
                        if (isBookmarkIdeas) {
                            if (!bookmarkIdeasStore.isEmpty()) {
                                Log.i("createTree", "adding to bookmarkIdeasStore");
                                bookmarkIdeasStore.add(mainIdea);
                                bookmarkIdeasStoreIDs.add(mainIdea.ideaTreeID);
                            } else if (!bookmarkIdeasStoreTmp.isEmpty()) {
                                Log.i("createTree", "adding to bookmarkIdeasStoreTmp");
                                bookmarkIdeasStoreTmp.add(mainIdea);
                                bookmarkIdeasStoreTmpIDs.add(mainIdea.ideaTreeID);
                            } else if (bookmarkIdeasStoreTmp.isEmpty() && bookmarkIdeasStore.isEmpty()) {
                                bookmarkIdeasStore.add(mainIdea);
                                bookmarkIdeasStoreIDs.add(mainIdea.ideaTreeID);
                            }
                        }
                        if (isYourIdeas) {
                            personalIdeasStoreIDs.add(mainIdea.ideaTreeID);
                            if (!personalIdeasStore.isEmpty()) {
                                Log.i("createTree", "adding to bookmarkIdeasStore");
                                personalIdeasStore.add(mainIdea);
                            } else if (!personalIdeasStoreTmp.isEmpty()) {
                                Log.i("createTree", "adding to bookmarkIdeasStoreTmp");
                                personalIdeasStoreTmp.add(mainIdea);
                            } else if (personalIdeasStore.isEmpty() && personalIdeasStoreTmp.isEmpty()) {
                                personalIdeasStore.add(mainIdea);
                            }
                        }
                        addIdeaTreeToParse();
                    }
                }
            });
        }
        Log.i("popupActive", "dialog.is Showing()");
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void addIdeaTree(View view){
        activatePopUp();
    }

    private void addIdeaTreeToParse() {
        ParseObject parseIdeaTree = new ParseObject("IdeaTree");
        parseIdeaTree.saveInBackground(new SaveCallback() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void done(ParseException e) {
                if (e==null){
                    //adding the main idea to the tree
                    Idea idea = new Idea(IdeaTreeLibraryActivity.this,null,ideaTreeID, "Main");
                    idea.setTopMargin(5000);
                    idea.setLeftMargin(IdeaTreeActivity.startingLocation);
                    idea.setRandomId(new IDGenerator().generateID());
                    idea.setVectorCoordinates(idea, IdeaTreeActivity.IdeaAge.OLD);
                    idea.setVote(0);
                    idea.setLevel(0);
                    idea.setTitle(mainIdea.title);
                    idea.setDescription(mainIdea.description);
                    idea.setAuthor(ParseUser.getCurrentUser().getUsername());
                    addMainIdeaToParse(idea);
                } else {
                    Log.i("addIdeaerror",e.getMessage());
                }
            }
        });
    }

    //TODO add any extra idea data to parse here
    private void addMainIdeaToParse(final Idea idea) {
        ParseObject parseObjectMainIdea = new ParseObject("Idea");
        parseObjectMainIdea.put("x", idea.topLeftVector.x);
        parseObjectMainIdea.put("y", idea.topLeftVector.y);
        parseObjectMainIdea.put("IdeaTree",idea.getIdeaTreeID());
        parseObjectMainIdea.put("Title",title.getText().toString());
        parseObjectMainIdea.put("Description",description.getText().toString());
        parseObjectMainIdea.put("Identifier",idea.getIdentifier());
        parseObjectMainIdea.put("randomId",idea.randomId);
        parseObjectMainIdea.put("creation",TimeSetter.getTime());
        parseObjectMainIdea.put("voteTotal",idea.voteTotal);
        parseObjectMainIdea.put("voteMainTotal",0);
        parseObjectMainIdea.put("level",idea.level);
        parseObjectMainIdea.put("Author",idea.ideaAuthor);
        parseObjectMainIdea.saveInBackground(new SaveCallback() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("added", "Main idea successfully added to parse");
                    if (isAllIdeas){
                        if (!allIdeasStore.isEmpty()) {
                            activateAdapter(allIdeasStore);
                        } else {
                            activateAdapter(allIdeasStoreTmp);
                        }
                    }

                    if (isYourIdeas){
                        if (!personalIdeasStore.isEmpty()) {
                            activateAdapter(personalIdeasStore);
                        } else {
                            activateAdapter(personalIdeasStoreTmp);
                        }
                    }

                    if (isBookmarkIdeas){
                        if (!bookmarkIdeasStore.isEmpty()) {
                            activateAdapter(bookmarkIdeasStore);
                        } else {
                            activateAdapter(bookmarkIdeasStoreTmp);
                        }
                    }

                    Intent intent = new Intent(IdeaTreeLibraryActivity.this, IdeaTreeActivity.class);
                    intent.putExtra("IdeaTreeID",ideaTreeID);
                    startActivity(intent);
                            } else {
                                Toast.makeText(IdeaTreeLibraryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            }

    //creates a popup to define main idea
    private void activatePopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.main_idea_pop_up,null);
        title = (EditText)view.findViewById(R.id.title);
        description = (EditText)view.findViewById(R.id.description);
        createButton = (Button)view.findViewById(R.id.create);
        builder.setView(view);
        dialog = builder.create();
        builder.show();
        popupActive = true;
    }


    public void onClickDelete(View view) {
        MainIdea deleteIdea = (MainIdea)view.getTag();
        activateDeleteDialog(deleteIdea);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void activateAdapter(ArrayList<MainIdea> ideas) {
        arrayAdapter = new GridAdapter(IdeaTreeLibraryActivity.
                this,R.layout.adapterlayout,ideas);
        gridLayout.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        noIdeasMsg.setAlpha(0);
    }

    public void onClickIdeaTree(View view){
        Intent intent = new Intent(IdeaTreeLibraryActivity.this, IdeaTreeActivity.class);
        String clickedIdea = (String)view.getTag();
        intent.putExtra("IdeaTreeID", clickedIdea);
        Log.i("onClickIdeaTree",clickedIdea);
        startActivity(intent);
    }

    public void onClickCollate(View view){
        Log.i("onClickCollate","clickedIdea");
        Intent intent = new Intent(this,CollateActivity.class);
        MainIdea idea = (MainIdea) view.getTag();
        intent.putExtra("id",idea.ideaTreeID);
        intent.putExtra("title",idea.title);
        intent.putExtra("author",idea.author);
        Log.i("onClickCollate",idea.title);
        startActivity(intent);
    }





    private void activateDeleteDialog(final MainIdea mainIdea) {
        Log.i("activateDeleteDialog","resposne");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.delete_dialog,null);
        Button yes = (Button)view.findViewById(R.id.yes);
        Button no = (Button)view.findViewById(R.id.no);
        TextView message = (TextView)view.findViewById(R.id.message);
        TextView title = (TextView)view.findViewById(R.id.title);
        builder.setView(view);
        final AlertDialog show = builder.show();
        yes.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                show.dismiss();
                removeFromParse(mainIdea);
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show.dismiss();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void removeFromParse(final MainIdea mainIdea) {
//TODO perhaps only delete the idea from users library and not from parse
        Log.i("removeFromParse", "removal idea id "+mainIdea.ideaTreeID);
        List<String> userIdList = new ArrayList<>();
        String ideaList = "";
        if (isAllIdeas || isYourIdeas){
            ideaList = "IdeaTreeID";
            if (!allIdeasStore.isEmpty()) {
                allIdeasStore.remove(allIdeasStore.indexOf(mainIdea));
                for (MainIdea mainIdea1:allIdeasStore){
                     userIdList.add(mainIdea1.ideaTreeID);
                }
            } else if (!allIdeasStoreTmp.isEmpty()){
                allIdeasStoreTmp.remove(allIdeasStoreTmp.indexOf(mainIdea));
                for (MainIdea mainIdea1:allIdeasStoreTmp){
                    userIdList.add(mainIdea1.ideaTreeID);
                }
            } if (allIdeasStore.isEmpty() && allIdeasStoreTmp.isEmpty()){
                noIdeasMsg.setAlpha(1);
            }
            if (!personalIdeasStore.isEmpty()) {
                Log.i("removeFromParse","!personalIdeasStore.isEmpty()");
                personalIdeasStore.remove(personalIdeasStore.indexOf(mainIdea));
                for (MainIdea mainIdea1:personalIdeasStore){
                    userIdList.add(mainIdea1.ideaTreeID);
                }
            } else if (!personalIdeasStoreTmp.isEmpty()){
                Log.i("removeFromParse","personalIdeasStore.isEmpty()");
                personalIdeasStoreTmp.remove(personalIdeasStoreTmp.indexOf(mainIdea));
                for (MainIdea mainIdea1:personalIdeasStoreTmp){
                    userIdList.add(mainIdea1.ideaTreeID);
                }
            }
        }
            if (isYourIdeas){
                if (personalIdeasStoreTmp.isEmpty() && personalIdeasStore.isEmpty()){
                    noIdeasMsg.setAlpha(1);
                    }
                    }

        if (isBookmarkIdeas){
            ideaList = "BookmarkID";
            if (!bookmarkIdeasStore.isEmpty()) {
                Log.i("setAlpha","bookmarkIdeasStore not empty");
                Log.i("bookmarkIdeasStore",Integer.toString(bookmarkIdeasStore.size()));
                bookmarkIdeasStore.remove(bookmarkIdeasStore.indexOf(mainIdea));
                for (MainIdea mainIdea1:bookmarkIdeasStore){
                    userIdList.add(mainIdea1.ideaTreeID);
                }
            } else if (!!bookmarkIdeasStoreTmp.isEmpty()){
                Log.i("bookmarkIdeasStoreTmp",Integer.toString(bookmarkIdeasStoreTmp.size()));
                Log.i("setAlpha","bookmarkIdeasStoreTmp not empty");
                bookmarkIdeasStoreTmp.remove(bookmarkIdeasStoreTmp.indexOf(mainIdea));
                for (MainIdea mainIdea1:bookmarkIdeasStoreTmp){
                    userIdList.add(mainIdea1.ideaTreeID);
                }
                Log.i("bookmarkIdeasStoreTmp",Integer.toString(bookmarkIdeasStoreTmp.size()));
                Log.i("bookmarkIdeasStore",Integer.toString(bookmarkIdeasStore.size()));

            }
            if (bookmarkIdeasStore.isEmpty() && bookmarkIdeasStoreTmp.isEmpty()){
                Log.i("setAlpha","true");
                noIdeasMsg.setAlpha(1);
            }
        }
        arrayAdapter.notifyDataSetChanged();
        Log.i("idealist",ideaList);
        ParseUser.getCurrentUser().put(ideaList,userIdList);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e== null){
                    removeIdea(mainIdea);
                } else {
                    Toast.makeText(IdeaTreeLibraryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void removeIdea(MainIdea mainIdea) {
        List<String> ideaTreeList = ParseUser.getCurrentUser().getList("IdeaTreeID");
        List<String> bookmarkID = ParseUser.getCurrentUser().getList("BookmarkID");
        if (!ideaTreeList.contains(mainIdea.ideaTreeID) && !bookmarkID.contains(mainIdea.ideaTreeID)){
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Idea");
            query.whereEqualTo("IdeaTree",mainIdea.ideaTreeID);
            Log.i("removeFromParse",mainIdea.ideaTreeID);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e== null){
                        if (objects.size() > 0){
                            for (ParseObject object : objects){
                                object.deleteInBackground();
                            }
                        } else {
                            Toast.makeText(IdeaTreeLibraryActivity.this, "no objects", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea_tree_library);
        Intent intent = getIntent();
        FloatingActionButton addIdeaTreeButton = (FloatingActionButton)findViewById(R.id.addIdeaTree);
        gridLayout = (GridView)findViewById(R.id.gridView);
        ideaTrees = new ArrayList<>();
        ideaTreeIDs = new ArrayList<>();
        bookmarkIDs = new ArrayList<>();
        allIdeasStoreTmp = new ArrayList<>();
        bookmarkIdeasStoreTmp = new ArrayList<>();
        bookmarkIdeasStoreIDs = new ArrayList<>();
        bookmarkIdeasStoreTmpIDs = new ArrayList<>();
        personalIdeasStore = new ArrayList<>();
        personalIdeasStoreTmp = new ArrayList<>();
        personalIdeasStoreIDs = new ArrayList<>();
        personalIdeasStoreTmpIDs = new ArrayList<>();
        allIdeasButton = (Button)findViewById(R.id.allIdeas);
        bookmarkButton = (Button)findViewById(R.id.bookmarkIdeas);
        personalButton = (Button)findViewById(R.id.yourIdeas);
        allIdeasButton.setBackgroundColor((Color.parseColor("#00FF00")));
        bookmarkButton.setBackgroundResource(android.R.drawable.btn_default);
        personalButton.setBackgroundResource(android.R.drawable.btn_default);
        collatedListsButton = (Button)findViewById(R.id.topLists);
        collatedListsButton.setBackgroundResource(android.R.drawable.btn_default);
        isAllIdeas = true;
        popupActive = false;
        noIdeasMsg = (TextView)findViewById(R.id.noIdeasMsg);
        allIdeasStore = new ArrayList<>();
        bookmarkIdeasStore = new ArrayList<>();
        lastClicks = new ArrayList<>();
        lastClicks.add("all");
        downloadIdeas(allIdeas);

       //clearIDs();
    }


    private void downloadIdeas(String ideaType) {
        grabIdeaTreeIDs(ideaType);

    }


    private void addIdeas(List<String> ideaTreeIDs) {
        for (int i = 0; i < ideaTreeIDs.size(); i++) {
            ideaTrees.add(ideaTreeIDs.get(i));
            Log.i("addIdeas", ideaTreeIDs.toString());
            findMainIdea(ideaTreeIDs.get(i));
        }

    }

    public void grabIdeaTreeIDs(String ideaType) {
        if (ParseUser.getCurrentUser() != null) {
            if(ParseUser.getCurrentUser().getList(ideaType) != null){
                   if( !ParseUser.getCurrentUser().getList(ideaType).isEmpty()){
                Log.i("grabIdeaTreeIDs",ideaType);
                Log.i("ideaTreeIDs", "before " +ideaTreeIDs.toString());
                List<String> ideaTreeIDsx = ParseUser.getCurrentUser().getList(ideaType);

                Log.i("ideaTreeIDs","size " +Integer.toString(ideaTreeIDs.size()));
                Log.i("ideaTreeIDs","after " + ideaTreeIDs.toString());
                addIdeas(ideaTreeIDsx);
            } else {
                Log.i("onClickBookMarkIdeas","list is null");
            }
            }
        }
    }

    private void findMainIdea(final String ideaTreeID) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Idea");
        query.whereEqualTo("IdeaTree",ideaTreeID);
        query.whereEqualTo("Identifier", "Main");
        query.findInBackground(new FindCallback<ParseObject>() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size() > 0){
                        noIdeasMsg.setAlpha(0);
                        ideasAvailable = true;
                        MainIdea idea = new MainIdea(IdeaTreeLibraryActivity.this, objects.get(0).getString("IdeaTree"));
                        if (objects.get(0).getString("Title") != null) {
                            idea.setTitle(objects.get(0).getString("Title"));
                        }
                        if (objects.get(0).getString("Description") != null) {
                            idea.setDescription(objects.get(0).getString("Description"));
                        }
                        if (objects.get(0).getString("Author") != null) {
                            idea.author = objects.get(0).getString("Author");
                        }

                        if (isAllIdeas){
                            Log.i("findMainIdea","isallideas is true");
                            if (!allIdeasStore.isEmpty()) {
                                allIdeasStore.add(idea);
                                Log.i("findMainIdea","adding ideas to allIdeasStoree");
                                activateAdapter(allIdeasStore);
                            } else if (!allIdeasStoreTmp.isEmpty()){
                                allIdeasStoreTmp.add(idea);
                                Log.i("findMainIdea","adding ideas to allIdeasStoreetmp");
                                activateAdapter(allIdeasStoreTmp);
                            }  else if (allIdeasStoreTmp.isEmpty() && allIdeasStore.isEmpty()){
                                Log.i("findMainIdea","adding ideas to allIdeasStoree");
                                allIdeasStore.add(idea);
                                activateAdapter(allIdeasStore);
                            }
                        } else if (isBookmarkIdeas){
                            Log.i("bookmarkIdeasStoreIDs","ideatreeid "+idea.ideaTreeID);
                            Log.i("bookmarkIdeasStoreIDs",bookmarkIdeasStoreIDs.toString());
                            Log.i("bookmarkIdeasStoreIDsTm",bookmarkIdeasStoreTmpIDs.toString());
                            if (!bookmarkIdeasStore.isEmpty()) {
                                if (!bookmarkIdeasStoreIDs.contains(idea.ideaTreeID)) {
                                    bookmarkIdeasStore.add(idea);
                                    bookmarkIdeasStoreIDs.add(idea.ideaTreeID);
                                    Log.i("findMainIdea", "adding ideas to bookmarkIdeasStore");
                                }
                                activateAdapter(bookmarkIdeasStore);
                            }else if (!bookmarkIdeasStoreTmp.isEmpty()){

                                if (!bookmarkIdeasStoreTmpIDs.contains(idea.ideaTreeID)) {
                                    bookmarkIdeasStoreTmp.add(idea);
                                    bookmarkIdeasStoreTmpIDs.add(idea.ideaTreeID);
                                    Log.i("findMainIdea", "adding ideas to bookmarkIdeasStoretmp");
                                }
                                activateAdapter(bookmarkIdeasStoreTmp);
                            }  else if (bookmarkIdeasStoreTmp.isEmpty() && bookmarkIdeasStore.isEmpty()){
                                bookmarkIdeasStore.add(idea);
                                bookmarkIdeasStoreIDs.add(idea.ideaTreeID);
                                Log.i("findMainIdea","adding ideas to bookmarkIdeasStore");
                                Log.i("bookmarkIdeasStoresize",Integer.toString(bookmarkIdeasStore.size()));
                                activateAdapter(bookmarkIdeasStore);
                            }
                            Log.i("count","bookmarkideasstore " + Integer.toString(bookmarkIdeasStore.size()));
                            Log.i("count","bookmarkideasstoretmp " + Integer.toString(bookmarkIdeasStoreTmp.size()));
                        } else if (isYourIdeas){
                            Log.i("findMainIdea","isyouridea is true");
                            if (!personalIdeasStore.isEmpty()) {
                                personalIdeasStore.add(idea);
                                activateAdapter(personalIdeasStore);
                            }else if (!personalIdeasStoreTmp.isEmpty()){
                                personalIdeasStoreTmp.add(idea);
                                activateAdapter(personalIdeasStoreTmp);
                            }  else if (personalIdeasStoreTmp.isEmpty() && personalIdeasStore.isEmpty()){
                                personalIdeasStore.add(idea);
                                activateAdapter(personalIdeasStore);
                            }
                        }


                    } else {
                        Log.i("noIdeasMsg", "setting alpha");
                        noIdeasMsg.setAlpha(1);
                    }
                } else {
                    Log.i("findMainIdea", e.getMessage());
                }
            }
        });
    }


    //utility method to clear ids next to user
    private void clearIDs() {
        List clear = ParseUser.getCurrentUser().getList("IdeaTreeID");
        clear.clear();
        ParseUser.getCurrentUser().put("IdeaTreeID",clear);
        try {
            ParseUser.getCurrentUser().save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void onClickBookMarkIdeas(View view){
        if (!lastClicks.get(lastClicks.size() - 1).equals("bookmark")) {
            lastClicks.add("bookmark");
            isAllIdeas = false;
            isYourIdeas = false;
            isBookmarkIdeas = true;
            collatedListsButton.setBackgroundResource(android.R.drawable.btn_default);
            allIdeasButton.setBackgroundResource(android.R.drawable.btn_default);
            personalButton.setBackgroundResource(android.R.drawable.btn_default);
            bookmarkButton.setBackgroundColor((Color.parseColor("#00FF00")));
            cleanAllIdeas();
            cleanYourIdeas();
            downloadIdeas(bookmarkIdeas);
        }
        }

    private void cleanYourIdeas() {
        if (!personalIdeasStore.isEmpty()){
            personalIdeasStoreTmp.addAll(personalIdeasStore);
            Log.i("findMainIdea","clearing all items in allideastore and adding to allideasstoretmp");
            personalIdeasStore.clear();
            if (arrayAdapter !=null) {
                arrayAdapter.notifyDataSetChanged();
            }
        } else {
            Log.i("findMainIdea","clearing all items in allideasstoretmp and adding to allideastore");
            personalIdeasStore.addAll(personalIdeasStoreTmp);
            personalIdeasStoreTmp.clear();
            if (arrayAdapter !=null) {
                arrayAdapter.notifyDataSetChanged();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void onClickYourIdeas(View view) {
        if (!lastClicks.get(lastClicks.size() - 1).equals("your")) {
            lastClicks.add("your");
            isBookmarkIdeas = false;
            isAllIdeas = false;
            isYourIdeas = true;
            Log.i("onClickYourIdeas", "clicked");
            collatedListsButton.setBackgroundResource(android.R.drawable.btn_default);
            bookmarkButton.setBackgroundResource(android.R.drawable.btn_default);
            allIdeasButton.setBackgroundResource(android.R.drawable.btn_default);
            personalButton.setBackgroundColor((Color.parseColor("#00FF00")));
            Log.i("onClickYourIdeas", Integer.toString(ideaTreeIDs.size()));
            cleanAllIdeas();
            cleanBookmarks();
            if (!personalIdeasStore.isEmpty()) {
                personalIdeasStoreTmp.addAll(personalIdeasStore);
                Log.i("onClickYourIdeas", "personalIdeasStoreTmp size " + Integer.toString(personalIdeasStoreTmp.size()));
                Log.i("onClickYourIdeas", "personalIdeasStore not empty");
                personalIdeasStore.clear();
                arrayAdapter.notifyDataSetChanged();
                findPersonalIdeas(personalIdeasStoreTmp);
            } else if (!personalIdeasStoreTmp.isEmpty()) {
                Log.i("onClickYourIdeas", "personalIdeasStore size " + Integer.toString(personalIdeasStore.size()));
                Log.i("onClickYourIdeas", "personalIdeasStoreTmp not empty");
                personalIdeasStore.addAll(personalIdeasStoreTmp);
                personalIdeasStoreTmp.clear();
                arrayAdapter.notifyDataSetChanged();
                findPersonalIdeas(personalIdeasStore);
            } else if (personalIdeasStore.isEmpty() && personalIdeasStoreTmp.isEmpty()) {
                Log.i("onClickYourIdeas", "both empty");
                findPersonalIdeas(personalIdeasStore);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void cleanBookmarks() {
        if (!bookmarkIdeasStore.isEmpty()){
            bookmarkIdeasStoreTmp.addAll(bookmarkIdeasStore);
            bookmarkIdeasStoreTmpIDs.addAll(bookmarkIdeasStoreIDs);
            Log.i("findMainIdea","bookmarkideasstore is not empty clearing it and adding to tmp");
            bookmarkIdeasStoreIDs.clear();
            bookmarkIdeasStore.clear();
            if (arrayAdapter !=null) {
                arrayAdapter.notifyDataSetChanged();
            }
        } else if (!bookmarkIdeasStoreTmp.isEmpty()) {
            Log.i("findMainIdea","bookmarkideasstoretmp is not empty  clearing it and adding to bookmarkideasstore");
            bookmarkIdeasStore.addAll(bookmarkIdeasStoreTmp);
            bookmarkIdeasStoreIDs.addAll(bookmarkIdeasStoreTmpIDs);
            bookmarkIdeasStoreTmpIDs.clear();
            bookmarkIdeasStoreTmp.clear();
            if (arrayAdapter !=null) {
                arrayAdapter.notifyDataSetChanged();
            }
        }
    }

    private void cleanAllIdeas(){
        if (!allIdeasStore.isEmpty()){
            allIdeasStoreTmp.addAll(allIdeasStore);
            Log.i("findMainIdea","clearing all items in allideastore and adding to allideasstoretmp");
            allIdeasStore.clear();
            if (arrayAdapter !=null) {
                arrayAdapter.notifyDataSetChanged();
            }

        } else {
            Log.i("findMainIdea","clearing all items in allideasstoretmp and adding to allideastore");
            allIdeasStore.addAll(allIdeasStoreTmp);
            allIdeasStoreTmp.clear();
            if (arrayAdapter !=null) {
                arrayAdapter.notifyDataSetChanged();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void findPersonalIdeas(ArrayList<MainIdea> personalIdeasStoreX) {
        Log.i("findPersonalIdeas","personalIdeasStoreIDs "+Integer.toString(personalIdeasStoreIDs.size()));
            if (!allIdeasStore.isEmpty()) {
                Log.i("findPersonalIdeas", "allIdeasStore size  " + Integer.toString(allIdeasStore.size()));
                for (MainIdea idea : allIdeasStore) {
                    if (idea.author.equals(ParseUser.getCurrentUser().getUsername())) {
                        if (!personalIdeasStoreIDs.contains(idea.ideaTreeID)) {
                            Log.i("findPersonalIdeas", "adding id to personalIdeasStoreIDs");
                            personalIdeasStoreX.add(idea);
                            personalIdeasStoreIDs.add(idea.ideaTreeID);
                        }
                    }
                }
            } else {
                Log.i("findPersonalIdeas", "allIdeasStore.isEmpty");
                Log.i("findPersonalIdeas", "allIdeasStoreTmp size  " + Integer.toString(allIdeasStoreTmp.size()));
                for (MainIdea idea : allIdeasStoreTmp) {
                    if (idea.author.equals(ParseUser.getCurrentUser().getUsername())) {
                        if (!personalIdeasStoreIDs.contains(idea.ideaTreeID)) {
                            Log.i("findPersonalIdeas", "adding id to personalIdeasStoreTmpIDs");
                            personalIdeasStoreX.add(idea);
                            personalIdeasStoreIDs.add(idea.ideaTreeID);
                        }
                    }
                }
            }
        Log.i("findPersonalIdeas", "personalIdeasStoreX size " + Integer.toString(personalIdeasStoreX.size()));
        Log.i("findPersonalIdeas","personalIdeasStoreIDs "+personalIdeasStoreIDs.toString());
        activateAdapter(personalIdeasStoreX);
        if (personalIdeasStoreX.isEmpty()){
            Log.i("personalIdeasStoreX", "personalIdeasStoreX empty setting alpa");
            noIdeasMsg.setAlpha(1);
        }
            }



    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void onClickAllIdeas(View view) {
        if (!lastClicks.get(lastClicks.size() - 1).equals("all")) {
            lastClicks.add("all");
            isBookmarkIdeas = false;
            isAllIdeas = true;
            isYourIdeas = false;
            Log.i("onClickAllIdeas", "clicked");
            collatedListsButton.setBackgroundResource(android.R.drawable.btn_default);
            bookmarkButton.setBackgroundResource(android.R.drawable.btn_default);
            personalButton.setBackgroundResource(android.R.drawable.btn_default);
            allIdeasButton.setBackgroundColor((Color.parseColor("#00FF00")));
            Log.i("onClickAllIdeas", Integer.toString(ideaTreeIDs.size()));
            cleanBookmarks();
            if (!allIdeasStore.isEmpty()) {
                Log.i("findMainIdea", "allIdeasStore is not empty adding to it");
                activateAdapter(allIdeasStore);
            } else if (!allIdeasStoreTmp.isEmpty()) {
                Log.i("findMainIdea", "allIdeasStoretmp is not empty adding to it");
                activateAdapter(allIdeasStoreTmp);
            } else if (allIdeasStoreTmp.isEmpty() && allIdeasStore.isEmpty()) {
                noIdeasMsg.setAlpha(1);
            }
        }
    }

    public void onClickCreateBookmark(View view){
       List<String> bookmarkList = ParseUser.getCurrentUser().getList("BookmarkID");
        MainIdea mainIdea = (MainIdea) view.getTag();
        if (bookmarkList != null) {
            bookmarkList.add(mainIdea.ideaTreeID);
        } else {
            bookmarkList = new ArrayList<>();
        }
            Log.i("onClickCreateBookmark",bookmarkList.toString());
            ParseUser.getCurrentUser().put("BookmarkID",bookmarkList);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null){
                        Toast.makeText(IdeaTreeLibraryActivity.this, "IdeaTree bookmarked", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

     @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
     public void onClickCollatedLists(View view){

        Intent intent = new Intent(this, CollatedListsActivity.class);
        startActivity(intent);
     }


}
