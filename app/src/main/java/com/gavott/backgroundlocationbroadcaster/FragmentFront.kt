package com.gavott.backgroundlocationbroadcaster

import android.content.Context
import android.graphics.Color
import android.os.*
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gavott.backgroundlocationbroadcaster.TimeUnit.getSuitableTimeUnit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask


class AdapterFront(context: Context): RecyclerView.Adapter<AdapterFront.MyViewHolder>(){
    class MyViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val uri: TextView = view.findViewById(R.id.uri)
        val mess: TextView = view.findViewById(R.id.mess)
        lateinit var params:ViewGroup.LayoutParams
        init {
            params = view.layoutParams
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater=LayoutInflater.from(parent.context)
        val viewRow=layoutInflater.inflate(R.layout.item_list_front, parent, false)
        return MyViewHolder(viewRow)
        //val binding:ItemListMainBinding= DataBindingUtil.inflate(layoutInflater, R.layout.item_list_main, parent, false)
        //return MyViewHolder(binding)
    }

    override fun getItemCount(): Int { return repositoryTab.listSite.size }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //holder.view.uri.text=repositoryTab.listSite[position].uri
        //holder.bind(repositoryTab.listSite[position])
        val site=repositoryTab.listSite[position]
        val view=holder.view
        val (uri, boEnable) = site
        val strUri=uri.substring(8)
        var strTmp:String;   if(strUri.length>5){ strTmp="$strUri (<a href=\"$uri\">link</a>)" }else { strTmp="<a href=\"$uri\">$strUri</a>" }
        holder.uri.text = HtmlCompat.fromHtml(strTmp, HtmlCompat.FROM_HTML_MODE_COMPACT);
        holder.uri.setMovementMethod(LinkMovementMethod.getInstance());

        val StrMessRetTmp=MyInternet.StrMessRet
        var strMessRetTmp=if(StrMessRetTmp!=null && position<StrMessRetTmp.size) StrMessRetTmp[position] else ""

        if("^Visible".toRegex().containsMatchIn(strMessRetTmp)) holder.mess.setTextColor(Color.parseColor("#33dd33"))
        else if("^Hidden".toRegex().containsMatchIn(strMessRetTmp)) holder.mess.setTextColor(Color.RED)
        else holder.mess.setTextColor(Color.BLACK)

        holder.mess.text=strMessRetTmp

        if(boEnable) {
            view.visibility =View.VISIBLE;
            view.setLayoutParams(holder.params)
        }else{
            view.visibility=View.GONE;
            view.setLayoutParams(RecyclerView.LayoutParams(0, 0));
        }

    }
    fun setMess(i:Int){

    }
}


