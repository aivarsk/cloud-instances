package lv.jug.rubylightcontest2.impl;

import java.lang.Math;
import lv.jug.rubylightcontest2.CloudAPI;
import lv.jug.rubylightcontest2.InstanceManager;

public class InstanceManagerImpl implements InstanceManager {
    // EMA for last 12 hours
    private static final double SCALE_ALPHA = 2.0 / (12.0 + 1.0);
    private double scaleAvg_ = 1; 

    private double insurance_ = 1.0;

    // Per hour EMA for last 7 days
    private static final double HOUR_ALPHA = 2.0 / (7.0 + 1.0);
    private double [] hourAvg_ = new double[24];

    private int instances_ = 100;

    private int currentHour_ = 0;

    public InstanceManagerImpl() {
        for (int i = 0; i < 24; i++) {
            hourAvg_[i] = -1;
        }
    }

    private double getInsurance(long requestCount) {
        // increase insurance quickly in case of fines
        // reduce slowly back to 1.0 during the next 48 hours or so
        long capacity = instances_ * MAX_REQUEST_PER_INSTANCE;
        double rate = (double)requestCount / (double)capacity;
        if (rate < 1.0) {
            insurance_ = Math.max(1.0, insurance_ - ((insurance_ - 1.0) / 48.));
        } else if (rate > 1.0) {
            insurance_ *= (rate + 0.11);
        }
        return insurance_;
    }

    // Prediction is very difficult, especially about the future.
    private long predictRequests(long requestCount) {
        int prevHour = (currentHour_ + 23) % 24;
        double prevLoad = hourAvg_[prevHour];

        // Current load, update averages
        double currentLoadBefore = hourAvg_[currentHour_];
        if (currentLoadBefore == -1) {
            hourAvg_[currentHour_] = requestCount;
        } else {
            hourAvg_[currentHour_] = (double)requestCount * HOUR_ALPHA + hourAvg_[currentHour_] * (1.0 - HOUR_ALPHA);
        }
        double currentLoad = hourAvg_[currentHour_];

        int nextHour = (currentHour_ + 1) % 24;
        double nextLoad = hourAvg_[nextHour];

        if (prevLoad == -1) {
            // First call, wild guess
            return (long)((double)requestCount * 1.5);
        } else if (currentLoadBefore == -1) {
            double move = (double)requestCount / prevLoad;
            double scale;
            if (move > 1.0) {
                scale = 1.2;
            } else {
                // Fall may stop at some point
                scale = 1.3;
            }
            return (long)((double)requestCount * move * scale);
        } else {
            // Smooth scale
            double scale = Math.sqrt((double)requestCount / currentLoadBefore);
            scaleAvg_ = scale * SCALE_ALPHA + scaleAvg_ * (1.0 - SCALE_ALPHA);

            double predictedMove = nextLoad / currentLoad;
            return (long)((double)requestCount * predictedMove * scaleAvg_ * getInsurance(requestCount));
        }
    }

    @Override
    public void loadNotification(long requestCount, CloudAPI cloudApi) {
        long prediction = predictRequests(requestCount);

        int needInstances = (int)(prediction / MAX_REQUEST_PER_INSTANCE + 1);

        if (needInstances > instances_) {
            cloudApi.startInstances(needInstances - instances_);
        } else if (needInstances < instances_) {
            cloudApi.stopInstances(instances_ - needInstances);
        }

        instances_ = needInstances;
        currentHour_ = (currentHour_ + 1) % 24;
    }
}
