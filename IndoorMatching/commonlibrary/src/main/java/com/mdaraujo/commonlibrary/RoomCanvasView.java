package com.mdaraujo.commonlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.mdaraujo.commonlibrary.model.BeaconInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomCanvasView extends View {

    private static final String TAG = "RoomCanvasView";

    private static final int radius = 20;
    private static final int marginX = radius + 5;
    private static final int marginY = radius + 5;

    public Paint mPaint;
    private List<BeaconInfo> beaconCircles;

    public RoomCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        beaconCircles = new ArrayList<>();
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

        BeaconInfo phone = null;

        if (beaconsInfo.size() >= 3) {
            Collections.sort(beaconsInfo, (o1, o2) -> o1.getDistance().compareTo(o2.getDistance()));
            BeaconInfo b1 = beaconsInfo.get(0);
            BeaconInfo b2 = beaconsInfo.get(1);
            BeaconInfo b3 = beaconsInfo.get(2);
            phone = trackPhone(b1.getPosX(), b1.getPosY(), b1.getDistance().floatValue(),
                    b2.getPosX(), b2.getPosY(), b2.getDistance().floatValue(),
                    b3.getPosX(), b3.getPosY(), b3.getDistance().floatValue());

            beaconsInfo.add(phone);
            Log.d(TAG, phone.toString());
        }

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

        Log.d(TAG, "canvasWidth: " + canvasWidth + " maxBeaconX: " + maxBeaconX);
        Log.d(TAG, "canvasHeight: " + canvasHeight + " maxBeaconY: " + maxBeaconY);
        Log.d(TAG, "Ratio: " + ratio);

        beaconCircles.clear();

        for (BeaconInfo beaconInfo : beaconsInfo) {
            float x = marginX + (beaconInfo.getPosX() - minX) * ratio;
            float y = marginY + canvasHeight - ((beaconInfo.getPosY() - minY) * ratio);
            beaconCircles.add(new BeaconInfo(beaconInfo.getName(), beaconInfo.getColor(), x, y));
        }

        // remove added phone
        if (phone != null) {
            beaconsInfo.remove(phone);
        }


        //important. Refreshes the view by calling onDraw function
        invalidate();
    }

    //A function to apply trilateration formulas to return the (x,y) intersection point of three circles
    private BeaconInfo trackPhone(float x1, float y1, float r1, float x2, float y2, float r2, float x3, float y3, float r3) {
        float A = 2 * x2 - 2 * x1;
        float B = 2 * y2 - 2 * y1;
        float C = r1 * r1 - r2 * r2 - x1 * x1 + x2 * x2 - y1 * y1 + y2 * y2;
        float D = 2 * x3 - 2 * x2;
        float E = 2 * y3 - 2 * y2;
        float F = r2 * r2 - r3 * r3 - x2 * x2 + x3 * x3 - y2 * y2 + y3 * y3;
        float x = (C * E - F * B) / (E * A - B * D);
        float y = (C * D - A * F) / (B * D - A * E);
        return new BeaconInfo("Phone", Color.BLACK, x, y);

    }

}
