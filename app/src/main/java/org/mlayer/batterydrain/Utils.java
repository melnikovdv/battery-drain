package org.mlayer.batterydrain;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import timber.log.Timber;

public class Utils {

    public static boolean hasNetworkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private static Handler handler = null;

    public static void showShort(Context context, String message) {
        showToast(context, message, Toast.LENGTH_SHORT);
    }

    private static void showToast(Context context, String message, int length) {
        if (context != null && !TextUtils.isEmpty(message)) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(context, message, length).show();
            } else {
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
                handler.post(() -> {
                    try {
                        Toast.makeText(context, message, length).show();
                    } catch (Exception e) {
                        Timber.d("showToast failed: %s", e.getMessage());
                    }
                });
            }
        }
    }
}
