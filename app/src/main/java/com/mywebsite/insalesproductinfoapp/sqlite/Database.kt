package com.mywebsite.insalesproductinfoapp.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.mywebsite.insalesproductinfoapp.model.ListItem
import com.mywebsite.insalesproductinfoapp.model.TableObject
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.*


class Database(private val context: Context) : SQLiteOpenHelper(
    context,
    databaseName,
    null,
    databaseVersion
) {

    companion object {
        private const val databaseVersion = 2
        private const val databaseName = "magic_qr_database"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODE_DATA = "code_data"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IMAGE = "image"
        private const val COLUMN_QUANTITY = "quantity"
        private const val DEFAULT_TABLE_NAME = "default_table"

        private const val LIST_FIELDS_TABLE_NAME = "list_fields"
        private const val LIST_COLUMN_ID = "id"
        private const val LIST_COLUMN_FIELD_NAME = "field_name"
        private const val LIST_COLUMN_TABLE_NAME = "table_name"
        private const val LIST_COLUMN_OPTIONS = "options"
        private const val LIST_COLUMN_TYPE = "type"

        private const val L_TABLE_NAME = "list"
        private const val L_COLUMN_ID = "id"
        private const val L_COLUMN_LIST_NAME = "list_name"

        private const val LIST_META_DATA_TABLE_NAME = "list_metadata"
        private const val LIST_META_DATA_COLUMN_ID = "id"
        private const val LIST_META_DATA_COLUMN_LIST_ID = "list_id"
        private const val LIST_META_DATA_COLUMN_VALUE = "value"

        private const val EXPORT_TABLE_COLUMNS = "export_data_columns"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val defaultTable = ("CREATE TABLE IF NOT EXISTS " + DEFAULT_TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CODE_DATA + " TEXT," + COLUMN_DATE + " TEXT," + COLUMN_IMAGE + " TEXT," + COLUMN_QUANTITY + " INTEGER DEFAULT 1)")

        val listFieldTable =
            ("CREATE TABLE IF NOT EXISTS $LIST_FIELDS_TABLE_NAME($LIST_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$LIST_COLUMN_FIELD_NAME TEXT,$LIST_COLUMN_TABLE_NAME TEXT,$LIST_COLUMN_OPTIONS TEXT, $LIST_COLUMN_TYPE TEXT)")

        val listTable =
            ("CREATE TABLE IF NOT EXISTS $L_TABLE_NAME($L_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$L_COLUMN_LIST_NAME TEXT)")

        val listMetaDataTable =
            ("CREATE TABLE IF NOT EXISTS $LIST_META_DATA_TABLE_NAME($LIST_META_DATA_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$LIST_META_DATA_COLUMN_LIST_ID INTEGER,$LIST_META_DATA_COLUMN_VALUE TEXT)")

        val exportColumnsTable =
            ("CREATE TABLE IF NOT EXISTS $EXPORT_TABLE_COLUMNS(id INTEGER PRIMARY KEY AUTOINCREMENT,table_name TEXT,columns TEXT)")


        db!!.execSQL(listFieldTable)
        db.execSQL(listTable)
        db.execSQL(listMetaDataTable)
        db.execSQL(defaultTable)
        db.execSQL(exportColumnsTable)
    }

    fun createTable(tableName: String, fieldsList: ArrayList<String>) {
        val db = this.writableDatabase
        val queryBuilder = StringBuilder()
        queryBuilder.append("CREATE TABLE IF NOT EXISTS $tableName(id INTEGER PRIMARY KEY AUTOINCREMENT,")
        for (i in 0 until fieldsList.size) {
            if (fieldsList[i].toLowerCase(Locale.ENGLISH) == "quantity") {
                queryBuilder.append(" ${fieldsList[i].toLowerCase(Locale.ENGLISH)} INTEGER")
            } else {
                queryBuilder.append(" ${fieldsList[i].toLowerCase(Locale.ENGLISH)} TEXT")
            }

            if (i != fieldsList.size - 1) {
                queryBuilder.append(",")
            }
        }
        queryBuilder.append(")")
        db.execSQL(queryBuilder.toString())
    }

    fun createTableFromCsv(tableName: String, fieldsList: ArrayList<String>) {
        var db = this.writableDatabase
        val queryBuilder = StringBuilder()
        queryBuilder.append("CREATE TABLE IF NOT EXISTS $tableName(_id INTEGER PRIMARY KEY AUTOINCREMENT,")
        for (i in 0 until fieldsList.size) {
            if (fieldsList[i].toLowerCase(Locale.ENGLISH) == "quantity") {
                queryBuilder.append(" ${fieldsList[i].toLowerCase(Locale.ENGLISH)} INTEGER")
            } else {
                queryBuilder.append(" ${fieldsList[i].toLowerCase(Locale.ENGLISH)} TEXT")
            }

            if (i != fieldsList.size - 1) {
                queryBuilder.append(",")
            }
        }
        queryBuilder.append(")")
        db.execSQL(queryBuilder.toString())

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $DEFAULT_TABLE_NAME")
        onCreate(db)
    }


    fun getTableDate(tableName: String, column: String, order: String): List<TableObject> {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val tableObjectList = mutableListOf<TableObject>()
        if (column.isEmpty() && order.isEmpty()) {
            val selectQuery = "SELECT  * FROM $tableName"

            val list = mutableListOf<Pair<String, String>>()
            var tableObject: TableObject? = null
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    tableObject = TableObject(
                        cursor.getString(0).toInt(),
                        cursor.getString(1),
                        cursor.getString(2),
                        if (cursor.isNull(3)) "" else cursor.getString(3)
                    )
                    tableObject.quantity = cursor.getInt(4)
                    if (columns!!.size >= 6) {
                        for (i in 5 until columns.size) {
                            val col = columns[i]
                            var pair: Pair<String, String>? = null
                            pair = Pair(col, cursor.getString(i))

                            list.add(pair)
                        }
                        tableObject.dynamicColumns.addAll(list)
                        list.clear()
                    }
                    tableObjectList.add(tableObject)
                } while (cursor.moveToNext())
            }

            cursor.close()
        }
        else {
            val selectQuery =
                "SELECT  * FROM $tableName ORDER BY $column ${order.toUpperCase(Locale.ENGLISH)}"
            val list = mutableListOf<Pair<String, String>>()
            var tableObject: TableObject? = null
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    tableObject = TableObject(
                        cursor.getString(0).toInt(),
                        cursor.getString(1),
                        cursor.getString(2),
                        if (cursor.isNull(3)) "" else cursor.getString(3)
                    )
                    tableObject.quantity = cursor.getInt(4)
                    if (columns!!.size >= 6) {
                        for (i in 5 until columns.size) {
                            val col = columns[i]
                            var pair: Pair<String, String>? = null
                            pair = Pair(col, cursor.getString(i))

                            list.add(pair)
                        }
                        tableObject.dynamicColumns.addAll(list)
                        list.clear()
                    }
                    tableObjectList.add(tableObject)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        //db.close()
        return tableObjectList

    }

    fun getTableDateFromCsv(
        tableName: String,
        column: String,
        order: String
    ): List<List<Pair<String, String>>> {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val tableObjectList = mutableListOf<List<Pair<String, String>>>()
        if (column.isEmpty() && order.isEmpty()) {
            val selectQuery = "SELECT  * FROM $tableName"

            var list = mutableListOf<Pair<String, String>>()

            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    for (i in columns!!.indices) {
                        val col = columns[i]
                        var pair: Pair<String, String>? = null
                        pair = Pair(col, cursor.getString(i))

                        list.add(pair)
                    }
                    tableObjectList.add(list)
                    list = mutableListOf()

                } while (cursor.moveToNext())
            }
        } else {
            val selectQuery =
                "SELECT  * FROM $tableName ORDER BY $column ${order.toUpperCase(Locale.ENGLISH)}"
            var list = mutableListOf<Pair<String, String>>()
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    for (i in columns!!.indices) {
                        val col = columns[i]
                        var pair: Pair<String, String>? = null
                        pair = Pair(col, cursor.getString(i))

                        list.add(pair)
                    }
                    tableObjectList.add(list)
                    list = mutableListOf()

                } while (cursor.moveToNext())
            }
        }

