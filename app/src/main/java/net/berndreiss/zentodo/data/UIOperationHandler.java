package net.berndreiss.zentodo.data;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.os.Handler;

import net.berndreiss.zentodo.Mode;
import net.berndreiss.zentodo.SharedData;
import net.berndreiss.zentodo.adapters.ListTaskListAdapter;
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
 * Implementation of the DataManagerI interface handling UI interaction.
 * By handling all UI actions via this interface, we can let the client stub handle everything.
 * This means that it does not matter, whether an operation was performed locally or has been
 * received from the server.
 * Functions not essential for the apps UI are //IGNORED.
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

    /**
     * Update the id for if the task is in the adapters task list.
     * @param adapter the current adapter
     * @param task the task id
     * @param newId the new id to set
     */
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
        //get the current adapter but ignore FOCUS and LIST since we have a fresh task on our hands
        switch (sharedData.mode){
            case LIST_NO:  adapter = sharedData.noListAdapter; break;
            case LIST_ALL:  adapter = sharedData.allTasksAdapter; break;
            case DROP: adapter = sharedData.dropAdapter; break;
            case PICK: adapter = sharedData.pickAdapter; break;
            default:
                return;
        }
        //updating the UI needs to be handled by the main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            adapter.tasks.add(task);
            adapter.notifyDataSetChanged();
            //in PICK also update the other lists
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
            case LIST_NO:  deleteFromAdapter(sharedData.noListAdapter, id, true); break;
            case LIST_ALL:  deleteFromAdapter(sharedData.allTasksAdapter, id, true); break;
            case DROP: deleteFromAdapter(sharedData.dropAdapter, id, true); break;
            case PICK: {
                //see if the task is in any of the PICK lists, if so, delete it
                boolean deleted = deleteFromAdapter(sharedData.pickAdapter, id, false);
                if (!deleted) deleted = deleteFromAdapter(sharedData.doNowAdapter, id, false);
                if (!deleted) deleted = deleteFromAdapter(sharedData.doLaterAdapter, id, false);
                if (!deleted) deleteFromAdapter(sharedData.moveToListAdapter, id, false);
                if (deleted){
                    //update all lists UI
                    //updating the UI needs to be handled by the main thread
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> sharedData.pickAdapter.update());
                }
            }
            case FOCUS: deleteFromAdapter(sharedData.focusAdapter, id, true);
        }
    }

    /**
     * Delete an entry from the data set. Adjusts positions also.
     * @param adapter the adapter to delete from
     * @param id the task id
     * @param notify notify the adapter about changes
     * @return true if removed, false otherwise
     */
    @SuppressLint("NotifyDataSetChanged")
    private boolean deleteFromAdapter(TaskListAdapter adapter, long id, boolean notify){
        Optional<Task> task = adapter.tasks.stream().filter(t -> t.getId() == id).findFirst();
        if (task.isEmpty())
            return false;
        //remove the task
        adapter.tasks.removeIf(t -> t.getId() == id);
        //decrement all positions greater than the task (including list positions)
        adapter.tasks.forEach(t -> {
            //decrement position
            if (t.getPosition() > task.get().getPosition())
                t.setPosition(t.getPosition()-1);
            //decrement list position
            if (t.getList() != null && Objects.equals(t.getList(), task.get().getList()) && t.getListPosition() > task.get().getListPosition())
                t.setListPosition(t.getListPosition()-1);
        });
        if (notify) {
            //updating the UI needs to be handled by the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(adapter::notifyDataSetChanged);
        }
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
            case DROP: swapInAdapter(sharedData.dropAdapter, task, position, true); break;
            case FOCUS: swapInAdapter(sharedData.focusAdapter, task, position, true); break;
            case PICK: {
                swapInAdapter(sharedData.pickAdapter, task, position, true);
                swapInAdapter(sharedData.doNowAdapter, task, position, true);
                swapInAdapter(sharedData.doLaterAdapter, task, position, true);
                swapInAdapter(sharedData.moveToListAdapter, task, position, true);
            }
        }
    }

    /**
     * Swaps a task with another task holding the position (as the field, not the actual position in the list).
     *
     * @param adapter  the current adapter
     * @param task     the task to move
     * @param position the position the other task has to hold
     * @param notify notify the adapter about changes
     */
    @SuppressLint("NotifyDataSetChanged")
    private void swapInAdapter(TaskListAdapter adapter, long task, int position, boolean notify) {
        //See if we can find both tasks in the adapter
        Optional<Task> task1 = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        Optional<Task> task2 = adapter.tasks.stream().filter(t -> t.getPosition() == position).findFirst();
        //If so get both tasks from the database
        Optional<Task> updatedTask1 = Optional.empty();
        if (task1.isPresent())
            updatedTask1 = sharedData.clientStub.getTask(task);
        Optional<Task> updatedTask2 = Optional.empty();
        if (task2.isPresent())
            updatedTask2 = sharedData.clientStub.getTask(task2.get().getId());
        //If both tasks (adapter and database) are present remove the existing one from the adapter
        //and add the one retrieved from the database
        if (task1.isPresent() && updatedTask1.isPresent()) {
            adapter.tasks.remove(task1.get());
            adapter.tasks.add(updatedTask1.get());
        }
        if (task2.isPresent() && updatedTask2.isPresent()) {
            adapter.tasks.remove(task2.get());
            adapter.tasks.add(updatedTask2.get());
        }
        //sort tasks by position
        adapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
        if (notify) {
            //updating the UI needs to be handled by the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                //check whether something is being currently moved in the adapter. If so, do not update.
                //this leads to problems and when the movement is finished the adapter will be notified
                //about changes anyways
                if (!sharedData.itemIsInMotion)
                    adapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void swapListEntries(long list, long task, int position) {
        if (sharedData.mode == Mode.LIST)
            swapListEntriesForAdapter(sharedData.listAdapter, list, task, position, true);
    }

    /**
     * Swap list entries in adapter.
     * @param adapter the adapter
     * @param list the list
     * @param task the task id of the task to move
     * @param position the list position to swap with
     * @param notify notify the adapter about changes
     */
    @SuppressLint("NotifyDataSetChanged")
    private void swapListEntriesForAdapter(ListTaskListAdapter adapter, long list, long task, int position, boolean notify){
        //See if we can find both tasks in the adapter
        Optional<Task> task1 = adapter.tasks.stream().filter(t -> t.getList() == list).filter(t -> t.getId() == task).findFirst();
        Optional<Task> task2 = adapter.tasks.stream().filter(t -> t.getList() == list).filter(t -> t.getListPosition() == position).findFirst();
        //If so get both tasks from the database
        Optional<Task> updatedTask1 = Optional.empty();
        Optional<Task> updatedTask2 = Optional.empty();
        if (task1.isPresent())
            updatedTask1 = sharedData.clientStub.getTask(task);
        if (task2.isPresent())
            updatedTask2 = sharedData.clientStub.getTask(task2.get().getId());
        //If both tasks (adapter and database) are present remove the existing one from the adapter
        //and add the one retrieved from the database
        if (task1.isPresent() && updatedTask1.isPresent()) {
            adapter.tasks.remove(task1.get());
            adapter.tasks.add(updatedTask1.get());
        }
        if (task2.isPresent() && updatedTask2.isPresent()) {
            adapter.tasks.remove(task2.get());
            adapter.tasks.add(updatedTask2.get());
        }
        //sort by list position
        adapter.tasks.sort(Comparator.comparingInt(Task::getListPosition));
        if (notify) {
            //updating the UI needs to be handled by the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                //check whether something is being currently moved in the adapter. If so, do not update.
                //this leads to problems and when the movement is finished the adapter will be notified
                //about changes anyways
                if (!sharedData.itemIsInMotion)
                    adapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void updateTask(long task, String value) {
        switch (sharedData.mode){
            case DROP -> updateTaskForAdapter(sharedData.dropAdapter, task, value, true);
            case FOCUS -> updateTaskForAdapter(sharedData.focusAdapter, task, value, true);
            case LIST -> updateTaskForAdapter(sharedData.listAdapter, task, value, true);
            case LIST_NO -> updateTaskForAdapter(sharedData.noListAdapter, task, value, true);
            case LIST_ALL -> updateTaskForAdapter(sharedData.allTasksAdapter, task, value, true);
            case PICK -> {
                boolean updated = updateTaskForAdapter(sharedData.pickAdapter, task, value, true);
                if (updated) return;
                updated = updateTaskForAdapter(sharedData.doLaterAdapter, task, value, true);
                if (updated) return;
                updated = updateTaskForAdapter(sharedData.doNowAdapter, task, value, true);
                if (updated) return;
                updateTaskForAdapter(sharedData.moveToListAdapter, task, value, true);
            }
        }
    }

    /**
     * Update the literal task name in the adapter.
     * @param adapter the adapter
     * @param task the task id
     * @param taskName the new task name
     * @param notify notify the adapter about changes
     */
    private boolean updateTaskForAdapter(TaskListAdapter adapter, long task, String taskName, boolean notify){
        //check whether task is in adapters list
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return false;
        //set name and update UI
        taskFound.get().setTask(taskName);
        if (notify) {
            //updating the UI needs to be handled by the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(adapter::notifyDataSetChanged);
        }
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void updateFocus(long task, boolean value) {
        switch(sharedData.mode){
            case DROP -> {if (value) removeFromAdapter(sharedData.dropAdapter, task, true);}
            case FOCUS -> {
                //remove either way: strictly speaking if value the task should not be in FOCUS
                //still we make sure to remove it before adding it again as a precaution
                removeFromAdapter(sharedData.focusAdapter, task, !value);
                //add the task if focus is true
                if (value) {
                    Optional<Task> taskFound = sharedData.clientStub.getTask(task);
                    if (taskFound.isEmpty())
                        return;
                    sharedData.focusAdapter.tasks.add(taskFound.get());
                    sharedData.focusAdapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
                    //updating the UI needs to be handled by the main thread
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
        User user = sharedData.clientStub.getUser();
        List<Task> tasksToPick = sharedData.database.getTaskManager().loadTasksToPick(user.getId(), user.getProfile());
        Optional<Task> taskFound = tasksToPick.stream().filter(t -> t.getId() == task).findFirst();
        //It is possible the task has been assigned a list but has not been removed from DROP yet
        //therefore, we check for that too
        if (taskFound.isEmpty() || taskFound.get().getDropped()){
            boolean removed = removeFromAdapter(sharedData.pickAdapter, task, false);
            if (!removed) removed = removeFromAdapter(sharedData.doNowAdapter, task, false);
            if (!removed) removed = removeFromAdapter(sharedData.doLaterAdapter, task, false);
            if (!removed) removed = removeFromAdapter(sharedData.moveToListAdapter, task, false);
            if (removed) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.pickAdapter.update());
            }
        } else{
            boolean handled = addTaskIfExists(sharedData.pickAdapter, taskFound.get(), false);
            if (handled) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.pickAdapter.update());
                return;
            }
            handled = addTaskIfExists(sharedData.doNowAdapter, taskFound.get(), false);
            if (handled) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.pickAdapter.update());
                return;
            }
            handled = addTaskIfExists(sharedData.doLaterAdapter, taskFound.get(), false);
            if (handled) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.pickAdapter.update());
                return;
            }
            handled = addTaskIfExists(sharedData.moveToListAdapter, taskFound.get(), false);
            if (handled) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.pickAdapter.update());
                return;
            }
            //task was in no list -> add to pickAdapter
            sharedData.pickAdapter.tasks.add(taskFound.get());
            sharedData.pickAdapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
            //updating the UI needs to be handled by the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> sharedData.pickAdapter.update());
        }
    }

    /**
     * If task is in adapter remove it and add current version from database.
     * @param adapter the adapter
     * @param task the task id
     * @param notify notify the adapter about changes
     * @return true if task was interchanged, false otherwise
     */
    private boolean addTaskIfExists(TaskListAdapter adapter, Task task, boolean notify) {
        Optional<Task> taskInList = adapter.tasks.stream().filter(t -> t.getId() == task.getId()).findFirst();
        if (taskInList.isEmpty())
            return false;
        adapter.tasks.remove(taskInList.get());
        adapter.tasks.add(task);
        adapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
        if (notify) {
            //updating the UI needs to be handled by the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(adapter::notifyDataSetChanged);
        }
        return true;
    }

    /**
     * Update the focus field for the task if it is in the adapters list.
     * @param adapter the adapter
     * @param task the task id
     * @param value the value to set
     */
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
     * @param notify notify the adapter about changes
     * @return true if task was removed, false otherwise
     */
    @SuppressLint("NotifyDataSetChanged")
    private boolean removeFromAdapter(TaskListAdapter adapter, long task, boolean notify){
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return false;
        adapter.tasks.remove(taskFound.get());
        if (notify) {
            //updating the UI needs to be handled by the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(adapter::notifyDataSetChanged);
        }
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
            case LIST_NO: removeFromAdapter(sharedData.noListAdapter, task, true); break;
            case LIST_ALL: updateListForAdapter(sharedData.allTasksAdapter, task, false); break;
            case DROP: removeFromAdapter(sharedData.dropAdapter, task, true); break;
            case PICK: {
                addRemoveFromPick(task);
                break;
            }
            case FOCUS: updateListForAdapter(sharedData.focusAdapter, task, true); break;
            case LIST:
            {
                //remove the task from the list if present (should strictly speaking only be
                //necessary if list==null, but remove it anyways as a safety precaution)
                Optional<Task> taskFound = sharedData.listAdapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
                taskFound.ifPresent(value -> sharedData.listAdapter.tasks.remove(value));
                //if list==null we update the UI and are done
                if (list == null){
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> sharedData.listAdapter.notifyDataSetChanged());
                    return;
                }
                //get the task from the database to also have the list position
                Optional<Task> newTask = sharedData.clientStub.getTask(task);
                //if the task does not exist or the list is not the one opened, return
                if (newTask.isEmpty() || list != sharedData.listAdapter.taskList.getId())
                    return;
                sharedData.listAdapter.tasks.add(newTask.get());
                //updating the UI needs to be handled by the main thread
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> sharedData.listAdapter.notifyDataSetChanged());
            }
        }
    }

    /**
     * Update the list for the task if it is in the adapters list.
     * @param adapter the adapter
     * @param task the task id
     * @param notify notify the adapter about changes
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateListForAdapter(TaskListAdapter adapter, long task, boolean notify){
        //look for the task and return if it isn't there
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return;
        //get and insert the task from the DB so that the list position is correct too
        Optional<Task> newTask = sharedData.clientStub.getTask(task);
        if (newTask.isEmpty())
            return;
        adapter.tasks.remove(taskFound.get());
        //sort by position
        adapter.tasks.sort(Comparator.comparingInt(Task::getPosition));
        if (notify) {
            //updating the UI needs to be handled by the main thread
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(adapter::notifyDataSetChanged);
        }
    }

    @Override
    public void updateReminderDate(long task, Instant value) {
        switch (sharedData.mode){
            case PICK -> addRemoveFromPick(task);
            case DROP -> removeFromAdapter(sharedData.dropAdapter, task, true);
            case FOCUS -> removeFromAdapter(sharedData.focusAdapter, task, true);
            case LIST -> updateReminderDateForAdapter(sharedData.listAdapter, task, value);
        }
    }

    /**
     * Update the reminder date for the task if it is in the adapter.
     * @param adapter the adapter
     * @param task the task id
     * @param value the value to set
     */
    private void updateReminderDateForAdapter(TaskListAdapter adapter, long task, Instant value){
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return;
        taskFound.get().setReminderDate(value);
    }

    @Override
    public void updateRecurrence(long task, String value) {
        switch (sharedData.mode){
            case DROP -> removeFromAdapter(sharedData.dropAdapter, task, true);
            case FOCUS -> setRecurrenceForAdapter(sharedData.focusAdapter, task, value);
            case LIST -> setRecurrenceForAdapter(sharedData.listAdapter, task, value);
            case PICK -> addRemoveFromPick(task);
        }
    }

    /**
     * Set the recurrence for the task if it is in the adapter.
     * @param adapter the adapter
     * @param task the task id
     * @param value the value to set
     */
    private void setRecurrenceForAdapter(TaskListAdapter adapter, long task, String value){
        Optional<Task> taskFound = adapter.tasks.stream().filter(t -> t.getId() == task).findFirst();
        if (taskFound.isEmpty())
            return;
        taskFound.get().setRecurrence(value);
    }

    @Override
    public void updateListColor(long list, String color) {

        //set header color to chosen color or default if white is chosen
        //if (color.startsWith("ffffff", 3)) {

            //this.layout.setBackgroundColor(ContextCompat.getColor(sharedData.context, R.color.header_background));

        //} else {

            //this.layout.setBackgroundColor(Color.parseColor(color));

        //}
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
