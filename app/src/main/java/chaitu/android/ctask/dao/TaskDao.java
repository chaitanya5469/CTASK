package chaitu.android.ctask.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import chaitu.android.ctask.classes.Task;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM Tasks ORDER BY id DESC")
    List<Task> getAllTasks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(Task task);

    @Delete
    void deleteTask(Task task);
}
