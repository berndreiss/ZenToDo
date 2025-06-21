package net.berndreiss.zentodo.data;


import android.annotation.SuppressLint;
import android.os.Looper;
import android.os.Handler;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.exceptions.InvalidActionException;
import net.berndreiss.zentodo.exceptions.PositionOutOfBoundException;
import net.berndreiss.zentodo.operations.ClientOperationHandlerI;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenServerMessage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UIOperationHandler implements ClientOperationHandlerI {

    public TaskListAdapter adapter = null;

    public UIOperationHandler(){};

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
    public String getToken(long l) {
        return "";
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
    public User getUserByEmail(String s) {
        return null;
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
    public Task addNewTask(Task Task) {
        if (adapter == null)
            return null;
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            adapter.tasks.stream()
                    .filter(e -> e.getPosition() >= Task.getPosition())
                    .forEach(e -> e.setPosition(e.getPosition()+1));

            adapter.tasks.add(Task.getPosition(), Task);
            adapter.notifyDataSetChanged();
        });
        return Task;
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
        if (adapter == null)
            return;
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            Optional<Task> Task = adapter.tasks.stream().filter(e -> e.getId() == id).findFirst();
            if (Task.isEmpty())
                return;
            adapter.tasks.stream()
                    .filter(e -> e.getPosition() > Task.get().getPosition())
                    .forEach(e -> e.setPosition(e.getPosition()-1));

            adapter.tasks.stream()
                    .filter(e -> e.getList() != null)
                    .filter(e -> e.getList().equals(e.getList()))
                    .filter(e -> e.getListPosition() > Task.get().getListPosition())
                    .forEach(e -> e.setListPosition(e.getListPosition()-1));

            adapter.tasks.remove(Task.get().getPosition());
            adapter.notifyDataSetChanged();
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
    public void swapListEntries(long l, long l1, int i) throws PositionOutOfBoundException {

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
    public void updateEmail(String s) throws InvalidActionException, IOException, URISyntaxException {

    }
}
