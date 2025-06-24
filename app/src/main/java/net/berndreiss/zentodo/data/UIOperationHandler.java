package net.berndreiss.zentodo.data;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.os.Handler;

import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.DropTaskListAdapter;
import net.berndreiss.zentodo.adapters.FocusTaskListAdapter;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
import net.berndreiss.zentodo.adapters.PickTaskListAdapter;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.operations.ClientOperationHandlerI;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenServerMessage;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the DataManagerI interface using SQLite and the ZenSQLiteHelper.
 */
public class UIOperationHandler implements ClientOperationHandlerI {

    public SharedData sharedData;

    /**
     * Create a new instance of the ui operation handler.
     * @param sharedData the data shared across the app
     */
    public UIOperationHandler(@NotNull SharedData sharedData){
        this.sharedData = sharedData;
    }

    @Override
    public void post(List<Task> list) {
        //IGNORED
    }

    @Override
    public void updateId(long user, int profile, long task, long newId) {
        if (sharedData.adapter == null)
            return;
        sharedData.adapter.tasks.stream().filter(t -> t.getId() == task).forEach(t -> t.setId(newId));
    }

    @Override
    public void setTimeDelay(long l) {
        //IGNORED
    }

    @Override
    public void addToQueue(User user, ZenServerMessage zenServerMessage) {
        //IGNORED
    }

    @Override
    public List<ZenServerMessage> getQueued(long l) {
        //IGNORED
        return Collections.emptyList();
    }

    @Override
    public void clearQueue(long l) {
        //IGNORED
    }

    @Override
    public Optional<String> getToken(long l) {
        //IGNORED
        return Optional.empty();
    }

    @Override
    public void setToken(long l, String s) {
        //IGNORED
    }

    @Override
    public User addUser(long l, String s, String s1, long l1) {
        //IGNORED
        return null;
    }

    @Override
    public void removeUser(long l) {
        //IGNORED
    }

    @Override
    public Optional<User> getUserByEmail(String s) {
        //IGNORED
        return Optional.empty();
    }

    @Override
    public boolean userExists(long l) {
        //IGNORED
        return false;
    }

    @Override
    public boolean isEnabled(long l) {
        //IGNORED
        return false;
    }

    @Override
    public void enableUser(long l) {
        //IGNORED
    }

    @Override
    public void setDevice(long l, long l1) {
        //IGNORED
    }

    @Override
    public void setClock(long l, VectorClock vectorClock) {
        //IGNORED
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addNewTask(Task task) {
        if (sharedData.adapter == null)
            return;
        //Dropped tasks are not to be added to focus adapters
        if (sharedData.adapter instanceof FocusTaskListAdapter)
            return;
        if (sharedData.adapter instanceof ListTaskListAdapter && task.getList() != null){
            Optional<TaskList> list = sharedData.clientStub.getListByName(((ListTaskListAdapter) sharedData.adapter).taskList);
            if (list.isEmpty())
                //TODO I think this is undefined behaviour
                return;
            if (task.getList() != list.get().getId())
                return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        final TaskListAdapter adapter;
        if (sharedData.adapter instanceof PickTaskListAdapter)
            adapter = ((PickTaskListAdapter) sharedData.adapter).pickAdapter;
        else
            adapter = sharedData.adapter;
        handler.post(() -> {
            adapter.tasks.stream()
                    .filter(e -> e.getPosition() >= task.getPosition())
                    .forEach(e -> e.setPosition(e.getPosition() + 1));

            adapter.tasks.add(task.getPosition(), task);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public Task addNewTask(String s) {
        if (sharedData.adapter == null)
            return null;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
        return null;
    }

    @Override
    public Task addNewTask(String s, int i) {
        if (sharedData.adapter == null)
            return null;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
        return null;
    }

    @Override
    public void removeTask(long id) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public Optional<Task> getTask(long l) {
        return Optional.empty();
    }

    @Override
    public List<Task> loadTasks() {
        return Collections.emptyList();
    }
    @Override
    public List<Task> loadFocus() {
        return Collections.emptyList();
    }

    @Override
    public List<Task> loadDropped() {
        return Collections.emptyList();
    }

    @Override
    public List<Task> loadList(Long list) {
        return Collections.emptyList();
    }

    @Override
    public List<TaskList> loadLists() {
        return null;
    }

    @Override
    public Map<Long, String> getListColors() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<TaskList> getListByName(String name) {
        return Optional.empty();
    }

    @Override
    public void swapTasks(long task, int position) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void swapListEntries(long l, long l1, int i) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void updateTask(long l, String s) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void updateFocus(long l, boolean b) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void updateDropped(long l, boolean b) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void updateList(long l, Long aLong) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void updateReminderDate(long l, Instant instant) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void updateRecurrence(long l, String s) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void updateListColor(long list, String color) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> sharedData.adapter.reset());
    }

    @Override
    public void updateUserName(String s) {
        //TODO implement
    }

    @Override
    public void updateEmail(String s) {
        //TODO implement
    }

    @Override
    public List<TaskList> getLists() {
        return Collections.emptyList();
    }

    @Override
    public TaskList addNewList(String s, String s1) {
        //TODO implement
        return null;
    }
}