//        db.close()
        return tableObjectList

    }

    fun insertDefaultTable(code_data: String, date: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("code_data", code_data)
        values.put("date", date)
        db.insert(DEFAULT_TABLE_NAME, null, values)
//        db.close()
    }

    fun insertData(tableName: String, data: List<Pair<String, String>>) {
        val db = this.writableDatabase
        val values = ContentValues()
        for (i in data.indices) {
            if (data[i].second.isEmpty()) {
                values.put(data[i].first, "")
            } else {
                values.put(data[i].first, data[i].second)
            }
        }
        val res = db.insert(tableName, null, values)
//        db.close()
    }

    fun convertStringToUTF8(s: String): String? {
        var out: String? = null
        out = try {
            String(s.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))
        } catch (e: UnsupportedEncodingException) {
            return null
        }
        return out
    }

    fun updateData(tableName: String, data: List<Pair<String, String>>, id: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        for (i in data.indices) {
            if (data[i].second.isEmpty()) {
                continue
            } else {
                values.put(data[i].first, data[i].second)
            }
        }
        return db.update(tableName, values, "id=$id", null) > 0

    }

    fun updateDataCsv(tableName: String, data: List<Pair<String, String>>, id: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        for (i in data.indices) {
            if (data[i].second.isEmpty()) {
                continue
            } else {
                values.put(data[i].first, data[i].second)
            }
        }
        return db.update(tableName, values, "_id=$id", null) > 0

    }

    fun generateTable(tableName: String) {
        val db = this.writableDatabase
        var tName = ""
        tName = if (tableName.contains(" ")) {
            tableName.replace(" ", "_")
        } else {
            tableName
        }
        val createTable =
            ("CREATE TABLE IF NOT EXISTS ${tName.toLowerCase(Locale.ENGLISH)} (id INTEGER PRIMARY KEY AUTOINCREMENT,code_data TEXT,date TEXT,image TEXT,quantity INTEGER DEFAULT 1)")
        db.execSQL(createTable)
    }

    fun addNewColumn(tableName: String, column: Pair<String, String>, defaultValue: String) {
        val db = this.writableDatabase
        var cName = ""
        cName = if (column.first.contains(" ")) {
            column.first.replace(" ", "_")
        } else {
            column.first
        }
        if (tableExists(tableName)) {
            if (defaultValue.isNotEmpty()) {
                val query =
                    "ALTER TABLE ${tableName.toLowerCase(Locale.ENGLISH)} ADD COLUMN $cName ${column.second} DEFAULT $defaultValue"
                db.execSQL(query)
            } else {
                val query =
                    "ALTER TABLE ${tableName.toLowerCase(Locale.ENGLISH)} ADD COLUMN $cName ${column.second}"
                db.execSQL(query)
            }
        }
    }

    fun getTableColumns(tableName: String): Array<String>? {
        val db = this.readableDatabase
        if (tableExists(tableName)) {
            val c: Cursor = db.rawQuery("SELECT * FROM $tableName WHERE 0", null)
            c.use { cursor ->
                return cursor.columnNames
            }
        } else {
            return null
        }
    }


    fun getAllDatabaseTables(): List<String> {
        val db = this.readableDatabase
        val list = mutableListOf<String>()

        val c: Cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT IN('sqlite_sequence','android_metadata','codes_history','dynamic_qr_codes','list_fields','list','list_metadata','export_data_columns')",
            null
        )

        if (c.moveToFirst()) {
            while (!c.isAfterLast) {
                list.add(c.getString(c.getColumnIndex("name")))
                c.moveToNext()
            }
        }
