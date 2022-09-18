package com.example.myapplication

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.provider.ContactsContract.RawContacts.Data
import android.text.Editable
import android.util.Log
import android.util.SparseBooleanArray
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import java.util.Queue

class DbPresets{
    companion object{
        val DB_VERSION = 1
        val DB_NAME = "studentsDB"
        val TABLE_NAME = "students"


        val KEY_ID = "_id"
        val KEY_NAME = "name"

        val CREATE_SQL = "create table $TABLE_NAME ($KEY_ID integer primary key, $KEY_NAME text)"
        val DROP_SQL = "drop table if exists $TABLE_NAME"
    }
}

class DbManager(context: Context) : SQLiteOpenHelper(context, DbPresets.DB_NAME, null, DbPresets.DB_VERSION){
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(DbPresets.CREATE_SQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL(DbPresets.DROP_SQL)

        onCreate(db)
    }

}

class MainActivity : Activity() {

    val students = ArrayList<String>()
    val DataBaseManager: DbManager

    var add_btn: MaterialButton? = null
    var remove_btn: MaterialButton? = null
    var name_input: EditText? = null
    var student_list: ListView? = null
    var student_adapter: ArrayAdapter<String>? = null

    init{
        DataBaseManager = DbManager(this)
    }

    private fun updatestudents(){
        students.clear()

        val database = DataBaseManager.writableDatabase
        val cursor = database.query(DbPresets.TABLE_NAME, null, null, null, null, null, null)

        with(cursor){

            if(!moveToFirst()){
                student_adapter?.notifyDataSetChanged()
                return
            }

            val nameIndex = getColumnIndex(DbPresets.KEY_NAME)

            do{
                students.add(getString(nameIndex))
            }while(moveToNext())

            close()
        }
        student_adapter?.notifyDataSetChanged()
    }
    private fun addstudent(name: String){
        val database = DataBaseManager.writableDatabase

        val cvalues = ContentValues()
        cvalues.put(DbPresets.KEY_NAME, name)

        database.insert(DbPresets.TABLE_NAME, null, cvalues)

        name_input?.setText("", TextView.BufferType.EDITABLE)
    }


    fun showToast(text: String, duration: Int){
        Toast.makeText(applicationContext, text, duration).show()
    }

    inner class ButtonListener: View.OnClickListener{
        override fun onClick(view: View?) {
            if(view == null)
                return
            when(view.id){
                add_btn?.id -> {
                    if (name_input?.text.toString() != "") {
                        addstudent(name_input?.text.toString())
                        showToast("Студент добавлен в список", Toast.LENGTH_SHORT)
                        updatestudents()
                    }
                    else
                        showToast("Всмысле у студента нет имени?(", Toast.LENGTH_SHORT)
                }

                remove_btn?.id ->{
                    val selected = student_list?.checkedItemPositions!!
                    val database = DataBaseManager.writableDatabase

                    var changes = 0

                    for(i in 0 until (student_adapter?.count ?: 0)){
                        if (selected[i]){
                            changes++
                            database.delete(DbPresets.TABLE_NAME, "${DbPresets.KEY_NAME} = ?", arrayOf(students.get(i)))
                        }
                    }

                    student_list?.clearChoices()
                    updatestudents()

                    if(changes <= 0){
                        showToast("Изменений не произошло", Toast.LENGTH_SHORT)
                        return
                    }
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        add_btn = findViewById(R.id.add)
        remove_btn = findViewById(R.id.remove)
        name_input = findViewById(R.id.name)
        student_list = findViewById(R.id.data_list)

        student_adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, students)

        student_list?.adapter = student_adapter!!

        add_btn?.setOnClickListener(ButtonListener())
        remove_btn?.setOnClickListener(ButtonListener())

        updatestudents()
    }
}
