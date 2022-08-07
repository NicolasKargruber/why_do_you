package com.nicokarg.whydoyou.database

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
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.model.NoteModal
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DBHandler(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
// creating a constructor for our database handler.

    val logTag = "DBHandler"
    private val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm")

    companion object {
        // creating a constant variables for our database.
        // below variable is for our database name.
        private const val DB_NAME = "maindb"

        // below int is our database version
        private const val DB_VERSION = 1

        // ------------------------------------
        // below variable is for our apps table name.
        private const val APPS_TABLE_NAME = "my_apps"

        private const val APP_ID_COL = "app_id"
        private const val NAME_COL = "name"
        private const val ICON_ID_COL = "icon_id"
        private const val ICON_COL = "icon"
        private const val PACKAGE_COL = "package"
        private const val CATEGORY_COL = "category"
        private const val IS_SYSTEM_COL = "isSystem"
        private const val IS_LOCKED_COL = "isLocked"
        private const val LAST_LOCKED_COL = "lastLocked"

        // ------------------------------------
        // below variable is for our notes table name.
        private const val NOTES_TABLE_NAME = "my_notes"

        private const val NOTES_ID_COL = "notes_id"
        private const val POSITION_COL = "position"
        private const val CONTENT_COL = "content"
    }


    // below method is for creating a database by running a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types: Null, Integer, Real, Text, Blob
        val queryApps = ("CREATE TABLE " + APPS_TABLE_NAME + " ("
                + APP_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + ICON_ID_COL + " INTEGER,"
                + ICON_COL + " BLOB,"
                + PACKAGE_COL + " TEXT,"
                + CATEGORY_COL + " INTEGER,"
                + IS_SYSTEM_COL + " INTEGER,"
                + IS_LOCKED_COL + " INTEGER,"
                + LAST_LOCKED_COL + " TEXT)")
        val queryNotes = ("CREATE TABLE " + NOTES_TABLE_NAME + " ("
                + NOTES_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + POSITION_COL + " INTEGER,"
                + CONTENT_COL + " TEXT)")

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(queryApps)
        db.execSQL(queryNotes)
    }

    // this method is use to add new app to our sqlite database.
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
            values.put(ICON_ID_COL, icon.first)
            values.putDrawable(ICON_COL, icon.second)
            values.put(PACKAGE_COL, packageName)
            values.put(CATEGORY_COL, category)
            values.put(IS_SYSTEM_COL, isSystemApp)
            values.put(IS_LOCKED_COL, isLocked)
            values.put(LAST_LOCKED_COL, sdf.format(lastTimeLocked))
        }

        // after adding all values we are passing
        // content values to our table.
        db.insert(APPS_TABLE_NAME, null, values)

        // at last we are closing our
        // database after adding database.
        db.close()

        Log.d(logTag, "Added new app: ${app.name}")
    }

    // we have created a new method for reading all the apps.
    fun readApps(): ArrayList<AppModal> {
        // on below line we are creating a
        // database for reading our database.
        val db = this.readableDatabase

        // on below line we are creating a cursor with query to read data from database.
        val cursorApps = db.rawQuery("SELECT * FROM $APPS_TABLE_NAME", null)

        // on below line we are creating a new array list.
        val appModalArrayList: ArrayList<AppModal> = ArrayList()

        // moving our cursor to first position.
        if (cursorApps.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                appModalArrayList.add(
                    AppModal(
                        cursorApps.getString(1),
                        Pair(
                            cursorApps.getInt(2), cursorApps.getDrawable(3)
                        ),
                        cursorApps.getString(4),
                        cursorApps.getInt(5),
                        cursorApps.getInt(6) > 0,
                        cursorApps.getInt(7) > 0,
                        sdf.parse(cursorApps.getString(8))!!
                    )
                )
                //ToDo check if id works
                appModalArrayList.last().id = cursorApps.getInt(0) // get id of in DB
            } while (cursorApps.moveToNext())
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        cursorApps.close()

        Log.d(logTag, "Just read DB table")
        return appModalArrayList
    }

    // below is the method for updating our apps
    fun updateApp(updatedApp: AppModal) {
        val db = this.writableDatabase
        val values = ContentValues()
        updatedApp.apply { // updates only name and icon
            values.put(NAME_COL, name)
            values.put(ICON_ID_COL, icon.first)
            values.putDrawable(ICON_COL, icon.second)
            values.put(CATEGORY_COL, category)
        }
        db.update(
            APPS_TABLE_NAME,
            values,
            "$PACKAGE_COL=?",
            arrayOf(updatedApp.packageName)
        ) // package name is unique
        db.close()

        Log.d(logTag, "Updated app in DB: ${updatedApp.name}")
    }

    // below is the method for updating the isLocked value of an app
    fun updateIsLockedOfApp(appPackage: String, isLocked: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(IS_LOCKED_COL, isLocked)
        db.update(APPS_TABLE_NAME, values, "$PACKAGE_COL=?", arrayOf(appPackage))
        db.close()

        Log.d(logTag, "Set isLocked in DB of app $appPackage to $isLocked")
    }

    // below is the method for updating the lastLocked date of an app
    fun updateLastLockedOfApp(appPackage: String, lastTimeLocked: Date) {
        val formattedDate = sdf.format(lastTimeLocked)
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(LAST_LOCKED_COL, formattedDate)
        db.update(APPS_TABLE_NAME, values, "$PACKAGE_COL=?", arrayOf(appPackage))
        db.close()

        Log.d(logTag, "Set lastTimeLocked in DB of app $appPackage to $formattedDate")
    }

    // below is the method for checking weather an app is in db
    fun getAppFromDB(appPackage: String): AppModal? {
        val db = this.readableDatabase
        val cursorApps =
            db.rawQuery("SELECT * FROM $APPS_TABLE_NAME WHERE $PACKAGE_COL=?", arrayOf(appPackage))
        var appModal: AppModal? = null
        if (cursorApps.moveToFirst()) { // moving our cursor to first position.
            appModal = AppModal(
                cursorApps.getString(1),
                Pair(
                    cursorApps.getInt(2), cursorApps.getDrawable(3)
                ),
                cursorApps.getString(4),
                cursorApps.getInt(5),
                cursorApps.getInt(6) > 0,
                cursorApps.getInt(7) > 0,
                sdf.parse(cursorApps.getString(8))!!
            )
            //ToDo check if id works
            appModal.id = cursorApps.getInt(0) // get id of in DB
        }
        cursorApps.close()
        return appModal
    }

    // below is the method for deleting our app.
    fun deleteApp(appPackage: String) {
        val db = this.writableDatabase
        db.delete(APPS_TABLE_NAME, "$PACKAGE_COL=?", arrayOf(appPackage))
        db.close()

        Log.d(logTag, "Deleted from DB: $appPackage")
    }

    // this method is use to add new app to our sqlite database.
    fun addNewNote(note: NoteModal) {
        val db = this.writableDatabase
        val values = ContentValues()
        note.apply {
            values.put(POSITION_COL, position)
            values.put(CONTENT_COL, content)
        }
        db.insert(NOTES_TABLE_NAME, null, values)
        db.close()

        Log.d(logTag, "Added new note: ${note.content}")
    }

    // we have created a new method for reading all the apps.
    fun readNotes(): ArrayList<NoteModal> {
        val db = this.readableDatabase
        val cursorNotes = db.rawQuery("SELECT * FROM $NOTES_TABLE_NAME", null)
        val noteModalArrayList: ArrayList<NoteModal> = ArrayList()
        if (cursorNotes.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                noteModalArrayList.add(
                    NoteModal(
                        cursorNotes.getInt(1),
                        cursorNotes.getString(2)
                    )
                )
                noteModalArrayList.last().id =
                    cursorNotes.getInt(0) // get android of noteModalArrayList in DB
            } while (cursorNotes.moveToNext())
            // moving our cursor to next.
        }
        cursorNotes.close()

        Log.d(logTag, "Just read DB table to get NOTES")
        return noteModalArrayList
    }

    // below is the method for updating our notes
    fun updateNote(originalPos: Int, updatedNote: NoteModal) {
        val db = this.writableDatabase
        val values = ContentValues()
        updatedNote.apply {
            values.put(POSITION_COL, position)
            values.put(CONTENT_COL, content)
        }
        db.update(NOTES_TABLE_NAME, values, "$POSITION_COL=?", arrayOf(originalPos.toString()))
        db.close()

        Log.d(logTag, "Updated note in DB to: ${updatedNote.content}")
    }


    // below is the method for deleting our app.
    fun deleteNote(notePos: Int) {
        val db = this.writableDatabase
        db.delete(NOTES_TABLE_NAME, "$POSITION_COL=?", arrayOf(notePos.toString()))
        db.close()

        Log.d(logTag, "Deleted Note from DB with position: $notePos")
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS $APPS_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $NOTES_TABLE_NAME")
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