//        db.close()
        return list
    }

    fun tableExists(tableName: String?): Boolean {
        val db = this.readableDatabase
        if (tableName == null || db == null || !db.isOpen) {
            return false
        }
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?",
            arrayOf("table", tableName)
        )
        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    fun isFieldExist(tableName: String, fieldName: String): Boolean {
        var isExist = false

        val db = this.writableDatabase
        val res = db.rawQuery("PRAGMA table_info($tableName)", null)
        res.moveToFirst()
        do {
            val currentColumn = res.getString(1)
            if (currentColumn == fieldName) {
                isExist = true
            }
        } while (res.moveToNext())

        return isExist
    }

    fun insertFieldList(fieldName: String, tableName: String, options: String, type: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(LIST_COLUMN_FIELD_NAME, fieldName)
        values.put(LIST_COLUMN_TABLE_NAME, tableName)
        values.put(LIST_COLUMN_OPTIONS, options)
        values.put(LIST_COLUMN_TYPE, type)
        db.insert(LIST_FIELDS_TABLE_NAME, null, values)
//        db.close()
    }

    fun getFieldList(fieldName: String, tableName: String): Pair<String, String>? {
        val db = this.readableDatabase
        var options: Pair<String, String>? = null

        val selectQuery = "SELECT  * FROM $LIST_FIELDS_TABLE_NAME WHERE $LIST_COLUMN_FIELD_NAME='${
            fieldName.toLowerCase(
                Locale.ENGLISH
            )
        }' AND $LIST_COLUMN_TABLE_NAME='${tableName.toLowerCase(Locale.ENGLISH)}'"

        val cursor: Cursor? = db.rawQuery(selectQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    options = Pair(cursor.getString(3), cursor.getString(4))

                } while (cursor.moveToNext())
            }
