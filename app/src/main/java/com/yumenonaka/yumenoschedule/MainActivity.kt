package com.yumenonaka.yumenoschedule

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.yumenonaka.yumenoschedule.apis.YumenoApis
import com.yumenonaka.yumenoschedule.utility.CommonUtility
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var isLoading = true
    private lateinit var mainLayout: ViewGroup

    override fun onResume() {
        super.onResume()
        isLoading = true
        setContentView(R.layout.loading)
        animateLoadingScreen()
        initialize()
    }

    override fun onPause() {
        super.onPause()
        isLoading = false
    }

    private fun initialize() {
        val executor: Executor =  Executors.newSingleThreadExecutor()
        executor.execute {
            val response: String = YumenoApis.getRecentSchedule()
            val data: JSONArray = JSONObject(response).getJSONArray("data")
            val parsedData: LinkedHashMap<String, ArrayList<JSONObject>> = CommonUtility.parseScheduleData(data)
            val dateKeySet: List<String> = ArrayList(parsedData.keys)
            runOnUiThread {
                isLoading = false
                setContentView(R.layout.activity_main)
                val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefresh)
                swipeRefreshLayout.setOnRefreshListener{
                    isLoading = true
                    setContentView(R.layout.loading)
                    animateLoadingScreen()
                    swipeRefreshLayout.isRefreshing = false
                    initialize()
                }

                mainLayout = findViewById(R.id.mainLayout)
                for (i in dateKeySet.indices) {
                    val scheduleDate: String = dateKeySet[i]
                    val scheduleItems: ArrayList<JSONObject> = parsedData[scheduleDate]!!
                    val scheduleItemsCount: Int = scheduleItems.size
                    val scheduleListLayout: View = layoutInflater.inflate(R.layout.schedule_date, mainLayout, false)
                    val displayDate: TextView = scheduleListLayout.findViewById(R.id.dateText)
                    displayDate.text = scheduleDate
                    mainLayout.addView(scheduleListLayout)
                    for (j in 0 until scheduleItemsCount) {
                        val scheduleListBtn: View = layoutInflater.inflate(R.layout.schedule_item, mainLayout, false)
                        val btn: Button = scheduleListBtn.findViewById(R.id.scheduleTitle)
                        val desc: TextView = scheduleListBtn.findViewById(R.id.scheduleDescription)
                        val scheduleTime: String = if(scheduleItems[j].has("startTime")) scheduleItems[j].getString("startTime") + "  " else ""
                        val scheduleTitle: String = scheduleTime + scheduleItems[j].getString("eventName")
                        val scheduleDescription: String = if(scheduleItems[j].has("description")) scheduleItems[j].getString("description") else "N/A"
                        btn.text = scheduleTitle
                        btn.setOnClickListener {
                            desc.visibility = if (desc.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }
                        desc.text = HtmlCompat.fromHtml(scheduleDescription, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        mainLayout.addView(scheduleListBtn)
                    }
                }
            }
        }
    }

    private fun animateLoadingScreen() {
        val loadingThread = Thread {
            val loadingView: ImageView = findViewById(R.id.loadingView)
            val imgs: Array<Bitmap> = arrayOf (
                BitmapFactory.decodeStream(this.resources.openRawResource(R.raw.load_1)),
                BitmapFactory.decodeStream(this.resources.openRawResource(R.raw.load_2)),
                BitmapFactory.decodeStream(this.resources.openRawResource(R.raw.load_3)),
                BitmapFactory.decodeStream(this.resources.openRawResource(R.raw.load_4))
            )
            while (isLoading) {
                for (i in 0..3) {
                    if (!isLoading) break
                    runOnUiThread {
                        loadingView.setImageBitmap(imgs[i])
                    }
                    Thread.sleep(250)
                }
            }
        }
        loadingThread.start()
    }
}
