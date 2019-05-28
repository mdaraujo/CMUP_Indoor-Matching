package com.mdaraujo.commonlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.mdaraujo.commonlibrary.model.BeaconInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomCanvasView extends View {

    private static final String TAG = "RoomCanvasView";

    private static final int radius = 20;
    private static final int marginX = radius + 5;
    private static final int marginY = radius + 5;

    private static final int nRandomGuesses = 10;
    private static final int nInformedGuesses = 100;
    private static final float informedRange = 1.5f;

    private Paint mPaint;
    private List<BeaconInfo> beaconCircles;
    private PointF avgPosition;

    public RoomCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        beaconCircles = new ArrayList<>();
        avgPosition = null;
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


        // get only the beacons with distance info
        List<BeaconInfo> beaconsWithDistance = new ArrayList<>();

        for (BeaconInfo beaconInfo : beaconsInfo)
            if (beaconInfo.getDistance() != null)
                beaconsWithDistance.add(beaconInfo);

        // estimate phone position by generating guesses and calculating their probability
        if (!beaconsWithDistance.isEmpty()) {

            List<PointF> guess = new ArrayList<>();
            Random random = new Random();
            float hx;
            float hy;

            // generate guesses
            for (int i = 0; i < nRandomGuesses + nInformedGuesses; i++) {

                if (i < nRandomGuesses || avgPosition == null) {
                    hx = minX + random.nextFloat() * (maxBeaconX - minX);
                    hy = minY + random.nextFloat() * (maxBeaconY - minY);
                } else {
                    // informed guesses variate in 1.5m from last guess
                    float infMinX = Math.max(minX, avgPosition.x - informedRange);
                    hx = infMinX + random.nextFloat() * (avgPosition.x + 2 * informedRange);
                    float infMinY = Math.max(minY, avgPosition.y - informedRange);
                    hy = infMinY + random.nextFloat() * (avgPosition.y + 2 * informedRange);
                }
                guess.add(new PointF(hx, hy));
            }

            if (avgPosition != null) {
                guess.add(avgPosition);
            }

            // calculate guess probability and take best
            PointF bestGuess = new PointF();
            double maxProb = 0;
            double prob;

            for (int i = 0; i < guess.size(); i++) {
                prob = 1;
                hx = guess.get(i).x;
                hy = guess.get(i).y;

                for (BeaconInfo beacon : beaconsWithDistance) {
                    double hDist = Math.sqrt(Math.pow(beacon.getPosX() - hx, 2) + Math.pow(beacon.getPosY() - hy, 2));
                    prob *= (1 / (10 * Math.pow(1 - (hDist / beacon.getDistance()), 2) + 1));
//                    Log.d(TAG, String.format("HDist: %f, Beacon Dist: %f", hDist, beacon.getDistance()));
//                    Log.d(TAG, String.format("Prob Intern: %f", 1 / (10 * Math.pow(1 - (hDist / beacon.getDistance()), 2) + 1)));
                }

//                Log.d(TAG, String.format("Prob: %f, X: %f, Y: %f", prob, hx, hy));
                if (prob > maxProb) {
                    maxProb = prob;
                    bestGuess.set(hx, hy);
                }
            }

            if (avgPosition != null) {
                avgPosition.x = 0.90f * avgPosition.x + 0.1f * bestGuess.x;
                avgPosition.y = 0.90f * avgPosition.y + 0.1f * bestGuess.y;
            } else {
                avgPosition = bestGuess;
            }

            Log.d(TAG, String.format("Max Prob: %f, Better Guess: X: %f, Y: %f", maxProb, avgPosition.x, avgPosition.y));

            beaconsInfo.add(new BeaconInfo("Phone", Color.BLACK, avgPosition.x, avgPosition.y));
        }

        // TODO maybe change borders coordinates to include phone position

        // draw all beacons + phone estimation
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