//            db.close()

            return options
        } else {
            return null
        }

    }

    fun insertList(listName: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(L_COLUMN_LIST_NAME, listName)
        return db.insert(L_TABLE_NAME, null, values)
        //db.close()
    }

    fun insertExportColumns(tableName: String, columns: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("table_name", tableName)
        values.put("columns", columns)
        return db.insert(EXPORT_TABLE_COLUMNS, null, values)
        //db.close()
    }

    fun insertListValue(listId: Int, value: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(LIST_META_DATA_COLUMN_LIST_ID, listId)
        values.put(LIST_META_DATA_COLUMN_VALUE, value)
        db.insert(LIST_META_DATA_TABLE_NAME, null, values)
//        db.close()
    }

    fun getList(): List<ListItem> {
        val db = this.readableDatabase
        val list = mutableListOf<ListItem>()
        val selectQuery = "SELECT  * FROM $L_TABLE_NAME"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(ListItem(cursor.getInt(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
//        db.close()
        return list
    }

    fun getListValues(listId: Int): String {
        val db = this.readableDatabase
        val list = mutableListOf<String>()
        var listOptions = ""
        val selectQuery =
            "SELECT  * FROM $LIST_META_DATA_TABLE_NAME WHERE $LIST_META_DATA_COLUMN_LIST_ID=$listId"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(2))
//                listOptions += cursor.getString(2) + ","
            } while (cursor.moveToNext())
        }
//        db.close()
        listOptions = list.joinToString()
        Log.d("TEST199", listOptions)
        return listOptions
    }

    fun getTableOriginalColumns(tableName: String): String {
        val db = this.readableDatabase
        var columns = ""
        val selectQuery = "SELECT  * FROM $EXPORT_TABLE_COLUMNS WHERE table_name='$tableName'"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                columns = cursor.getString(2)
            } while (cursor.moveToNext())
        }
        return columns
    }

    fun getFieldListValues(listId: Int): List<String> {
        val db = this.readableDatabase
        val list = mutableListOf<String>()
        val selectQuery =
            "SELECT  * FROM $LIST_META_DATA_TABLE_NAME WHERE $LIST_META_DATA_COLUMN_LIST_ID=$listId"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(2))
            } while (cursor.moveToNext())
        }
