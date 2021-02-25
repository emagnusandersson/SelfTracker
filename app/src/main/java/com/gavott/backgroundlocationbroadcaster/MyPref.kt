package com.gavott.backgroundlocationbroadcaster


import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class RepositoryTab(){
    lateinit var listSite:MutableList<DataRow>


//    var StrMessRet= mutableListOf<String>()
//    private val _intTrash: MutableLiveData<Int> = MutableLiveData(0)
//    val intTrash: LiveData<Int> = _intTrash

    init{
        preferences.registerOnSharedPreferenceChangeListener{sharedPreferences, key->
            if(key!= "listSite") return@registerOnSharedPreferenceChangeListener
            updateCache()
        }

        updateCache()
    }
    private fun updateCache(){
        val jsonSite=preferences.getString("listSite","[]")?:"[]"
        try{
            val listSiteT= Json.decodeFromString<List<DataRow>>(jsonSite)
            listSite = listSiteT.toMutableList()
        } catch(err:Exception) {
            Log.e("myTag", err.toString())
            listSite=mutableListOf()
        }
    }

    fun writeData(): RepositoryTab{
        var jsonSite = Json.encodeToString(listSite)
        preferences.edit().putString("listSite", jsonSite).apply()
        return this
    }

    fun updateAllEnabledISeq(): RepositoryTab{     listSite.forEach { row -> if(row.boEnable) row.iSeq++ };    return this    }
    fun moveRow(iRow:Int, iPos:Int): RepositoryTab{
        val rowT=listSite.removeAt(iRow)
        listSite.add(iPos, rowT)
        return this
    }
    fun toggleRow(iRow:Int, boEnable:Boolean): RepositoryTab{     listSite[iRow].boEnable=boEnable;    return this    }
    fun addRow(uri:String, iRole:Int, strUUID:String): RepositoryTab{
        val rowT= DataRow(uri,iRole=iRole, strUUID=strUUID)
        listSite.add(rowT)
        return this
    }
    fun deleteRow(iRow:Int): RepositoryTab{ listSite.removeAt(iRow); return this }
    fun editRow(iRow:Int, uri:String, iRole:Int=0, strUUID:String): RepositoryTab{
        listSite[iRow].uri=uri; listSite[iRow].iRole=iRole; listSite[iRow].strUUID=strUUID; listSite[iRow].iSeq=0; listSite[iRow].boEnable=true
        return this
    }
    //fun setKey(iRow:Int, strUUID:String){ listSite[iRow].strUUID=strUUID;    return this  }
    fun anyOn():Boolean{
        var boOn=false
        listSite.forEach { row -> if(row.boEnable ) boOn=true}
        return boOn
    }
    fun getNRow():Int{return listSite.size }
    fun isUriInArray(uri:String): Int?{
        var iRow:Int?=null
        listSite.forEachIndexed { index, dataRow ->
            if(dataRow.uri==uri) {iRow=index; return@forEachIndexed}
        }
        return iRow
    }


    // readRawPref and writeRawPref should not be used (will be removed)
    fun readRawPref() :String {
        val jsonSite=preferences.getString("listSite", "[]")?:"[]"
        return jsonSite
    }
}
