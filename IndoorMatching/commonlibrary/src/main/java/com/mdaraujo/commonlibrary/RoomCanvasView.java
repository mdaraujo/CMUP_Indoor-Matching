package com.mdaraujo.commonlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.mdaraujo.commonlibrary.model.BeaconInfo;

import java.util.ArrayList;
import java.util.List;

public class RoomCanvasView extends View {

    private static final String TAG = "RoomCanvasView";

    private static final int radius = 20;
    private static final int marginX = radius + 5;
    private static final int marginY = radius + 5;

    private Paint mPaint;
    private List<BeaconInfo> beaconCircles;

    public RoomCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        beaconCircles = new ArrayList<>();
        reset();
    }

    public void reset() {
        beaconCircles.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.LTGRAY);

        for (BeaconInfo beacon : beaconCircles) {
            mPaint.setColor(beacon.getColor());
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setAntiAlias(true);
            canvas.drawCircle(beacon.getPosX(), beacon.getPosY(), radius, mPaint);
        }

//        Log.d(TAG, "Points: " + beaconCircles);

    }

    public void drawBeacons(List<BeaconInfo> beaconsInfo) {

        if (beaconsInfo.isEmpty())
            return;

        // calculate borders coordinates
        float maxBeaconX = 0;
        float maxBeaconY = 0;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;

        for (int i = 0; i < beaconsInfo.size() - 1; i++) {
            for (int j = i + 1; j < beaconsInfo.size(); j++) {
                float xDistance = Math.abs(beaconsInfo.get(i).getPosX() - beaconsInfo.get(j).getPosX());
                if (xDistance > maxBeaconX) {
                    maxBeaconX = xDistance;
                }

                float yDistance = Math.abs(beaconsInfo.get(i).getPosY() - beaconsInfo.get(j).getPosY());
                if (yDistance > maxBeaconY) {
                    maxBeaconY = yDistance;
                }
            }

            if (beaconsInfo.get(i).getPosX() < minX)
                minX = beaconsInfo.get(i).getPosX();
            if (beaconsInfo.get(i).getPosY() < minY)
                minY = beaconsInfo.get(i).getPosY();

        }

        int lastPos = beaconsInfo.size() - 1;

        if (beaconsInfo.get(lastPos).getPosX() < minX)
            minX = beaconsInfo.get(lastPos).getPosX();
        if (beaconsInfo.get(lastPos).getPosY() < minY)
            minY = beaconsInfo.get(lastPos).getPosY();

        int canvasWidth = getWidth() - 2 * marginX;
        int canvasHeight = getHeight() - 2 * marginY;

        float xRatio = canvasWidth / maxBeaconX;
        float yRatio = canvasHeight / maxBeaconY;

        float ratio = xRatio;

        if (yRatio < ratio)
            ratio = yRatio;

//        Log.d(TAG, "canvasWidth: " + canvasWidth + " maxBeaconX: " + maxBeaconX);
//        Log.d(TAG, "canvasHeight: " + canvasHeight + " maxBeaconY: " + maxBeaconY);
//        Log.d(TAG, "Ratio: " + ratio);

        beaconCircles.clear();

        for (BeaconInfo beaconInfo : beaconsInfo) {
            float x = marginX + (beaconInfo.getPosX() - minX) * ratio;
            float y = marginY + canvasHeight - ((beaconInfo.getPosY() - minY) * ratio);
            beaconCircles.add(new BeaconInfo(beaconInfo.getName(), beaconInfo.getColor(), x, y));
        }

        //important. Refreshes the view by calling onDraw function
        invalidate();
    }

}
