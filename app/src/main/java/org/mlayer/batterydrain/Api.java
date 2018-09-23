package org.mlayer.batterydrain;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Api {

    private static final String URL_HOST = "https://jsonplaceholder.typicode.com";
    private static final String URL_PHOTOS = URL_HOST + "/photos";

    private final OkHttpClient client;

    public Api(OkHttpClient client) {
        this.client = client;
    }

    public Response getPhoto(int id) throws IOException {
        Request request = new Request.Builder()
                .url(URL_PHOTOS + "/" + id)
                .build();
        return client.newCall(request).execute();
    }
}
