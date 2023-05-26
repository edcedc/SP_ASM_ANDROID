package com.csl.ams.SystemFragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.csl.ams.CustomMediaPlayer;
import com.csl.ams.Entity.Asset;
import com.csl.ams.Entity.Pallet.Record;
import com.csl.ams.Entity.SpinnerOnClickEvent;
import com.csl.ams.Event.Barcode.ScanBarcodeResult;
import com.csl.ams.Event.Barcode.ScanBarcodeTimeout;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.DialogEvent;
import com.csl.ams.Event.RFID.ScanRFIDResult;
import com.csl.ams.InternalStorage;
import com.csl.ams.MainActivity;
import com.csl.ams.NewHandHeld.MyUtil;
import com.csl.ams.R;
import com.csl.ams.Response.LevelData;
import com.csl.ams.Response.ListingResponse;
import com.csl.ams.SharedPrefsUtils;
import com.csl.ams.SystemFragment.BaseFragment;
import com.csl.ams.WebService.Callback.GetListingCallback;
import com.csl.ams.WebService.RetrofitClient;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import rfid.uhfapi_y2007.core.Util;
import rfid.uhfapi_y2007.entities.AntennaPowerStatus;
import rfid.uhfapi_y2007.entities.Flag;
import rfid.uhfapi_y2007.entities.MemoryBank;
import rfid.uhfapi_y2007.entities.Session;
import rfid.uhfapi_y2007.entities.SessionInfo;
import rfid.uhfapi_y2007.entities.TagParameter;
import rfid.uhfapi_y2007.entities.WriteTagParameter;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgQValueConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgReaderCapabilityQuery;
import rfid.uhfapi_y2007.protocol.vrp.MsgRfidStatusQuery;
import rfid.uhfapi_y2007.protocol.vrp.MsgSessionConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgTagWrite;

public class BindEpcFragment extends BaseFragment {
    //    var playerN:CustomMediaPlayer? = null
    CustomMediaPlayer playerN;

    int minPower = 0;
    int maxPower = 32;
    List<String> powerList = new ArrayList<>();
    List<String> typeList = new ArrayList<>();

    AppCompatSpinner powerSpinner, typeSpinner;

    TextView scannedBarcode, scannedEpc, generatedEpc;

    Button scanBarcodeBtn, scanEpcBtn, confirm;
    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
    String userId = Hawk.get(InternalStorage.Login.USER_ID, "");
    String serverId = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");//response.body().getData().get(i).getUserid());

    ArrayList<View> locationViewList = new ArrayList<>();
    ArrayList<View> categoryViewList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerN = MainActivity.sharedObjects.playerN;

        Log.i("getDateString","getDateString" + getDateString());
        /*
        Realm.getDefaultInstance().beginTransaction();

        for(int i = 0 ; i < 505; i ++) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            Record record = new Record();
            record.setBarcode(i + "");

            Random rand = new Random();

            int upperbound = 99999999;
            int int_1 = rand.nextInt(upperbound);
            int int_2 = rand.nextInt(upperbound);
            int int_3 = rand.nextInt(upperbound);

            record.setEpc(int_1 + "" + int_2 + "" + int_3);
            if(i % 2 == 0) {
                record.setType("Box");
            } else {
                record.setType("Pallet");
            }
            record.setDatetime(dateFormat.format(new Date()));
            record.setUserid(userId);
            record.setCompanyid(companyId);
            Realm.getDefaultInstance().insertOrUpdate(record);
        }
        Realm.getDefaultInstance().commitTransaction();
*/

