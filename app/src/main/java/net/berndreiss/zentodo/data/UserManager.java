package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.berndreiss.zentodo.exceptions.DuplicateUserIdException;
import net.berndreiss.zentodo.exceptions.InvalidUserActionException;
import net.berndreiss.zentodo.operations.OperationType;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenServerMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the UserManagerI interface using SQLite and the ZenSQLiteHelper.
 */
public class UserManager implements UserManagerI{

    /** The SQLite helper */
    private final ZenSQLiteHelper zenSQLiteHelper;

    /**
     * Create new instance of UserManager.
     * @param zenSQLiteHelper the helper for interacting with the database
     */
    public UserManager(ZenSQLiteHelper zenSQLiteHelper){
        this.zenSQLiteHelper = zenSQLiteHelper;
    }

    @Override
    public synchronized void addToQueue(User user, ZenServerMessage message) {
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (!message.arguments.isEmpty()) {
            sb.append(message.arguments.get(0).toString());
            message.arguments.stream().skip(1).forEach(o -> sb.append(",").append(o.toString()));
        }
        sb.append("}");
        String arguments = sb.toString();
        values.put("TYPE", String.valueOf(message.type.ordinal()));
        values.put("ARGUMENTS", arguments);
        values.put("TIMESTAMP", ZenSQLiteHelper.dateToEpoch(message.timeStamp));
        values.put("USER_ID", user.getId());
        values.put("CLOCK", message.clock.jsonify());
        db.insert("QUEUE", null, values);
    }

