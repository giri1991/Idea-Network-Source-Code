package com.parse.starter;


import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;


import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.Inflater;

//TODO needs to extend constraint layout
public class Idea extends ConstraintLayout{



    View view;

    private static final String IDEA_TAG = "idea";
    public static final int heightDp = 160;
    public static final int widthDp = 140;
    public static final int heightDpWidgets = 84;
    public static final int widthDpWidgets = 43;
    public static final int rightOffset = 14;
    public int width;
    public int height;
    Context mContext;

    int topMargin;
    int leftMargin;

    public IdeaVector topLeftVector;
    public IdeaVector topRightVector;
    public IdeaVector bottomLeftVector;
    public IdeaVector bottomRightVector;
    public String id;
    String ideaTreeID = "";
    String objectId = "";
    String parentId = "";
    String randomId = "";
    TextView author;
    TextView title;
    TextView description;
    ImageButton upVote;
    ImageButton downVote;
    ImageButton comments;
    TextView totalVote;
    DpConverter dpConverter;
    public LineData lineDataDownload;
    LineData lineData;
    DistanceMap distanceMap;
    int voteTotal;
    String ideaAuthor;
    String lineageId;
    IdeaTreeActivity.IdeaChange ideaChange;
    double rise;
    double run;

    int level;
    boolean secondaryIdea;
    ArrayList<String> inheritedIds;



    //Note, remember at these @requireApi notations, surround with if statements to account for
    //versions lower than required.
   @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public Idea(Context context, @Nullable AttributeSet attrs,@Nullable String ideaTreeID, @Nullable String id) {
        super(context, attrs);

        this.ideaTreeID = ideaTreeID;
        this.id = id;
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.
                LAYOUT_INFLATER_SERVICE);
       view = inflater.inflate(R.layout.idea,this,true);
       title = (TextView)view.findViewById(R.id.title);
       description = (TextView)view.findViewById(R.id.description);
       author = (TextView)view.findViewById(R.id.author);
       upVote = (ImageButton)view.findViewById(R.id.upVote);
       upVote.setTag(view);
       totalVote = (TextView)view.findViewById(R.id.totalVote);
       downVote = (ImageButton)view.findViewById(R.id.downVote);
       downVote.setTag(view);
       comments = (ImageButton)view.findViewById(R.id.comments);
       comments.setTag(view);
       dpConverter = new DpConverter(mContext);
       setIdeaTags();
       getIdeaDimensions();

    }

    public Idea(Context context){
        super(context);
        dpConverter = new DpConverter(context);
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public Idea(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setJSONLineMap(String lineMap){
        Gson gson = new Gson();
        lineDataDownload = gson.fromJson(lineMap,LineData.class);
    }

    public void setLineMap(DistanceMap distanceMap, float offset){
        lineData = new LineData(distanceMap.parentVector.x + offset,distanceMap.parentVector.y +
                offset,distanceMap.ideaVector.x,distanceMap.ideaVector.y);
        lineData.setLinePos(distanceMap.linePos);
        Log.i("setLineMap", "x: " + Double.toString(distanceMap.parentX) +
                " y: " + Double.toString(distanceMap.parentY));
    }

    public void setWidth(int width){
        this.width = width;
    }

    public void setHeight(int height){
        this.height = height;
    }

    public void setLevel(int l){
        level = l;
    }

    public void setAuthor(String user){
        ideaAuthor = user;
        author.setText(ideaAuthor);
    }

    public void setLineageId(String lineageId){
        this.lineageId = lineageId;
    }

    public void setInheritedIds(Idea parentIdea) {
        inheritedIds = new ArrayList<>();
        if (!parentIdea.id.equals("Main")) {
            Log.i("setInheritedIds", "parent " +parentIdea.randomId);
            if (parentIdea.inheritedIds != null) {
                inheritedIds.addAll(parentIdea.inheritedIds);
            }
        } else {
            inheritedIds.add(parentIdea.randomId);
        }
            inheritedIds.add(randomId);
        }



    //TODO may not need this, perhaps review and delete
    private void setIdeaTags() {
        view.setTag(IDEA_TAG);
    }

    //gets ID for IdeaTree this idea belongs to
    public String getIdeaTreeID(){
        return ideaTreeID;
    }



    public void getIdeaDimensions(){
        view.post(new Runnable() {
            @Override
            public void run() {
                Log.i("dimensions", Integer.toString(view.getWidth())+ "h: " +
                        Integer.toString(view.getHeight()));
            }
        });
    }

    public void setGradient(DistanceMap distanceMap){
        rise = distanceMap.normalisedRawUnitY;
        run = distanceMap.normalisedRawUnitX;
    }

    //setters for idea positioning

    public void setTopMargin(int topMargin){
        this.topMargin = topMargin;
    }

    public void setLeftMargin(int leftMargin){
        this.leftMargin = leftMargin;
    }

    public void setChange(IdeaTreeActivity.IdeaChange ideaChange){
        this.ideaChange = ideaChange;
    }

   public Rect setVectorCoordinates(Idea idea, IdeaTreeActivity.IdeaAge age){
        initVectors();
        if (idea.getIdentifier().equals("Child") && age != IdeaTreeActivity.IdeaAge.OLD) {
            return topLeftVector.createRect(false,width,height);
        } else {
            return null;
        }

   }

   public void setVectorCoordinatesAlias(){
       initVectors();
   }

   private void initVectors(){
       double widgetWidthPixels = dpConverter.convertDpToPixel(widthDpWidgets);
       topLeftVector = new IdeaVector(leftMargin,topMargin,mContext);
       topRightVector = new IdeaVector(leftMargin + width + widgetWidthPixels - dpConverter.convertDpToPixel(rightOffset)
               ,topMargin,mContext);
       bottomLeftVector = new IdeaVector(leftMargin,topMargin+height,mContext);
       Log.i("botIdea", Integer.toString(height));
       bottomRightVector = new IdeaVector(leftMargin+width + widgetWidthPixels,topMargin+height,mContext);
   }


    public void setVote(Integer vote){
       voteTotal = vote;
       totalVote.setText(Integer.toString(voteTotal));

}

   //gets idea identifier as child or parent
    public String getIdentifier(){
        return id;
    }

    //sets the ideas parent id
    public void setParentId(String parentId){
        this.parentId = parentId;
    }

    public void setRandomId(String randomId){
        this.randomId = randomId;
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public void setDescription(String description){
        this.description.setText(description);
    }

    public void parseToPixels(){
        setLeftMargin((int) dpConverter.convertDpToPixel(topLeftVector.x));
        setTopMargin((int) dpConverter.convertDpToPixel(topLeftVector.y));
        setWidth((int)dpConverter.convertDpToPixel(width));
        setHeight((int)dpConverter.convertDpToPixel(height));
        setVectorCoordinates(this, IdeaTreeActivity.IdeaAge.OLD);
    }

    public void parseToDp(){
        setLeftMargin((int) dpConverter.convertPixelToDp(topLeftVector.x));
        setTopMargin((int) dpConverter.convertPixelToDp(topLeftVector.y));
        setWidth((int)dpConverter.convertPixelToDp(width));
        setHeight((int)dpConverter.convertPixelToDp(height));
        setVectorCoordinates(this, IdeaTreeActivity.IdeaAge.OLD);
    }

    public DistanceMap generateDistanceMap(IdeaVector parent, IdeaVector child){
        DistanceMap distanceMap = new DistanceMap(child);
        distanceMap.setParentVector(parent.x,parent.y);
        distanceMap.distanceGen();
        distanceMap.vectorNormalise();
        return distanceMap;
    }

}

