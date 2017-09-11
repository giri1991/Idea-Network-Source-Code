package com.parse.starter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;


@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CustomDragShadow extends View.DragShadowBuilder {
    Idea idea;
    Context mContext;
    Bitmap bitmap;


    public CustomDragShadow(Context mContext,View view){
        super(view);
        this.mContext = mContext;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.placeholder);

    }

    @Override
    public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
        int width, height;

        width = getView().getWidth();
        height = getView().getHeight();
//TODO change drag shadow to up to date and higher quality bitmap idea
        Log.i("ondraw",Integer.toString(width) + "Y " + Integer.toString(height));

        // Sets the size parameter's width and height values. These get back to the system
        // through the size parameter.
         outShadowSize.set(width, height);

        // Sets the touch point's position to be in the middle of the drag shadow
           outShadowTouchPoint.set(width/2 , height/2 );
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        canvas.drawBitmap(bitmap,0,0,null);
        bitmap.recycle();
        bitmap = null;

    }
}
