package chaitu.android.ctask.adapters;





import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;

import chaitu.android.ctask.R;
import chaitu.android.ctask.classes.Task;
import chaitu.android.ctask.enums.Priority;
import chaitu.android.ctask.listeners.TaskListener;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskListener taskListener;

    public TaskAdapter(List<Task> taskList, TaskListener taskListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            taskList.sort(Comparator.comparing(Task::getPriority));
        }
        this.taskList = taskList;
        this.taskListener = taskListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
    holder.setNote(taskList.get(position));
    holder.view.setOnClickListener(view -> {
        taskListener.onTaskClicked(view,taskList.get(position),position);
    });
    holder.view.setOnLongClickListener(view -> {
        taskListener.onTaskLongClicked(view,taskList.get(position),position);
        return true;
    });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView title,desc,date,priority;
        View view;
         TaskViewHolder(@NonNull View itemView) {
            super(itemView);
             title=itemView.findViewById(R.id.textTitle);
             desc=itemView.findViewById(R.id.textSubtitle);
             date=itemView.findViewById(R.id.dateTime);
             priority=itemView.findViewById(R.id.priority);

             view=itemView;

        }
        void setNote(Task task){
             title.setText(task.getTitle());
             desc.setText(task.getDescription());
             date.setText(task.getDueDate());
             GradientDrawable drawable=(GradientDrawable)view.getBackground();
             drawable.setColor(task.getColor());
             if (task.getPriority().equals(Priority.HIGH)) {
                priority.setText("HIGH");
                priority.setTextColor(Color.RED);
            } else if (task.getPriority().equals(Priority.MEDIUM)) {
                    priority.setText("Medium");
                    priority.setTextColor(Color.YELLOW);
             }else{
                 priority.setText("Low");
                 priority.setTextColor(Color.WHITE);
             }

        }

    }
}
