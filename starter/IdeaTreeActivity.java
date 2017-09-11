package com.parse.starter;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.parse.starter.Collation.Alias;
import com.parse.starter.Fragments.ToolBarFragment;
import com.parse.starter.Messaging.MessageContainer;
import com.parse.starter.Messaging.MessagingAdapter;
import com.parse.starter.Messaging.PopUp;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TimerTask;

import  android.os.Handler;

public class IdeaTreeActivity extends AppCompatActivity implements View.OnLongClickListener {

    RelativeLayout rl;
    public static final int IdeaTreeWidth = 10000;
    public static final int IdeaTreeHeight = 10000;
    public static final int positionOffset = 70;
    public static final int minSpace = Idea.widthDp/8;
    public static final int rightVectorOffset = 100;
    //TODO change interval to every second
    public static final long intervalQuery = 60000;
    public static int startingLocation = 5000;
    private int startingLocationPx;
    private int startingLocationPy;
    DpConverter dpConverter;
    DisplayMetrics displayMetrics;
    Intent intent;
    String ideaTreeID;

    Map<String,Idea> ideaHashMap;
    Map<String,Alias> aliasMap;
    public ArrayList<Idea> ideas;
    Idea idea;
    ArrayList<Rect> collidingIdeas;
    public  ArrayList<Boolean> safeToAdd;
    ArrayList<Boolean> hasLineCollided;
    ArrayList<String> ideaTags;
    ArrayList<DrawLine> lines;
    ArrayList<DrawLine> collidingLines;
    ArrayList<MessageContainer> messageContainers;

    DrawLine connectingLine;
    RelativeLayout.LayoutParams params;
    Idea parentIdea;
    public int width = 0;
    public int height = 0;
    Idea mainIdea;
    Idea localIdea;
    Rect mainIdeaRect;
    List<ParseObject>wholeIdeas;
    List<Idea>existingIdeas;
    LayoutInflater inflater;

    Handler handler;
    Thread thread;

    Bitmap bmp;
    PopUp popUp;
    MessagingAdapter adapter;
    String commentId;
    String reply;
    SpannableString initialTag;
    boolean replyEnabled;
    boolean userInChat;

    HScroll hsv;

    public enum IdeaAge{
    OLD,
    NEW
}

    public enum IdeaChange{
        VOTE,
        COMMENTS
    }

    //  @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*BIDIRECTIONAL SCROLLVIEW*/
        ScrollView sv = new ScrollView(this);
        hsv = new HScroll(this);
        hsv.sv = sv;
        /*END OF BIDIRECTIONAL SCROLLVIEW*/
        rl = new RelativeLayout(this);

        dpConverter = new DpConverter(this);
        sv.addView(rl, new RelativeLayout.LayoutParams((int) dpConverter.convertDpToPixel(IdeaTreeWidth),
                (int) dpConverter.convertDpToPixel(IdeaTreeHeight)));
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        hsv.addView(sv, params);
        setContentView(hsv);

        inflater = this.getLayoutInflater();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        ideaHashMap = new HashMap<>();
        ideas = new ArrayList<>();


        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        rl.setClipChildren(false);
        rl.setClipToPadding(false);



        startingLocationPx= (int)dpConverter.convertDpToPixel(startingLocation);
        startingLocationPy = (int)dpConverter.convertDpToPixel(startingLocation);
        Log.i("startingLocation", Integer.toString(startingLocation));
        height = (int) dpConverter.convertDpToPixel(Idea.heightDp);
        width = (int) dpConverter.convertDpToPixel(Idea.widthDp);
        lines = new ArrayList<>();


        intent = getIntent();
        ideaTreeID = intent.getStringExtra("IdeaTreeID");
        Log.i("onClickIdeaTreeAfter", ideaTreeID );
        Toast.makeText(IdeaTreeActivity.this, "IdeaTree created", Toast.LENGTH_SHORT).show();

        //query Idea class to extract idea data for display
        wholeIdeas = new ArrayList<>();


        queryIdeas(ideaTreeID,null);
        queryAlias();

