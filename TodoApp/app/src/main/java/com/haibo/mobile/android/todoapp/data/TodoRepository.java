package com.haibo.mobile.android.todoapp.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.haibo.mobile.android.todoapp.model.Priority;
import com.haibo.mobile.android.todoapp.model.Todo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.haibo.mobile.android.todoapp.data.TodoDatabaseHelper.DATE_TIME_FORMAT;
import static java.util.Calendar.MINUTE;

/**
 * Created by hyan on 8/14/17.
 */

public class TodoRepository {
    private static TodoRepository repository;

    private TodoDatabaseHelper helper;

    private TodoRepository(Context mContext){
        helper = new TodoDatabaseHelper(mContext, "Todo.db", null, 1);
    }

    public static TodoRepository getRepository(Context mContext) {
        if (null == repository) {
            repository = new TodoRepository((mContext));
        }
        return repository;
    }

    public List<Todo> allNotDone() throws ParseException {
        List<Todo> todos = new ArrayList<>();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from Todo where done = 0", null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String todoTitle = cursor.getString(cursor.getColumnIndex("title"));
            Date due = DATE_TIME_FORMAT.parse(cursor.getString(cursor.getColumnIndex("due")));
            int priority = cursor.getInt(cursor.getColumnIndex("priority"));
            Todo todo = new Todo(id, todoTitle, due, Priority.values()[priority]);
            todos.add(todo);
        }
        return todos;
    }

    public Todo todoById(int id) throws ParseException {
        List<Todo> todos = new ArrayList<>();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from Todo where id = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            String todoTitle = cursor.getString(cursor.getColumnIndex("title"));
            Date due = DATE_TIME_FORMAT.parse(cursor.getString(cursor.getColumnIndex("due")));
            int priority = cursor.getInt(cursor.getColumnIndex("priority"));
            return new Todo(id, todoTitle, due, Priority.values()[priority]);
        }
        return null;
    }

    public void insertTodo(String title, Date due, String priorityStr) {
        int priority = 0;

        switch (priorityStr) {
            case "Low":
                priority = 0;
                break;
            case "Medium":
                priority = 1;
                break;
            case "High":
                priority = 2;
                break;
        }
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("insert into Todo(title, due, priority, done) values(?, ?, ?, ?)",
                new String[]{title, DATE_TIME_FORMAT.format(due), String.valueOf(priority), "0"});
    }

    public void updateTodo(int id, String title, Date due, String priorityStr) {
        int priority = 0;

        switch (priorityStr) {
            case "Low":
                priority = 0;
                break;
            case "Medium":
                priority = 1;
                break;
            case "High":
                priority = 2;
                break;
        }
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("update Todo set title = ?, due =?, priority = ? where id = ?",
                new String[]{title, DATE_TIME_FORMAT.format(due), String.valueOf(priority), String.valueOf(id)});
    }

    public void updateTodoDone(int id) {
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("update Todo set done = 1 where id = ?",
                new String[]{String.valueOf(id)});
    }


    public void deleteTodo(int id) {
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("delete from Todo where id = ?",
                new String[]{String.valueOf(id)});
    }

    public List<Todo> toExpired(int expireMinute) throws ParseException{
        Calendar calendar = Calendar.getInstance();
        calendar.add(MINUTE, expireMinute);

        Calendar calendarNow = Calendar.getInstance();
        List<Todo> todos = new ArrayList<>();
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from Todo where done = 0 and due > ? and due < ? order by due desc",
                new String[]{DATE_TIME_FORMAT.format(calendarNow.getTime()), DATE_TIME_FORMAT.format(calendar.getTime())});

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String todoTitle = cursor.getString(cursor.getColumnIndex("title"));
            Date due = DATE_TIME_FORMAT.parse(cursor.getString(cursor.getColumnIndex("due")));
            int priority = cursor.getInt(cursor.getColumnIndex("priority"));
            Todo todo = new Todo(id, todoTitle, due, Priority.values()[priority]);
            todos.add(todo);
        }
        return todos;
    }
}