fun calcTDiff():Int{
    val tHide= preferences.getInt("tHide", 0)
    var tDiff=tHide-getTUnix();
    return tDiff.toInt()
}



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFront.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFront : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var textTHide: TextView

    var boSpinnerHideTimerInitialized=false
    private lateinit var spinnerHideTimer: Spinner

    private lateinit var butOn: Button
    private lateinit var butOff: Button
    private lateinit var butCheck: Button
    private lateinit var butToManage: Button
    private lateinit var butToSetting: Button

    //private lateinit var dialogPerm: DialogPerm

    private lateinit var messOK: TextView
    private lateinit var messNOK: TextView
    private lateinit var messOKBG: TextView
    private lateinit var messNOKBG: TextView
    val MY_PERMISSIONS_REQUEST_LOCATION=1234

    //15s, 1m, 2m, 5m, 10m, 15m, 20m, 30m, 40m, 60m, 90m, 2h, 3h, 4h, 5h, 6h, 8h, 10h, 12h, 18h, 24h, 36h, 2d, 3d, 4d, 5d, 6d, 7d, 14d, 30d, ∞
    val arrHideTimer=arrayOf(15, 60, 120, 300, 600, 900, 1200, 1800, 2400, 3600, 90*60, 2*3600, 3*3600, 4*3600, 5*3600, 6*3600, 8*3600, 10*3600,
            12*3600, 18*3600, 86400, 36*3600, 2*86400, 3*86400, 4*86400, 5*86400, 6*86400, 7*86400, 14*86400, 30*86400)
    val StrHideTimer=arrHideTimer.map { val (v,u)=TimeUnit.getSuitableTimeUnit(it); v.toString()+u }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        MyInternet.clearStrMessRet(); MyInternet.triggerSemNewMess()
        setUI_THide()
    }
    fun setUI_THide(){
        var tHide=preferences.getInt("tHide",T_INTERVAL_DEFAULT)
        val date = Date(tHide.toLong() * 1000)
        var tDiff=tHide-getTUnix()
        var formLong:java.text.SimpleDateFormat
        var strT:String; var colorT= Color.GREEN
        if(tDiff<=0) {strT="Off";colorT=Color.RED}
        else {
            if (tDiff < 24 * 3600) {
                formLong = java.text.SimpleDateFormat("HH:mm:ss")
            } else if (tDiff > 36 * 3600) {
                formLong = java.text.SimpleDateFormat("yyyy-MM-dd")
            } else {
                formLong = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            }
            val strDate = formLong.format(date);
            var (strRem, strU)=getSuitableTimeUnit(tDiff.toInt());
            strT = "On, hiding: $strDate (In $strRem $strU)"
        }
        textTHide.text=strT
        textTHide.setTextColor(colorT)
        //textTHide.setBackgroundColor(colorT)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View=inflater.inflate(R.layout.fragment_front, container, false)
        val linkInfo:TextView=view.findViewById(R.id.linkInfo)
        linkInfo.text= HtmlCompat.fromHtml("<a href=\"$uriInfo\">Info</a>", HtmlCompat.FROM_HTML_MODE_COMPACT);
        linkInfo.setMovementMethod(LinkMovementMethod.getInstance());


        textTHide=view.findViewById(R.id.textTHide)

          // Update Interval
        spinnerHideTimer =view.findViewById(R.id.spinnerTHide)
        val tmpAdapter= ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item, StrHideTimer);    spinnerHideTimer.adapter = tmpAdapter
        var tTmp=preferences.getInt("hideTimer",T_INTERVAL_DEFAULT);    var (_, ind)=closest2Val(arrHideTimer, tTmp);     spinnerHideTimer.setSelection(ind)
        spinnerHideTimer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!boSpinnerHideTimerInitialized) {   boSpinnerHideTimerInitialized = true; return; }
                val hideTimer=arrHideTimer[id.toInt()];
                preferences.edit().putInt("hideTimer", hideTimer).apply()

            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }

