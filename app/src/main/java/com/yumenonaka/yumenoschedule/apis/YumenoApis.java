package com.yumenonaka.yumenoschedule.apis;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
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
