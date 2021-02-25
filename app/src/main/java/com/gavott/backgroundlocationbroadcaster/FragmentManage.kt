package com.gavott.backgroundlocationbroadcaster

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial


class AdapterManage(context: Context): RecyclerView.Adapter<AdapterManage.MyViewHolder>(){
    //private val sitesList= ArrayList<Site>()
    //private val sitesList= listOf(DataRow("abc"), DataRow("def"), DataRow("ghi"))
    //private val sitesList=(context as MainActivity).sitesList
    //private val sitesList=repositoryTab.listSite
    val myContext=context

    //    class MyViewHolder(val binding: ItemListMainBinding): RecyclerView.ViewHolder(binding.root){
//        fun bind(dataRow:DataRow){
//            binding.uri.text=dataRow.uri
//            //view.uri=dataRow.uri
//            //binding.uriTextView.text=site.uri
//            //binding.strUUIDTextView.text=site.strUUID
//            //binding.itemListMainLayout.butKey.setOnClickListener{ cbKeyButton(site) }
//        }
//    }
    class MyViewHolder(val view: View): RecyclerView.ViewHolder(view){
        val uri: TextView= view.findViewById(R.id.uri)
        val switchEnable: SwitchMaterial = view.findViewById(R.id.switchEnable)
        val butDelete: Button= view.findViewById(R.id.butDelete)
        init{
            switchEnable.setOnClickListener {
                var b= (it as SwitchMaterial).isChecked
                Log.i("myTag", "switchBoEnable $b $adapterPosition")
                repositoryTab.toggleRow(adapterPosition, b).writeData()
                (view.context as MainActivity).fragmentManage.recyclerViewManage.adapter?.notifyItemChanged(
                    adapterPosition
                )
            }
            butDelete.setOnClickListener {
                val uriTmp=repositoryTab.listSite[adapterPosition].uri
                AlertDialog.Builder(view.context)
                        .setTitle("Deleting entry:")
                        .setMessage(uriTmp)
                        .setPositiveButton(
                            android.R.string.yes,
                            DialogInterface.OnClickListener { dialog, which ->
                                Log.i("myTag", "Deleted $uriTmp")
                                repositoryTab.deleteRow(adapterPosition).writeData()
                                val adapterTmp =
                                    (view.context as MainActivity).fragmentManage.recyclerViewManage.adapter!!
                                adapterTmp.notifyItemRemoved(adapterPosition);
                                adapterTmp.notifyItemRangeChanged(
                                    adapterPosition,
                                    adapterTmp.getItemCount()
                                );
                            })
                        //.setNegativeButton(android.R.string.no, null)
                        //.setIcon(android.R.drawable.ic_dialog_alert)
                        .show()

            }
        }


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater=LayoutInflater.from(parent.context)
        val viewRow=layoutInflater.inflate(R.layout.item_list_manage, parent, false)

        val holder=MyViewHolder(viewRow)
        //holder.switchBoEnable.setOnCheckedChangeListener{ compoundButton: CompoundButton, boOn: Boolean -> cbEnableButton( boOn)}

        return holder
        //val binding:ItemListMainBinding= DataBindingUtil.inflate(layoutInflater, R.layout.item_list_main, parent, false)
        //return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return repositoryTab.listSite.size }
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

        holder.switchEnable.setChecked(boEnable)




    }
//    fun setList(sites:List<Site>){
//        repositoryTab.listSite.clear()
//        repositoryTab.listSite.addAll(sites)
//    }
}


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentManage.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentManage : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var butAdd: Button
    private lateinit var btnBack: Button

    lateinit var recyclerViewManage:RecyclerView

    //private var that=this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
          // Listening to Back pressed event
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    //Toast.makeText(context, "abc", Toast.LENGTH_LONG).show()
                    (context as MainActivity).fragmentManager.popBackStack()  // Actually gooing back
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View=inflater.inflate(R.layout.fragment_manage, container, false)
        val linkInfo:TextView=view.findViewById(R.id.linkInfo)
        linkInfo.text= HtmlCompat.fromHtml(
            "<a href=\"$uriInfo\">Info</a>",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        );
        linkInfo.setMovementMethod(LinkMovementMethod.getInstance());

//        butAdd=view.findViewById(R.id.buttonAdd)
//        butAdd.setOnClickListener(View.OnClickListener {
//            (context as MainActivity).fragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, (context as MainActivity).fragmentAdd, null)
//                .addToBackStack(null).commit()
//        })



        butAdd = view.findViewById(R.id.buttonAdd)
        butAdd.setOnClickListener(View.OnClickListener {
            val dialog = Dialog(it.context)
            dialog.setContentView(R.layout.dialog_add)

            val linkListOfValidSites: TextView = dialog.findViewById(R.id.linkListOfValidSites)
            linkListOfValidSites.text = HtmlCompat.fromHtml(
                "<a href=\"$uriListOfValidSites\">List of valid sites</a>",
                HtmlCompat.FROM_HTML_MODE_COMPACT
            );
            linkListOfValidSites.setMovementMethod(LinkMovementMethod.getInstance());

            val inpUri = dialog.findViewById<EditText>(R.id.inpUri)
            dialog.findViewById<Button>(R.id.butSave)
                .setOnClickListener(View.OnClickListener cb@{
                    Log.i("myTag", "Save clicked")
                    val (err, strUriShort, strUUID) = processUri(inpUri.text.toString())
                    if (err != null) {
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show(); return@cb;
                    }
                    strUriShort!!; strUUID!!
                    var iRow=repositoryTab.isUriInArray(strUriShort)
                    if(iRow!=null){repositoryTab.editRow(iRow, strUriShort,0,strUUID)  }
                    else {  repositoryTab.addRow(strUriShort, 0, strUUID).writeData()  }

                    recyclerViewManage.adapter?.notifyDataSetChanged()
                    dialog.dismiss()
                })
            dialog.show()
            val window = dialog.getWindow()
            window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        })


        recyclerViewManage=view.findViewById(R.id.recyclerViewManage)
        recyclerViewManage.layoutManager= LinearLayoutManager(context)
        recyclerViewManage.adapter=AdapterManage(context!!)

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment bbb.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentManage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}