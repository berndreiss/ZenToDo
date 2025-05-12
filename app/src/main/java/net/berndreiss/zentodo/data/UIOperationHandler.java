package net.berndreiss.zentodo.data;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenServerMessage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UIOperationHandler implements ClientOperationHandler{

    public TaskListAdapter adapter = null;

    public UIOperationHandler(){};

    @Override
    public void post(List<Entry> list) {

    }

    @Override
    public void updateId(long l, long l1, long l2) {

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
    public Entry addNewEntry(Entry entry) {
        if (adapter == null)
            return null;
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            adapter.entries.stream()
                    .filter(e -> e.getPosition() >= entry.getPosition())
                    .forEach(e -> e.setPosition(e.getPosition()+1));

            adapter.entries.add(entry.getPosition(), entry);
            adapter.notifyDataSetChanged();
        });
        return entry;
    }

    @Override
    public Entry addNewEntry(String s) {
        return null;
    }

    @Override
    public Entry addNewEntry(String s, int i) {
        return null;
    }

    @Override
    public void removeEntry(long l) {

    }

    @Override
    public Optional<Entry> getEntry(long l) {
        return Optional.empty();
    }

    @Override
    public List<Entry> loadEntries() {
        return Collections.emptyList();
    }
    @Override
    public List<Entry> loadFocus() {
        return Collections.emptyList();
    }

    @Override
    public List<Entry> loadDropped() {
        return Collections.emptyList();
    }

    @Override
    public List<Entry> loadList(String s) {
        return Collections.emptyList();
    }

    @Override
    public List<String> loadLists() {
        return null;
    }

    @Override
    public Map<String, String> getListColors() {
        return Collections.emptyMap();
    }

    @Override
    public void swapEntries(long l, int i) {

    }

    @Override
    public void swapListEntries(long l, int i) {

    }

    @Override
    public void updateTask(long l, String s) {

    }

    @Override
    public void updateFocus(long l, int i) {

    }

    @Override
    public void updateDropped(long l, int i) {

    }

    @Override
    public void updateList(long l, String s, int i) {

    }

    @Override
    public void updateReminderDate(long l, Long aLong) {

    }

    @Override
    public void updateRecurrence(long l, Long aLong, String s) {

    }

    @Override
    public void updateListColor(String s, String s1) {

    }

    @Override
    public void updateUserName(long l, String s) {

    }

    @Override
    public boolean updateEmail(long l, String s) {
        return false;
    }

}
