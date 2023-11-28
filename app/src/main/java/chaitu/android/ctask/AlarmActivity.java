package chaitu.android.ctask;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import chaitu.android.ctask.activities.NoteActivity;
import chaitu.android.ctask.classes.Task;
import chaitu.android.ctask.database.TaskDatabase;

public class AlarmActivity extends AppCompatActivity {
    private Task task;
    private TextView title,desc,textView3;
    private Button button;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        task=(Task) getIntent().getSerializableExtra("task");
        title=findViewById(R.id.title);
        desc=findViewById(R.id.desc);
        button=findViewById(R.id.dialog_button);
        textView3=findViewById(R.id.textView3);


        button.setOnClickListener(view -> {
            Intent intent=new Intent(AlarmActivity.this, NoteActivity.class);
            intent.putExtra("isViewOrUpdate",true);
            intent.putExtra("isDelete",true);
            intent.putExtra("task",task);
            startActivity(intent);
            finish();
        });
        if (task!=null) {
            switch (task.getPriority()) {
                case LOW:
                    textView3.setText("Low");
                case MEDIUM:
                    textView3.setText("Medium");
                case HIGH:{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        setTurnScreenOn(true);
                    }
                    else {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                |WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                |WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

                    }
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            |WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            |WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

                    Vibrator vibrator=(Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
                    }else vibrator.vibrate(500);


                }

            }
            title.setText(task.getTitle());
            desc.setText(task.getDescription());

        }
        class delTask extends AsyncTask<Void,Void,Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                TaskDatabase.getTaskDatabase(getApplicationContext()).taskDao().deleteTask(task);
                return null;
            }
            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
            }
        }
        new delTask().execute();

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }
}