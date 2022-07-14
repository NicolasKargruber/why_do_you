package com.example.timekeeper.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.example.timekeeper.data.AppModal
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


class DBHandler(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
// creating a constructor for our database handler.

    val logTag = "DBHandler"

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

    companion object {
        // creating a constant variables for our database.
        // below variable is for our database name.
        private const val DB_NAME = "appsdb"

        // below int is our database version
        private const val DB_VERSION = 1

        // below variable is for our table name.
        private const val TABLE_NAME = "myapps"

        // below variable is for our id column.
        private const val ID_COL = "id"

        // below variable is for our course name column
        private const val NAME_COL = "name"

        // below variable id for our course duration column.
        private const val ICON_COL = "icon"

        // below variable for our course description column.
        private const val PACKAGE_COL = "package"

        // below variable is for our course tracks column.
        private const val IS_LOCKED_COL = "isLocked"

        // below variable is for our course tracks column.
        private const val LAST_LOCKED_COL = "lastLocked"
    }

    // below method is for creating a database by running a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types: Null, Integer, Real, Text, Blob
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + ICON_COL + " BLOB,"
                + PACKAGE_COL + " TEXT,"
                + IS_LOCKED_COL + " INTEGER,"
                + LAST_LOCKED_COL + " TEXT)")

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query)
    }

    // this method is use to add new course to our sqlite database.
    fun addNewApp(app: AppModal) {
        // on below line we are creating a variable for
        // our sqlite database and calling writable method
        // as we are writing data in our database.
        val db = this.writableDatabase

        // on below line we are creating a
        // variable for content values.
        val values = ContentValues()

        // on below line we are passing all values
        // along with its key and value pair.
        app.apply {
            values.put(NAME_COL, name)
            values.putDrawable(ICON_COL, icon)
            values.put(PACKAGE_COL, packageName)
            values.put(IS_LOCKED_COL, isLocked)
            values.put(LAST_LOCKED_COL,sdf.format(lastTimeLocked))
        }

        // after adding all values we are passing
        // content values to our table.
        db.insert(TABLE_NAME, null, values)

        // at last we are closing our
        // database after adding database.
        db.close()

        Log.d(logTag, "Added new app: ${app.name}")
    }

    // we have created a new method for reading all the courses.
    fun readApps(): ArrayList<AppModal> {
        // on below line we are creating a
        // database for reading our database.
        val db = this.readableDatabase

        // on below line we are creating a cursor with query to read data from database.
        val cursorApps = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        // on below line we are creating a new array list.
        val courseModalArrayList: ArrayList<AppModal> = ArrayList()

        // moving our cursor to first position.
        if (cursorApps.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                courseModalArrayList.add(
                    AppModal(
                        cursorApps.getString(1),
                        cursorApps.getDrawable(2),
                        cursorApps.getString(3),
                        cursorApps.getInt(4) > 0,
                        sdf.parse(cursorApps.getString(5))!!
                    )
                )
            } while (cursorApps.moveToNext())
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        cursorApps.close()

        Log.d(logTag, "Just read DB table")
        return courseModalArrayList
    }

    // below is the method for updating our apps
    fun updateApp(originalAppName: String, updatedApp: AppModal) {

        // calling a method to get writable database.
        val db = this.writableDatabase
        val values = ContentValues()

        // on below line we are passing all values
        // along with its key and value pair.
        updatedApp.apply {
            values.put(NAME_COL, name)
            values.putDrawable(ICON_COL, icon)
            values.put(PACKAGE_COL, packageName)
        }

        // on below line we are calling a update method to update our database and passing our values.
        // and we are comparing it with name of our course which is stored in original name variable.
        db.update(TABLE_NAME, values, "name=?", arrayOf(originalAppName))

        db.close()

        Log.d(logTag, "Updated app in DB: $originalAppName")
    }

    // below is the method for updating the isLocked value of an app
    fun updateIsLockedOfApp(app: AppModal) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(IS_LOCKED_COL, app.isLocked)
        db.update(TABLE_NAME, values, "name=?", arrayOf(app.name))
        db.close()

        Log.d(logTag, "Set isLocked in DB of app ${app.name} to ${app.isLocked}")
    }

    // below is the method for updating the lastLocked date of an app
    fun updateLastLockedOfApp(app: AppModal) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(LAST_LOCKED_COL, sdf.format(app.lastTimeLocked))
        db.update(TABLE_NAME, values, "name=?", arrayOf(app.name))
        db.close()

        Log.d(logTag, "Set lastLocked in DB of app ${app.name} to ${app.lastTimeLocked}")
    }

    // below is the method for deleting our course.
    fun deleteApp(appName: String) {
        // on below line we are creating
        // a variable to write our database.
        val db = this.writableDatabase
        // on below line we are calling a method to delete our
        // course and we are comparing it with our course name.
        db.delete(TABLE_NAME, "name=?", arrayOf(appName))
        db.close()

        Log.d(logTag, "Deleted from DB: $appName")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun ContentValues.putDrawable(key: String, img: Drawable?) {
        val bm = img?.toBitmap()
        val outputStream = ByteArrayOutputStream()
        bm?.compress(CompressFormat.PNG, 0, outputStream)
        put(key, outputStream.toByteArray())
    }

    private fun Cursor.getDrawable(i: Int): Drawable {
        val imgByte: ByteArray = this.getBlob(i)
        val bm = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size)
        return bm.toDrawable(context!!.resources)
    }
}