//        Log.i("myTag","0")
//        Handler().postDelayed({ Log.i("myTag","1") }, 1000)
        butOn=view.findViewById(R.id.butOn)
        butOn.setOnClickListener(View.OnClickListener cb@{
              // bailouts
            if(repositoryTab.listSite.size==0) { Toast.makeText(this.context, "List is empty", Toast.LENGTH_SHORT).show(); return@cb}
            var boAny=0; repositoryTab.listSite.forEach foreach@{ if(it.boEnable) {boAny=1; return@foreach;}};
            if(boAny==0) { Toast.makeText(this.context, "No enabled entries", Toast.LENGTH_SHORT).show(); return@cb}

            val tUnix = System.currentTimeMillis() / 1000L
            val hideTimer= preferences.getInt("hideTimer", HIDE_TIMER_DEFAULT)
            val tHide=tUnix.toInt()+hideTimer
            preferences.edit().putInt("tHide", tHide).apply()
            setUI_THide()
            val tInterval= preferences.getInt("tInterval", T_INTERVAL_DEFAULT)

            boFirstMessage =true
            val (boFineOK, boBGOK)=getPermissions(requireContext())
            //var (boFineOK, boBGOK)=startLocationUpdatesW(requireContext())
            if(!boFineOK || !boBGOK) {
                val dialogPerm=(context as MainActivity).dialogPerm; dialogPerm.setInfoTexts(boFineOK, boBGOK);
                dialogPerm.show(childFragmentManager, "blabla")
                return@cb
            }

            startLocationUpdates()
            val (t, u)=TimeUnit.getSuitableTimeUnit(tInterval) ///1000
            Toast.makeText(this.context, "The Location will be updated every $t $u", Toast.LENGTH_SHORT).show();

            cbButtOnFin()
        })
        butOff=view.findViewById(R.id.butOff)
        butOff.setOnClickListener(View.OnClickListener cb@{
            preferences.edit().putInt("tHide", 0).apply()
            setUI_THide()
            //stopTic();   setupButtsBasedOnTDiff(0);

            GlobalScope.launch {
                var strMess=""
                repositoryTab.updateAllEnabledISeq().writeData()
                preferences.edit().putInt("tHide", 0).apply()
                stopLocationUpdates()
                val nMess= MyInternet.sendMessAll(queue, repositoryTab.listSite, false, false)
                strMess="$nMess messages sent."
                if(strMess.length>0) Log.i("myTag", strMess)
            }
        })
        butCheck=view.findViewById(R.id.butCheck)
        butCheck.setOnClickListener(View.OnClickListener {
            setUI_THide()
            GlobalScope.launch {
                var strMess=""
                repositoryTab.updateAllEnabledISeq().writeData()
                val nMess= MyInternet.sendMessAll(queue, repositoryTab.listSite, true)
                strMess="$nMess messages sent."
                if(strMess.length>0) Log.i("myTag", strMess)
            }
        })

        butToManage=view.findViewById(R.id.butToManage)
        butToManage.setOnClickListener(View.OnClickListener {
            (context as MainActivity).fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, (context as MainActivity).fragmentManage, null).addToBackStack(null).commit()
        })
        butToSetting=view.findViewById(R.id.butToSetting)
        butToSetting.setOnClickListener(View.OnClickListener {
            (context as MainActivity).fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, (context as MainActivity).fragmentSetting, null).addToBackStack(null).commit()
        })

        val recyclerViewFront:RecyclerView=view.findViewById(R.id.recyclerViewFront)
        recyclerViewFront.layoutManager= LinearLayoutManager(context)
        val adapterTmp=AdapterFront(context!!)
        recyclerViewFront.adapter=adapterTmp


        var (boFineOK,boBGOK)= getPermissions(context!!)
        if(!boFineOK || !boBGOK) {
            val dialogPerm=(context as MainActivity).dialogPerm; //dialogPerm.setInfoTexts(boFineOK, boBGOK);
            dialogPerm.show(childFragmentManager, "blabla");
        }


        val obsSemMess= Observer<Int> { semNewMess->
            val l=MyInternet.StrMessRet.size.toString()
            Log.i("myTag", "Messages observed, StrMessRet.size: $l" )
            adapterTmp.notifyDataSetChanged()
        }
        MyInternet.semNewMess.observe(viewLifecycleOwner, obsSemMess)



          // setupButtsBasedOnTDiff and start ticker
        //var tDiff=preferences.getInt("tHide",0)-getTUnix();   setupButtsBasedOnTDiff(tDiff.toInt());   if(tDiff>0) startNewTic(tDiff.toInt());


        return view
    }
    fun cbButtOnFin(){  // Finalizing "on"-click, is also called in permission callback
        val tUnix=getTUnix();
        val tDiff= preferences.getInt("hideTimer", HIDE_TIMER_DEFAULT)
        val tHide=tUnix+tDiff;
        preferences.edit().putInt("tHide", tHide.toInt()).apply();
        setUI_THide()
        //setupButtsBasedOnTDiff(tDiff);  startNewTic(tDiff);
    }
    fun setupButtsBasedOnTDiff(tDiffT:Int){
        var boShow=tDiffT>0;
        val tDiff=if(tDiffT<=0) 0 else tDiffT
        var (tDiffU,u)=TimeUnit.getSuitableTimeUnit(tDiff);
        var strDiff=tDiffU.toString()+u
        var str="";     var tmpS:Int;  var tmpH:Int
        val intMax=2147483647
        if(boShow) {
            val hideTimer= preferences.getInt("hideTimer", HIDE_TIMER_DEFAULT)
            str=if(hideTimer>=intMax)"∞" else strDiff;
            tmpS=Color.GREEN; tmpH=android.R.drawable.btn_default;
        } else {  str="On";   tmpS=android.R.drawable.btn_default;   tmpH=Color.RED;   }
        butOn.text=str
        butOn.setBackgroundColor(tmpS);    butOff.setBackgroundColor(tmpH);
        butToSetting.setEnabled(!boShow);  butToManage.setEnabled(!boShow);
    }


      // To enable "ticking" search for all lines with "setupButtsBasedOnTDiff" and uncomment them
    var timer: Timer?=null
    fun startNewTic(tDiff:Int){
        val tSleep=TimeUnit.getNextTick(tDiff);
        stopTic()
        timer= Timer()
        timer?.schedule(timerTask {fTicEnd()},tSleep.toLong()*1000);
        Log.i("myTag","tDiff: "+tDiff+", tSleep: "+tSleep );
    }
    var fTicEnd:()->Unit={
        var tDiff=calcTDiff();
        stopTic();
        if(tDiff<0) { var boShow=0; } else { startNewTic(tDiff); }
        mHandler.obtainMessage(1).sendToTarget()
        //(context as MainActivity).fragmentFront.setupButtsBasedOnTDiff(tDiff.toInt());
    }
    fun stopTic(){ timer?.cancel();}

    var mHandler: Handler =  Handler(Looper.getMainLooper()) {
        var tDiff=calcTDiff();
        (context as MainActivity).fragmentFront.setupButtsBasedOnTDiff(tDiff.toInt());
        true //True if no further handling is desired
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


