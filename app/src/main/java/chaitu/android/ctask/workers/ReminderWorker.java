package chaitu.android.ctask.workers;

import static chaitu.android.ctask.classes.CONSTANTS.toTask;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import chaitu.android.ctask.AlarmActivity;
import chaitu.android.ctask.R;
import chaitu.android.ctask.activities.MainActivity;
import chaitu.android.ctask.activities.NoteActivity;
import chaitu.android.ctask.classes.Task;
import chaitu.android.ctask.database.TaskDatabase;
import chaitu.android.ctask.enums.Priority;

public class ReminderWorker extends Worker {
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Task task=toTask(getInputData().getString("task"));
        NotificationCompat.Builder notification=new NotificationCompat.Builder(getApplicationContext(),"notify_001")
                .setSmallIcon(R.drawable.logo_no_background)
                .setContentTitle(task.getTitle())
                .setContentText(task.getDescription())
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setAutoCancel(false)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        setForegroundAsync(new ForegroundInfo(0,notification.build()));
        if (task.getPriority().equals(Priority.HIGH)) {
            //We ll see abt that
            if (Settings.canDrawOverlays(getApplicationContext())) {
                Intent intent=new Intent(getApplicationContext(), AlarmActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(Intent.ACTION_NEW_OUTGOING_CALL);
                intent.putExtra("task",task);

                getApplicationContext().startActivity(intent);
            } else if (!Settings.canDrawOverlays(getApplicationContext())) {
                createNotification(task);
            }
        }else {
            createNotification(task);
        }
        TaskDatabase.getTaskDatabase(getApplicationContext()).taskDao().deleteTask(task);

        return Result.success();
    }

    private void createNotification(@NonNull Task task) {
        String title = task.getTitle();
        String date = task.getDueDate();
        //Click on Notification
        Intent intent=new Intent(getApplicationContext(),AlarmActivity.class);
        intent.putExtra("task",task);
        //Notification Builder
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        //here we set all the properties for the notification
        RemoteViews contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.image, R.drawable.logo_no_background);

         Intent intent1=new Intent(getApplicationContext(),NoteActivity.class);
        intent1.putExtra("task",task);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent1, PendingIntent.FLAG_IMMUTABLE);
        contentView.setOnClickPendingIntent(R.id.flashButton, pendingSwitchIntent);
        contentView.setTextViewText(R.id.message, title);
        contentView.setTextViewText(R.id.date, date);
        mBuilder.setSmallIcon(R.drawable.alarm_24);
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(true);
        mBuilder.setAutoCancel(true);
        if (task.getPriority().equals(Priority.MEDIUM))
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        else mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.build().flags = Notification.FLAG_NO_CLEAR | Notification.PRIORITY_HIGH;
        mBuilder.setContent(contentView);
        mBuilder.setContentIntent(pendingIntent);
        //we have to create notification channel after api level 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }
        Notification notification = mBuilder.build();
        notificationManager.notify(1, notification);

    }
}
