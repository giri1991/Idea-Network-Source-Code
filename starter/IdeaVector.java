package com.parse.starter;

//sets the vector coordinates of each point of the idea

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IdeaVector  {
    public double x;
    public double y;
    int stopX;
    int stopY;
    int ideaPixelHeight;
    int ideaPixelWidth;
    Idea idea;
    private static final int searchRectScalar = 5;
    private static final int topOffset = 16;
    Context mContext;
    Boolean isCollidingIdea;
    Idea endVector;
    ArrayList<IdeaVector> endVectorsArray;
    Map<Integer,IdeaVector> endVectorMap;
    ArrayList<DistanceMap> distanceMaps;

    double parentX;
    double parentY;
    IdeaVector aliasVector;

    DrawLine.LinePos linePos;

    public IdeaVector(double x, double y,Context mContext){
        this.x = x;
        this.y = y;
        this.mContext = mContext;
        isCollidingIdea = false;
    }

    public IdeaVector(double x, double y){
        this.x = x;
        this.y = y;
    }

    public IdeaVector(double x, double y, Idea endVector, int ideaPixelHeight, int ideaPixelWidth, Context mContext, Boolean isAxisCheck){
        this.endVector = endVector;
        this.mContext = mContext;
        this.x = x;
        this.y = y;
        this.ideaPixelHeight = ideaPixelHeight;
        this.ideaPixelWidth = ideaPixelWidth;
        if (isAxisCheck) {
            addToAxisArray();
        } else {
            addToCornerArray();
        }
    }

    public IdeaVector(double x,double y,IdeaVector aliasVector){
        this.x = x;
        this.y = y;
        this.aliasVector=aliasVector;
    }

    private void addToCornerArray() {
        endVectorsArray = new ArrayList<>();
        endVectorsArray.add(new IdeaVector(endVector.bottomLeftVector.x ,
                endVector.bottomLeftVector.y,mContext));
        endVectorsArray.add(new IdeaVector(endVector.bottomRightVector.x,
                endVector.bottomRightVector.y,mContext));
        endVectorsArray.add(new IdeaVector(endVector.topLeftVector.x,
                endVector.topLeftVector.y,mContext));
        endVectorsArray.add(new IdeaVector(endVector.topRightVector.x,
                endVector.topRightVector.y,mContext));
    }

    private void addToAxisArray() {
       endVectorsArray = new ArrayList<>();
       IdeaVector botVec = new IdeaVector(endVector.bottomLeftVector.x + ideaPixelWidth/2,
                endVector.bottomLeftVector.y,mContext);
       botVec.setLineTag(DrawLine.LinePos.TOP);
       IdeaVector leftVec = new IdeaVector(endVector.bottomLeftVector.x,
                endVector.bottomLeftVector.y - ideaPixelHeight/2,mContext);
       leftVec.setLineTag(DrawLine.LinePos.RIGHT);
       IdeaVector topVec =  new IdeaVector(endVector.topLeftVector.x + ideaPixelWidth/2,
                endVector.topLeftVector.y +topOffset ,mContext);
        topVec.setLineTag(DrawLine.LinePos.BOT);
       IdeaVector rightVec = new IdeaVector(endVector.topRightVector.x,
                endVector.topRightVector.y + topOffset+ ideaPixelHeight/2,mContext);
        rightVec.setLineTag(DrawLine.LinePos.LEFT);
        endVectorsArray.add(botVec);
        endVectorsArray.add(leftVec);
        endVectorsArray.add(rightVec);
        endVectorsArray.add(topVec);
    }

    private void setLineTag(DrawLine.LinePos linePos){
        this.linePos = linePos;
    }

        public void translateIdea(){
        x += 20;
        y += 20;
    }

    public Rect createRect(Boolean isCollidingIdea,int width, int height){
        Rect searchRect = new Rect();
        if (isCollidingIdea) {
            searchRect.set((int)x,(int) y,(int) x+width,(int) y+height);
            return searchRect;
        } else {
            int mX = (int)x-2*width;
            int mY = (int)y-2*height;
            searchRect.set(mX, mY,mX+searchRectScalar*width, mY+searchRectScalar*height);
            return searchRect;
        }
    }


    public DistanceMap distanceCalculator(boolean isAlias){
        if (!isAlias) {
            distanceMaps = new ArrayList<>();
            for (int i = 0; i < endVectorsArray.size(); i++) {
                Log.i("distcalc", "x: " + Double.toString(x) + " y: " + Double.toString(y) +
                        "endx: " + Double.toString(endVectorsArray.get(i).x) + " endy: " + Double.toString(endVectorsArray.get(i).y));
                DistanceMap distanceMap = new DistanceMap(endVectorsArray.get(i));
                distanceMap.setParentVector(x, y);
                distanceMap.distanceGen();
                distanceMap.vectorNormalise();
                distanceMap.setLinePos(endVectorsArray.get(i).linePos);
                distanceMaps.add(distanceMap);
            }

            int magA = Math.min(distanceMaps.get(0).getDistance(),distanceMaps.get(1).getDistance());
            int magB = Math.min(magA,distanceMaps.get(2).getDistance());
            int smallestMag = Math.min(magB,distanceMaps.get(3).getDistance());
            Log.i("magnitude", Integer.toString(smallestMag));
            //endVectorsArray.clear();
            for (DistanceMap distanceMap1 : distanceMaps){
                if (distanceMap1.getDistance() == smallestMag){
                    return distanceMap1;
                }
            }
        } else {
            DistanceMap distanceMap = new DistanceMap(aliasVector);
            distanceMap.setParentVector(x, y);
            distanceMap.distanceGen();
            distanceMap.vectorNormalise();
            return distanceMap;
        }
       return null;
    }

}
