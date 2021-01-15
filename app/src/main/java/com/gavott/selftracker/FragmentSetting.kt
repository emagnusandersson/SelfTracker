package com.gavott.selftracker

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationRequest
import com.google.android.material.switchmaterial.SwitchMaterial




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFront.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSetting : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var boSpinnerAccuracyInitialized=false
    var boSpinnerUpdateIntervalInitialized=false
    var boSpinnerFastestIntervalInitialized=false
    private lateinit var spinnerAccuracy: Spinner
    private lateinit var spinnerUpdateInterval: Spinner
    private lateinit var spinnerFastestInterval: Spinner
    private lateinit var butEditSmallestDisplacement: Button
    private lateinit var butBack: Button
    private lateinit var butSave: Button
    val arrUpdateInterval=arrayOf(5,10, 20, 60, 120, 300, 600, 1200, 3600, 2*3600, 6*3600, 12*3600, 86400) //
    val StrUpdateInterval=arrUpdateInterval.map { val (v,u)=TimeUnit.getSuitableTimeUnit(it); v.toString()+u }
    val arrAccuracy= arrayOf(LocationRequest.PRIORITY_LOW_POWER, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, LocationRequest.PRIORITY_HIGH_ACCURACY)
    val StrAccuracy=arrayOf("City level (Cell tower)", "Block level (Wifi)", "GPS")
//    val arrAccuracy= mutableListOf<Int>()
//    val StrAccuracy=mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View =inflater.inflate(R.layout.fragment_setting, container, false)

        var helpTmp: TextView =view.findViewById(R.id.helpSettings)
        helpTmp.text= HtmlCompat.fromHtml("<a href=\"$uriHelpSettings\">help</a>", HtmlCompat.FROM_HTML_MODE_COMPACT);
        helpTmp.setMovementMethod(LinkMovementMethod.getInstance());


            // ActiveTriggering
        val switchActiveTriggering: SwitchMaterial = view.findViewById(R.id.switchActiveTriggering)
        var boActiveTriggering=preferences.getBoolean("boActiveTriggering",true);
        switchActiveTriggering.setChecked(boActiveTriggering)
        switchActiveTriggering.setOnClickListener {
            var b= (it as SwitchMaterial).isChecked
            Log.i("myTag", "switchActive $b")
            preferences.edit().putBoolean("boActiveTriggering", b).apply()
            spinnerAccuracy.setEnabled(b);
            spinnerUpdateInterval.setEnabled(b);
        }


            // Accuracy
