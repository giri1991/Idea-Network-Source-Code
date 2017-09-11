package com.parse.starter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class DrawLine extends View {
    Paint paint;
    Idea idea;
    Context mContext;
    DpConverter dpConverter;
    Idea parentIdea;
    ArrayList<Rect> collidingIdeas;
    ArrayList<CollisionMap> currentPoints;
    private static final float increment = 0.25f;
    private static final int lineOffsetAxis = 5;
    private static final int lineOffsetCorner = 10;
    public static final int rightRectOffset = 40;
    Path path;
    int ideaPixelHeight;
    int ideaPixelWidth;
    ArrayList<Boolean> collisions;
    ArrayList<Integer> distances;
    Map<Integer,DistanceMap> distanceMapList;
    IdeaVector lineStart;
    boolean isPathClear = true;
    float parentX;
    float parentY;

    IdeaVector origin;
    IdeaVector goal;

    IdeaVector firstClearPoint;

    IdeaVector rightVector;
    IdeaVector leftVector;
    IdeaVector topVector;
    IdeaVector botVector;

    LinePos linePos;

//TODO need  to amend the line drawn to the bottom right corner of each idea.
    public DrawLine(Context context, Idea idea, Idea parentIdea,@Nullable ArrayList<Rect>collidingIdeas) {
        super(context);
        this.idea = idea;
        this.parentIdea = parentIdea;
        this.collidingIdeas = collidingIdeas;
        init(context);
        pathFind();
    }

    public DrawLine(Context context,Idea idea){
        super(context);
        this.idea = idea;
        init(context);
        lineDraw();
    }

    public DrawLine(Context context){
        super(context);
        this.mContext = context;
        dpConverter = new DpConverter(mContext);
        ideaPixelHeight = (int)dpConverter.convertDpToPixel(Idea.heightDp);
        ideaPixelWidth = (int)dpConverter.convertDpToPixel(Idea.widthDp);
    }

    public DrawLine(IdeaVector origin, IdeaVector goal, Context context){
        super(context);
        Log.i("DrawLine","goal "+ "x "+Double.toString(goal.x)+"y "+Double.toString(goal.y));
        Log.i("DrawLine","origin "+ "x "+Double.toString(origin.x)+"y "+Double.toString(origin.y));
        init(context);
        drawAliasLine(origin,goal);

    }

    private void init(Context context) {
        this.mContext = context;
        dpConverter = new DpConverter(mContext);
        ideaPixelHeight = (int)dpConverter.convertDpToPixel(Idea.heightDp);
        ideaPixelWidth = (int)dpConverter.convertDpToPixel(Idea.widthDp);
        path = new Path();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
    }

    private void drawAliasLine(IdeaVector origin, IdeaVector goal) {
        this.origin = origin;
        this.goal = goal;
        Log.i("onDraw", "Originx: " + Double.toString(origin.x) + " Originy: " + Double.toString(origin.y));
        Log.i("onDraw", "Goalx: " + Double.toString(goal.x) + " Goaly: " + Double.toString(goal.y));
        parsePointPixel(origin);
        parsePointPixel(goal);
        path.moveTo((float)origin.x ,(float) origin.y);
        path.lineTo((float) goal.x, (float) goal.y);
    }

    public enum LinePos{
        TOP,
        LEFT,
        BOT,
        RIGHT,
    }

    private void lineDraw() {
            IdeaVector lineDataParent = new IdeaVector(idea.lineDataDownload.parentX,idea.lineDataDownload.parentY);
            IdeaVector lineDataChild = new IdeaVector(idea.lineDataDownload.ideaX,idea.lineDataDownload.ideaY);
           Log.i("firstClearPoint", "x: " + Double.toString(lineDataParent.x) +" y: " + Double.toString(lineDataParent.y));
            parsePointPixel(lineDataParent);
            parsePointPixel(lineDataChild);
            tagLineStart(lineDataParent.x,lineDataParent.y);
            path.moveTo((float)lineDataParent.x ,(float) lineDataParent.y);
            path.lineTo((float) lineDataChild.x, (float) lineDataChild.y);
            isDirectPathClear(idea.generateDistanceMap(lineDataParent,lineDataChild),false);
    }

    private void pathFind() {
        DistanceMap distanceMap = axisCheck();
        tagLineStart(distanceMap.parentX,distanceMap.parentY);
        Log.i("parentXDistance","x: " + Double.toString(distanceMap.parentX));
        path.moveTo((float) distanceMap.parentX
                        + (float)dpConverter.convertDpToPixel(lineOffsetAxis),
                (float)distanceMap.parentY + (float)dpConverter.convertDpToPixel(lineOffsetAxis));
        firstClearPoint = isDirectPathClear(distanceMap,true);
        if (firstClearPoint != null) {
            parsePointPixel(firstClearPoint);
            path.lineTo((float) firstClearPoint.x+(float)dpConverter.convertDpToPixel(lineOffsetAxis),
                    (float) firstClearPoint.y+ (float)dpConverter.convertDpToPixel(lineOffsetAxis));
            parsePointDp(distanceMap.parentVector);
            Log.i("firstClearPoint", "x: " + Double.toString(distanceMap.parentVector.x) +
                    " y: " + Double.toString(distanceMap.parentVector.y));
            parsePointDp(distanceMap.ideaVector);
            idea.setGradient(distanceMap);
            idea.setLineMap(distanceMap,lineOffsetAxis);
        } else {
            DistanceMap distanceMapCorner = cornerCheck();
            idea.setGradient(distanceMapCorner);
            tagLineStart(distanceMapCorner.parentX,distanceMapCorner.parentY);
            path.moveTo((float)distanceMapCorner.parentX  -(float)dpConverter.convertDpToPixel(lineOffsetCorner),
                    (float)distanceMapCorner.parentY+(float)dpConverter.convertDpToPixel(lineOffsetCorner));
            IdeaVector firstClearCornerPoint = isDirectPathClear(distanceMapCorner,true);
            Log.i("cornerAfter","x: " + Double.toString(dpConverter.convertPixelToDp(distanceMapCorner.parentX)) +
                    " y: " + Double.toString(dpConverter.convertPixelToDp(distanceMapCorner.parentY)));
            if (firstClearCornerPoint != null){
                Log.i("firstClearCornerPoint", "x: " + Double.toString(firstClearCornerPoint.x) +
                        " y: " + Double.toString(firstClearCornerPoint.y));
                parsePointPixel(firstClearCornerPoint);
                path.lineTo((float) firstClearCornerPoint.x + (float)dpConverter.convertDpToPixel(lineOffsetCorner),
                        (float) firstClearCornerPoint.y + (float)dpConverter.convertDpToPixel(lineOffsetCorner));
                parsePointDp(distanceMapCorner.parentVector);
                parsePointDp(distanceMapCorner.ideaVector);
                idea.setLineMap(distanceMapCorner,lineOffsetCorner);
            } else {
                //TODO seems like top left positioning ubnnecesarily activates this toast. amend.
                Toast.makeText(mContext, "This branch is obstructed, please try a clear path", Toast.LENGTH_SHORT).show();
                isPathClear = false;
            }
        }
        }

    private void tagLineStart(double parentX, double parentY) {
        lineStart = new IdeaVector(parentX,parentY);
        parsePointDp(lineStart);
    }


    private DistanceMap axisCheck() {
        ArrayList<IdeaVector> vectors = new ArrayList<>();
        ArrayList<DistanceMap> mapList = new ArrayList<>();
        distanceMapList = new HashMap<>();
        rightVector = new IdeaVector(parentIdea.topRightVector.x
                , parentIdea.topRightVector.y + ideaPixelHeight/2,idea,ideaPixelHeight,ideaPixelWidth,mContext,true);
        Log.i("cornerLeft","x " + Double.toString(parentIdea.topLeftVector.x)+
                "y "+ Double.toString(parentIdea.topLeftVector.y));
        vectors.add(rightVector);
        leftVector =new IdeaVector(parentIdea.topLeftVector.x , parentIdea.topLeftVector.y
                + ideaPixelHeight/2,idea,ideaPixelHeight,ideaPixelWidth,mContext,true);
        vectors.add(leftVector);
        botVector = new IdeaVector(parentIdea.bottomLeftVector.x+ideaPixelWidth/2
                , parentIdea.bottomLeftVector.y,idea,ideaPixelHeight,ideaPixelWidth,mContext,true);
        vectors.add(botVector);
        topVector = new IdeaVector(parentIdea.topLeftVector.x + ideaPixelWidth/2,
                parentIdea.topLeftVector.y,idea,ideaPixelHeight,ideaPixelWidth,mContext,true);
        vectors.add(topVector);
        for (int i=0; i<vectors.size();i++) {
            mapList.add(vectors.get(i).distanceCalculator(false));
            distanceMapList.put(mapList.get(i).getDistance(),mapList.get(i));
        }

        distances = new ArrayList<>(distanceMapList.keySet());
        Collections.sort(distances, new Comparator<Integer>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1,o2);
            }
        });
        Log.i("distances", distances.toString());
        //returns smallest distance coordinates between ideas.
        return distanceMapList.get(distances.get(0));
    }


    public DistanceMap axisCheckAlias(Idea parentIdea,IdeaVector aliasVector) {
        ArrayList<IdeaVector> vectors = new ArrayList<>();
        ArrayList<DistanceMap> mapList = new ArrayList<>();
        distanceMapList = new HashMap<>();
        rightVector = new IdeaVector(parentIdea.topRightVector.x
                , parentIdea.topRightVector.y + ideaPixelHeight/2,aliasVector);
        Log.i("cornerLeft","x " + Double.toString(parentIdea.topLeftVector.x)+
                "y "+ Double.toString(parentIdea.topLeftVector.y));
        vectors.add(rightVector);
        leftVector =new IdeaVector(parentIdea.topLeftVector.x , parentIdea.topLeftVector.y + ideaPixelHeight/2
                ,aliasVector);
        vectors.add(leftVector);
        botVector = new IdeaVector(parentIdea.bottomLeftVector.x+ideaPixelWidth/2
                , parentIdea.bottomLeftVector.y,aliasVector);
        vectors.add(botVector);
        topVector = new IdeaVector(parentIdea.topLeftVector.x + ideaPixelWidth/2,
                parentIdea.topLeftVector.y,aliasVector);
        vectors.add(topVector);
        for (int i=0; i<vectors.size();i++) {
            mapList.add(vectors.get(i).distanceCalculator(true));
            distanceMapList.put(mapList.get(i).getDistance(),mapList.get(i));
        }

        distances = new ArrayList<>(distanceMapList.keySet());
        Collections.sort(distances, new Comparator<Integer>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1,o2);
            }
        });
        Log.i("distances", distances.toString());
        //returns smallest distance coordinates between ideas.
        return distanceMapList.get(distances.get(0));
    }


    private DistanceMap cornerCheck() {
        ArrayList<IdeaVector> vectors = new ArrayList<>();
        ArrayList<DistanceMap> mapList = new ArrayList<>();
        distanceMapList = new HashMap<>();
        IdeaVector topRightVector = new IdeaVector(parentIdea.topRightVector.x, parentIdea.topRightVector.y
                ,idea,ideaPixelHeight,ideaPixelWidth,mContext,false);
        vectors.add(topRightVector);
        IdeaVector topLeftVector =new IdeaVector(parentIdea.topLeftVector.x , parentIdea.topLeftVector.y
                ,idea,ideaPixelHeight,ideaPixelWidth,mContext,false);
        vectors.add(topLeftVector);
        IdeaVector botLeftVector = new IdeaVector(parentIdea.bottomLeftVector.x
                , parentIdea.bottomLeftVector.y,idea,ideaPixelHeight,ideaPixelWidth,mContext,false);
        vectors.add(botLeftVector);
        IdeaVector botRightVector = new IdeaVector(parentIdea.bottomRightVector.x,
                parentIdea.bottomRightVector.y,idea,ideaPixelHeight,ideaPixelWidth,mContext,false);
        vectors.add(botRightVector);
        for (int i=0; i<vectors.size();i++) {
            mapList.add(vectors.get(i).distanceCalculator(false));
            distanceMapList.put(mapList.get(i).getDistance(),mapList.get(i));
        }

        distances = new ArrayList<>(distanceMapList.keySet());
        Collections.sort(distances, new Comparator<Integer>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1,o2);
            }
        });
        Log.i("distances", distances.toString());
        //returns smallest distance coordinates between ideas.
        return distanceMapList.get(distances.get(0));
    }

    //checks to see if simple direct path is clear for line
    private IdeaVector isDirectPathClear(DistanceMap distanceMap, Boolean collisionCheck) {
        Log.i("maxDistance", Double.toString(distanceMap.getDistance()));
        double maxDistance = dpConverter.convertPixelToDp(distanceMap.getDistance());
        double currentDistance = 0;
        double x = 0;
        double y = 0;
        collisions = new ArrayList<>();
        currentPoints = new ArrayList<>();
        IdeaVector currentPoint = new IdeaVector(distanceMap.parentX, distanceMap.parentY);
        parsePointDp(currentPoint);
        Log.i("currentPoint", "x: " + Double.toString(currentPoint.x) +
                " y: " + Double.toString(currentPoint.y));
        Log.i("ideavec", "x: " + Double.toString(distanceMap.ideaVector.x) +
                " y: " + Double.toString(distanceMap.ideaVector.y));

        while (currentDistance <= maxDistance) {

            Log.i("currentDistance", Double.toString(currentDistance));

            if (distanceMap.isEndXLessThanParent()) {
                x -= distanceMap.normalisedUnitX * increment;
                Log.i("EndXLessThanParent", Double.toString(distanceMap.normalisedUnitX));
            } else {
                x += distanceMap.normalisedUnitX * increment;
            }
            if (distanceMap.isEndYLessThanParent()) {
                y -= distanceMap.normalisedUnitY * increment;
            } else {
                y += distanceMap.normalisedUnitY * increment;
                Log.i("EndYMoreThanParent", Double.toString(distanceMap.normalisedUnitY));
            }
            IdeaVector nextPoint = new IdeaVector(currentPoint.x + x, currentPoint.y + y);
            Log.i("nextpointPoint", "x: " + Double.toString(nextPoint.x) +
                    " y: " + Double.toString(nextPoint.y));
            if (collisionCheck) {
                if (checkCollision(nextPoint)) {
                    Log.i("outsideCheck", "test");
                    return null;
                }
            } else {
                currentPoints.add(new CollisionMap(nextPoint,false));
            }
            currentDistance += distanceMap.normalisedMag * increment;
            Log.i("normalisedMag", Double.toString(distanceMap.normalisedMag));
        }
            if (collisionCheck) {
                for (CollisionMap collision : currentPoints) {
                    collisions.add(collision.getCollision());
                }

                if (!collisions.contains(true)) {
                    parsePointDp(distanceMap.ideaVector);
                    return distanceMap.ideaVector;
                }
            }
         return null;
    }

    private Boolean checkCollision(IdeaVector nextPoint) {
        CollisionMap collisionMap = new CollisionMap(nextPoint,true);
        currentPoints.add(collisionMap);
        for (int i = 0; i < collidingIdeas.size(); i++){
            if (nextPoint.x < collidingIdeas.get(i).right  && nextPoint.x > collidingIdeas.get(i)
                    .left && nextPoint.y > collidingIdeas.get(i).top && nextPoint.y < collidingIdeas
                    .get(i).bottom){
                Log.i("pointCollision","x: " + Double.toString(nextPoint.x)+ " y: " + Double.toString(nextPoint.y));
                collisionMap.addCollision(true);
                return true;
            } else {
                collisionMap.addCollision(false);

            }
        }
        return false;
}




    private void parsePointDp(IdeaVector point) {
        point.x = dpConverter.convertPixelToDp(point.x);
        point.y = dpConverter.convertPixelToDp(point.y);
    }

    private void parsePointPixel(IdeaVector point) {
        point.x = dpConverter.convertDpToPixel(point.x);
        point.y = dpConverter.convertDpToPixel(point.y);
    }



    public DrawLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.
                LAYOUT_INFLATER_SERVICE);
    }

    public DrawLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DrawLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        widthMeasureSpec = (int)dpConverter.convertDpToPixel(IdeaTreeActivity.IdeaTreeWidth);
        heightMeasureSpec = (int)dpConverter.convertDpToPixel(IdeaTreeActivity.IdeaTreeHeight);
        Log.i("onMeasure","widthspec " + Integer.toString(widthMeasureSpec)+
                "heightspec " + Integer.toString(heightMeasureSpec));
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (origin!=null) {
            Log.i("DrawLine", "onDraw called for alias line");
        }
        //TODO uncomment to add circles to path
        //path.addCircle(parentX,parentY,10, Path.Direction.CW);
       // path.addCircle((float)firstClearPoint.x,(float)firstClearPoint.y,10, Path.Direction.CW);
        Log.i("onDraw","test");
        canvas.drawPath(path,paint);


    }
}
