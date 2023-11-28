package chaitu.android.ctask.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import chaitu.android.ctask.classes.Task;
import chaitu.android.ctask.dao.TaskDao;

@Database(entities = Task.class,version = 1,exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {

    private static TaskDatabase taskDatabase;

    public static synchronized TaskDatabase getTaskDatabase(Context context){
        if (taskDatabase == null) {
            taskDatabase= Room.databaseBuilder(context,TaskDatabase.class,"tasks_db").build();
        }
        return taskDatabase;
    };
    public abstract TaskDao taskDao();
}