//        if((context as MainActivity).boNet) {arrAccuracy.add(LocationRequest.PRIORITY_LOW_POWER); StrAccuracy.add("City level (cell tower)");
//            arrAccuracy.add(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); StrAccuracy.add("Block level (Wifi)")}
//        if((context as MainActivity).boGps) {arrAccuracy.add(LocationRequest.PRIORITY_HIGH_ACCURACY); StrAccuracy.add("GPS")}

        spinnerAccuracy =view.findViewById(R.id.spinnerAccuracy)
        var tmpAdapter= ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item, StrAccuracy);    spinnerAccuracy.adapter = tmpAdapter
        var intPriorityOld=preferences.getInt("intAccuracy",LocationRequest.PRIORITY_HIGH_ACCURACY);   val indPriorityOld=arrAccuracy.indexOf(intPriorityOld);
        if(indPriorityOld==-1) {Log.e("myTag", "No such intAccuracy"); return null;}

        val cbY=fun(){ (context as MainActivity).finishAndRemoveTask(); }
        spinnerAccuracy.setSelection(indPriorityOld)
        spinnerAccuracy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!boSpinnerAccuracyInitialized) {   boSpinnerAccuracyInitialized = true; return; }
                val intT=arrAccuracy[id.toInt()];
                if(android.os.Build.VERSION.SDK_INT < 28 ){
                    if(!(context as MainActivity).boGps && intT==LocationRequest.PRIORITY_HIGH_ACCURACY) {
                        showLocationEnablePop(context as MainActivity, "GPS is disabled in the device settings.", "Go to the device settings to enable GPS-location","Cancel", cbY=cbY)
                        spinnerAccuracy.setSelection(indPriorityOld);
                        return
                    } else if(!(context as MainActivity).boNet && (intT==LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY || intT==LocationRequest.PRIORITY_LOW_POWER)) {
                        showLocationEnablePop(context as MainActivity, "Wifi/cell-tower-location is disabled in the device settings.", "Go to the device settings to enable Wifi/cell-tower-location","Cancel", cbY=cbY)
                        spinnerAccuracy.setSelection(indPriorityOld);
                        return
                    }
                }

                preferences.edit().putInt("intAccuracy", intT).apply()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
        spinnerAccuracy.setEnabled(boActiveTriggering);

            // Update Interval
        spinnerUpdateInterval =view.findViewById(R.id.spinnerTInterval)
        tmpAdapter= ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item, StrUpdateInterval);    spinnerUpdateInterval.adapter = tmpAdapter
        var tTmp=preferences.getInt("tInterval",T_INTERVAL_DEFAULT);    var (_, ind)=closest2Val(arrUpdateInterval, tTmp);     spinnerUpdateInterval.setSelection(ind)
        spinnerUpdateInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!boSpinnerUpdateIntervalInitialized) {   boSpinnerUpdateIntervalInitialized = true; return; }
                val tUpdateInterval=arrUpdateInterval[id.toInt()];
                val tFastestInterval=arrUpdateInterval[spinnerFastestInterval.selectedItemPosition];
                if(tUpdateInterval<tFastestInterval) {
                    spinnerFastestInterval.setSelection(id.toInt())
                    preferences.edit().putInt("tInterval", tUpdateInterval).putInt("tFastestInterval", tFastestInterval).apply()
                }else  preferences.edit().putInt("tInterval", tUpdateInterval).apply()

            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
        spinnerUpdateInterval.setEnabled(boActiveTriggering);

            // Fastest Interval
        spinnerFastestInterval =view.findViewById(R.id.spinnerFastestInterval)
        tmpAdapter= ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item, StrUpdateInterval);    spinnerFastestInterval.adapter = tmpAdapter
        tTmp=preferences.getInt("tFastestInterval",T_INTERVAL_DEFAULT);     val (_, indB)=closest2Val(arrUpdateInterval, tTmp);    spinnerFastestInterval.setSelection(indB)
        spinnerFastestInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!boSpinnerFastestIntervalInitialized) {   boSpinnerFastestIntervalInitialized = true; return; }
                val tFastestInterval=arrUpdateInterval[id.toInt()];
                val tUpdateInterval=arrUpdateInterval[spinnerUpdateInterval.selectedItemPosition]
                if(tUpdateInterval<tFastestInterval) {
                    spinnerUpdateInterval.setSelection(id.toInt());
                    preferences.edit().putInt("tInterval", tUpdateInterval).putInt("tFastestInterval", tFastestInterval).apply()
                }else  preferences.edit().putInt("tFastestInterval", tFastestInterval).apply()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }


            // Smallest Displacement
        butEditSmallestDisplacement=view.findViewById(R.id.butEditSmallestDisplacement)
        var floatTmp=preferences.getFloat("floatSmallestDisplacement",0f)
        butEditSmallestDisplacement.setText(floatTmp.toString()+" m")

        butEditSmallestDisplacement.setOnClickListener(View.OnClickListener {
            val builder= AlertDialog.Builder(it.context)
            builder.setTitle("Smallest displacement (in meters)")
            builder.setMessage("If you have moved less than this, no location update is sent (to save bandwidth)")

            val input = EditText(it.context)
            input.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            var floatTmp=preferences.getFloat("floatSmallestDisplacement",0f)
            input.setText(floatTmp.toString())
            builder.setView(input)

            builder.setPositiveButton("Save",
                DialogInterface.OnClickListener cb@{ dialog, which ->
                    var floatTmp = input.text.toString().toFloatOrNull()
                    if(floatTmp==null) { Toast.makeText(this.context, "Not a valid number", Toast.LENGTH_SHORT).show(); return@cb }
                    preferences.edit().putFloat("floatSmallestDisplacement", floatTmp!!).apply()
                    butEditSmallestDisplacement.setText(floatTmp.toString()+" m")
                })

            builder.show()
        })

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment aaa.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFront().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}