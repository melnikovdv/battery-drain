package org.mlayer.batterydrain;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class Receiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive");
        RequestRunner requestRunner = App.getInstance().getRequestRunner();
        Observable.create(requestRunner::runRequests).subscribeOn(Schedulers.io()).subscribe();
        requestRunner.schedule();
    }
}
