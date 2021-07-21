package com.yumenonaka.yumenoschedule.apis;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YumenoApis {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String BaseApiURL = "https://www.yumeno-naka.moe/yumeno_api";

    public static String getRecentSchedule() throws IOException {
        Request request = new Request.Builder()
                .url(BaseApiURL + "/recentEvent?useBr=true")
                .build();
        Response response = client.newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }
}
