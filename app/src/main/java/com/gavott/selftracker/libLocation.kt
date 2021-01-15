package com.gavott.selftracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.component1
import androidx.core.location.component2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

var locationCallback = object : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult?) {
        Log.i("myTag", "in locationResult");
        if(locationResult==null) { Log.i("myTag", "locationResult==null"); return}
        var (lat, lng)=locationResult.locations[0]
        val tHide= preferences.getInt("tHide", 0);
        val tUnix = System.currentTimeMillis() / 1000L;
        val boOn=tHide>tUnix;
        if(!boOn) {stopLocationUpdates(); return}

        repositoryTab.updateAllEnabledISeq() .writeData()

        val boSetTHide=boFirstMessage; boFirstMessage=false
        GlobalScope.launch {
            val hideTimer= preferences.getInt("hideTimer", HIDE_TIMER_DEFAULT)
            val nMess=MyInternet.sendMessAll(queue, repositoryTab.listSite, false, true, hideTimer, lat, lng, boSetTHide)
            Log.i("myTag", "$nMess messages sent")
        }
    }
}
fun getPermissions(context: Context):List<Boolean> {
    var boFineOK= ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    var boBGOK= ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    if(android.os.Build.VERSION.SDK_INT < 29 ) { boBGOK=true;}
    return listOf(boFineOK, boBGOK)
}
@SuppressLint("MissingPermission")
fun startLocationUpdates() {
    val tInterval= preferences.getInt("tInterval", T_INTERVAL_DEFAULT)
    val tFastestInterval= preferences.getInt("tFastestInterval", T_INTERVAL_DEFAULT)
    val boActiveTriggering= preferences.getBoolean("boActiveTriggering", true)
    val intAccuracy= preferences.getInt("intAccuracy", LocationRequest.PRIORITY_HIGH_ACCURACY)
    val floatSmallestDisplacement= preferences.getFloat("floatSmallestDisplacement", FLOAT_SMALLEST_DISPLACEMENT)

    val intTmp=if(boActiveTriggering) intAccuracy else LocationRequest.PRIORITY_NO_POWER
    locationRequest?.setInterval(tInterval.toLong()*1000)?.setFastestInterval(tFastestInterval.toLong()*1000)?.setPriority(intTmp)?.setSmallestDisplacement(floatSmallestDisplacement)
    var tmp=fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    tmp.addOnFailureListener{
        Log.i("myTag", "fail")
    }
}
//fun startLocationUpdatesW(context: Context):List<Boolean> {
//    val (boFineOK, boBGOK)=getPermissions(context)
//    if(!boFineOK || !boBGOK) {
//        val dialogPerm=(context as MainActivity).dialogPerm; dialogPerm.setInfoTexts(boFineOK, boBGOK);
//        dialogPerm.show(childFragmentManager, "blabla")
//    }  else startLocationUpdates()
//    if(!boFineOK || !boBGOK) {}  else startLocationUpdates()
//    return listOf(boFineOK, boBGOK)
//}
fun stopLocationUpdates() {
    fusedLocationClient.removeLocationUpdates(locationCallback)
    Log.i("myTag", "stopLocationUpdates")
}
fun isLocationEnabled(context:Context){
    LocationManagerCompat.isLocationEnabled(context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
}

const val MY_PERMISSIONS_REQUEST_LOCATION=1234
const val T_INTERVAL_DEFAULT=60
const val HIDE_TIMER_DEFAULT=15
const val FLOAT_SMALLEST_DISPLACEMENT=0.0F

lateinit var fusedLocationClient: FusedLocationProviderClient
lateinit var locationRequest: LocationRequest

var boFirstMessage=false // "On"-button clicked