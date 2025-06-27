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
        switch (sharedData.mode){
            case DROP -> updateIdForAdapter(sharedData.dropAdapter, task, newId);
            case FOCUS -> updateIdForAdapter(sharedData.focusAdapter, task, newId);
            case LIST -> updateIdForAdapter(sharedData.listAdapter, task, newId);
            case LIST_NO -> updateIdForAdapter(sharedData.noListAdapter, task, newId);
            case LIST_ALL -> updateIdForAdapter(sharedData.allTasksAdapter, task, newId);
            case PICK -> {
                updateIdForAdapter(sharedData.pickAdapter, task, newId);
                updateIdForAdapter(sharedData.doLaterAdapter, task, newId);
                updateIdForAdapter(sharedData.doNowAdapter, task, newId);
                updateIdForAdapter(sharedData.moveToListAdapter, task, newId);
            }
        }
    }

    private void updateIdForAdapter(TaskListAdapter adapter, long task, long newId){
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return;
        taskFound.get().setId(newId);
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
            case LIST_NO:  deleteFromAdapter(sharedData.noListAdapter, id); break;
            case LIST_ALL:  deleteFromAdapter(sharedData.allTasksAdapter, id); break;
            case DROP: deleteFromAdapter(sharedData.dropAdapter, id); break;
            case PICK: {
                boolean deleted = deleteFromAdapter(sharedData.pickAdapter, id);
                if (!deleted) deleted = deleteFromAdapter(sharedData.doNowAdapter, id);
                if (!deleted) deleted = deleteFromAdapter(sharedData.doLaterAdapter, id);
                if (!deleted) deleteFromAdapter(sharedData.moveToListAdapter, id);
                if (deleted){
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> sharedData.pickAdapter.update());
                }
            }
        }
    }

    /**
     * Delete an entry from the data set. Adjusts positions also.
     * @param adapter the adapter to delete from
     * @param id the task id
     * @return true if removed, false otherwise
     */
    @SuppressLint("NotifyDataSetChanged")
    private boolean deleteFromAdapter(TaskListAdapter adapter, long id){
        Handler handler = new Handler(Looper.getMainLooper());
        Optional<Task> task = adapter.tasks.stream().filter(t -> t.getId() == id).findFirst();
        if (task.isEmpty())
            return false;
        handler.post(() -> {
            adapter.tasks.removeIf(t -> t.getId() == id);
            adapter.tasks.forEach(t -> {
                if (t.getPosition() > task.get().getPosition())
                    t.setPosition(t.getPosition()-1);
                if (t.getList() != null && Objects.equals(t.getList(), task.get().getList()) && t.getListPosition() > task.get().getListPosition())
                    t.setListPosition(t.getListPosition()-1);
            });
            adapter.notifyDataSetChanged();
        });
        return true;
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
        if (Objects.requireNonNull(sharedData.mode) == Mode.LIST)
            swapListEntriesForAdapter(sharedData.listAdapter, list, task, position);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void swapListEntriesForAdapter(TaskListAdapter adapter, long list, long task, int position){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->{
            Optional<Task> task1 = adapter.tasks.stream().filter(t -> t.getList() == list).filter(t -> t.getId() == task).findFirst();
            Optional<Task> task2 = adapter.tasks.stream().filter(t -> t.getList() == list).filter(t -> t.getListPosition() == position).findFirst();
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
            adapter.tasks.sort(Comparator.comparingInt(Task::getListPosition));
            if (!sharedData.itemIsInMotion)
                adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void updateTask(long task, String value) {
        switch (sharedData.mode){
            case DROP -> updateTaskForAdapter(sharedData.dropAdapter, task, value);
            case FOCUS -> updateTaskForAdapter(sharedData.focusAdapter, task, value);
            case LIST -> updateTaskForAdapter(sharedData.listAdapter, task, value);
            case LIST_NO -> updateTaskForAdapter(sharedData.noListAdapter, task, value);
            case LIST_ALL -> updateTaskForAdapter(sharedData.allTasksAdapter, task, value);
            case PICK -> {
                updateTaskForAdapter(sharedData.pickAdapter, task, value);
                updateTaskForAdapter(sharedData.doLaterAdapter, task, value);
                updateTaskForAdapter(sharedData.doNowAdapter, task, value);
                updateTaskForAdapter(sharedData.moveToListAdapter, task, value);
            }
        }
    }

    private void updateTaskForAdapter(TaskListAdapter adapter, long task, String taskName){
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return;
        taskFound.get().setTask(taskName);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(adapter::notifyDataSetChanged);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void updateFocus(long task, boolean value) {
        switch(sharedData.mode){
            case DROP -> {if (value) removeFromAdapter(sharedData.dropAdapter, task);}
            case FOCUS -> {
                //remove either way: strictly speaking if value the task should not be in FOCUS
                //still we make sure to remove it before adding it again as a precaution
                removeFromAdapter(sharedData.focusAdapter, task);
                if (value) {
                    Optional<Task> taskFound = sharedData.clientStub.getTask(task);
                    if (taskFound.isEmpty())
                        return;
                    sharedData.focusAdapter.tasks.add(taskFound.get());
                    sharedData.focusAdapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(()-> sharedData.focusAdapter.notifyDataSetChanged());
                }
            }
            case PICK -> addRemoveFromPick(task);
            case LIST -> updateFocus(sharedData.listAdapter, task, value);
        }
    }


    //TODO simplify this method
    /**
     * Add a task to pick if it is to be picked and remove it otherwise. Goes through all relevant
     * adapters and performs the relevant action.
     * @param task the task in question
     */
    @SuppressLint("NotifyDataSetChanged")
    private void addRemoveFromPick(long task){
        List<Task> tasksToPick = sharedData.database.getTaskManager().loadTasksToPick();
        Optional<Task> taskFound = tasksToPick.stream().filter(t -> t.getId() == task).findFirst();
        //It is possible the task has been assigned a list but has not been removed from DROP yet
        //therefore, we check for that too
        if (taskFound.isEmpty() || taskFound.get().getDropped()){
            boolean removed = removeFromAdapter(sharedData.pickAdapter, task);
            if (!removed) removed = removeFromAdapter(sharedData.doNowAdapter, task);
            if (!removed) removed = removeFromAdapter(sharedData.doLaterAdapter, task);
            if (!removed) removed = removeFromAdapter(sharedData.moveToListAdapter, task);
            if (removed) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.pickAdapter.update());
            }
        } else{
            Optional<Task> taskInList = sharedData.pickAdapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
            if (taskInList.isPresent()){
                sharedData.pickAdapter.tasks.remove(taskInList.get());
                sharedData.pickAdapter.tasks.add(taskFound.get());
                sharedData.pickAdapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.pickAdapter.update());
                return;
            }
            taskInList = sharedData.doNowAdapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
            if (taskInList.isPresent()){
                sharedData.doNowAdapter.tasks.remove(taskInList.get());
                sharedData.doNowAdapter.tasks.add(taskFound.get());
                sharedData.doNowAdapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.doNowAdapter.update());
                return;
            }
            taskInList = sharedData.doLaterAdapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
            if (taskInList.isPresent()){
                sharedData.doLaterAdapter.tasks.remove(taskInList.get());
                sharedData.doLaterAdapter.tasks.add(taskFound.get());
                sharedData.doLaterAdapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.doLaterAdapter.update());
                return;
            }
            taskInList = sharedData.moveToListAdapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
            if (taskInList.isPresent()){
                sharedData.moveToListAdapter.tasks.remove(taskInList.get());
                sharedData.moveToListAdapter.tasks.add(taskFound.get());
                sharedData.moveToListAdapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.moveToListAdapter.update());
                return;
            }
            //task was in no list -> add to pickAdapter
            sharedData.pickAdapter.tasks.add(taskFound.get());
            sharedData.pickAdapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> sharedData.pickAdapter.update());
        }


    }

    private void updateFocus(TaskListAdapter adapter, long task, boolean value){
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return;
        taskFound.get().setFocus(value);
    }

    /**
     * Remove task from adapter without adjusting positions.
     * @param adapter the adapter to remove from
     * @param task the task id
     * @return true if task was removed, false otherwise
     */
    @SuppressLint("NotifyDataSetChanged")
    private boolean removeFromAdapter(TaskListAdapter adapter, long task){
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return false;
        adapter.tasks.remove(taskFound.get());
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(adapter::notifyDataSetChanged);
        return true;
    }
    @Override
    public void updateDropped(long task, boolean value) {
        //IGNORED
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void updateList(long task, Long list) {
        //TODO update color too
        switch (sharedData.mode){
            case DROP: updateListForAdapter(sharedData.dropAdapter, task); break;
            case PICK: {
                addRemoveFromPick(task);
                break;
            }
            case FOCUS: updateListForAdapter(sharedData.focusAdapter, task); break;
            case LIST:
            {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    Optional<Task> taskFound = sharedData.listAdapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
                    if (taskFound.isEmpty())
                        return;
                    Optional<Task> newTask = sharedData.clientStub.getTask(task);
                    if (newTask.isEmpty())
                        return;
                    sharedData.listAdapter.tasks.remove(taskFound.get());
                    sharedData.listAdapter.tasks.add(newTask.get());
                    sharedData.listAdapter.notifyDataSetChanged();
                });
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateListForAdapter(TaskListAdapter adapter, long taskId){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            Optional<Task> task = adapter.tasks.stream().filter(t -> t.getId() == taskId).findFirst();
            if (task.isEmpty())
                return;
            Optional<Task> newTask = sharedData.clientStub.getTask(taskId);
            if (newTask.isEmpty())
                return;
            adapter.tasks.remove(task.get());
            adapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
            adapter.notifyDataSetChanged();
        });
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

    @Override
    public List<User> getUsers() {
        //IGNORED
        return Collections.emptyList();
    }
}
