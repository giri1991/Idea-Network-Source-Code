package com.parse.starter;

import android.support.annotation.Nullable;

import java.util.ArrayList;

public class CollisionMap {

    IdeaVector currentPoint;
    ArrayList<Boolean> collisionList;

    public CollisionMap(IdeaVector currentPoint, Boolean collectCollision){
        this.currentPoint = currentPoint;
        if (collectCollision) {
            collisionList = new ArrayList<>();
        }
    }

    public void addCollision(Boolean b){
        collisionList.add(b);
    }

    public Boolean getCollision(){
        if (collisionList.contains(true)){
            return true;
        } else {
            return false;
        }
    }
}
