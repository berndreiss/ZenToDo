package net.berndreiss.zentodo.data;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.os.Handler;

import net.berndreiss.zentodo.Mode;
import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.operations.ClientOperationHandlerI;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenServerMessage;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    //WE ASSUME THE TASK HAS BEEN NEWLY ADDED
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addNewTask(Task task) {
        final TaskListAdapter adapter;
        switch (sharedData.mode){
            case LIST_NO:  adapter = sharedData.noListAdapter; break;
            case LIST_ALL:  adapter = sharedData.allTasksAdapter; break;
            case DROP: adapter = sharedData.dropAdapter; break;
            case PICK: adapter = sharedData.pickAdapter; break;
            default:
                return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            adapter.tasks.add(task);
            adapter.notifyDataSetChanged();
            if (sharedData.mode == Mode.PICK)
                sharedData.pickAdapter.update();
        });
    }

    @Override
    public Task addNewTask(String s) {
        //IGNORED
        return null;
    }

    @Override
    public Task addNewTask(String s, int i) {
        //IGNORED
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void removeTask(long id) {
        switch (sharedData.mode){
            case LIST_NO:  removeFromAdapter(sharedData.noListAdapter, id); break;
            case LIST_ALL:  removeFromAdapter(sharedData.allTasksAdapter, id); break;
            case DROP: removeFromAdapter(sharedData.dropAdapter, id); break;
            case PICK: {
                removeFromAdapter(sharedData.pickAdapter, id);
                removeFromAdapter(sharedData.doNowAdapter, id);
                removeFromAdapter(sharedData.doLaterAdapter, id);
                removeFromAdapter(sharedData.moveToListAdapter, id);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void removeFromAdapter(TaskListAdapter adapter, long id){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            Optional<Task> task = adapter.tasks.stream().filter(t -> t.getId() == id).findFirst();
            if (task.isEmpty())
                return;
            adapter.tasks.removeIf(t -> t.getId() == id);
            adapter.tasks.forEach(t -> {
                if (t.getPosition() > task.get().getPosition())
                    t.setPosition(t.getPosition()-1);
                if (t.getList() != null && Objects.equals(t.getList(), task.get().getList()) && t.getListPosition() > task.get().getListPosition())
                    t.setListPosition(t.getListPosition()-1);
            });
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public Optional<Task> getTask(long l) {
        //IGNORED
        return Optional.empty();
    }

    @Override
    public List<Task> loadTasks() {
        //IGNORED
        return Collections.emptyList();
    }
    @Override
    public List<Task> loadFocus() {
        //IGNORED
        return Collections.emptyList();
    }

    @Override
    public List<Task> loadDropped() {
        //IGNORED
        return Collections.emptyList();
    }

    @Override
    public List<Task> loadList(Long list) {
        //IGNORED
        return Collections.emptyList();
    }

    @Override
    public List<TaskList> loadLists() {
        //IGNORED
        return null;
    }

    @Override
    public Map<Long, String> getListColors() {
        //IGNORED
        return Collections.emptyMap();
    }

    @Override
    public Optional<TaskList> getListByName(String name) {
        //IGNORED
        return Optional.empty();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void swapTasks(long task, int position) {
        switch (sharedData.mode){
            case DROP: swapInAdapter(sharedData.dropAdapter, task, position); break;
            case FOCUS: swapInAdapter(sharedData.focusAdapter, task, position); break;
            case PICK: {
                swapInAdapter(sharedData.pickAdapter, task, position);
                swapInAdapter(sharedData.doNowAdapter, task, position);
                swapInAdapter(sharedData.doLaterAdapter, task, position);
                swapInAdapter(sharedData.moveToListAdapter, task, position);
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void swapInAdapter(TaskListAdapter adapter, long task, int position){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->{
            Optional<Task> task1 = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
            Optional<Task> task2 = adapter.tasks.stream().filter(t -> t.getPosition() == position).findFirst();
            Optional<Task> updatedTask1 = Optional.empty();
            Optional<Task> updatedTask2 = Optional.empty();
            if (task1.isPresent())
                updatedTask1 = sharedData.clientStub.getTask(task);
            if (task2.isPresent())
                updatedTask2 = sharedData.clientStub.getTask(task2.get().getId());
            if (task1.isPresent() && updatedTask1.isPresent()) {
                adapter.tasks.remove(task1.get());
                adapter.tasks.add(updatedTask1.get());
            }
            if (task2.isPresent() && updatedTask2.isPresent()) {
                adapter.tasks.remove(task2.get());
                adapter.tasks.add(updatedTask2.get());
            }
            adapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
            if (!sharedData.itemIsInMotion)
                adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void swapListEntries(long list, long task, int position) {
    }

    @Override
    public void updateTask(long task, String value) {
    }

    @Override
    public void updateFocus(long task, boolean value) {
    }

    @Override
    public void updateDropped(long task, boolean value) {
    }

    @Override
    public void updateList(long task, Long list) {
    }

    @Override
    public void updateReminderDate(long task, Instant value) {
    }

    @Override
    public void updateRecurrence(long task, String value) {
    }

    @Override
    public void updateListColor(long list, String color) {
        //TODO implement
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
        //IGNORED
        return Collections.emptyList();
    }

    @Override
    public TaskList addNewList(String s, String s1) {
        //TODO implement
        return null;
    }
}
