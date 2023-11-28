package chaitu.android.ctask.classes;

import androidx.work.Data;

import com.google.gson.Gson;

public class CONSTANTS {
    public static final int REQUEST_CODE_ADD_NOTE=1;
    public static final int REQUEST_CODE_UPDATE_NOTE=2;
    public static final int REQUEST_CODE_SHOW_NOTE=3;
    public static final int REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 1001;
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 2323;


    public static String FormatTime(int hour, int minute) {                                                //this method converts the time into 12hr format and assigns am or pm
        String time;
        time = "";
        String formattedMinute;
        if (minute / 10 == 0) {
            formattedMinute = "0" + minute;
        } else {
            formattedMinute = "" + minute;
        }
        if (hour == 0) {
            time = "12" + ":" + formattedMinute + " AM";
        } else if (hour < 12) {
            time = hour + ":" + formattedMinute + " AM";
        } else if (hour == 12) {
            time = "12" + ":" + formattedMinute + " PM";
        } else {
            int temp = hour - 12;
            time = temp + ":" + formattedMinute + " PM";
        }
        return time;
    }
    public static String toString(Task task){
        Gson gson = new Gson();
        return gson.toJson(task);
    }
    public static Task toTask(String s) {
        Gson gson = new Gson();
        return gson.fromJson(s,Task.class);
    }
    public static Data createInputDataForUri(String s) {
        Data.Builder builder = new Data.Builder();
        if (s != null) {
            builder.putString("task", s);
        }
        return builder.build();
    }
}