        try {
            MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt((int) (Hawk.get("bindepc", 1) ) + "")});
            MyUtil.reader.Send(pMsg);
            SessionInfo si = new SessionInfo();

            //si.Session = Session.values()[0];
            //si.Flag = Flag.values()[2];
            si.Session = Session.values()[0];
            si.Flag = Flag.values()[2];

            MsgSessionConfig msgS = new MsgSessionConfig(si);
            MyUtil.reader.Send(msgS);

            byte q = 4;
            MsgQValueConfig msg = new MsgQValueConfig(q);
            MyUtil.reader.Send(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
//>>>>>>> 21bd5ea9a24b8f231d31d52d9b3f02c78b8000e6
        view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.bind_epc_fragment, null);
        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).replaceFragment(new SavedListFragment());
            }
        });
        powerSpinner = (AppCompatSpinner) view.findViewById(R.id.power_spinner);
        typeSpinner = (AppCompatSpinner) view.findViewById(R.id.type_spinner);

        scanEpcBtn = (Button) view.findViewById(R.id.scan_rfid);
        scanBarcodeBtn = (Button) view.findViewById(R.id.scan_barcode);

        scannedBarcode = view.findViewById(R.id.scanned_barcode);
        scannedEpc = view.findViewById(R.id.scanned_epc);
        confirm = view.findViewById(R.id.confirm);

        generatedEpc = view.findViewById(R.id.generated_epc);

        generatedEpc.setText(SharedPrefsUtils.getStringPreference(MainActivity.mContext, "generatedEPC"));

        if(SharedPrefsUtils.getStringPreference(MainActivity.mContext, "prefix") == null) {
            SharedPrefsUtils.setStringPreference(MainActivity.mContext, "prefix", "00");
        }

        if(generatedEpc.getText().toString().length() == 0) {
            generatedEpc.setText(SharedPrefsUtils.getStringPreference(MainActivity.mContext, "prefix") + getDateString() + "0000000");
        }

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateBarcodeEpc()) {

                    if(Realm.getDefaultInstance().where(Record.class).equalTo("barcode",scannedBarcode.getText().toString()).equalTo("userid", Hawk.get(InternalStorage.Login.USER_ID, "")).equalTo("companyid", companyId).findAll().size() == 0) {
                        generatedEpc.setText(SharedPrefsUtils.getStringPreference(MainActivity.mContext, "prefix") + getDateString() + generatedEpc.getText().toString().substring(17));

                        writeTag(generatedEpc.getText().toString());

                    } else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.app_name))
                                .setMessage(getString(R.string.record_exists))
                                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        generatedEpc.setText(SharedPrefsUtils.getStringPreference(MainActivity.mContext, "prefix") + getDateString() + generatedEpc.getText().toString().substring(17));

                                        writeTag(generatedEpc.getText().toString());

                                    }
                                })
                                .setPositiveButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();

                    }

                } else {
                    if(scannedBarcode.getText().toString().length() == 0 && scannedEpc.getText().toString().length() == 0) {
                        EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name),
                                MainActivity.mContext.getString(R.string.empty_epc) + "\n" + MainActivity.mContext.getString(R.string.empty_barcode)
                        ));
                    } else if(scannedEpc.getText().toString().length() == 0 ) {
                        EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name),
                                MainActivity.mContext.getString(R.string.empty_epc)
                        ));
                    } else if(scannedBarcode.getText().toString().length() == 0 ) {
                        EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name),
                                MainActivity.mContext.getString(R.string.empty_barcode)
                        ));
                    }

                }
            }
        });

        for (int i = 0; i <= 32; i++) {
            powerList.add(i + "");
        }

        typeList.add("Pallet");
        typeList.add("Box");

        ArrayAdapter<String> powerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, powerList);
        powerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        powerSpinner.setAdapter(powerAdapter);

        powerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("yoyo", "yoyo " + powerSpinner.getSelectedItemPosition());
                MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt(/*"30"*/ (int)(powerSpinner.getSelectedItemPosition()) + "")});
                Hawk.put("bindepc", (int)(powerSpinner.getSelectedItemPosition()));

                if (MyUtil.reader != null && !MyUtil.reader.Send(pMsg)) {
                    Log.i("error", "error");
                } else {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        } );

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        scanEpcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scanEpcBtn.getText().toString().equals(getString(R.string.scan_rfid))) {
                    scannedEpc.setText("");
                    scanEpcBtn.setText(getString(R.string.stop));
                    ((MainActivity) MainActivity.mContext).scanEpc();
                } else {
                    scanEpcBtn.setText(getString(R.string.scan_rfid));
                    ((MainActivity) MainActivity.mContext).stop();
                }
            }
        });

        scanBarcodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scanBarcodeBtn.getText().toString().equals(getString(R.string.scan))) {
                    scannedBarcode.setText("");
                    scanBarcodeBtn.setText(getString(R.string.stop));
                    ((MainActivity)getActivity()).scanBarcode();
                } else {
                    scanBarcodeBtn.setText(getString(R.string.scan));
                    ((MainActivity)getActivity()).stopScanBarcode();
                }
            }
        });

        /*
        MsgReaderCapabilityQuery msg = new MsgReaderCapabilityQuery();
        if(MyUtil.reader != null && MyUtil.reader.Send(msg)) {
            int antCount = msg.getReceivedMessage().getAntennaCount();
            int minPower = msg.getReceivedMessage().getMinPowerValue();
            int maxPower = msg.getReceivedMessage().getMaxPowerValue();

            if (antCount > 0) {
                String[] powers = new String[maxPower - minPower + 1];
                for (int j = minPower; j <= maxPower; j++) {
                    powers[j - minPower] = j + "";
                }

                MsgRfidStatusQuery msgState = new MsgRfidStatusQuery();
                if (MyUtil.reader.Send(msgState))
                {
                    AntennaPowerStatus[] aps = msgState.getReceivedMessage().getAntennas();
                    for (AntennaPowerStatus a : aps)
                    {
                        if(a.AntennaNO == 1) {
                            Log.i("power","power " + a.PowerValue);//(int)(a.PowerValue / 32f * 100));
                            powerSpinner.setSelection( a.PowerValue);
                            break;
                        }
                    }
                }
            }
        }*/
        powerSpinner.setSelection( Hawk.get("bindepc", 1));


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(((MainActivity)getActivity()).isURLReachable() ){
                    RetrofitClient.getSPGetWebService().listingLevel(companyId).enqueue(new GetListingCallback());
                } else {
                    ArrayList<Asset> myAsset =  Hawk.get(InternalStorage.OFFLINE_CACHE.RETURN, new ArrayList<Asset>());
                    EventBus.getDefault().post(new CallbackResponseEvent(myAsset));

                    ListingResponse listingResponse = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findFirst();

                    Log.i("listing case", "listing case 3 " + listingResponse.getCatSize() + " " + listingResponse.getLocSize() + " " + myAsset.size());

                    ListingResponse newListingResponse = new ListingResponse();
                    newListingResponse.setCatSize(listingResponse.getCatSize());
                    newListingResponse.setLocSize(listingResponse.getLocSize());

                    EventBus.getDefault().post(new CallbackResponseEvent(newListingResponse));
                }
            }
        }, 300);
    }

    public boolean validateBarcodeEpc() {
        if(scannedBarcode.getText().toString().length() > 0 && scannedEpc.getText().toString().length() > 0) {
            return true;
        }
        return false;
    }

    private void writeTag(String data) {
        if(MyUtil.selectParam == null){
            Toast.makeText(getActivity(),"请选择操作标签",Toast.LENGTH_SHORT).show();
            return;
        }
        if(/*MyUtil.checkTagPwd(etWriteTagPwd)*/true){
            int c = 1;//(ckbTagWriteEpc.isChecked() ? 1 : 0) + (ckbTagWriteUser.isChecked() ? 1 : 0) + (ckbTagWriteReserved.isChecked() ? 1 : 0);
            if(c==0)
            {
                Toast.makeText(getActivity(),"请勾选写入区域！",Toast.LENGTH_SHORT).show();
                return;
            }
            //  if (ckbTagWriteEpc.isChecked())
            //    {
            //  if (!MyUtil.checkEtInfo(etTagWriteEpc)){
            //      Toast.makeText(getActivity(),"写入数据格式错误！\r\n写入数据不能为空，必须为16进制字符，长度为4的整数倍",Toast.LENGTH_LONG).show();
            //      return;
            //  }
            String ptr = "2";//etTagWriteEpcPtr.getText().toString();

            if(ptr.equals("")){
                Toast.makeText(getActivity(),"起始地址不能为空",Toast.LENGTH_LONG).show();
                return;
            }
            try {
                int p = Integer.valueOf(ptr);
                if(p < 2){
                    Toast.makeText(getActivity(),"EPC起始地址不能小于2",Toast.LENGTH_LONG).show();
                    return;
                }
            }
            catch (NumberFormatException e){
                Toast.makeText(getActivity(),"起始地址应为10进制数据",Toast.LENGTH_LONG).show();
                return;
            }
            //   }
            /*
            if (ckbTagWriteUser.isChecked())
            {
                if (!MyUtil.checkEtInfo(etTagWriteUser)){
                    Toast.makeText(getActivity(),"写入数据格式错误！\r\n写入数据不能为空，必须为16进制字符，长度为4的整数倍",Toast.LENGTH_LONG).show();
                    return;
                }
                String ptr = etTagWriteUserPtr.getText().toString();

                if(ptr.equals("")){
                    Toast.makeText(getActivity(),"起始地址不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    Integer.valueOf(ptr);
                }
                catch (NumberFormatException e){
                    Toast.makeText(getActivity(),"起始地址应为10进制数据",Toast.LENGTH_LONG).show();
                    return;
                }
            }
            if (ckbTagWriteReserved.isChecked())
            {
                if (etTagWriteKpwd.getText().toString().equals("") && etTagWriteApwd.getText().toString().equals(""))
                {
                    Toast.makeText(getActivity(),"写入数据格式错误！\r\n写入数据不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                if (etTagWriteKpwd.getText().toString().length() > 0)
                {
                    if (!MyUtil.checkTagPwd(etTagWriteKpwd)){
                        Toast.makeText(getActivity(),"写入数据格式错误！\r\n写入数据不能为空，必须为16进制字符，长度为4的整数倍",Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (etTagWriteApwd.getText().toString().length() > 0)
                {
                    if (!MyUtil.checkTagPwd(etTagWriteApwd)){
                        Toast.makeText(getActivity(),"写入数据格式错误！\r\n写入数据不能为空，必须为16进制字符，长度为4的整数倍",Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            */
            WriteTagParameter wp = new WriteTagParameter();
            wp.SelectTagParam = MyUtil.selectParam;
            wp.AccessPassword = Util.ConvertHexStringToByteArray("00000000"/*etWriteTagPwd.getText().toString()*/);
            wp.WriteDataAry = new TagParameter[c];
            int p = 0;
            //if (ckbTagWriteEpc.isChecked())
            //{
            TagParameter tp = new TagParameter();
            tp.MemoryBank = MemoryBank.EPCMemory;
            tp.Ptr = Integer.parseInt("2");
            tp.TagData = Util.ConvertHexStringToByteArray(data);
            wp.WriteDataAry[p] = tp;
            p++;
            //}
            /*
            if (ckbTagWriteUser.isChecked())
            {
                TagParameter tp = new TagParameter();
                tp.MemoryBank = MemoryBank.UserMemory;
                tp.Ptr = Integer.parseInt(etTagWriteUserPtr.getText().toString());
                tp.TagData = Util.ConvertHexStringToByteArray(etTagWriteUser.getText().toString());
                wp.WriteDataAry[p] = tp;
                p++;
            }
            if (ckbTagWriteReserved.isChecked())
            {
                TagParameter tp = new TagParameter();
                tp.MemoryBank = MemoryBank.ReservedMemory;
                tp.TagData = new byte[8];
                if (etTagWriteKpwd.getText().toString().equals(""))
                {
                    tp.Ptr = 0x02;
                    tp.TagData = new byte[4];
                }
                int i =0;
                if (!etTagWriteKpwd.getText().toString().equals(""))//销毁密码
                {
                    byte[] kp = Util.ConvertHexStringToByteArray(etTagWriteKpwd.getText().toString());
                    for(byte b : kp)
                    {
                        tp.TagData[i]= b;
                        i++;
                    }
                }
                if (!etTagWriteApwd.getText().toString().equals(""))//访问密码
                {
                    byte[] ap = Util.ConvertHexStringToByteArray(etTagWriteApwd.getText().toString());
                    for(byte b : ap)
                    {
                        tp.TagData[i]= b;
                        i++;
                    }
                }
                wp.WriteDataAry[p] = tp;
            }

             */
            MsgTagWrite msg = new MsgTagWrite(wp);
            if (MyUtil.reader.Send(msg)) {
                String lastLocation = "";
                String lastLocationRono = "";
                String lastCategory = "";
                String lastCategoryRono = "";

                for(int i = locationViewList.size() - 1; i >= 0; i--) {
                    Spinner current = (Spinner) locationViewList.get(i);

                    Log.i("lastLoc", "lastLoc " + current.getSelectedItem());

                    if(current.getSelectedItem() != null && current.getSelectedItem().toString().length() > 0 && !current.getSelectedItem().toString().equals("-")) {
                        try {
                            //if(locationViewList.size() - 1 == 0) {
                            //    lastLocation = current.getSelectedItem().toString();
                            //} else {
                            lastLocation = current.getSelectedItem().toString()+ "->" + lastLocation ;
                            //}
                            int position = current.getSelectedItemPosition();

                            List<LevelData> lz = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(LevelData.class).equalTo("level", 1 + i).equalTo("type", 1).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
                            Log.i("lz","lz0  " + current.getSelectedItem().toString());

                            if(lastLocationRono.isEmpty()) {
                                lastLocationRono = lz.get(position - 1).getRono();
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        //break;
                    }
                }

                if(lastLocation.length() > 0) {
                    lastLocation = lastLocation.substring(0, lastLocation.length() - 2);
                }

                Log.i("categoryViewList", "categoryViewList " + categoryViewList.size());

                for(int i = categoryViewList.size() - 1; i >= 0; i--) {
                    Spinner current = (Spinner) categoryViewList.get(i);

                    if(current.getSelectedItem() != null && current.getSelectedItem().toString().length() > 0 && !current.getSelectedItem().toString().equals("-")) {
                        try {

                            //if(categoryViewList.size() - 1 == 0) {
                            //    lastCategory = current.getSelectedItem().toString();
                            //} else {
                            //    lastCategory += "->" + current.getSelectedItem().toString();
                            //}
                            lastCategory = current.getSelectedItem().toString()+ "->" + lastCategory ;

                            //lastCategory = current.getSelectedItem().toString();
                            int position = current.getSelectedItemPosition();

                            List<LevelData> lz = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(LevelData.class).equalTo("level", 1 + i).equalTo("type", 0).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
                            Log.i("lz","lz0  " + current.getSelectedItem().toString());

                            if(lastCategoryRono.isEmpty()) {
                                lastCategoryRono = lz.get(position - 1).getRono();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                       // break;
                    }
                }

                if(lastCategory.length() > 0) {
                    lastCategory = lastCategory.substring(0, lastCategory.length() - 2);
                }


                Log.i("save", "save " + locationViewList.size() + " " + categoryViewList.size());

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

                Record record = new Record();
                record.setBarcode(scannedBarcode.getText().toString());
                record.setEpc(generatedEpc.getText().toString());

                record.setCategoryName(lastCategory);
                record.setCategoryRono(lastCategoryRono);

                record.setLocationName(lastLocation);
                record.setLocationRono(lastLocationRono);

                record.setDatetime(dateFormat.format(new Date()));
                record.setUserid(userId);
                record.setCompanyid(companyId);

                Realm.getDefaultInstance().beginTransaction();
                Realm.getDefaultInstance().insertOrUpdate(record);
                Realm.getDefaultInstance().commitTransaction();

                Log.i("data", "data " + typeSpinner.getSelectedItem());

                //Toast.makeText(getActivity(), "写标签成功", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.write_epc_succes)));
                String temp = (Integer.parseInt(generatedEpc.getText().toString().substring(17)) + 1) + "";

                int zeroMissed = 7 - temp.length();

                if(zeroMissed < 0) {
                    temp = "0000000";
                }
                String result = "";

                for(int i = 0; i < zeroMissed; i++) {
                    result += "0";
                }
                result += temp;

                generatedEpc.setText(SharedPrefsUtils.getStringPreference(MainActivity.mContext, "prefix") + getDateString() + result);

                SharedPrefsUtils.setStringPreference(MainActivity.mContext, "generatedEPC", SharedPrefsUtils.getStringPreference(MainActivity.mContext, "prefix") + getDateString() +result);

                resetBarcodeEpc();

                ((MainActivity)MainActivity.mContext).updateDrawerStatus();

            } else {
                EventBus.getDefault().post(new DialogEvent(MainActivity.mContext.getString(R.string.app_name), MainActivity.mContext.getString(R.string.fail)));

                //Toast.makeText(getActivity(), "写标签失败:" + msg.getErrorInfo().getErrMsg(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void resetBarcodeEpc() {
        scannedEpc.setText("");
        scannedBarcode.setText("");
    }

    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view.findViewById(R.id.back).setVisibility(View.INVISIBLE);
        ((TextView) view.findViewById(R.id.toolbar_title)).setText(MainActivity.mContext.getString(R.string.menu_binding));

        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScanRFIDResult result) {
        if (playerN != null)
            playerN.start();

        ((MainActivity) MainActivity.mContext).stop();
        scannedEpc.setText(result.getEpc());
        scanEpcBtn.setText(getString(R.string.scan_rfid));

        MyUtil.selectParam = new TagParameter();
        MyUtil.selectParam.TagData = Util.ConvertHexStringToByteArray(result.getEpc());
        MyUtil.selectParam.MemoryBank = MemoryBank.EPCMemory;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScanBarcodeResult result) {
        if (playerN != null)
            playerN.start();

        scannedBarcode.setText(result.getBarcode());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ScanBarcodeTimeout timeout) {
        scanBarcodeBtn.setText(getString(R.string.scan));
    }

    public String getDateString() {
        //13
        //23021
        //41417
        //22896

        //total 17
        // 24 - 17 = 7
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
        return dateFormat.format(new Date());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        if (event.getResponse() instanceof ListingResponse) {
            if(((MainActivity)getActivity()).isURLReachable() ) {
                Realm.getDefaultInstance().beginTransaction();
                Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findAll().deleteAllFromRealm();

                ListingResponse listingResponse = ((ListingResponse) event.getResponse());
                listingResponse.setPk(companyId + serverId + "SP_LISTING_LEVEL");
                Realm.getDefaultInstance().insertOrUpdate(listingResponse);
                Realm.getDefaultInstance().commitTransaction();
            }
            ListingResponse l = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findFirst();
            Log.i("Retrofit", "Retrofit ListingResponse " + l);

            int categorySize = ((ListingResponse) event.getResponse()).getCatSize();
            int locationSize = ((ListingResponse) event.getResponse()).getLocSize();
            int count = Realm.getDefaultInstance().where(LevelData.class).equalTo("companyid", companyId).equalTo("userid", serverId).findAll().size();

            Log.i("RetrofitClient", "LevelData done " + count);

            ListingResponse l2 = Realm.getDefaultInstance().where(ListingResponse.class).equalTo("pk", companyId + serverId + "SP_LISTING_LEVEL").findFirst();
            categorySize = (l2).getCatSize();
            locationSize = (l2).getLocSize();

            ViewGroup spinnerRoot = (ViewGroup) view.findViewById(R.id.sp_category);
            spinnerRoot.removeAllViews();
            categoryViewList.clear();

            for(int i = 0; i < categorySize; i++) {
                Log.i("category", "category" + i);

                Spinner spinner = new Spinner(BindEpcFragment.this.getActivity());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = (int)convertDpToPixel(10);
                layoutParams.bottomMargin = (int)convertDpToPixel(10);

                spinner.setLayoutParams(layoutParams);

                spinnerRoot.addView(spinner);

                LinearLayout ll = new LinearLayout(getActivity());
                ll.setBackgroundColor(Color.parseColor("#C9CACA"));
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)convertDpToPixel(1));
                layoutParams.topMargin = (int)convertDpToPixel(2);

                if(i + 1 < locationSize)
                    layoutParams.bottomMargin = (int)convertDpToPixel(2);

                ll.setLayoutParams(layoutParams);

                spinnerRoot.addView(ll);

                categoryViewList.add(spinner);
            }

            List<LevelData> levelZero = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(LevelData.class).equalTo("level", 1).equalTo("type", 0).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
            setSpinner(levelZero, ((Spinner)categoryViewList.get(0)), 1, 0);

            ViewGroup spinnerRoot_loc = (ViewGroup) view.findViewById(R.id.sp_location);
            spinnerRoot_loc.removeAllViews();
            locationViewList.clear();

            for(int i = 0; i < locationSize; i++) {
                Log.i("location", "location" + i);

                Spinner spinner = new Spinner(BindEpcFragment.this.getActivity());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = (int)convertDpToPixel(10);
                layoutParams.bottomMargin = (int)convertDpToPixel(10);

                spinner.setLayoutParams(layoutParams);

                spinnerRoot_loc.addView(spinner);

                LinearLayout ll = new LinearLayout(getActivity());
                ll.setBackgroundColor(Color.parseColor("#C9CACA"));
                layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)convertDpToPixel(1));
                layoutParams.topMargin = (int)convertDpToPixel(2);

                if(i + 1 < locationSize)
                    layoutParams.bottomMargin = (int)convertDpToPixel(2);

                ll.setLayoutParams(layoutParams);

                spinnerRoot_loc.addView(ll);

                locationViewList.add(spinner);
            }

            List<LevelData> lz = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(LevelData.class).equalTo("level", 1).equalTo("type", 1).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
            setSpinner(lz, ((Spinner)locationViewList.get(0)), 1, 1);

        }
    }

    public void setSpinner(List<LevelData> categoryList, Spinner spinner, int layer, int type) {
        List<String> location = new ArrayList<>();
        location.add("-");

        for(int i = 0; i < categoryList.size(); i++) {
            if(categoryList.get(i).getName() != null)
                location.add(categoryList.get(i).getName());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, location);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(type == 0) {
                    for (int i = layer; i < categoryViewList.size(); i++) {
                        setSpinner(new ArrayList<>(), (Spinner)categoryViewList.get(i), i, type);
                    }
                } else if(type == 1) {
                    for (int i = layer; i < locationViewList.size(); i++) {
                        setSpinner(new ArrayList<>(), (Spinner)locationViewList.get(i), i, type);
                    }
                }

                if(position != 0) {
                    Log.i("type", "type " + layer + " " + type + " " + categoryList.size());
                    EventBus.getDefault().post(new SpinnerOnClickEvent(layer, type, categoryList.get(position - 1).getRono()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SpinnerOnClickEvent event) {

        List<LevelData> levelZero = Realm.getDefaultInstance().copyFromRealm(Realm.getDefaultInstance().where(LevelData.class).equalTo("fatherNo", event.getFatherno()).equalTo("level", event.getLayer() + 1).equalTo("type", event.getType()).equalTo("companyid", companyId).equalTo("userid",serverId).findAll());
        Log.i("SpinnerOnClickEvent", "SpinnerOnClickEvent " + levelZero.size() + " " + event.getLayer()  + " " + event.getType() + " " + event.getFatherno() +  " " + companyId);

        try {
            if(event.getType() == 1) {
                setSpinner(levelZero, ((Spinner) locationViewList.get(event.getLayer())), event.getLayer() + 1, 1);
            } else {
                setSpinner(levelZero, ((Spinner) categoryViewList.get(event.getLayer())), event.getLayer() + 1, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  float convertDpToPixel(float dp){
        return dp * ((float) getActivity().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
