package chaitu.android.ctask.activities;

import static chaitu.android.ctask.classes.CONSTANTS.REQUEST_CODE_ADD_NOTE;
import static chaitu.android.ctask.classes.CONSTANTS.REQUEST_CODE_SHOW_NOTE;
import static chaitu.android.ctask.classes.CONSTANTS.REQUEST_CODE_UPDATE_NOTE;
import static chaitu.android.ctask.classes.CONSTANTS.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import chaitu.android.ctask.R;
import chaitu.android.ctask.adapters.TaskAdapter;
import chaitu.android.ctask.classes.CONSTANTS;
import chaitu.android.ctask.classes.Task;
import chaitu.android.ctask.database.TaskDatabase;
import chaitu.android.ctask.listeners.TaskListener;

public class MainActivity extends AppCompatActivity implements TaskListener {
    ImageView addTask,cancel,delete;
    public RecyclerView tasks;
    public TaskAdapter taskAdapter;
    public List<Task>taskList,selectedTasks;
    public TextView taskView,textView;
    public LinearLayout layout;
    public boolean isMultiselect=false;
    public int noteSelectedPosition=-1;
    public AlertDialog dialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskList=new ArrayList<>();
        selectedTasks=new ArrayList<>();
        taskAdapter=new TaskAdapter(taskList, this);
        taskView=findViewById(R.id.tasksview);
        textView=findViewById(R.id.text);
        layout=findViewById(R.id.multi);
        cancel=findViewById(R.id.cancel);
        delete=findViewById(R.id.delete);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            // App is not exempted from battery optimization restrictions
            // Handle the scenario where background execution may be limited
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog, findViewById(R.id.label));
            TextView tv = view.findViewById(R.id.delText);

            tv.setText("To Remind you efficiently, Please grant permission to run in background");
            tv.setCompoundDrawables(null, null, null, null);
            builder.setView(view);
            dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.confirm).setOnClickListener(view1 -> {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivityForResult(intent, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            });
            view.findViewById(R.id.cancel).setOnClickListener(view1 -> {
                dialog.dismiss();
                showSnackBar(getWindow().getDecorView(), "Please enable battery optimization to remind tasks efficiently");
            });
            dialog.show();
        }
        addTask=findViewById(R.id.addTask);
        tasks=findViewById(R.id.recycleView);
        tasks.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        addTask.setOnClickListener(v -> startActivityForResult(new Intent(getApplicationContext(),NoteActivity.class),REQUEST_CODE_ADD_NOTE));
        getTasks(REQUEST_CODE_SHOW_NOTE);
        tasks.setAdapter(taskAdapter);
        setLayout();

        cancel.setOnClickListener(view -> {
            setLayout();
            for (int i=0;i<taskList.size();i++){
                Task task=taskList.get(i);
                if (selectedTasks.contains(task)) {
                    this.onTaskClicked(tasks.getChildAt(i),task,i);
                    selectedTasks.remove(task);
                }

                if (i == taskList.size() - 1) {
                    isMultiselect=false;
                }

            }

        });
        delete.setOnClickListener(view -> deleteTasks());
    }
    @SuppressLint("MissingInflatedId")
    private void deleteTasks() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view= LayoutInflater.from(this).inflate(R.layout.dialog, findViewById(R.id.label));
        TextView tv= view.findViewById(R.id.delText);

        tv.setText("Are U sure to delete "+selectedTasks.size()+" tasks?");
        builder.setView(view);
        dialog=builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.confirm).setOnClickListener(view1 -> {
                @SuppressLint("StaticFieldLeak")
                class delTask extends AsyncTask<Void,Void,Void>{
                    @Override
                    protected Void doInBackground(Void... voids) {
                        for (int i=0;i<selectedTasks.size();i++){
                            TaskDatabase.getTaskDatabase(getApplicationContext()).taskDao().deleteTask(selectedTasks.get(i));
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        showSnackBar(tasks,"Tasks Deleted Successfully");
                        dialog.dismiss();
                        cancel.callOnClick();
                        getTasks(4);
                    }
                }
                new delTask().execute();
            });
            view.findViewById(R.id.cancel).setOnClickListener(view1 -> {
                dialog.dismiss();
                cancel.callOnClick();
            });
            dialog.show();
        }
    private void showSnackBar(View v,String text) {
        Snackbar snackbar = Snackbar.make(
                v, // Pass the view reference
                text, // The message you want to display
                Snackbar.LENGTH_LONG // The duration of the Snackbar (LENGTH_LONG or LENGTH_SHORT)
        );
        View snackbarView = snackbar.getView(); // Get the Snackbar's view
        snackbarView.setBackgroundColor(Color.parseColor("#FF0ADD26")); // Set background color
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        Drawable icon = getResources().getDrawable(R.drawable.done_24);
        icon.setTint(getResources().getColor(R.color.colorIcons));
        textView.setCompoundDrawablesWithIntrinsicBounds(icon,null,null,null);// Set text color



        snackbar.setActionTextColor(Color.YELLOW); // Set action text color
        snackbar.show(); // Display the Snackbar

    }


    private void setLayout() {
        if (isMultiselect) {
            layout.setVisibility(View.VISIBLE);
            taskView.setVisibility(View.INVISIBLE);
        }else{
            layout.setVisibility(View.INVISIBLE);
            taskView.setVisibility(View.VISIBLE);
        }
        if (selectedTasks.isEmpty()) {
            isMultiselect=false;
            layout.setVisibility(View.GONE);
            taskView.setVisibility(View.VISIBLE);
        }else{
            textView.setText(selectedTasks.size()+" Selected");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE&&resultCode==RESULT_OK) {
            getTasks(REQUEST_CODE_ADD_NOTE);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getTasks(REQUEST_CODE_UPDATE_NOTE);
            }
        } else if (requestCode==REQUEST_IGNORE_BATTERY_OPTIMIZATIONS&&resultCode!=RESULT_OK) {
            showSnackBar(getWindow().getDecorView(),"Please enable battery optimization to remind tasks efficiently");
        }
    }

    private void getTasks(final int requestCode){
        @SuppressLint("StaticFieldLeak")
        class getTasks extends AsyncTask<Void,Void,List<Task>>{

            @Override
            protected List<Task> doInBackground(Void... voids) {
                return TaskDatabase.getTaskDatabase(getApplicationContext()).taskDao().getAllTasks();
            }

            @Override
            protected void onPostExecute(List<Task> list) {
                super.onPostExecute(list);

                if (requestCode==REQUEST_CODE_SHOW_NOTE) {
                    taskList.addAll(list);
                    taskAdapter.notifyDataSetChanged();
                }else if (requestCode==REQUEST_CODE_ADD_NOTE){
                    if (!list.isEmpty()) {
                        taskList.add(0, list.get(0));
                        taskAdapter.notifyItemInserted(0);
                        tasks.smoothScrollToPosition(0);
                    }
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    taskList.remove(noteSelectedPosition);
                    taskList.add(noteSelectedPosition,list.get(noteSelectedPosition));
                    taskAdapter.notifyItemChanged(noteSelectedPosition);
                    tasks.smoothScrollToPosition(noteSelectedPosition);
                } else if (requestCode == 4) {
                    taskList.clear();
                    taskList.addAll(list);
                    taskAdapter.notifyDataSetChanged();
                }


            }
        }
        new getTasks().execute();

    }



    @Override
    public void onTaskClicked(View view,Task task, int position) {
        if (!isMultiselect) {
            noteSelectedPosition=position;
            Intent intent=new Intent(getApplicationContext(),NoteActivity.class);
            intent.putExtra("isViewOrUpdate",true);
            intent.putExtra("task",task);
            startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
        }else {
            if (!selectedTasks.contains(task)) {
                selectedTasks.add(task);
                view.setBackgroundDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.bg_search));
            }else{
                selectedTasks.remove(task);
                GradientDrawable drawable= (GradientDrawable) AppCompatResources.getDrawable(MainActivity.this,R.drawable.bg_task);
                drawable.setColor(task.getColor());
                view.setBackgroundDrawable(drawable);


            }
        }
        if (selectedTasks.isEmpty()) {
            isMultiselect=false;
        }
        setLayout();
    }

    @Override
    public void onTaskLongClicked(View view,Task task, int position) {
        if (!isMultiselect) {
            isMultiselect=true;
        }
        if (!selectedTasks.contains(task)) {
            selectedTasks.add(task);
            view.setBackgroundDrawable(AppCompatResources.getDrawable(MainActivity.this,R.drawable.bg_search));
        }else{
            selectedTasks.remove(task);
            GradientDrawable drawable= (GradientDrawable) AppCompatResources.getDrawable(MainActivity.this,R.drawable.bg_task);
            drawable.setColor(task.getColor());
            view.setBackgroundDrawable(drawable);
        }
        if (selectedTasks.isEmpty()) {
            isMultiselect=false;
        }
        setLayout();
    }
}