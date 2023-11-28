package chaitu.android.ctask.activities;

import static chaitu.android.ctask.classes.CONSTANTS.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE;
import static chaitu.android.ctask.classes.CONSTANTS.FormatTime;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import chaitu.android.ctask.R;
import chaitu.android.ctask.classes.CONSTANTS;
import chaitu.android.ctask.classes.Task;
import chaitu.android.ctask.database.TaskDatabase;
import chaitu.android.ctask.enums.Priority;
import chaitu.android.ctask.workers.ReminderWorker;
import vadiole.colorpicker.ColorModel;
import vadiole.colorpicker.ColorPickerDialog;

public class NoteActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private EditText editTextDate,TaskTitle,TaskDescription;
    private ImageView buttonChooseColor,saveTask,back;
    public int colorEnvelope;

    public String selectedValue;
    public Task tasks;


    private int selectedColor = Color.WHITE;
    public String date,time;
    View view;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        date=new Date(Calendar.getInstance().getTimeInMillis()).toString();
        colorEnvelope=getResources().getColor(R.color.colorAccent);
        back=findViewById(R.id.imageBack);
        view=findViewById(R.id.icon);
        editTextDate = findViewById(R.id.editTextDate);
        buttonChooseColor=findViewById(R.id.colorpick);
        saveTask=findViewById(R.id.saveTask);
        TaskTitle=findViewById(R.id.taskTitle);
        TaskDescription=findViewById(R.id.taskDescription);

        editTextDate.setOnClickListener(this::showDatePickerDialog);

        buttonChooseColor.setOnClickListener(v -> showColorPickerDialog());

        // Set up the Spinner for task priority
        Spinner spinnerPriority = findViewById(R.id.spinnerPriority);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_options, R.layout.item);
        adapter.setDropDownViewResource(R.layout.item);
        spinnerPriority.setAdapter(adapter);
        int defaultPosition = 1; // Set the position of the default item
        spinnerPriority.setSelection(defaultPosition);
        selectedValue="Medium";

        spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedValue = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedValue="Medium";
            }
        });



        saveTask.setOnClickListener(view -> {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !Settings.canDrawOverlays(getApplicationContext())) {
                RequestPermission();
            }else addNote();
        });
        back.setOnClickListener(view -> finish());
        setColor(colorEnvelope);
        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            tasks = (Task)getIntent().getSerializableExtra("task");
            if (tasks != null) {
                editTextDate.setText(tasks.getDueDate());
                TaskTitle.setText(tasks.getTitle());
                TaskDescription.setText(tasks.getDescription());
                colorEnvelope= tasks.getColor();
                if (tasks.getPriority().equals(Priority.HIGH)) {
                    spinnerPriority.setSelection(0);
                } else if (tasks.getPriority().equals(Priority.MEDIUM)) {
                    spinnerPriority.setSelection(1);
                }else spinnerPriority.setSelection(2);
                setColor(colorEnvelope);
            }

        }


    }

    private void addNote() {
        date=editTextDate.getText().toString().trim();
       final Task task;
        String title=TaskTitle.getText().toString().trim();
        String description=TaskDescription.getText().toString().trim();
        if (title.isEmpty()){
            showSnackBar(saveTask,"Please Enter Title");
            return;
        } else if (description.isEmpty()) {
            showSnackBar(saveTask,"Please Enter Description");
            return;
        }else {
            if (Objects.equals(selectedValue, "High")) {
                task=new Task(title,description, date, Priority.HIGH,colorEnvelope);
            } else if (Objects.equals(selectedValue, "Medium")) {
                task=new Task(title,description, date,Priority.MEDIUM,colorEnvelope);
                }else{
                task=new Task(title,description, date,Priority.LOW,colorEnvelope);

            }
        }
        @SuppressLint("StaticFieldLeak")
        class SaveTask extends AsyncTask<Void,Void,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                if (tasks != null) {
                    try {
                        TaskDatabase.getTaskDatabase(getApplicationContext()).taskDao().deleteTask(tasks);
                    } catch (Exception e) {
                        Log.d("tag",e.getMessage());
                    }
                }
                TaskDatabase.getTaskDatabase(getApplicationContext()).taskDao().insertTask(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                setAlarm(task);
                Intent intent=new Intent();
                setResult(RESULT_OK,intent);
                finish();


            }
        }
        new SaveTask().execute();



    }
    private void RequestPermission() {
        // Check if Android M or higher
        // Show alert dialog to the user saying a separate permission is needed
        // Launch the settings activity if the user prefers
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    }
    private void setAlarm(Task task) {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm");

        try {
            java.util.Date date1 = formatter.parse(task.getDueDate());
            // 7 Next get DateTime for today
            Calendar todayDateTime = Calendar.getInstance();

            // 8
            long delayInSeconds = ((date1.getTime()) - (todayDateTime.getTimeInMillis()))/1000;

            createWorkRequest(task, delayInSeconds+43261);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void createWorkRequest(Task task, long delayInSeconds) {
        Constraints constraints=new Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .setRequiresStorageNotLow(false).build();
        OneTimeWorkRequest myWorkRequest =
                new OneTimeWorkRequest.Builder(ReminderWorker.class)
                        .setConstraints(constraints)
                        .setInitialDelay(delayInSeconds,TimeUnit.SECONDS)

                        .setInputData(CONSTANTS.createInputDataForUri(CONSTANTS.toString(task)))
                        .setBackoffCriteria(BackoffPolicy.LINEAR,OneTimeWorkRequest.MIN_BACKOFF_MILLIS,TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueue(myWorkRequest);
        Toast.makeText(this, delayInSeconds+"", Toast.LENGTH_SHORT).show();
    }


    private void showSnackBar(View v,String text) {
        Snackbar snackbar = Snackbar.make(
                v, // Pass the view reference
                text, // The message you want to display
                Snackbar.LENGTH_LONG // The duration of the Snackbar (LENGTH_LONG or LENGTH_SHORT)
        );
        View snackbarView = snackbar.getView(); // Get the Snackbar's view
        snackbarView.setBackgroundColor(Color.parseColor("#FF4081")); // Set background color
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        Drawable icon = getDrawable(R.drawable.baseline_error_24);
        icon.setTint(getResources().getColor(R.color.colorIcons));
        textView.setCompoundDrawablesWithIntrinsicBounds(icon,null,null,null);// Set text color


        snackbar.setActionTextColor(Color.YELLOW); // Set action text color
        snackbar.show(); // Display the Snackbar

    }

    private void showColorPickerDialog() {
      ColorPickerDialog dialog=  new ColorPickerDialog.Builder()
                .setInitialColor(colorEnvelope)
                .setColorModel(ColorModel.RGB)
                .setColorModelSwitchEnabled(true)
                .setButtonOkText(R.string.confirm)
                .setButtonCancelText(R.string.cancel)
                .onColorSelected(this::setColor)
                .create();
      dialog.setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.Base_ThemeOverlay_AppCompat_Dark);

      dialog.show(getSupportFragmentManager(),"hi");


    }

    private void setColor(int envelope) {
        colorEnvelope=envelope;
        GradientDrawable drawable= (GradientDrawable)view.getBackground();
        drawable.setColor(colorEnvelope);
    }





    public void showDatePickerDialog(View view) {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view1, year1, month1, dayOfMonth) -> {
                    // Update the editTextDate with the selected date
                    editTextDate.setText(dayOfMonth + "-" + (month1 + 1) + "-" + year1);
                    int hour=calendar.get(Calendar.HOUR);
                    int min=calendar.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog=new TimePickerDialog(NoteActivity.this,NoteActivity.this,hour,min, true);
                    timePickerDialog.show();

                }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        String date=editTextDate.getText().toString().trim();
        editTextDate.setText(date+" "+FormatTime(hour,minute));
        time=hour+":"+minute;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(NoteActivity.this)) {
                showSnackBar(getWindow().getDecorView(),"Please enable it to notify you with high priority");
            } else {
                addNote();
                // Permission Granted-System will work
            }

        }
    }
}

