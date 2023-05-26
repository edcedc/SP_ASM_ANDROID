package com.csl.ams.SystemFragment

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.csl.ams.*
import com.csl.ams.Entity.Pallet.Record
import com.csl.ams.Entity.Pallet.RecordClone
import com.csl.ams.Event.Barcode.ScanBarcodeResult
import com.csl.ams.Event.Barcode.ScanBarcodeTimeout
import com.csl.ams.Event.CustomTextWatcherEvent
import com.csl.ams.Event.DialogEvent
import com.csl.ams.Event.RFID.ScanRFIDResult
import com.csl.ams.Event.SystemUI.NetworkInventoryDoneEvent
import com.csl.ams.WebService.RetrofitClient
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SavedListFragment : BaseFragment() {
    var listView: ListView? = null
    var listAdapter: ListAdapter? = null
    val companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "")
    var playerN: CustomMediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerN = MainActivity.sharedObjects.playerN

        view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.saved_list_fragment, null)
        view.findViewById<View>(R.id.menu).setOnClickListener {
            (MainActivity.mContext as MainActivity).mDrawerLayout.openDrawer(Gravity.RIGHT)
        }

        (view.findViewById<View>(R.id.filter_editext) as EditText).addTextChangedListener(
            CustomTextWatcher()
        )

        view.findViewById<TextView>(R.id.toolbar_title).setText(MainActivity.mContext.getText(R.string.records))

        view.findViewById<ImageView>(R.id.back).setOnClickListener{
            (MainActivity.mContext as MainActivity).onBackPressed()
        }

        view.findViewById<Button>(R.id.scan_barcode).setOnClickListener {
            (MainActivity.mContext as MainActivity).scanBarcode()

            if (view.findViewById<Button>(R.id.scan_barcode).getText().toString().equals(getString(R.string.scan))) {
                view.findViewById<Button>(R.id.scan_barcode).setText("");
                view.findViewById<Button>(R.id.scan_barcode).setText(getString(R.string.stop));
                (MainActivity.mContext as MainActivity).scanBarcode()
            } else {
                view.findViewById<Button>(R.id.scan_barcode).setText(getString(R.string.scan));
                (MainActivity.mContext as MainActivity).stopScanBarcode()
            }

        }
        view.findViewById<Button>(R.id.scan_rfid).setOnClickListener {

            if (view.findViewById<Button>(R.id.scan_rfid).text.toString() == getString(R.string.scan_rfid)) {
           //     scannedEpc.setText("")
                view.findViewById<Button>(R.id.scan_rfid).text = getString(R.string.stop)
                (MainActivity.mContext as MainActivity).scanEpc()
            } else {
                view.findViewById<Button>(R.id.scan_rfid).text = getString(R.string.scan_rfid)
                (MainActivity.mContext as MainActivity).stop()
            }
        }
        listView = view.findViewById(R.id.listview)
        listAdapter = ListAdapter(Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll())

        listView!!.adapter = listAdapter
        Log.i("size", "size " + Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll().size)

        if(Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll().size == 0) {
            //view.findViewById<Button>(R.id.upload).visibility = View.GONE
            view.findViewById<TextView>(R.id.no_data).visibility = View.VISIBLE
        } else {
            //view.findViewById<Button>(R.id.upload).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.no_data).visibility = View.GONE
        }

        view.findViewById<Button>(R.id.upload).setOnClickListener {

            if(!BaseUtil.isNetworkAvailable(MainActivity.mContext)) {
                EventBus.getDefault().post(DialogEvent(getString(R.string.app_name), "Internet not available"));
                return@setOnClickListener
            }

            var count = 0;
            var checkedCount = Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll().size

            AsyncTask.execute {
                Realm.getDefaultInstance().refresh()
                var strJson = "[";

                for (record in Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll() ) {
                    strJson += Gson().toJson(RecordClone(record), RecordClone::class.java)
                    strJson += ",";
                    count++;
                    Log.i("data", "count " + count);

                    // EventBus.getDefault().post(LoginDownloadProgressEvent(count * 1.0f/checkedCount * 1.0f))

                    if ((count > 0 && count % 500 == 0) || count == checkedCount) {
                        if(strJson.length > 1) {
                            strJson = strJson.substring(0, strJson.length - 1);
                        }
                        strJson += "]"

                        FuelManager.instance.timeoutInMillisecond = 3000
                        FuelManager.instance.timeoutReadInMillisecond = 3000

                        //http://192.168.1.183:3000
                        Log.i("data", "data " + strJson);

                        (RetrofitClient.api + "/MobileWebService.asmx/UploadRegistrationData").httpPost(listOf("strJson" to strJson, "companyID" to companyId)).requestProgress { readBytes, totalBytes ->
                            val progress = readBytes.toFloat() / totalBytes.toFloat()

                            //if (progress > 0)
                            //    EventBus.getDefault().post(LoginDownloadProgressEvent(progress))

                            if (progress >= 1) {
                                Realm.getDefaultInstance().refresh()
                                Log.i("count", "count " + count + " " + checkedCount)
                            }
                        }.response { request, response, result ->
                            Log.i("response", "response "  + response.statusCode + " " + result.toString());

                            if(result.toString().contains("Failure") || result.toString().contains("Failed")) {
                                //failure = true;
                                EventBus.getDefault().post(NetworkInventoryDoneEvent(""))

                            } else {

                                if(count == checkedCount) {
                                    Realm.getDefaultInstance().beginTransaction()
                                    Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll().deleteAllFromRealm()
                                    Realm.getDefaultInstance().commitTransaction()
                                    Realm.getDefaultInstance().refresh()


                                    try {
                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }

                                    EventBus.getDefault().post(NetworkInventoryDoneEvent("UploadStockTake"))
                                }
                            }
                            //}
                        }
                    }
                }

            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return view
    }

    inner class ListAdapter(recordList: List<Record>) : BaseAdapter() {
        var recordList: List<Record> = ArrayList()
        override fun getCount(): Int {
            return recordList.size
        }

        override fun getItem(position: Int): Record {
            return recordList[position]
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).hashCode().toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val view: View

            view = convertView ?: LayoutInflater.from(MainActivity.mContext).inflate(R.layout.row_list_view_saved_list, null)
            (view.findViewById<View>(R.id.barcode) as TextView).text = getItem(position).barcode
            (view.findViewById<View>(R.id.type) as TextView).text = getItem(position).categoryName //+ "(" + getItem(position).categoryRono + ")"
            (view.findViewById<View>(R.id.location) as TextView).text = getItem(position).locationName// + "(" + getItem(position).locationRono + ")"
            (view.findViewById<View>(R.id.type_rono) as TextView).text = getItem(position).categoryRono
            (view.findViewById<View>(R.id.location_rono) as TextView).text = getItem(position).locationRono

            (view.findViewById<View>(R.id.epc) as TextView).text = getItem(position).epc
            (view.findViewById<View>(R.id.datetime) as TextView).text = getItem(position).datetime


            view.setOnLongClickListener {

                AlertDialog.Builder(activity!!)
                    .setTitle(activity!!.getString(R.string.app_name))
                    .setMessage(getString(R.string.confirm_deleting) + " " +  " " + getItem(position).barcode + "( " + getItem(position).epc + ")  ?")
                    .setPositiveButton(
                        android.R.string.ok
                    ) { dialog, which ->

                        Realm.getDefaultInstance().beginTransaction()
                        Realm.getDefaultInstance().where(Record::class.java)
                            .equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, ""))
                            .equalTo("barcode", getItem(position).barcode)
                            .equalTo("epc", getItem(position).epc).equalTo("companyid", companyId)
                            .findAll().deleteAllFromRealm()
                        Realm.getDefaultInstance().commitTransaction()

                        listAdapter = ListAdapter(Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll())
                        listView!!.adapter = listAdapter

                        if(listView!!.adapter.count > 0) {
                            this@SavedListFragment.view.findViewById<TextView>(R.id.toolbar_title).setText(MainActivity.mContext.getText(R.string.records).toString() + " (" + listView!!.adapter.count + ")")
                        } else {
                            this@SavedListFragment.view.findViewById<TextView>(R.id.toolbar_title).setText(MainActivity.mContext.getText(R.string.records).toString() )
                        }

                        if(Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll().size == 0) {
                            // view.findViewById<Button>(R.id.upload).visibility = View.GONE
                            this@SavedListFragment.view.findViewById<TextView>(R.id.no_data).visibility = View.VISIBLE
                        } else {
                            // view.findViewById<Button>(R.id.upload).visibility = View.VISIBLE
                            this@SavedListFragment.view.findViewById<TextView>(R.id.no_data).visibility = View.GONE
                        }
                        (MainActivity.mContext as MainActivity).updateDrawerStatus()

                    }.setNegativeButton(
                        android.R.string.cancel
                    ) { dialog, which -> }
                    .show()
                return@setOnLongClickListener true
            }
            return view
        }

        init {
            this.recordList = recordList
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(result: ScanBarcodeTimeout) {
        view.findViewById<Button>(R.id.scan_barcode).text = getString(R.string.scan);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(result: ScanBarcodeResult) {
        if (playerN != null)
            playerN!!.start()

        view.findViewById<Button>(R.id.scan_barcode).text = getString(R.string.scan);
        (view.findViewById<View>(R.id.filter_editext) as EditText).setText(result.barcode)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: CustomTextWatcherEvent?) {
        if(event!!.title.length > 0) {
            listAdapter = ListAdapter(
                Realm.getDefaultInstance().where(Record::class.java)
                    .beginGroup()
                    .contains("barcode", event!!.title)
                    .or()
                    .contains("epc", event!!.title)
                    .or()
                    .contains("locationName", event!!.title)
                    .or()
                    .contains("datetime", event!!.title)
                    .endGroup()
                    .equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, ""))
                    .equalTo("companyid", companyId)
                    .findAll()
            )
        } else {
            listAdapter = ListAdapter(Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll())
        }
        listView!!.adapter = listAdapter

        if(listView!!.adapter.count > 0) {
            this@SavedListFragment.view.findViewById<TextView>(R.id.toolbar_title).setText(MainActivity.mContext.getText(R.string.records).toString() + " (" + listView!!.adapter.count + ")")
        } else {
            this@SavedListFragment.view.findViewById<TextView>(R.id.toolbar_title).setText(MainActivity.mContext.getText(R.string.records).toString() )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: NetworkInventoryDoneEvent?) {
        if(event!!.type == null || event!!.type.equals("")) {
            EventBus.getDefault().post(DialogEvent(getString(R.string.app_name), "Network Error"))

            listAdapter = ListAdapter(Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll())
            listView!!.adapter = listAdapter
        } else {
            EventBus.getDefault().post(DialogEvent(getString(R.string.app_name), "Upload Success"))

            listAdapter = ListAdapter(Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll())
            listView!!.adapter = listAdapter



            if(Realm.getDefaultInstance().where(Record::class.java).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll().size == 0) {
               // view.findViewById<Button>(R.id.upload).visibility = View.GONE
                view.findViewById<TextView>(R.id.no_data).visibility = View.VISIBLE
            } else {
               // view.findViewById<Button>(R.id.upload).visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.no_data).visibility = View.GONE
            }

        }

        if(listView!!.adapter.count > 0) {
            this@SavedListFragment.view.findViewById<TextView>(R.id.toolbar_title).setText(MainActivity.mContext.getText(R.string.records).toString() + " (" + listView!!.adapter.count + ")")
        } else {
            this@SavedListFragment.view.findViewById<TextView>(R.id.toolbar_title).setText(MainActivity.mContext.getText(R.string.records).toString() )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(result: ScanRFIDResult) {
        if (playerN != null) playerN!!.start()
        (MainActivity.mContext as MainActivity).stop()
        view.findViewById<Button>(R.id.scan_rfid).text = getString(R.string.scan_rfid)

        (view.findViewById<View>(R.id.filter_editext) as EditText).setText(result.epc)
    }
}