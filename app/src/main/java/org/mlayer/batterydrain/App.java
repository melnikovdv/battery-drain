package org.mlayer.batterydrain;

import android.app.Application;

import com.orhanobut.hawk.Hawk;

import org.mlayer.batterydrain.logger.LogWriter;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import timber.log.Timber;

public class App extends Application {

    private static App app;

    private LogWriter logger;

    private OkHttpClient client = new OkHttpClient();
    private Api api = new Api(client);

    private Storage storage;
    private RequestRunner requestRunner;

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        storage = new Storage(this);
        requestRunner = new RequestRunner(this, api, storage);
    }

    private void initLogger() {
        Timber.plant(new Timber.DebugTree());
        logger = createLogger();
        Timber.plant(new Timber.Tree() {
            @Override
            protected void log(int priority, String tag, String message, Throwable t) {
                logger.log(priority, tag, message);
            }
        });
    }

    private LogWriter createLogger() {
        try {
            String primaryFile = getExternalCacheDir().getAbsolutePath() + "/primary.log";
            String secondaryFile = getExternalCacheDir().getAbsolutePath() + "/secondary.log";
            return LogWriter.init(primaryFile, secondaryFile, 8096, 0);
        } catch (IOException e) {
            Timber.e(e);
        }
        return null;
    }

    public App() {
        app = this;
    }

    public static App getInstance() {
        return app;
    }

    public Api getApi() {
        return api;
    }


    public RequestRunner getRequestRunner() {
        return requestRunner;
    }

    public Storage getStorage() {
        return storage;
    }
}
