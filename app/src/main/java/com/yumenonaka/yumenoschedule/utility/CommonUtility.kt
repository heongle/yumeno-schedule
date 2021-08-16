package com.yumenonaka.yumenoschedule.utility

import org.json.JSONArray
import org.json.JSONObject

class CommonUtility {
    companion object {
        fun parseScheduleData(data: JSONArray): LinkedHashMap<String, ArrayList<JSONObject>> {
            var curDate: String = data.getJSONObject(0).getString("eventDate") // Get the first element date
            val parsedData: LinkedHashMap<String, ArrayList<JSONObject>> = LinkedHashMap() // Prepare the map to store processed data
            val items: ArrayList<JSONObject> = ArrayList() // The array list to store list of schedule for particular date (same date)
            items.add(data.getJSONObject(0)) // Add first element
            for (i in 1 until data.length()) {
                val newDate = data.getJSONObject(i).getString("eventDate")
                if (curDate != newDate) {
                    parsedData[curDate] = ArrayList(items) // if date changed then put all the schedule items into the corresponding date
                    curDate = newDate // date changed so update the current date
                    items.clear() // clear the items if date changed
                }
                items.add(data.getJSONObject(i))
            }
            parsedData[curDate] = ArrayList(items) // Add the last schedule item into the map

            return parsedData
        }
    }
}
