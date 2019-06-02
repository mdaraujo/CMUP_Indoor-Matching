package com.mdaraujo.commonlibrary;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.util.Log;

import com.mdaraujo.commonlibrary.model.BeaconInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PositionEstimation {

    private static final String TAG = "PositionEstimation";

    private static final int nRandomGuesses = 50;
    private static final int nInformedGuesses = 150;
    private static final float informedRange = 1.5f;
    private static final float maxInfluence = 0.2f;
    private static final float minInfluence = 0.08f;

    private PointF avgPosition;
    private double avgConfidence;

    private PointF bestGuess;
    private double maxProb;
    private double factor;

    public PositionEstimation() {
        reset();
    }

    public void reset() {
        avgPosition = null;
        avgConfidence = 0;
    }

    public void estimate(List<BeaconInfo> beaconsInfo) {

        // get only the beacons with distance info
        List<BeaconInfo> beaconsWithDistance = new ArrayList<>();

        for (BeaconInfo beaconInfo : beaconsInfo)
            if (beaconInfo.getDistance() != null)
                beaconsWithDistance.add(beaconInfo);

        if (beaconsWithDistance.isEmpty())
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


        // estimate phone position by generating guesses and calculating their probability
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
                hx = avgPosition.x - informedRange + random.nextFloat() * (avgPosition.x + 2 * informedRange);
                hy = avgPosition.y - informedRange + random.nextFloat() * (avgPosition.y + 2 * informedRange);
            }
            guess.add(new PointF(hx, hy));
        }

        if (avgPosition != null) {
            guess.add(avgPosition);
        }

        // calculate guess probability and take best
        bestGuess = new PointF();
        maxProb = 0;
        double prob;

        for (int i = 0; i < guess.size(); i++) {
            prob = 1;
            hx = guess.get(i).x;
            hy = guess.get(i).y;

            for (BeaconInfo beacon : beaconsWithDistance) {
                double hDist = Math.sqrt(Math.pow(beacon.getPosX() - hx, 2) + Math.pow(beacon.getPosY() - hy, 2));
                prob *= (1 / (10 * Math.pow(1 - (hDist / beacon.getDistance()), 2) + 1));
//                prob *= Math.sqrt(10) / ((beacon.getDistance() * Math.PI) + 1);
//                    Log.d(TAG, String.format("HDist: %f, Beacon Dist: %f", hDist, beacon.getDistance()));
//                    Log.d(TAG, String.format("Prob Intern: %f", 1 / (10 * Math.pow(1 - (hDist / beacon.getDistance()), 2) + 1)));
            }

//                Log.d(TAG, String.format("Prob: %f, X: %f, Y: %f", prob, hx, hy));
            if (prob > maxProb) {
                maxProb = prob;
                bestGuess.set(hx, hy);
            }
        }

        // average confidence and position to stabilize estimation
        avgConfidence = (1 - maxProb) * avgConfidence + maxProb * maxProb;

//            float factor = (float) (1 - maxProb) * minInfluence + (float) maxProb * maxInfluence;

        if (avgPosition != null) {

            if (maxProb > avgConfidence)
                factor = maxInfluence;
            else
                factor = (avgConfidence - maxProb) / avgConfidence * minInfluence + (maxProb / avgConfidence) * maxInfluence;


            float factorF = (float) factor;

            avgPosition.x = (1 - factorF) * avgPosition.x + factorF * bestGuess.x;
            avgPosition.y = (1 - factorF) * avgPosition.y + factorF * bestGuess.y;
        } else {
            avgPosition = bestGuess;
        }

//        Log.i(TAG, getStatusString());
    }

    public PointF getEstimation() {
        return avgPosition;
    }

    public PointF getCurrentBestGuess() {
        return bestGuess;
    }

    @SuppressLint("DefaultLocale")
    public String getStatusString() {
        return String.format("AVG: %.2f, Curr: %.2f; Inf: %.2f", avgConfidence, maxProb, factor);
    }
}
