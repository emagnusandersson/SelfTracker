package com.gavott.selftracker

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import kotlinx.serialization.*

val uriInfo="https://gavott.com/selfTracker"
val uriListOfValidSites="https://gavott.com/selfTracker_ListOfValidSites"
val uriHelpSettings="https://gavott.com/selfTracker_helpSettings"
val uriPrivacy="https://gavott.com/selfTracker_privacy"
//val uriPrivacy="https://emagnusandersson.github.io/selfTracker/privacy.html"
// https://demo.closeby.market#12345678901234567890123456789012

@Serializable
data class DataRow(var uri: String="", var boEnable: Boolean = true, var strUUID: String="", var iRole: Int=0, var iSeq: Int=0)
@Serializable
data class DataMess(val keyRemoteControl:String="", val iSeq: Int=0, val boCheck: Boolean=true, val boShow: Boolean=false, val iRole: Int=0, val hideTimer: Int=0, val lat: Double=0.0, val lng: Double=0.0, val boSetTHide: Boolean=false )




var boFirstActivityCreation=true // First creation of main activity

lateinit var preferences: SharedPreferences
lateinit var repositoryTab:RepositoryTab

class MainActivity : AppCompatActivity() {
    lateinit var fragmentManager: FragmentManager
    val fragmentFront = FragmentFront()
    val fragmentManage = FragmentManage()
    val fragmentSetting = FragmentSetting()
    lateinit var dialogPerm: DialogPerm
    var myThis=this
    var boGps=false
    var boNet=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(boFirstActivityCreation) {
            preferences = this.getSharedPreferences("myPref", Context.MODE_PRIVATE)
            repositoryTab=RepositoryTab()
            queue = Volley.newRequestQueue(application)
        }

        //dialogPerm = DialogPerm.newInstance(false, false)
        dialogPerm = DialogPerm()

        fragmentManager = getSupportFragmentManager()
        //val frameLayout: FrameLayout = findViewById(R.id.fragment_container)
        if(savedInstanceState != null) { return; }
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, fragmentFront, null)
        fragmentTransaction.commit()


        if(boFirstActivityCreation){
            locationRequest= LocationRequest.create();  //.apply { interval=tInterval;  fastestInterval=tFastestInterval;  priority=intPriority; smallestDisplacement=floatSmallestDisplacement }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//            val tHide= preferences.getInt("tHide", 0);    val tUnix = System.currentTimeMillis() / 1000L;
//            if(tHide>tUnix && repositoryTab.anyOn())  startLocationUpdatesW(this)
//            if(tHide>tUnix && repositoryTab.anyOn()) {
//
//                if (!boFineOK || !boBGOK) {
//                    dialogPerm.setInfoTexts(boFineOK, boBGOK);
//                    dialogPerm.show()
//                } else startLocationUpdates()
//            }
        }
        boFirstActivityCreation=false

        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var boLocationEnabled=false
        if(android.os.Build.VERSION.SDK_INT >= 28 ) {
            boLocationEnabled=manager.isLocationEnabled();
            if(!boLocationEnabled)   {
                val cbE=fun(){ (myThis as MainActivity).finishAndRemoveTask(); }
                showLocationEnablePop(this, "Location (via GPS / wifi+celltower) is disabled, do you want to enable it?", "Yes", "Exit", cbN=cbE)
            }
        } else {
            try {  boGps = manager.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (ex: Exception) { }
            try {  boNet = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (ex: Exception) { }
            var intAccuracy=preferences.getInt("intAccuracy",LocationRequest.PRIORITY_HIGH_ACCURACY);

            if(!boGps && boNet) {
                if(intAccuracy==LocationRequest.PRIORITY_HIGH_ACCURACY) {
                    val cbY=fun(){ (myThis as MainActivity).finishAndRemoveTask(); }
                    val cbN=fun(){ val intT=LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;  preferences.edit().putInt("intAccuracy", intT).apply()}
                    showLocationEnablePop(this, "GPS is disabled in the device settings.",
                        "Go to the device settings to enable GPS","Use Wifi/cell-tower-location", cbY, cbN)
                }
            } else if(boGps && !boNet) {
                if(intAccuracy==LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY || intAccuracy==LocationRequest.PRIORITY_LOW_POWER) {
                    val cbY=fun(){ (myThis as MainActivity).finishAndRemoveTask(); }
                    val cbN=fun(){ val intT=LocationRequest.PRIORITY_HIGH_ACCURACY;  preferences.edit().putInt("intAccuracy", intT).apply()}
                    showLocationEnablePop(this, "Wifi/cell-tower-location is disabled in the device settings.",
                        "Go to the device settings to enable Wifi/cell-tower-location","Use GPS", cbY, cbN)
                }
            } else if(!boGps && !boNet) {
                val strMess="Both GPS location and Wifi/cell-tower-location are disabled\nI recommend you to enable at least \"Wifi/cell-tower\")"
                val strY = "Go to the settings to enable them";  val strN = "Exit"
                val cbE=fun(){ (myThis as MainActivity).finishAndRemoveTask(); }
                showLocationEnablePop(this, strMess, strY, strN, cbE, cbE)
            }
        }
    }
}
fun showLocationEnablePop(context:Context, strMess:String, strY:String="Yes", strN:String="No", cbY:()->Unit={}, cbN:()->Unit={}) {
    val dialogBuilder = AlertDialog.Builder(context)
    dialogBuilder.setMessage(strMess)
        .setCancelable(false)
        .setPositiveButton(strY, DialogInterface.OnClickListener { dialog, id ->
            val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            //context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            cbY();
        })
        .setNegativeButton(strN, DialogInterface.OnClickListener { dialog, id ->  cbN(); dialog.cancel()  })
    val alert = dialogBuilder.create()
    alert.show()
}