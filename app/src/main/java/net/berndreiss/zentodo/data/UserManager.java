package net.berndreiss.zentodo.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.berndreiss.zentodo.OperationType;
import net.berndreiss.zentodo.util.VectorClock;
import net.berndreiss.zentodo.util.ZenServerMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class UserManager implements UserManagerI{

    private SQLiteHelper sqLiteHelper;
    
    
    public UserManager(SQLiteHelper sqLiteHelper){
        this.sqLiteHelper = sqLiteHelper;
    }

     

    @Override
    public void addToQueue(User user, ZenServerMessage message) {

        Log.v("TEST", "INSERT INTO QUEUE");
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
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
        values.put("TIMESTAMP", SQLiteHelper.dateToEpoch(message.timeStamp));
        values.put("USER_ID", user.getId());
        values.put("CLOCK", message.clock.jsonify());

        db.insert("QUEUE", null, values);

    }

    @Override
    public List<ZenServerMessage> getQueued(long userId) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT TYPE, ARGUMENTS, CLOCK, TIMESTAMP FROM QUEUE WHERE USER_ID=?", new String[]{String.valueOf(userId)});

        cursor.moveToFirst();

        List<ZenServerMessage> result = new ArrayList<>();

        while (!cursor.isAfterLast()){
            OperationType type = OperationType.values()[cursor.getInt(0)];
            Log.v("GET QUEUE", String.valueOf(type));
            String argsString = cursor.getString(1);
            Log.v("GET QUEUE", String.valueOf(argsString));
            VectorClock clock = new VectorClock(cursor.getString(2));
            Log.v("GET QUEUE", String.valueOf(clock.jsonify()));
            Instant timeStamp = Instant.ofEpochSecond(cursor.getLong(3));
            Log.v("GET QUEUE", String.valueOf(timeStamp));

            String[] argsSplit = argsString.substring(1, argsString.length()-1).split(",");

            List<Object> args = new ArrayList<>(Arrays.asList(argsSplit));

            result.add(new ZenServerMessage(type, args, clock, timeStamp));
            cursor.moveToNext();
        }

        cursor.close();
        return result;
    }

    @Override
    public void clearQueue(long userId) {

        SQLiteDatabase db= sqLiteHelper.getWritableDatabase();

        db.delete("QUEUE", "", null);
    }

    @Override
    public User addUser(long id, String email, String userName, long device) {

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        int profileId = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM PROFILES  WHERE ID=" + id, null);
        if (cursor.isAfterLast())
            profileId = cursor.getInt(0);
        cursor.close();
        System.out.println("ADD PROFILE " + profileId + " FOR USER " + id);
        ContentValues profileValues = new ContentValues();
        profileValues.put("ID", profileId);
        profileValues.put("USER", id);
        db.insert("PROFILES", null, profileValues);

        ContentValues values = new ContentValues();
        values.put("ID", id);
        values.put("MAIL", email);
        values.put("NAME", userName);
        values.put("DEVICE", device);
        VectorClock clock = new VectorClock(device);
        values.put("CLOCK", clock.jsonify());
        values.put("PROFILE", profileId);

        db.insert("USERS", null, values);

        User user = new User(email, userName, device);
        user.setId(id);
        user.setClock(clock.jsonify());

        Profile profile = new Profile();
        ProfileId profileIdentifier = new ProfileId((int) profileId, user);
        profile.setProfileId(profileIdentifier);
        user.setProfile(profile.getId());

        System.out.println("NEW USER " + id + " WITH PROFILE " + user.getProfile());
        return user;

    }

    @Override
    public Profile addProfile(long aLong, String s) {
        return null;
    }

    @Override
    public Profile addProfile(long aLong) {
        return null;
    }

    @Override
    public void removeUser(long userId) {
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        db.delete("USERS", "ID=?", new String[]{String.valueOf(userId)});
    }

    @Override
    public void removeProfile(long id) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        db.delete("PROFILES", "ID = ?", new String[]{String.valueOf(id)});
    }

    @Override
    public Optional<User> getUser(long id){
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE ID=?", new String[]{String.valueOf(id)});
        List<User> users = getListOfUsers(cursor);
        cursor.close();
        System.out.println("GETTING USER " + id);
        System.out.println(users.size());
        if(users.isEmpty())
            return Optional.empty();

        return Optional.of(users.get(0));
    }
    @Override
    public Optional<User> getUserByEmail(String email) {
        System.out.println("GET USER BY MAIL " + email);
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE MAIL=?", new String[]{email});

        List<User> users = getListOfUsers(cursor);

        cursor.close();

        if (users.size() > 1)
            throw new RuntimeException("Two users with same email exist");
        if (users.isEmpty())
            return Optional.empty();

        System.out.println("USER " + users.get(0).getId() + " WITH PROFILE " + users.get(0).getProfile());
        return Optional.of(users.get(0));

    }

    @Override
    public Optional<Profile> getProfile(long userId, long id) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM PROFILES WHERE USER=? AND ID = ?", new String[]{String.valueOf(userId), String.valueOf(id)});

        System.out.println("GETTING PROFILE " + id + " FOR USER " + userId);
        List<Profile> profiles = getListOfProfiles(cursor);

        cursor.close();
        System.out.println("SIZE " + profiles.size());
        if (profiles.size() > 1)
            System.out.println("Profiles with identical id for user " + userId);
        if (profiles.isEmpty())
            return Optional.empty();

        Optional<User> user = getUser(userId);

        if (user.isEmpty())
            return Optional.empty();

        Profile profile = profiles.get(0);
        profile.getProfileId().setUser(user.get());
        return Optional.of(profile);
    }

    @Override
    public boolean userExists(long userId) {
        return false;
    }

    @Override
    public boolean isEnabled(long userId) {

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT ENABLED FROM USERS WHERE ID=?", new String[]{String.valueOf(userId)});

        cursor.moveToFirst();

        boolean enabled = false;

        if (!cursor.isAfterLast())
            enabled = SQLiteHelper.intToBool(cursor.getInt(0));

        cursor.close();
        ;

        return enabled;
    }

    @Override
    public void enableUser(long userId) {

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ENABLED", 1);

        db.update("USERS", values, "ID=?", new String[]{String.valueOf(userId)});
        ;
    }

    @Override
    public void setDevice(long userId, long id) {

    }

    @Override
    public void setClock(long userId, VectorClock vectorClock) {


        try (SQLiteDatabase db = sqLiteHelper.getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put("CLOCK", vectorClock.jsonify());
            db.update("USERS", values, "ID=?", new String[]{String.valueOf(userId)});
        }
    }

    @Override
    public String getToken(long user) {
        try (SQLiteDatabase db = sqLiteHelper.getReadableDatabase()) {

            try(Cursor cursor = db.rawQuery("SELECT TOKEN FROM TOKENS WHERE USER=?", new String[]{String.valueOf(user)})) {

                cursor.moveToFirst();
                if (cursor.isAfterLast())
                    return null;
                return cursor.getString(0);
            }
        }
    }

    @Override
    public void setToken(long user, String token) {

        try (SQLiteDatabase db = sqLiteHelper.getWritableDatabase()){
            ContentValues values = new ContentValues();
            values.put("USER", user);
            values.put("TOKEN", token);
            db.insertWithOnConflict("TOKENS", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    @Override
    public void updateUserName(Long l, String s) {

    }

    @Override
    public boolean updateEmail(Long userId, String s) {
        return false;
    }

    @Override
    public List<User> getUsers() {

        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM USERS", null);

        List<User> users = getListOfUsers(cursor);

        cursor.close();
        ;

        return users;
    }

    @Override
    public List<Profile> getProfiles(long userId) {
        SQLiteDatabase database = sqLiteHelper.getReadableDatabase();

        Cursor cursorU = database.rawQuery("SELECT * FROM USERS WHERE ID = ?", new String[]{String.valueOf(userId)});
        List<User> users = getListOfUsers(cursorU);
        cursorU.close();
        if (users.isEmpty())
            return new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT * FROM PROFILES WHERE USER = ?", new String[]{String.valueOf(userId)});
        List<Profile> profiles = getListOfProfiles(cursor);

        for (Profile p: profiles)
            p.getProfileId().setUser(users.get(0));

        return profiles;
    }
    private List<User> getListOfUsers(Cursor cursor){
        List<User> users = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()){

            long id = cursor.getInt(0);
            String email = cursor.getString(1);
            String userName = cursor.getString(2);
            boolean enabled = SQLiteHelper.intToBool(cursor.getInt(3));
            int device = cursor.getInt(4);
            int profile = cursor.getInt(5);

            //TODO ADD USERID!!!
            User user = new User(email, userName, device);
            user.setId(id);
            user.setEnabled(enabled);
            user.setProfile(profile);
            users.add(user);
            cursor.moveToNext();
        }

        return users;

    }
    private List<Profile> getListOfProfiles(Cursor cursor){
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
            profiles.add(profile);
            cursor.moveToNext();
        }

        return profiles;

    }
}
