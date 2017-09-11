package com.parse.starter;


public class LineData {
    public double parentX;
    public double parentY;
    public double ideaX;
    public double ideaY;
    public DrawLine.LinePos linePos;



    public LineData(double parentX, double parentY, double ideaX, double ideaY){
        this.parentX = parentX;
        this.parentY = parentY;
        this.ideaX = ideaX;
        this.ideaY = ideaY;
    }

    public void setLinePos(DrawLine.LinePos linePos){
        this.linePos = linePos;
    }


}
