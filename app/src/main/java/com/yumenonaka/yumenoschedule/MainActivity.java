package com.yumenonaka.yumenoschedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yumenonaka.yumenoschedule.apis.YumenoApis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private boolean isLoading = true;
    private ViewGroup mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        animateLoadingScreen();
        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLoading = true;
        setContentView(R.layout.loading);
        animateLoadingScreen();
        initialize();
    }

    private void initialize() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(()->{
            try {
                String response = YumenoApis.getRecentSchedule();
                JSONArray data = (new JSONObject(response)).getJSONArray("data");
                LinkedHashMap<String, ArrayList<JSONObject>> parsedData = parseScheduleData(data);
                List<String> dateKeySet = new ArrayList<>(parsedData.keySet());
                runOnUiThread(()->{
                    isLoading = false;
                    setContentView(R.layout.activity_main);
                    SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefresh);
                    swipeRefreshLayout.setOnRefreshListener(()->{
                        isLoading = true;
                        setContentView(R.layout.loading);
                        animateLoadingScreen();
                        swipeRefreshLayout.setRefreshing(false);
                        initialize();
                    });

                    mainLayout = findViewById(R.id.mainLayout);
                    LayoutInflater inflater = getLayoutInflater();

                    for(int i = 0; i < dateKeySet.size(); ++i) {
                        String scheduleDate = dateKeySet.get(i);
                        ArrayList<JSONObject> scheduleItems = parsedData.get(scheduleDate);
                        assert scheduleItems != null;
                        int scheduleItemsCount = scheduleItems.size();
                        View scheduleListLayout = inflater.inflate(R.layout.schedule_date, mainLayout, false);
                        TextView displayDate = scheduleListLayout.findViewById(R.id.dateText);
                        displayDate.setText(scheduleDate);
                        mainLayout.addView(scheduleListLayout);
                        for(int j = 0; j < scheduleItemsCount; ++j) {
                            View scheduleListBtn = inflater.inflate(R.layout.schedule_item, mainLayout, false);
                            Button btn = scheduleListBtn.findViewById(R.id.scheduleTitle);
                            TextView desc = scheduleListBtn.findViewById(R.id.scheduleDescription);
                            try {
                                String scheduleTime = scheduleItems.get(j).has("startTime") ? scheduleItems.get(j).getString("startTime") + "  " : "";
                                String scheduleTitle = scheduleTime + scheduleItems.get(j).getString("eventName");
                                btn.setText(scheduleTitle);
                                btn.setOnClickListener((v) -> {
                                    if(desc.getVisibility() == View.VISIBLE) {
                                        desc.setVisibility(View.GONE);
                                    } else {
                                        desc.setVisibility(View.VISIBLE);
                                    }
                                });

                                desc.setText(HtmlCompat.fromHtml(scheduleItems.get(j).getString("description"), HtmlCompat.FROM_HTML_MODE_COMPACT));
                                mainLayout.addView(scheduleListBtn);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings({"BusyWait"})
    private void animateLoadingScreen() {
        Thread loadingThread = new Thread(() -> {
            ImageView loadingView = findViewById(R.id.loadingView);
            Bitmap[] imgs = {
                    BitmapFactory.decodeStream(this.getResources().openRawResource(R.raw.load_1)),
                    BitmapFactory.decodeStream(this.getResources().openRawResource(R.raw.load_2)),
                    BitmapFactory.decodeStream(this.getResources().openRawResource(R.raw.load_3)),
                    BitmapFactory.decodeStream(this.getResources().openRawResource(R.raw.load_4))
            };
            while (isLoading) {
                for (int i = 0; i < 4; ++i) {
                    if (isLoading) {
                        int finalI = i;
                        runOnUiThread(() -> {
                            loadingView.setImageBitmap(imgs[finalI]);
                        });
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
            }
        });
        loadingThread.start();
    }

    @NotNull
    private LinkedHashMap<String, ArrayList<JSONObject>> parseScheduleData(@NotNull JSONArray data) throws JSONException {
        String curDate = data.getJSONObject(0).getString("eventDate"); // Get the first element date
        LinkedHashMap<String, ArrayList<JSONObject>> parsedData = new LinkedHashMap<>(); // Prepare the map to store processed data
        ArrayList<JSONObject> items = new ArrayList<>(); // The array list to store list of schedule for particular date (same date)
        items.add(data.getJSONObject(0)); // Add first element
        for(int i = 1; i < data.length(); ++i) {
            String newDate = data.getJSONObject(i).getString("eventDate");
            if(!curDate.equals(newDate)) {
                parsedData.put(curDate, new ArrayList<>(items)); // if date changed then put all the schedule items into the corresponding date
                curDate = newDate; // date changed so update the current date
                items.clear(); // clear the items if date changed
            }
            items.add(data.getJSONObject(i));
        }
        parsedData.put(curDate, new ArrayList<>(items)); // Add the last schedule item into the map
        return parsedData;
    }
}