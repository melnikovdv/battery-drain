package org.mlayer.batterydrain;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.WorkerThread;

import com.orhanobut.hawk.Hawk;

import java.io.IOException;

import io.reactivex.ObservableEmitter;
import okhttp3.Response;
import timber.log.Timber;

public class RequestRunner {

    private Context context;
    private AlarmManager alarmManager;

    private Api api;
    private Storage storage;

    public static final int RECEIVER_ID = 343;


    public RequestRunner(Context context, Api api, Storage storage) {
        this.context = context;
        this.api = api;
        this.storage = storage;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void start(int periodSeconds, int requestsCount, Integer maxRequestsCount) {
        Timber.d("start with period: %d, requests count: %d, max requests count: %d",
                periodSeconds, requestsCount, maxRequestsCount);
        if (Build.VERSION.SDK_INT >= 23) {
            storage.setInitialData(periodSeconds, requestsCount, maxRequestsCount);
            schedule();
        } else {
            Utils.showShort(context, "Пока только для API >= 23");
        }
    }

    public void stop() {
        Timber.d("stop");
        cancelAlarm();
        storage.setShouldScheduleNext(false);
    }

    public boolean isScheduled() {
        return storage.shouldScheduleNext();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void schedule() {
        if (storage.shouldScheduleNext()) {
            Timber.d("setExactAndAllowWhileIdle");
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + storage.getPeriod() * 1000, getReceiverPendingIntent());
        }
    }

    private PendingIntent getReceiverPendingIntent() {
        Intent intent = new Intent(context, Receiver.class);
        return PendingIntent.getBroadcast(context, RECEIVER_ID, intent, 0);
    }

    public void cancelAlarm() {
        alarmManager.cancel(getReceiverPendingIntent());
    }

    @WorkerThread
    public void runRequests(ObservableEmitter<Object> emitter) {
        Timber.d("runRequests");
        for (int i = 0; i < storage.getRequestsCount(); i++) {
            int maxRequestsCount = storage.getMaxRequestsCount();
            int succeededRequestsCount = storage.getSucceededRequestsCount();

            if (succeededRequestsCount >= maxRequestsCount) {
                break;
            }

            int num = getNumAndInc();
            runRequest(num);
        }

        int maxRequestsCount = storage.getMaxRequestsCount();
        int succeededRequestsCount = storage.getSucceededRequestsCount();
        if (maxRequestsCount > 0 && succeededRequestsCount >= maxRequestsCount) {
            storage.setShouldScheduleNext(false);
        }

        emitter.onComplete();
    }

    private int getNumAndInc() {
        int result = storage.getCurrentNum();
        if (result > 4999) {
            result = 0;
        }
        storage.setCurrentNum(result + 1);
        return result;
    }

    @WorkerThread
    private void runRequest(final int num) {
        try {
            Response response = api.getPhoto(num);
            if (!response.isSuccessful()) {
                Timber.e("Request %d failed. http error code: %d", num, response.code());
            } else {
                if (num == 1) {
                    Timber.d("Response headers size = %d", response.headers().byteCount());
                }
                storage.incSucceededRequestsCount();
                Timber.d("Request %d succeded. Response size = %d", num, response.body().string().length());
            }
        } catch (IOException e) {
            Timber.e("Request %d failed. Error: %s", num, e.getMessage());
        }
    }
}
