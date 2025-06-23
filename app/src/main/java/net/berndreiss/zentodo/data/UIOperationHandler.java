package net.berndreiss.zentodo.data;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.os.Handler;

import net.berndreiss.zentodo.SharedData;
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

    }

    @Override
    public void updateId(long l, long l1, long l2, long l3) {

    }


    @Override
    public void setTimeDelay(long l) {

    }

    @Override
    public void addToQueue(User user, ZenServerMessage zenServerMessage) {

    }

    @Override
    public List<ZenServerMessage> getQueued(long l) {
        return Collections.emptyList();
    }

    @Override
    public void clearQueue(long l) {

    }


    @Override
    public Optional<String> getToken(long l) {
        return Optional.empty();
    }

    @Override
    public void setToken(long l, String s) {

    }

    @Override
    public User addUser(long l, String s, String s1, long l1) {
        return null;
    }

    @Override
    public void removeUser(long l) {

    }


    @Override
    public Optional<User> getUserByEmail(String s) {
        return Optional.empty();
    }

    @Override
    public boolean userExists(long l) {
        return false;
    }

    @Override
    public boolean isEnabled(long l) {
        return false;
    }

    @Override
    public void enableUser(long l) {

    }

    @Override
    public void setDevice(long l, long l1) {

    }

    @Override
    public void setClock(long l, VectorClock vectorClock) {

    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addNewTask(Task Task) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            sharedData.adapter.tasks.stream()
                    .filter(e -> e.getPosition() >= Task.getPosition())
                    .forEach(e -> e.setPosition(e.getPosition()+1));

            sharedData.adapter.tasks.add(Task.getPosition(), Task);
            sharedData.adapter.notifyDataSetChanged();
        });
    }

    @Override
    public Task addNewTask(String s) {
        return null;
    }

    @Override
    public Task addNewTask(String s, int i) {
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void removeTask(long id) {
        if (sharedData.adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            Optional<Task> Task = sharedData.adapter.tasks.stream().filter(e -> e.getId() == id).findFirst();
            if (Task.isEmpty())
                return;
            sharedData.adapter.tasks.stream()
                    .filter(t -> t.getPosition() > Task.get().getPosition())
                    .forEach(t -> t.setPosition(t.getPosition()-1));

            sharedData.adapter.tasks.stream()
                    .filter(t -> t.getList() != null)
                    .filter(t -> t.getList().equals(t.getList()))
                    .filter(t -> t.getListPosition() > Task.get().getListPosition())
                    .forEach(t -> t.setListPosition(t.getListPosition()-1));

            sharedData.adapter.tasks.remove(Task.get().getPosition());
            sharedData.adapter.notifyDataSetChanged();
        });
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
    public void swapTasks(long l, int i) {

    }

    @Override
    public void swapListEntries(long l, long l1, int i) {

    }

    @Override
    public void updateTask(long l, String s) {

    }

    @Override
    public void updateFocus(long l, boolean b) {

    }

    @Override
    public void updateDropped(long l, boolean b) {

    }

    @Override
    public void updateList(long l, Long aLong) {

    }

    @Override
    public void updateReminderDate(long l, Instant instant) {

    }

    @Override
    public void updateRecurrence(long l, String s) {

    }

    @Override
    public void updateListColor(long list, String color) {

    }

    @Override
    public void updateUserName(String s) {

    }

    @Override
    public void updateEmail(String s) {

    }
}
