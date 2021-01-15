package com.gavott.selftracker

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DialogPerm : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: Boolean? = null
    private var param2: Boolean? = null

    private lateinit var messOK: TextView
    private lateinit var messNOK: TextView
    private lateinit var messOKBG: TextView
    private lateinit var messNOKBG: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getBoolean(ARG_PARAM1)
            param2 = it.getBoolean(ARG_PARAM2)
        }

        var dialogPerm = Dialog(context!!)
        dialogPerm.setContentView(R.layout.dialog_permission)


        val linkInfo:TextView=dialogPerm.findViewById(R.id.linkPrivacy)
        linkInfo.text= HtmlCompat.fromHtml("<a href=\"$uriPrivacy\">Privacy policy</a>", HtmlCompat.FROM_HTML_MODE_COMPACT);
        linkInfo.setMovementMethod(LinkMovementMethod.getInstance());


        //val butAllowLoc: TextView = dialogPerm.findViewById(R.id.butAllowLoc)
        val butAllowBGLoc: TextView = dialogPerm.findViewById(R.id.butAllowBGLoc)
        messOK = dialogPerm.findViewById(R.id.messOK)
        messNOK = dialogPerm.findViewById(R.id.messNOK)
        messOKBG = dialogPerm.findViewById(R.id.messOKBG)
        messNOKBG = dialogPerm.findViewById(R.id.messNOKBG)

        dialogPerm.findViewById<Button>(R.id.butAllowLoc)
                .setOnClickListener(View.OnClickListener cb@{
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),  MY_PERMISSIONS_REQUEST_LOCATION)
                })
        dialogPerm.findViewById<Button>(R.id.butAllowBGLoc)
                .setOnClickListener(View.OnClickListener cb@{
                    var boFineOK= ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if(!boFineOK) { Toast.makeText(context!!, "Approve use of location first", Toast.LENGTH_SHORT).show(); return@cb }
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),  MY_PERMISSIONS_REQUEST_LOCATION)
                })

        val window = dialogPerm.getWindow()
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        var (boFineOK,boBGOK)= getPermissions(context!!)
        //setInfoTexts(param1!!, param2!!)
        setInfoTexts(boFineOK,boBGOK)

        return dialogPerm
    }
    fun setInfoTexts(boFineOK:Boolean, boBGOK:Boolean){
        if(boFineOK) { messOK.setVisibility(View.VISIBLE ); messNOK.setVisibility(View.GONE ); } else { messOK.setVisibility(View.GONE ); messNOK.setVisibility(View.VISIBLE ); }
        if(boBGOK) { messOKBG.setVisibility(View.VISIBLE ); messNOKBG.setVisibility(View.GONE ); } else { messOKBG.setVisibility(View.GONE ); messNOKBG.setVisibility(View.VISIBLE ); }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if(grantResults.isEmpty()) {  Log.e("myTag", "User interaction was cancelled."); return }
        if (requestCode==MY_PERMISSIONS_REQUEST_LOCATION) {
            if(  grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.i("myTag", "${permissions[0]} not granted.");
                return
            }
            var (boFineOK,boBGOK)=getPermissions(context!!)
            setInfoTexts(boFineOK, boBGOK)

            if(boFineOK && boBGOK) {
                var tDiff=preferences.getInt("tHide",0)-getTUnix();
                if(tDiff>0 && repositoryTab.anyOn())  {
                    startLocationUpdates()
                    (context as MainActivity).fragmentFront.cbButtOnFin()
                }
                Log.i("myTag", "GOT PERMISSION")
                this.dismiss()
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    companion object {
        const val TAG = "PurchaseConfirmationDialog"
        @JvmStatic
        fun newInstance(param1: Boolean, param2: Boolean) =
                DialogPerm().apply {
                    arguments = Bundle().apply {
                        putBoolean(ARG_PARAM1, param1)
                        putBoolean(ARG_PARAM2, param2)
                    }
                }
    }
}