        rl.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch(action){
                    case DragEvent.ACTION_DROP:
                        generateParams(event);
                        break;
                }
                return true;

            }
        });
    }

    private void queryAlias() {
        ParseQuery<ParseObject> query = new ParseQuery<>("Alias");
        Log.i("queryAlias",ideaTreeID);
        query.whereEqualTo("IdeaTreeId",ideaTreeID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if(!objects.isEmpty()){
                        extractAliasData(objects);
                    } else {
                        Log.i("queryAlias","objects empty");
                    }
                } else {
                    Log.i("queryAlias","error " +e.getMessage());
                }
            }
        });
    }

    private void extractAliasData(List<ParseObject> objects) {
        aliasMap = new HashMap<>();
        for (ParseObject object:objects){
            Alias alias = new Alias(object.getNumber("x").doubleValue(),object.getNumber("y").doubleValue(),
                    object.getString("randomId"), object.getString("ideaTreeId"),this);
            alias.extractLineData(object.getString("LineMap"));
            aliasMap.put(alias.randomId,alias);
            alias.setVectors();
            DrawLine drawLine = new DrawLine(alias.parentVector,alias.aliasVector,this);
            lines.add(drawLine);
            rl.addView(drawLine);
            Log.i("alias", "x "+Double.toString(alias.x)+"y "+Double.toString(alias.y) +
                    "randomId "+ alias.randomId);
        }

    }

    private void queryIdeas(String ideaTreeID,@Nullable final TimeSetter timeSetter) {
        existingIdeas = new ArrayList<>();
        ParseQuery<ParseObject> query = new ParseQuery<>("Idea");
        query.whereEqualTo("IdeaTree",ideaTreeID);
        Log.i("idtest", ideaTreeID);
        if (timeSetter != null){
            Log.i("timesetter","ts is not null");
            query.whereLessThan("update",timeSetter.presentTime);
            query.whereGreaterThan("update",timeSetter.beforeTime);

        }
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                Log.i("test", "works here");
                if (e == null) {
                    if (!objects.isEmpty()) {
                        Log.i("test", "queryIdeasTrue");
                                    //found all info related to stored ideas on parse
                                    //TODO review for backward compatibility.
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        Log.i("xTest",Integer.toString(objects.size()));
                                        for (ParseObject object : objects) {
                                                if (timeSetter != null) {
                                                    // Log.i("vttotal",Integer.toString(object.getNumber("voteTotal").intValue()));
                                                    if (object.getNumber("creation") != null && object.getString("Author") != null){
                                                        if ((object.getNumber("creation").longValue()) >= timeSetter.beforeTime
                                                                && !object.getString("Author").
                                                                equals(ParseUser.getCurrentUser().getUsername())) {
                                                            //TODO review this logic when considering comment changes
                                                            Log.i("dateTestafter", Long.toString(object.getNumber("creation").longValue()));
                                                            wholeIdeas.add(object);
                                                        } else {
                                                            Log.i("dateTestbefore", Long.toString(object.getNumber("creation").longValue()));
                                                            updateExisting(object);
                                                            Log.i("idea has changed", object.getString("randomId"));
                                                        }
                                                } } else {
                                                    wholeIdeas.add(object);
                                                }
                                        }

                            if (!wholeIdeas.isEmpty()) {
                                extractIdeaData(wholeIdeas);
                            }


                            if (timeSetter == null){
                                Log.i("thread", "running");
                                activateRunnable();
                            }


                        }} else {
                        Log.i("test", "objects is empty");
                        }
                }else{
                         Log.i("test", e.getMessage());
                            }
                        }

        });

    }

    private void updateExisting(ParseObject object) {
        //TODO add comment discrepency check
        localIdea = ideaHashMap.get(object.getString("randomId"));
        if (localIdea != null) {
            int newVote = object.getNumber("voteTotal").intValue();
            if (newVote != localIdea.voteTotal) {
                localIdea.setVote(newVote);
            }
        }
    }


    private void activateRunnable() {
        handler = new Handler();
        thread = new Thread(){
            @Override
            public void run() {
                Log.i("thread", "inside");
                    TimeSetter timeSetter = new TimeSetter();
                    queryIdeas(ideaTreeID,timeSetter);
                if (userInChat) {
                    Log.i("activateRunnable", commentId);
                    queryMessages(new TimeSetter());
                }
                    handler.postDelayed(this,intervalQuery);
                }
        };
        thread.start();


    }

    private void queryMessages(TimeSetter timeSetter) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
        query.whereEqualTo("randomId", commentId);
        if (timeSetter != null){
            Log.i("newMsg","ts is not null");
            query.whereLessThan("update",timeSetter.presentTime);
            Log.i("newPresentTime", Long.toString(timeSetter.presentTime));
            query.whereGreaterThan("update",timeSetter.beforeTime);
            Log.i("newbeforeTime", Long.toString(timeSetter.beforeTime));
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                    if (e==null){
                        if (!objects.isEmpty()){
                            for (ParseObject object : objects){
                                Log.i("newMsg", object.getString("message"));
                                    setMessageContainer(object);
                            }
                        } else {
                            Log.i("newMsg", "objects empty");
                        }
                    } else {
                        Log.i("newMsger", e.getMessage());
                    }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void generateParams(DragEvent event) {
         params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //TODO becareful here if you change the dimensions of the Idea the mouse positioning will be off
        //TODO therefore, review these calculations upon dimension change
        hasLineCollided = new ArrayList<>();
        collidingLines = new ArrayList<>();
        idea = new Idea(this,null,intent.getStringExtra("IdeaTreeID"), "Child");
        idea.setRandomId(new IDGenerator().generateID());
        idea.setOnLongClickListener(this);
        params.leftMargin = (int)dpConverter.convertPixelToDp((int)event.getX()-width/2);
        params.topMargin =  (int)dpConverter.convertPixelToDp((int)event.getY()-height/2);
        parentIdea.parseToDp();
        Rect rect = parentIdea.topLeftVector.createRect(false,Idea.widthDp,Idea.heightDp);
        parentIdea.parseToPixels();
        if (parentIdea.id.equals("Main")){
            idea.secondaryIdea = true;
        }
        idea.setWidth(Idea.widthDp);
        idea.setHeight(Idea.heightDp);
        idea.setTopMargin(params.topMargin);
        idea.setLeftMargin(params.leftMargin);
        idea.setVote(0);
        idea.setInheritedIds(parentIdea);
        idea.setAuthor(ParseUser.getCurrentUser().getUsername());
        idea.setVectorCoordinates(idea,IdeaAge.NEW);
        if (parentIdea.randomId.equals(mainIdea.randomId)){
            idea.setLineageId(new IDGenerator().generateID());
            idea.setLevel(1);
        } else {
            idea.setLineageId(parentIdea.lineageId);
            idea.setLevel(parentIdea.level+1);
        }
        Log.i("rect", "left: " + Integer.toString(rect.left) + "right: " + Integer.toString(rect.right)
                + "bot: " + Integer.toString(rect.bottom) + "top: " + Integer.toString(rect.top));
        if ((idea.topLeftVector.x + Idea.widthDp/2 < rect.right) &&
                (idea.topLeftVector.y + Idea.heightDp/2> rect.top) &&
                (rect.bottom > idea.topLeftVector.y + Idea.heightDp/2 ) &&
                (rect.left  < idea.topLeftVector.x + Idea.widthDp/2 )) {
            if (!lines.isEmpty()) {
                Log.i("lines","lines is not empty");
                for (DrawLine line : lines){
                    Log.i("linestartcoords","linestartX " + Double.toString(line.lineStart.x) +
                            "linestartY " + Double.toString(line.lineStart.y));
                    if (line.lineStart.x < rect.right && line.lineStart.x > rect
                            .left && line.lineStart.y > rect.top && line.lineStart.y < rect.bottom){
                        collidingLines.add(line);
                    }

                }
                if (!collidingLines.isEmpty()){
                    for (DrawLine collidingLine : collidingLines){
                        for (int i=0;i< collidingLine.currentPoints.size();i++) {
                            IdeaVector points = collidingLine.currentPoints.get(i).currentPoint;
                            if (points.x < idea.topRightVector.x && points.x > idea.topLeftVector.x
                                    && points.y > idea.topRightVector.y && points.y < idea.bottomRightVector.y){
                                Toast.makeText(this, "Please find somewhere less cluttered.", Toast.LENGTH_SHORT).show();
                                hasLineCollided.add(true);
                                break;
                            } else {
                                Log.i("lineCollision","not collided");
                                hasLineCollided.add(false);
                            }
                            }
                        }
                        if (!hasLineCollided.contains(true)){
                            parseQuery(rect);
                    }
                } else {
                    parseQuery(rect);
                }
            } else {
                Log.i("lines","lines is empty");
                parseQuery(rect);
            }
        }  else {
            Toast.makeText(this, "Please find a spot closer", Toast.LENGTH_SHORT).show();
        }
        }

    private void parseQuery(Rect rect) {
        //querying parse to find space for idea
        //TODO here add any more implementation to extract any extra data added to the idea at a later stage
        ParseQuery<ParseObject> query = new ParseQuery<>("Idea");
        query.whereEqualTo("IdeaTree", ideaTreeID);
        query.whereGreaterThan("y", rect.top);
        query.whereLessThan("y", rect.bottom);
        query.whereLessThan("x", rect.right);
        query.whereGreaterThan("x", rect.left);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    safeToAdd = new ArrayList<>();
                    if (objects.isEmpty()) {
                        Log.i("objectsparsequery","objects is empty");
                        addIdeaToParse();
                    } else {
                        collidingIdeas = new ArrayList<>();
                        ideaTags = new ArrayList<String>();
                        for (int i = 0; i < objects.size(); i++) {
                            ParseObject collidingIdea = objects.get(i);
                            if (!collidingIdea.getString("Identifier").equals("Main")) {
                                int collidingIdeaX = (int) collidingIdea.getNumber("x");
                                int collidingIdeaY = (int) collidingIdea.getNumber("y");
                                IdeaVector collidingIdeaVector = new IdeaVector(collidingIdeaX, collidingIdeaY, IdeaTreeActivity.this);
                                collidingIdeas.add(collidingIdeaVector.createRect(true, Idea.widthDp, Idea.heightDp));
                                IdeaVector collidingWidgetsVector = new IdeaVector(collidingIdeaX + Idea.widthDp, collidingIdeaY, IdeaTreeActivity.this);
                                collidingIdeas.add(collidingWidgetsVector.createRect(true, Idea.widthDpWidgets, Idea.heightDpWidgets));
                            }
                        }

                        calculateOverlap();
                        if (!safeToAdd.contains(false)) {
                            popUp();
                        } else {
                            Toast.makeText(getApplicationContext(), "This Idea is too close to another, please find more space.", Toast.LENGTH_SHORT).show();

                        }

                    }
                }
                }
        });
    }



    private void popUp() {
        idea.parseToPixels();
        connectingLine = new DrawLine(this,idea,parentIdea,collidingIdeas);
        idea.parseToDp();
        if (connectingLine.isPathClear) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = inflater.inflate(R.layout.main_idea_pop_up, null);
            final EditText title = (EditText) view.findViewById(R.id.title);
            final EditText description = (EditText) view.findViewById(R.id.description);
            Button createButton = (Button) view.findViewById(R.id.create);
            builder.setView(view);
            final AlertDialog dialog = builder.create();
            dialog.show();
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    idea.title.setText(title.getText().toString());
                    idea.description.setText(description.getText().toString());
                    addIdeaToParse();
                }
            });
        }
    }


    public void addIdeaToLayout(){
                params.leftMargin = (int)dpConverter.convertDpToPixel(params.leftMargin);
                params.topMargin =  (int)dpConverter.convertDpToPixel(params.topMargin);
                rl.addView(idea, params);
                rl.addView(connectingLine);
                ideas.add(idea);
                ideaHashMap.put(idea.randomId,idea);
                collidingIdeas.clear();
                safeToAdd.clear();
        }


    private void calculateOverlap() {
        for (int i = 0; i < collidingIdeas.size(); i++) {
            if ((idea.topRightVector.x - rightVectorOffset < collidingIdeas.get(i).left) ||
                    (idea.bottomLeftVector.y < collidingIdeas.get(i).top - minSpace) ||
                    (collidingIdeas.get(i).right + minSpace < idea.bottomLeftVector.x) ||
                    (collidingIdeas.get(i).bottom + minSpace < idea.topLeftVector.y )){
                safeToAdd.add(true);
            } else{
                safeToAdd.add(false);
                Log.i("safeToAddBefore", "collidingleft "+ Integer.toString(collidingIdeas.get(i).left - minSpace)
                        + "collidingIdeaRight "+ Double.toString(idea.topRightVector.x));
            }

        }
        if (((idea.topRightVector.x-rightVectorOffset < mainIdeaRect.left-minSpace) ||
                (idea.bottomLeftVector.y < mainIdeaRect.top-minSpace) ||
                (mainIdeaRect.right +minSpace + DrawLine.rightRectOffset < idea.bottomLeftVector.x) ||
                (mainIdeaRect.bottom+minSpace < idea.topLeftVector.y ))){
            safeToAdd.add(true);
        } else {
            safeToAdd.add(false);
        }
        Log.i("safeToAddAfter", safeToAdd.toString());
        ideaTags.clear();
    }


    //adds Idea data to parse
    public void addIdeaToParse() {
        if ((idea.topLeftVector.x >= 0)) {
            if (idea.topLeftVector.y >= 0) {

                if (connectingLine.isPathClear) {
                lines.add(connectingLine);
                final ParseObject parseObject = new ParseObject("Idea");
                 //TODO update   comments
                parseObject.put("x", idea.topLeftVector.x);
                parseObject.put("y", idea.topLeftVector.y);
                parseObject.put("IdeaTree", idea.getIdeaTreeID());
                parseObject.put("Identifier", idea.getIdentifier());
                parseObject.put("randomId", idea.randomId);
                parseObject.put("parentId",parentIdea.randomId);
                parseObject.put("lineageId", idea.lineageId);
                parseObject.put("creation",TimeSetter.getTime());
                parseObject.put("level",idea.level);
                parseObject.put("run", idea.run);
                parseObject.put("rise", idea.rise);
                parseObject.put("update",TimeSetter.getTime());
                parseObject.put("voteTotal",idea.voteTotal);
                parseObject.put("Author",idea.ideaAuthor);
                parseObject.put("Title",idea.title.getText().toString());
                parseObject.put("secondaryIdea",idea.secondaryIdea);
                parseObject.put("inheritedIds", idea.inheritedIds);
                parseObject.put("Description",idea.description.getText().toString());
                Gson gson = new Gson();
                String lineMapJSON = gson.toJson(idea.lineData);
                parseObject.put("LineMap",lineMapJSON);
                Log.i("test", parentIdea.randomId);
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i("added", "successfully added to parse");
                            idea.parseToPixels();
                            Log.i("cornerTest", Double.toString(idea.topLeftVector.x) + "y: " +
                                    Double.toString(idea.topLeftVector.y));
                                    addIdeaToLayout();

                        } else {
                            Log.i("not added", e.getMessage());
                        }
                    }
                });
            }} else {
                Toast.makeText(this, "Please try somewhere with more space", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please try somewhere with more space", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public  void extractIdeaData(List<ParseObject> objects) {
        String ideaTreeID = "";
        String identifier = "";
        int x;
        int y;
        String randomId = "";
        String parentId = "";
        String title = "";
        String description = "";
        String lineageId = "";
        int level;
        double gradient;
        Log.i("wholeExtract", Integer.toString(wholeIdeas.size()));
        for (int i = 0; i < objects.size(); i++) {
            ideaTreeID = objects.get(i).getString("IdeaTree");
            identifier = objects.get(i).getString("Identifier");
            x = (int)dpConverter.convertDpToPixel((int)objects.get(i).getNumber("x"));
            y = (int)dpConverter.convertDpToPixel((int)objects.get(i).getNumber("y"));
            Log.i("longfirst",objects.get(i).getCreatedAt().toString());
            randomId = objects.get(i).getString("randomId");
            parentId = objects.get(i).getString("parentId");
            title = objects.get(i).getString("Title");
            description = objects.get(i).getString("Description");
            lineageId =  objects.get(i).getString("lineageId");
            level = objects.get(i).getNumber("level").intValue();
            Idea idea = new Idea(this, null, ideaTreeID, identifier);
            if (!idea.id.equals("Main")) {
                idea.rise = objects.get(i).getNumber("rise").doubleValue();
                idea.run = objects.get(i).getNumber("run").doubleValue();
            }
            idea.setWidth(width);
            idea.setHeight(height);
            idea.setLeftMargin(x);
            idea.setTopMargin(y);
            idea.setRandomId(randomId);
            idea.setParentId(parentId);
            idea.setTitle(title);
            idea.secondaryIdea = objects.get(i).getBoolean("secondaryIdea");
            idea.setLevel(level);
            idea.setLineageId(lineageId);
            idea.setDescription(description);
            idea.setVectorCoordinates(idea, IdeaAge.OLD);
            idea.setOnLongClickListener(this);
            if (objects.get(i).getNumber("voteTotal") != null) {
                idea.setVote(objects.get(i).getNumber("voteTotal").intValue());
                if (objects.get(i).getList("voteContributors")!=null) {
                    if (objects.get(i).getList("voteContributors").contains(ParseUser.getCurrentUser().getUsername())) {
                        idea.totalVote.setTypeface(null, Typeface.BOLD);
                        idea.totalVote.setTextSize(16f);
                    }
                }} else {
                idea.setVote(0);
            }
            idea.setJSONLineMap(objects.get(i).getString("LineMap"));
            ideas.add(idea);
            ideaHashMap.put(randomId, idea);
        }



        for (int i = 0; i < ideas.size(); i++) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            if (ideas.get(i).parentId != null){
                //TODO need to reset the parentIDs after calculating inheritance.
                Log.i("setInheritedIds","child " + ideas.get(i).randomId);
                ideas.get(i).setInheritedIds(ideaHashMap.get(ideas.get(i).parentId));
            }
            if (ideas.get(i).getIdentifier().equals("Main")) {
                mainIdea = ideas.get(i);
                params.leftMargin =  (int)mainIdea.topLeftVector.x;
                params.topMargin =  (int)mainIdea.topLeftVector.y;
                rl.addView( mainIdea, params);
                mainIdea.setFocusable(true);
                mainIdea.setFocusableInTouchMode(true);
                mainIdea.requestFocus();
                int translationX = (int)-getScreenMiddleX();
                int translationY = (int)-getScreenMiddleY();
                mainIdea.setTranslationX(translationX);
                mainIdea.setTranslationY(translationY);
                mainIdea.setTopMargin(startingLocationPy+translationY);
                mainIdea.setLeftMargin(startingLocationPx+translationX);
                mainIdea.setVectorCoordinates(ideas.get(i),IdeaAge.OLD);
                mainIdea.parseToDp();
                mainIdeaRect = mainIdea.topLeftVector.createRect(true,Idea.widthDp,Idea.heightDp);
                mainIdea.parseToPixels();
                Log.i("ideatreepath",Double.toString( mainIdea.topRightVector.x) + "y " + Double.toString(mainIdea.topRightVector.y-Idea.heightDp/2));


            } else {
                if (ideaHashMap.get(ideas.get(i).parentId) != null) {
                    params.leftMargin = (int)ideas.get(i).topLeftVector.x;
                    params.topMargin = (int)ideas.get(i).topLeftVector.y;
                    DrawLine drawLine = new DrawLine(this, ideas.get(i));
                    Log.i("ideaID", "hello");
                    Log.i("ideaID",ideas.get(i).randomId);
                    lines.add(drawLine);
                    rl.addView(drawLine);
                    rl.addView(ideas.get(i), params);
                    Log.i("ideaID","success");

                }
            }
        }
        ideas.clear();
        wholeIdeas.clear();
    }

    private float getScreenMiddleX() {
        int displayWidth = displayMetrics.widthPixels;
        int startingPointX = startingLocationPx + width - displayWidth/2 - width/2;
        int endPointX = startingLocationPx;
        int translationX  = endPointX - startingPointX-width/14;
        Log.i("screen", Float.toString(translationX));
        return translationX;
    }

    private float getScreenMiddleY(){
        int displayHeight = displayMetrics.heightPixels;
        int startingPointY = startingLocationPy + height - displayHeight/2 - height/2;
        int endPointY = startingLocationPy;
        int translationY  = endPointY - startingPointY-width/4;
        Log.i("screen", Float.toString(translationY));
        return translationY;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onLongClick(final View v) {

        ClipData.Item item;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            item = new ClipData.Item((CharSequence)v.getTag());
            ClipData dragData = new ClipData((CharSequence)v.getTag(), new String[]{ClipDescription.
                    MIMETYPE_TEXT_PLAIN},item);
            CustomDragShadow myShadow = new CustomDragShadow(this,v);
            v.startDragAndDrop(dragData,myShadow,null,0);
            v.post(new Runnable() {
                @Override
                public void run() {
                    Log.i("view dimensions","width " + Integer.toString(v.getWidth()) + "height " +
                    Integer.toString(v.getHeight()));
                }
            });
        }
        parentIdea = (Idea)v;
        Log.i("longclick","left  " + Integer.toString(v.getLeft()) + " top   " + Integer.toString(v.getTop()));
        Log.i("longclick",parentIdea.randomId);
        return false;
    }

    public void upVoteOnClick(View view){
        voteClick(1,view);
    }

    public void downVoteOnClick(View view){
       voteClick(-1,view);
    }

    private void voteClick(final int n, final View view){
        final Idea idea = (Idea)view.getTag();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Idea");
        query.whereEqualTo("randomId", idea.randomId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (!objects.isEmpty()){
                        if(objects.get(0).getList("voteContributors")!= null) {
                            List<String> voteContributors = objects.get(0).getList("voteContributors");
                            if (!voteContributors.contains(ParseUser.getCurrentUser().getUsername())) {
                                idea.setVote(idea.voteTotal + n);
                                mainVote(n);
                                objects.get(0).put("voteTotal", idea.voteTotal);
                                voteContributors.add(ParseUser.getCurrentUser().getUsername());
                                objects.get(0).put("voteContributors", voteContributors);
                                objects.get(0).saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.i("votenousername", e.getMessage());
                                        } else {
                                            idea.totalVote.setTypeface(null, Typeface.BOLD);
                                            idea.totalVote.setTextSize(16f);
                                        }
                                    }
                                });
                            }}else {
                            objects.get(0).put("voteContributors", new ArrayList<>());
                            objects.get(0).add("voteContributors",ParseUser.
                                    getCurrentUser().getUsername());
                            idea.setVote(idea.voteTotal + n);
                            mainVote(n);
                            objects.get(0).put("voteTotal",idea.voteTotal);
                            objects.get(0).saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e!=null){
                                        Log.i("isusernamevote",e.getMessage());

                                    } else {
                                        idea.totalVote.setTypeface(null, Typeface.BOLD);
                                        idea.totalVote.setTextSize(16f);
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



    private void mainVote(final int n) {
        ParseObject vote = new ParseObject("Votes");
        vote.put("IdeaTreeId",ideaTreeID);
        vote.put("creation", TimeSetter.getTime());
        vote.saveInBackground();
        ParseQuery<ParseObject> query=  new ParseQuery<ParseObject>("Idea");
        query.whereEqualTo("Identifier", "Main");
        query.whereEqualTo("IdeaTree", ideaTreeID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (!objects.isEmpty()){
                        objects.get(0).put("voteMainTotal",objects.get(0).getNumber("voteMainTotal")
                         .intValue() +n);
                        objects.get(0).saveInBackground();
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void commentsOnClick(final View view) {
        Log.i("commentsOnClick","test");
            userInChat = true;
            final Idea idea = (Idea) view.getTag();
            commentId = idea.randomId;
            popUp = new PopUp(IdeaTreeActivity.this);
            messageContainers = new ArrayList<MessageContainer>();
            messageContainers.add(new MessageContainer());
            setMessagingAdapter();
            Log.i("locationIdea", Double.toString(idea.topRightVector.x));
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("IdeaComments");
            query.whereEqualTo("randomId", commentId);
            query.findInBackground(new FindCallback<ParseObject>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void done(final List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (!objects.isEmpty()) {
                            for (final ParseObject object : objects) {
                                //TODO adapt this parsefile code for further picture use
                                               /* ParseFile profilePic = objects.get(0).getParseFile("ProfilePic");
                                                if (profilePic != null) {
                                                    objects.get(0).getParseFile("ProfilePic").getDataInBackground(new GetDataCallback() {
                                                        @Override
                                                        public void done(byte[] data, ParseException e) {
                                                            if (e == null) {
                                                                if (data != null) {
                                                                    bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                                }

                                                            }
                                                        }
                                                    });
                                                }*/
                                               setMessageContainer(object);
                                Log.i("messageAuthor", object.getString("Author"));
                            }
                        }
                        popUp.activatePopUp();
                        setDismissListener();
                    } else {
                        Log.i("commentsclick", e.getMessage());
                    }
                }
            });
        }

    private void setMessageContainer(ParseObject object) {
        MessageContainer mContainer = new MessageContainer(
                object.getString("message"), object.getString("Author"));
        if (object.getString("replyTag") != null) {
            String replyTag = "@"+object.getString("replyTag");
            mContainer.setInitialTag(new SpannableString(replyTag));
            mContainer.stringBuilder();
        }
        messageContainers.add(mContainer);
        adapter.notifyDataSetChanged();
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
        adapter = new MessagingAdapter(IdeaTreeActivity.this
                ,messageContainers);
        popUp.listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    public void onClickReply(View view){
        TextView author = (TextView) view;
        initialTag = new SpannableString("@"+author.getTag().toString());
        adapter.reply.setText(stringBuilder(initialTag));
        replyEnabled = true;
    }

    public void onClickSend(View view){
        reply = adapter.reply.getText().toString();
        if (replyEnabled) {
            reply = obtainText(reply,initialTag.length());
        }
            Log.i("parseadderror", reply);
            addMessageToParse();

    }

    private String obtainText(String s, int start) {
       return s.substring(start,s.length());
    }

    private void addMessageToParse() {
        ParseObject parseObject = new ParseObject("IdeaComments");
        parseObject.put("randomId", commentId);
        parseObject.put("message", reply);
        parseObject.put("update", new TimeSetter().presentTime);
        if (replyEnabled) {
            //TODO once youve sorted out the profile inbox, need to set a system whereby
            //TODO repliers that are saved in "replyTag" on parse need to be queried and sent
            //TODO an alert that such and such has replied.
            parseObject.put("replyTag", obtainText(initialTag.toString(),1));
        }
        parseObject.put("Author", ParseUser.getCurrentUser().getUsername());
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    MessageContainer mContainer = new MessageContainer(reply,ParseUser.getCurrentUser()
                            .getUsername());
                    if (replyEnabled){
                        mContainer.setInitialTag(initialTag);
                        mContainer.stringBuilder();
                        replyEnabled = false;
                    }
                    messageContainers.add(mContainer);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.i("parseadderror",e.getMessage());
                }
            }
        });
    }

    private SpannableStringBuilder stringBuilder(SpannableString text){
        SpannableStringBuilder sp = new SpannableStringBuilder();
        text.setSpan(new ForegroundColorSpan(Color.parseColor("#335ce5")),0, text.length(),0);
        sp.append(text);
        return sp;
    }


}
