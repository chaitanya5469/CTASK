package chaitu.android.ctask.listeners;

import android.view.View;

import chaitu.android.ctask.classes.Task;

public interface TaskListener {
    void onTaskClicked(View view,Task task,int position);
    void onTaskLongClicked(View view,Task task, int position);
}