    @Override
    public List<ZenServerMessage> getQueued(long userId) {
        SQLiteDatabase db = zenSQLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TYPE, ARGUMENTS, CLOCK, TIMESTAMP FROM QUEUE WHERE USER_ID=?", new String[]{String.valueOf(userId)});
        cursor.moveToFirst();
        List<ZenServerMessage> result = new ArrayList<>();
        while (!cursor.isAfterLast()){
            OperationType type = OperationType.values()[cursor.getInt(0)];
            String argsString = cursor.getString(1);
            VectorClock clock = new VectorClock(cursor.getString(2));
            Instant timeStamp = Instant.ofEpochMilli(cursor.getLong(3));
            String[] argsSplit = argsString.substring(1, argsString.length()-1).split(",");

            List<Object> args = new ArrayList<>(Arrays.asList(argsSplit));

            result.add(new ZenServerMessage(type, args, clock, timeStamp));
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    @Override
    public synchronized void clearQueue(long userId) {
        SQLiteDatabase db= zenSQLiteHelper.getWritableDatabase();
        db.delete("QUEUE", "", null);
    }

    @Override
    public synchronized User addUser(long id, String email, String userName, Integer device) throws DuplicateUserIdException, InvalidUserActionException {
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        if (getUser(id).isPresent())
            throw new DuplicateUserIdException("User with id already exists: id " + id);
        if (getUserByEmail(email).isPresent())
            throw new InvalidUserActionException("User with email already exists: email " + email);
        int profileId = 0;
        ContentValues profileValues = new ContentValues();
        profileValues.put("ID", profileId);
        profileValues.put("USER", id);
        db.insert("PROFILES", null, profileValues);
        ContentValues values = new ContentValues();
        values.put("ID", id);
        values.put("MAIL", email);
        values.put("NAME", userName);
        if (device == null)
            values.put("DEVICE", "NULL");
        else
            values.put("DEVICE", device);
        VectorClock clock = new VectorClock(device);
        values.put("CLOCK", clock.jsonify());
        values.put("PROFILE", profileId);
        db.insert("USERS", null, values);
        User user = new User(email, userName, device);
        user.setId(id);
        user.setClock(clock.jsonify());
        Profile profile = new Profile();
        ProfileId profileIdentifier = new ProfileId(profileId, user);
        profile.setProfileId(profileIdentifier);
        user.setProfile(profile.getId());
        return user;
    }

    @Override
    public synchronized Profile addProfile(long userId, String name) throws InvalidUserActionException {
        Optional<User> user = getUser(userId);
        if (user.isEmpty())
            throw new InvalidUserActionException("User does not exist");
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM PROFILES WHERE USER = ?", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst())
            count = cursor.getInt(0);
        cursor.close();
        ContentValues values = new ContentValues();
        values.put("ID", count);
        values.put("USER", userId);
        if (name != null)
            values.put("NAME", name);
        db.insert("PROFILES", null, values);
        Profile profile = new Profile();
        ProfileId profileId = new ProfileId(count, user.get());
        profile.setProfileId(profileId);
        profile.setName(name);
        return profile;
    }

    @Override
    public synchronized Profile addProfile(long userId) throws InvalidUserActionException {
        return addProfile(userId, null);
    }

    @Override
    public synchronized void removeUser(long userId) throws InvalidUserActionException {
        if (userId == 0)
            throw new InvalidUserActionException("Cannot delete default user.");
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        db.delete("USERS", "ID=?", new String[]{String.valueOf(userId)});
        db.delete("PROFILES", "USER=?", new String[]{String.valueOf(userId)});
        db.delete("TASKS", "USER = ?", new String[]{String.valueOf(userId)});
    }


    @Override
    public synchronized void removeProfile(long userId, int id) throws InvalidUserActionException{
        if (userId == 0 && id == 0)
            throw new InvalidUserActionException("Cannot remove default profile of default user.");
        if (getProfiles(userId).size() == 1)
            throw new InvalidUserActionException("Cannot remove last profile of user.");
        SQLiteDatabase db = zenSQLiteHelper.getReadableDatabase();
        db.delete("TASKS", "USER = ? AND PROFILE = ?", new String[]{String.valueOf(userId), String.valueOf(id)});
        db.delete("PROFILES", "USER = ? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(id)});
        db.delete("PROFILE_LIST", "USER = ? AND PROFILE = ?", new String[]{
                String.valueOf(userId), String.valueOf(id)
        });
    }

    @Override
    public Optional<User> getUser(long id){
        SQLiteDatabase db = zenSQLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE ID=?", new String[]{String.valueOf(id)});
        List<User> users = getListOfUsers(cursor);
        cursor.close();
        if(users.isEmpty())
            return Optional.empty();
        return Optional.of(users.get(0));
    }
    @Override
    public Optional<User> getUserByEmail(String email) {
        SQLiteDatabase db = zenSQLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE MAIL=?", new String[]{email});
        List<User> users = getListOfUsers(cursor);
        cursor.close();
        if (users.size() > 1)
            throw new RuntimeException("Two users with same email exist");
        if (users.isEmpty())
            return Optional.empty();
        return Optional.of(users.get(0));
    }

    @Override
    public Optional<Profile> getProfile(long userId, long id) {
        Optional<User> user = getUser(userId);
        if (user.isEmpty())
            return Optional.empty();
        SQLiteDatabase db = zenSQLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM PROFILES WHERE USER=? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(id)});
        List<Profile> profiles = getListOfProfiles(cursor, user.get());
        cursor.close();
        if (profiles.isEmpty())
            return Optional.empty();
        Profile profile = profiles.get(0);
        profile.getProfileId().setUser(user.get());
        return Optional.of(profile);
    }

    @Override
    public synchronized void enableUser(long userId) {
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ENABLED", 1);
        db.update("USERS", values, "ID=?", new String[]{String.valueOf(userId)});
    }

    @Override
    public synchronized void setDevice(long userId, int id) {
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("DEVICE", id);
        db.update("USERS", values, "ID=?", new String[]{String.valueOf(userId)});
    }

    @Override
    public synchronized void setClock(long userId, VectorClock vectorClock) {
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("CLOCK", vectorClock.jsonify());
            db.update("USERS", values, "ID=?", new String[]{String.valueOf(userId)});
    }

    @Override
    public Optional<String> getToken(long user) {
        SQLiteDatabase db = zenSQLiteHelper.getReadableDatabase();
            try(Cursor cursor = db.rawQuery("SELECT TOKEN FROM TOKENS WHERE USER=?", new String[]{String.valueOf(user)})) {
                cursor.moveToFirst();
                if (cursor.isAfterLast())
                    return Optional.empty();
                return Optional.of(cursor.getString(0));
            }
    }

    @Override
    public synchronized void setToken(long user, String token) {
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("USER", user);
        values.put("TOKEN", token);
        db.insertWithOnConflict("TOKENS", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public synchronized void updateUserName(Long userId, String name) {
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", name);
        db.update("USERS", values, "ID = ?", new String[]{String.valueOf(userId)});
    }

    @Override
    public synchronized void updateEmail(Long userId, String mail) throws InvalidUserActionException {
        if (getUserByEmail(mail).isPresent() || userId == null)
            throw new InvalidUserActionException("User with mail already exists.");
        SQLiteDatabase db = zenSQLiteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("MAIL", mail);
        db.update("USERS", values, "ID = ?", new String[]{String.valueOf(userId)});
    }

    @Override
    public List<User> getUsers() {
        SQLiteDatabase db = zenSQLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USERS", null);
        List<User> users = getListOfUsers(cursor);
        cursor.close();
        return users.stream().filter(u -> u.getId() != 0).toList();
    }

    @Override
    public List<Profile> getProfiles(long userId) {
        SQLiteDatabase database = zenSQLiteHelper.getReadableDatabase();
        Cursor cursorU = database.rawQuery("SELECT * FROM USERS WHERE ID = ?", new String[]{String.valueOf(userId)});
        List<User> users = getListOfUsers(cursorU);
        cursorU.close();
        if (users.isEmpty())
            return new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM PROFILES WHERE USER = ?", new String[]{String.valueOf(userId)});
        return getListOfProfiles(cursor, users.get(0));
    }

    /**
     * Retrieve the list of users from a cursor.
     * @param cursor the cursor to read from
     * @return a list of users
     */
    private List<User> getListOfUsers(Cursor cursor){
        List<User> users = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            long id = cursor.getInt(0);
            String email = cursor.getString(1);
            String userName = cursor.getString(2);
            boolean enabled = ZenSQLiteHelper.intToBool(cursor.getInt(3));
            int device = cursor.getInt(4);
            int profile = cursor.getInt(5);
            String clock = cursor.getString(6);
            User user = new User(email, userName, device);
            user.setId(id);
            user.setEnabled(enabled);
            user.setProfile(profile);
            user.setDevice(device);
            user.setClock(clock);
            users.add(user);
            cursor.moveToNext();
        }
        return users;
    }

    /**
     * Retrieve the list of profiles from a cursor.
     * @param cursor the cursor to read data from
     * @param user the user associated with the profiles
     * @return a list of profiles
     */
    private List<Profile> getListOfProfiles(Cursor cursor, User user){
        List<Profile> profiles = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            Profile profile = new Profile();
            ProfileId profileId = new ProfileId();
            profileId.setId(id);
            profile.setProfileId(profileId);
            profile.setName(name);
            profile.getProfileId().setUser(user);
            profiles.add(profile);
            cursor.moveToNext();
        }
        return profiles;
    }
}
