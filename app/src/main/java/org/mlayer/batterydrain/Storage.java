package org.mlayer.batterydrain;

import android.content.Context;

import com.orhanobut.hawk.Hawk;

public class Storage {

    Storage(Context context) {
        Hawk.init(context).build();
    }

    public void setCurrentNum(int num) {
        Hawk.put("num", num);
    }

    public Integer getCurrentNum() {
        return Hawk.get("num", 1);
    }

    public void setPeriod(int period) {
        Hawk.put("period", period);
    }

    public int getPeriod() {
        return Hawk.get("period", 10);
    }

    public void setRequestsCount(int requestsCount) {
        Hawk.put("requestsCount", requestsCount);
    }

    public int getRequestsCount() {
        return Hawk.get("requestsCount", 1);
    }

    public void setShouldScheduleNext(boolean should) {
        Hawk.put("shouldScheduleNext", should);
    }

    public boolean shouldScheduleNext() {
        return Hawk.get("shouldScheduleNext", false);
    }

    public void setMaxRequestsCount(Integer maxRequestsCount) {
        Hawk.put("maxRequestsCount", maxRequestsCount);
    }

    public int getMaxRequestsCount() {
        return Hawk.get("maxRequestsCount", 0);
    }

    public int getSucceededRequestsCount() {
        return Hawk.get("succeededRequestsCount", 0);
    }

    public boolean setSucceededRequestsCount(int succeededRequestsCount) {
        return Hawk.put("succeededRequestsCount", succeededRequestsCount);
    }

    public boolean incSucceededRequestsCount() {
        int current = getSucceededRequestsCount();
        return setSucceededRequestsCount(current + 1);
    }

    public void setInitialData(int periodSeconds, int requestsCount, Integer maxRequestsCount) {
        setCurrentNum(0);
        setSucceededRequestsCount(0);
        setPeriod(periodSeconds);
        setRequestsCount(requestsCount);
        setMaxRequestsCount(maxRequestsCount);
        setShouldScheduleNext(true);
    }
}
