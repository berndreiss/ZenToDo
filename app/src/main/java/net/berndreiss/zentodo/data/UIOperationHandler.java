package net.berndreiss.zentodo.data;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

import android.annotation.SuppressLint;
import android.net.http.SslCertificate;
import android.os.Handler;
import android.os.Looper;

import net.berndreiss.zentodo.adapters.TaskListAdapter;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenServerMessage;

import java.util.Collections;
import java.util.List;

public class UIOperationHandler implements ClientOperationHandler{

    public TaskListAdapter adapter = null;

    public UIOperationHandler(){};

    @Override
    public void post(List<Entry> list) {

    }

    @Override
    public void updateId(long l, long l1) {

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
    public void clearQueue() {

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
    public void removeUser(String s) {

    }

    @Override
    public User getUserByEmail(String s) {
        return null;
    }

    @Override
    public boolean userExists(String s) {
        return false;
    }

    @Override
    public boolean isEnabled(String s) {
        return false;
    }

    @Override
    public void enableUser(String s) {

    }

    @Override
    public void setDevice(String s, long l) {

    }

    @Override
    public void setClock(String s, VectorClock vectorClock) {

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public Entry addNewEntry(long id, String task, Long userId, int position) {
        if (adapter == null)
            return null;
        Entry entry = new Entry(id, task, userId, position);
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            adapter.entries.stream()
                    .filter(e -> e.getPosition() >= position)
                    .forEach(e -> e.setPosition(e.getPosition()+1));

            adapter.entries.add(position, entry);
            adapter.notifyDataSetChanged();
        });
        return entry;
    }

    @Override
    public void delete(long l) {

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
