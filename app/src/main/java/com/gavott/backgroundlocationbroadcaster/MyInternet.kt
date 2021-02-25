package com.gavott.backgroundlocationbroadcaster

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object MyInternet {
    var StrMessRet= mutableListOf<String>()

    private val _semNewMess: MutableLiveData<Int> = MutableLiveData(0)
    val semNewMess: LiveData<Int> = _semNewMess

    fun prepareUri(row: DataRow, boCheck: Boolean, boShow: Boolean, hideTimer: Int, lat: Double, lng: Double, boSetTHide: Boolean): String {
        val dataMess = DataMess(row.strUUID, row.iSeq, boCheck, boShow, row.iRole, hideTimer, lat, lng, boSetTHide)
        //val jsonData = json.stringify(DataMess.serializer(), dataMess)
        val jsonData = Json.encodeToString(dataMess)
        val params = "?dataFromRemoteControl=" + jsonData; return row.uri + params
    }
    fun clearStrMessRet(){
        //_StrMessRet.value?.forEach { it.substring(0,0) }
        //StrMessRet.forEach { it.substring(0,0) }
        //StrMessRet.forEach { "" }
        //StrMessRet.forEach { return "" }
        //StrMessRet.map { "" }
        for((i,strT) in StrMessRet.withIndex()){
            StrMessRet[i]=""
        }
    }
    fun triggerSemNewMess(){
        _semNewMess.postValue(0)
    }

    suspend fun makeSingleRequest(queue: RequestQueue, url: String) = suspendCoroutine<String> { cont ->
        //val obj = json.parse(Data.serializer(), """{"a":42}""") // b is optional since it has default value
        //println(obj) // Data(a=42, b="42")
        val stringRequest = StringRequest(Request.Method.GET, url,
                { response -> cont.resume(response) },
                { err -> cont.resume(err.toString()) }
        )
        queue.add(stringRequest)
    }

    suspend fun sendMess(queue: RequestQueue, row: DataRow, boCheck: Boolean, boShow: Boolean = false, hideTimer: Int = 0, lat: Double = 0.0, lng: Double = 0.0, boSetTHide: Boolean = false): String {
        val urlT = prepareUri(row, boCheck, boShow, hideTimer, lat, lng, boSetTHide)
        val data = makeSingleRequest(queue, urlT)
        return data
    }

    suspend fun sendMessAll(queue: RequestQueue, listSite: MutableList<DataRow>, boCheck: Boolean, boShow: Boolean = false, hideTimer: Int = 0, lat: Double = 0.0, lng: Double = 0.0, boSetTHide: Boolean = false): Int {
        var iMess = 0
        clearStrMessRet()
        val lSite=listSite.size
//        val StrTmp=_StrMessRet.getValue()!!
//        var lMess=StrTmp.size;
//        for (i in lMess..lSite-1) { StrTmp.add("") }
        var lMess=StrMessRet.size;
        for (i in lMess..lSite-1) { StrMessRet.add("") }
        _semNewMess.postValue(0)
        val deferred = listSite.mapIndexed { i, row ->
            GlobalScope.async {
                var data = ""
                if (row.boEnable) {
                    var strMess: String
                    if (row.strUUID.length > 0) {
                        data = sendMess(queue, row, boCheck, boShow, hideTimer, lat, lng, boSetTHide)
                        data = data.take(200)
                        data = myJSEscape(data)
                        data = data.replace("`", "\\`")
                        iMess++
                        strMess = "$data, (iSeq: ${row.iSeq})" //Uri $i,
                    } else {
                        data = "Empty key"
                        strMess = "No request made (Empty key))" //Uri $i:
                    }
                    //Log.i("myTag", strMess)
                    //val StrTmp=_StrMessRet.value
                    //if(StrTmp==null) return@async
                    //StrTmp[i] = strMess
                    //_StrMessRet.postValue(StrTmp)
                    StrMessRet[i]=strMess
                    _semNewMess.postValue(0)
                    Log.i("myTag", "$i, $data")
                }
                data
            }
        }
        deferred.map { it.await() }
        return iMess
    }
}
lateinit var queue: RequestQueue