//        db.close()

        return list
    }

    fun updateBarcodeDetail(tableName: String, column: String, value: String, id: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(column, value)
        return if (tableName.contains("import")) {
            db.update(tableName, contentValues, "_id=$id", null) > 0
        } else {
            db.update(tableName, contentValues, "id=$id", null) > 0
        }

    }

    fun getUpdateBarcodeDetail(tableName: String, id: Int): TableObject? {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val selectQuery = "SELECT  * FROM $tableName WHERE id=$id"
        val list = mutableListOf<Pair<String, String>>()
        var tableObject: TableObject? = null
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                tableObject = TableObject(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2),
                    if (cursor.isNull(3)) "" else cursor.getString(3)
                )
                tableObject.quantity = cursor.getInt(4)
                if (columns!!.size >= 6) {
                    for (i in 5 until columns.size) {
                        val col = columns[i]
                        var pair: Pair<String, String>? = null
                        pair = Pair(col, cursor.getString(i))

                        list.add(pair)
                    }
                    tableObject.dynamicColumns.addAll(list)
                }
            } while (cursor.moveToNext())
        }
        return tableObject
    }

    fun getUpdateBarcodeDetail1(tableName: String, id: Int): List<Pair<String, String>> {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        //val tableObjectList = mutableListOf<Pair<String, String>?>()
        val selectQuery = "SELECT  * FROM $tableName WHERE _id=$id"

        val list = mutableListOf<Pair<String, String>>()

        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                for (i in columns!!.indices) {
                    val col = columns[i]
                    var pair: Pair<String, String>? = null
                    pair = Pair(col, cursor.getString(i))

                    list.add(pair)
                }
                //tableObjectList.addAll(list)
                //list = mutableListOf()

            } while (cursor.moveToNext())
        }
        return list
    }

    fun removeItem(tableName: String, id: Int): Boolean {
        var colName = ""
        colName = if (tableName.contains("import")) {
            "_id"
        } else {
            "id"
        }
        val db = this.writableDatabase
        return db.delete(tableName, "$colName=$id", null) > 0
    }

    fun deleteItem(tableName: String, code_data: String): Boolean {
        val db = this.writableDatabase
        return db.delete(tableName, "code_data=$code_data", null) > 0
    }

    fun searchItem(tableName: String, code_d: String): Boolean {
        val db = this.readableDatabase
//        val columns = getTableColumns(tableName)
//        val list = mutableListOf<Pair<String, String>>()
//        var tableObject: TableObject? = null
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE code_data='$code_d'", null)
//        if (cursor.moveToFirst()) {
//            do {
//                tableObject = TableObject(
//                    cursor.getString(0).toInt(),
//                    cursor.getString(1),
//                    cursor.getString(2),
//                    if (cursor.isNull(3)) "" else cursor.getString(3)
//                )
//                tableObject.quantity = cursor.getInt(4)
//                if (columns!!.size >= 6) {
//                    for (i in 5 until columns.size) {
//                        val col = columns[i]
//                        var pair: Pair<String, String>? = null
//                        pair = Pair(col, cursor.getString(i))
//
//                        list.add(pair)
//                    }
//                    tableObject.dynamicColumns.addAll(list)
//                }
//            } while (cursor.moveToNext())
//        }
//        return tableObject != null
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    fun getScanItem(tableName: String, code_d: String): TableObject? {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val list = mutableListOf<Pair<String, String>>()
        var tableObject: TableObject? = null
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE code_data='$code_d'", null)
        if (cursor.moveToFirst()) {
            do {
                tableObject = TableObject(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2),
                    if (cursor.isNull(3)) "" else cursor.getString(3)
                )
                tableObject.quantity = cursor.getInt(4)
                if (columns!!.size >= 6) {
                    for (i in 5 until columns.size) {
                        val col = columns[i]
                        var pair: Pair<String, String>? = null
                        pair = Pair(col, cursor.getString(i))

                        list.add(pair)
                    }
                    tableObject.dynamicColumns.addAll(list)
                }
            } while (cursor.moveToNext())
        }
        return tableObject
    }

    fun updateScanQuantity(tableName: String, code_data: String, quantity: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_QUANTITY, quantity)
        return db.update(tableName, contentValues, "code_data='$code_data'", null) > 0
    }

    fun getScanQuantity(tableName: String, code_d: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE code_data='$code_d'", null)
        return if (cursor != null) {
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndex("quantity"))
        } else {
            "-1"
        }
    }

    fun getDbPath(): String {
        val db = this.readableDatabase
        return db.path
    }

    fun deleteDatabase() {
        val db = this.writableDatabase
        context.deleteDatabase(databaseName)
        onCreate(db)
    }

    fun mergeDatabases(backupDbName: String, backupDbPath: String) {
        val db = this.writableDatabase
        val db1 = this.readableDatabase

        //db.execSQL("DROP TABLE IF EXISTS default_table")
        val dbPath = db.path

        val query = "ATTACH DATABASE '$backupDbPath' AS $databaseName"
        if (checkDb(dbPath)) {
            db.execSQL(query)

            val defaultTableColumnList = getTableColumns("default_table")
            val defaultColumnsList = mutableListOf<String>()
            defaultColumnsList.addAll(defaultTableColumnList!!.toList())
            Log.d("TEST199", defaultTableColumnList.toString())

            val dbCursor = db1.query(
                "$databaseName.default_table",
                null,
                null,
                null,
                null,
                null,
                null
            )
            val backupDefaultTableColumnList = mutableListOf<String>()
            backupDefaultTableColumnList.addAll(dbCursor.columnNames)
            val result = mutableListOf<String>()
            result.addAll(backupDefaultTableColumnList.filterNotIn(defaultColumnsList))
            if (result.isNotEmpty()) {
                for (i in 0 until result.size) {
                    val col = result[i]
                    addNewColumn("default_table", Pair(col, "TEXT"), "")
                }
            }
            backupDefaultTableColumnList.removeAt(0)
            val insertQuery =
                "INSERT INTO default_table(${backupDefaultTableColumnList.joinToString(",")}) SELECT ${
                    backupDefaultTableColumnList.joinToString(
                        ","
                    )
                } FROM $databaseName.default_table;"
            db.execSQL(insertQuery)


        }
//        else {
//
//            db.execSQL(
//                "ATTACH DATABASE '" + backupDbPath
//                        + File.separator + backupDbName + "' AS $databaseName"
//            )
//            db.execSQL("INSERT INTO ResultMaster SELECT * FROM $databaseName.ResultMaster;")
//        }

    }

    fun <T> Collection<T>.filterNotIn(collection: Collection<T>): Collection<T> {
        val set = collection.toSet()
        return filterNot { set.contains(it) }
    }

    private fun checkDb(path: String): Boolean {
        val file = File(path)
        var checkDB: SQLiteDatabase? = null
        if (file.exists()) {
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        }

        return checkDB != null
    }

    fun getBarcodeImages(tableName: String, id: Int): String {
        val db = this.readableDatabase
        var foundImages = ""
        val cursor = db.rawQuery("SELECT $COLUMN_IMAGE FROM $tableName WHERE id=$id", null)
        if (cursor.moveToFirst()) {
            foundImages = cursor.getString(cursor.getColumnIndex("image"))
        }
        return foundImages
    }

    fun deleteTable(tableName: String) {
        val db = this.writableDatabase

//        val query = "ATTACH DATABASE '$path' AS $databaseName"
//        if (checkDb(path)) {
//            db.execSQL(query)
//
        db.delete(tableName, null, null)
//        }


    }
}