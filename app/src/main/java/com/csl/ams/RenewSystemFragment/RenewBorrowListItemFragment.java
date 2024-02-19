package com.csl.ams.RenewSystemFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.csl.ams.CaptureActivityPortrait;
import com.csl.ams.CustomMediaPlayer;
import com.csl.ams.CustomTextWatcher;
import com.csl.ams.Entity.Asset;
import com.csl.ams.Entity.OfflineMode.BorrowAssets;
import com.csl.ams.Entity.SPEntityP2.AssetsDetail;
import com.csl.ams.Entity.SPEntityP3.BorrowAsset;
import com.csl.ams.Entity.SPEntityP3.BorrowDetailResponse;
import com.csl.ams.Entity.SPEntityP3.BorrowListItem;
import com.csl.ams.Event.BarcodeScanEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CustomTextWatcherEvent;
import com.csl.ams.Event.DialogEvent;
import com.csl.ams.Event.NetworkRecallEvent;
import com.csl.ams.Event.RFIDDataUpdateEvent;
import com.csl.ams.InternalStorage;
import com.csl.ams.InventoryRfidTask;
import com.csl.ams.MainActivity;
import com.csl.ams.NewHandHeld.MyUtil;
import com.csl.ams.OnBackPressEvent;
import com.csl.ams.R;
import com.csl.ams.RenewSystemFragment.Upload.UploadBorrowListItem;
import com.csl.ams.Response.APIResponse;
import com.csl.ams.SystemFragment.Adapter.AssetListAdapter;
import com.csl.ams.SystemFragment.AssetsDetailWithTabFragment;
import com.csl.ams.SystemFragment.BaseFragment;
import com.csl.ams.SystemFragment.LoginFragment;
import com.csl.ams.WebService.Callback.UpdateAssetEpcCallback;
import com.csl.ams.WebService.P2Callback.BorrowDetailCallback;
import com.csl.ams.WebService.RetrofitClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rfid.uhfapi_y2007.entities.Flag;
import rfid.uhfapi_y2007.entities.Session;
import rfid.uhfapi_y2007.entities.SessionInfo;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgQValueConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgSessionConfig;

public class RenewBorrowListItemFragment extends BaseFragment {
    CustomMediaPlayer playerO, playerN;
    private ListView listView, rfidListView;
    private View noResult;
    private Button start;
    public static boolean BORROW_LIST;
    public static String BORROW_NO = null;
    public static String BORROW_NAME = null;
    public static String DISPOSAL_NO = null;
    public static boolean DISPOSAL_LIST = false;

    private int tabPosition = 0;

    private HashMap abnormalHashMap = new HashMap<String, Boolean>();
    private HashMap epcFoundHashMap = new HashMap<String, Boolean>();
    private HashMap barcodeFoundHashMap = new HashMap<String, Boolean>();

    private ArrayList<BorrowAsset> borrowedAssets = new ArrayList<BorrowAsset>();
    private ArrayList<BorrowAsset> searchingAssets = new ArrayList<BorrowAsset>();
    private ArrayList<BorrowAsset> abnormalAssets = new ArrayList<BorrowAsset>();

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b) {
        super.onCreateView(li,vg, b);
        return view;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

//        MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt(/*"30"*/ (int) (Hawk.get("power_stocktake", 100f) / 100f * 32) + "")});
        MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{Hawk.get(InternalStorage.Rfid.POWER)});
        MyUtil.reader.Send(pMsg);
        SessionInfo si = new SessionInfo();

        //si.Session = Session.values()[0];
        //si.Flag = Flag.values()[2];
        si.Session = Session.S0;
        si.Flag = Flag.Flag_A_B;

        MsgSessionConfig msgS = new MsgSessionConfig(si);
        MyUtil.reader.Send(msgS);

        byte q = 4;
        MsgQValueConfig msg = new MsgQValueConfig(q);
        MyUtil.reader.Send(msg, 500);

        Realm.getDefaultInstance().beginTransaction();
        Realm.getDefaultInstance().where(BorrowAsset.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", userid)
                        .equalTo("tempAsset", true)
                        .equalTo("borrowed", false).findAll().deleteAllFromRealm();
        Realm.getDefaultInstance().commitTransaction();

        RealmResults<BorrowAssets> realmResults = Realm.getDefaultInstance().where(BorrowAssets.class).equalTo("pk", companyId + userid + BORROW_NO).findAll();

        RealmResults<BorrowAsset> rawData = Realm.getDefaultInstance().where(BorrowAsset.class)
                .equalTo("companyid", companyId)
                .equalTo("userid", userid)
                .equalTo("borrowno", BORROW_NO)
                .equalTo("borrowed", false)
                .findAll();

        Log.i("realmResults", "realmResults " + realmResults.size());

        if(realmResults.size() > 0) {
            String[] list = realmResults.get(0).getBorrowList().split(",");
            for(int i = 0; i < list.length; i++) {
                for(int y = 0; y < rawData.size(); y++) {
                    Log.i("data", "data " + rawData.get(y).getId() + " " + Integer.parseInt(list[i]) + " " + rawData.get(y).getEpc());
                    if(rawData.get(y).getId() == Integer.parseInt(list[i])) {
                        epcFoundHashMap.put(rawData.get(y).getEpc(),new Boolean(true));
                    }
                }
            }
        }

        playerO = MainActivity.sharedObjects.playerO;
        playerN = MainActivity.sharedObjects.playerN;

        view = LayoutInflater.from(getActivity()).inflate(R.layout.borrow_list_item_list_fragment, null);

        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(getActivity())
                        .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        .setPrompt("")
                        .setCameraId(0)
                        .setBeepEnabled(true)
                        .setBarcodeImageEnabled(true)
                        .setCaptureActivity(CaptureActivityPortrait.class)
                        .initiateScan();
            }
        });

        ((EditText) view.findViewById(R.id.edittext)).addTextChangedListener(new CustomTextWatcher());

        view.findViewById(R.id.add).setVisibility(View.GONE);

        listView = view.findViewById(R.id.listview);
        noResult = view.findViewById(R.id.no_result);
        start = view.findViewById(R.id.borrow_start);

        if (BORROW_LIST) {
            ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(0).setText(getString(R.string.borrowed));
        } else {
            ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(0).setText(getString(R.string.disposed));
        }

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getWaitingListId()) {
                    EventBus.getDefault().post(new DialogEvent(getString(R.string.app_name), getString(R.string.nothing_selected)));
                    return;
                }

                confirmBorrowDisposal();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start.getText().toString().equals(getString(R.string.start))) {
                    start.setText(getString(R.string.stop));
                    ((MainActivity) MainActivity.mContext).scanEpc();
                } else {
                    start.setText(getString(R.string.start));
                    ((MainActivity) MainActivity.mContext).stop();
                }
            }
        });

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).updateDrawerStatus();
                ((MainActivity) getActivity()).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });
        view.findViewById(R.id.borrow_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("onBackPress", "onBackPress 3");

                getActivity().onBackPressed();
            }
        });

        ((TabLayout) view.findViewById(R.id.tab_layout)).setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                listView.setSelection(0);
                listView.smoothScrollToPosition(0);
                ((MainActivity) getActivity()).hideKeyboard(getActivity());

                ((EditText) view.findViewById(R.id.edittext)).setText("");

                tabPosition = tab.getPosition();
                setupListView(getData());

                if (tabPosition != 1) {
                //    view.findViewById(R.id.borrow_start).setVisibility(View.GONE);
                  //  view.findViewById(R.id.confirm).setVisibility(View.GONE);
                   // view.findViewById(R.id.scan).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.borrow_start).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.confirm).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.scan).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(1).select();
        setupListView(getData());

        ArrayList<String> data = new ArrayList<>();
        data.add("demo/0000002714");

        BarcodeScanEvent rfidDataUpdateEvent = new BarcodeScanEvent("0000001274");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //EventBus.getDefault().post(rfidDataUpdateEvent);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 2000);

        EventBus.getDefault().post(rfidDataUpdateEvent);


        if(((MainActivity)MainActivity.mContext).isNetworkAvailable()) {
            RetrofitClient.getSPGetWebService().newBorrowListAssets(companyId, userid, BORROW_NO).enqueue(new BorrowDetailCallback(CONTINUOUS_BORROW_DETAIL));
        }
    }

    public static int CONTINUOUS_BORROW_DETAIL = 555;
    public void onResume() {
        super.onResume();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CustomTextWatcherEvent event) {
        setupListView(getData());
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(com.csl.ams.Event.BarcodeScanEvent event) {
        Log.i("barcode", "barcode " + barcodeFoundHashMap.get(event.getBarcode()) + " " + event.getBarcode());

        if(barcodeFoundHashMap.get(event.getBarcode()) != null) {
            barcodeFoundHashMap.put(event.getBarcode(), new Boolean(true));
        }

        if(epcFoundHashMap.get(event.getBarcode()) != null) {
            epcFoundHashMap.put(event.getBarcode(), new Boolean(true));
        }


        handleNoResult(getData());
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CallbackResponseEvent event) {
        Log.i("CallbackResponseEvent", "CallbackResponseEvent " + event);
        if (event.getResponse() instanceof APIResponse) {
            APIResponse apiResponse = (APIResponse) event.getResponse();

            String title = null;

            RetrofitClient.getSPGetWebService().newBorrowListAssets(companyId, userid, BORROW_NO).enqueue(new BorrowDetailCallback(CONTINUOUS_BORROW_DETAIL));

            if (apiResponse.getStatus() == 0) {
                Log.i("onBackPress", "onBackPress 4");


                EventBus.getDefault().post(new DialogEvent(BORROW_NO != null ? getString(R.string.borrow_list) : getString(R.string.disposal_list), BORROW_NO != null ? getString(R.string.borrow_success) : getString(R.string.disposal_success)));
                EventBus.getDefault().post(new NetworkRecallEvent());
                getActivity().onBackPressed();
            } else if (apiResponse.getStatus() == 1 && apiResponse.getBorrowCount() > 0) {
                EventBus.getDefault().post(new DialogEvent(BORROW_NO != null ? getString(R.string.borrow_list) : getString(R.string.disposal_list), getString(R.string.lend_some).replace("x", apiResponse.getBorrowCount() + "")));
                EventBus.getDefault().post(new NetworkRecallEvent());
                getActivity().onBackPressed();
            } else {
                EventBus.getDefault().post(new DialogEvent(BORROW_NO != null ? getString(R.string.borrow_list) : getString(R.string.disposal_list), getString(R.string.fail)));//getString(R.string.lend_some).replace("x", apiResponse.getBorrowCount() + "")));

            }


            Realm.getDefaultInstance().beginTransaction();
            Realm.getDefaultInstance().where(UploadBorrowListItem.class).equalTo("pk", companyId + userid + BORROW_NO).findAll().deleteAllFromRealm();
            Realm.getDefaultInstance().commitTransaction();

        } else
        if (event.type == CONTINUOUS_BORROW_DETAIL) {
            BorrowDetailResponse res = ((BorrowDetailResponse) event.getResponse());

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().where(BorrowAsset.class).equalTo("borrowno", res.getBorrowno()).equalTo("companyid", companyId).equalTo("userid", userid).findAll().deleteAllFromRealm();

            for (int i = 0; i < res.getData().size(); i++) {
                res.getData().get(i).setBorrowno(res.getBorrowno());
                res.getData().get(i).setCompanyid(companyId);
                res.getData().get(i).setUserid(userid);

                res.getData().get(i).setTimeString((long)i);
                Realm.getDefaultInstance().insertOrUpdate(res.getData().get(i));
            }
            Realm.getDefaultInstance().commitTransaction();

            ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(1).select();
            borrowedAssets.clear();
            searchingAssets.clear();
            abnormalAssets.clear();
            setupListView(getData());

        }
    }

    public void confirmBorrowDisposal() {

        if (((MainActivity) MainActivity.mContext).isURLReachable()) {

            if (LoginFragment.SP_API) {

                String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
                String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");
                String waitingListIdString = "";

                //for (int i = 0; i < getWaitingListId().size(); i++) {
                //    waitingListIdString += getWaitingListId().get(i) + (i == getWaitingListId().size() - 1 ? "" : ",");
                //}

                RealmResults<BorrowAsset> assets = Realm.getDefaultInstance().where(BorrowAsset.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", userid)
                        .equalTo("borrowno", BORROW_NO)
                        .equalTo("borrowed", false)
                        .sort("timeString")
                        .findAll();

                boolean found = false;

                for(int i = 0; i < assets.size(); i++) {
                    String epc = assets.get(i).getEpc();
                    String assetNo = assets.get(i).getAssetno();

                    if(epcFoundHashMap.get(epc) != null && epcFoundHashMap.get(epc).equals(new Boolean(true))) {
                        waitingListIdString += assets.get(i).getId() + ",";
                    } else if(barcodeFoundHashMap.get(assetNo) != null && barcodeFoundHashMap.get(assetNo).equals(new Boolean(true))) {
                        waitingListIdString += assets.get(i).getId() + ",";
                    }
                }

                if(waitingListIdString.length() > 0) {
                    waitingListIdString = waitingListIdString.substring(0, waitingListIdString.length() - 1);
                }

                Log.i("waitingList", "waitingList " + waitingListIdString + " " + BORROW_LIST);

                RetrofitClient.getSPGetWebService().borrowAssets(companyId, userid, waitingListIdString, BORROW_NO).enqueue(new UpdateAssetEpcCallback());
            }
        } else {

            String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID, "");
            String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, "");
            String waitingListIdString = "";

            //for (int i = 0; i < getWaitingListId().size(); i++) {
            //    waitingListIdString += getWaitingListId().get(i) + (i == getWaitingListId().size() - 1 ? "" : ",");
            //}

            RealmResults<BorrowAsset> assets = Realm.getDefaultInstance().where(BorrowAsset.class)
                    .equalTo("companyid", companyId)
                    .equalTo("userid", userid)
                    .equalTo("borrowno", BORROW_NO)
                    .equalTo("borrowed", false)
                    .sort("timeString")
                    .findAll();

            boolean found = false;

            for(int i = 0; i < assets.size(); i++) {
                String epc = assets.get(i).getEpc();
                String assetNo = assets.get(i).getAssetno();

                if(epcFoundHashMap.get(epc) != null && epcFoundHashMap.get(epc).equals(new Boolean(true))) {
                    waitingListIdString += assets.get(i).getId() + ",";
                    Log.i("waitingListIdString", "waitingListIdString " + assets.get(i).getAssetno() + " " + assets.get(i).getId());
                } else if(barcodeFoundHashMap.get(assetNo) != null && barcodeFoundHashMap.get(assetNo).equals(new Boolean(true))) {
                    waitingListIdString += assets.get(i).getId() + ",";
                    Log.i("waitingListIdString", "waitingListIdString " + assets.get(i).getAssetno() + " " + assets.get(i).getId());
                }
            }

            if(waitingListIdString.length() > 0) {
                waitingListIdString = waitingListIdString.substring(0, waitingListIdString.length() - 1);
            }

            Log.i("waitingListIdString", "waitingListIdString " + waitingListIdString);

            BorrowAssets uploadBorrowListItem = new BorrowAssets();
            uploadBorrowListItem.setBorrowno(BORROW_NO);
            uploadBorrowListItem.setName(BORROW_NAME);
            uploadBorrowListItem.setBorrowList(waitingListIdString);
            uploadBorrowListItem.setCompanyid(companyId);
            uploadBorrowListItem.setUserid(userid);
            uploadBorrowListItem.setPk(companyId + userid + BORROW_NO);

            Realm.getDefaultInstance().beginTransaction();

            Realm.getDefaultInstance().insertOrUpdate(uploadBorrowListItem);
            Realm.getDefaultInstance().commitTransaction();

            ((MainActivity) getActivity()).updateDrawerStatus();
            EventBus.getDefault().post(new DialogEvent(getActivity().getString(R.string.app_name), getActivity().getString(R.string.upload_tips)));
            EventBus.getDefault().post(new OnBackPressEvent());
            //savelocal();
        }
    }

    public boolean getWaitingListId() {
        RealmResults<BorrowAsset> assets = Realm.getDefaultInstance().where(BorrowAsset.class)
                .equalTo("companyid", companyId)
                .equalTo("userid", userid)
                .equalTo("borrowno", BORROW_NO)
                .equalTo("borrowed", false)
                .sort("timeString")
                .findAll();

        boolean found = false;

        for(int i = 0; i < assets.size(); i++) {
            String epc = assets.get(i).getEpc();
            String assetNo = assets.get(i).getAssetno();

            if(epcFoundHashMap.get(epc) != null && epcFoundHashMap.get(epc).equals(new Boolean(true))) {
                found = true;
            } else if(barcodeFoundHashMap.get(assetNo) != null && barcodeFoundHashMap.get(assetNo).equals(new Boolean(true))) {
                found = true;
            }
        }

        Log.i("found", "found " + found);

        return found;
    }


    public void setupListView(ArrayList<BorrowAsset> assetResponse) {
        handleNoResult(assetResponse);
        if(adapter == null) {
            adapter = new BorrowListAdapter(assetResponse);
            listView.setAdapter(adapter);
        } else {
            adapter.setData(assetResponse);
            adapter.notifyDataSetChanged();
        }
        int count = Realm.getDefaultInstance().where(BorrowAsset.class)
                .equalTo("companyid", companyId)
                .equalTo("userid", userid)
                .equalTo("borrowno", BORROW_NO)
                .equalTo("borrowed", false)
                .findAll().size();

        if(count == 0 ||
        Realm.getDefaultInstance().where(BorrowAsset.class)
                .equalTo("companyid", companyId)
                .equalTo("userid", userid)
                .equalTo("borrowno", BORROW_NO)
                .equalTo("borrowed", false)
                .sort("timeString")
                .equalTo("type", 1)
                .findAll().size() == 0
        ) {
            ((TabLayout) view.findViewById(R.id.tab_layout)).setVisibility(View.GONE);
            ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(0).select();
            view.findViewById(R.id.borrow_start).setVisibility(View.GONE);
            view.findViewById(R.id.confirm).setVisibility(View.GONE);
            view.findViewById(R.id.scan).setVisibility(View.GONE);
        } else {
            ((TabLayout) view.findViewById(R.id.tab_layout)).setVisibility(View.VISIBLE);
            //((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(0).select();
        }
    }

    BorrowListAdapter adapter = null;

    public class BorrowListAdapter extends BaseAdapter {
        ArrayList<BorrowAsset> data = new ArrayList<>();

        public BorrowListAdapter(ArrayList<BorrowAsset> data) {
            this.data = data;
        }

        public void setData(ArrayList<BorrowAsset> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public BorrowAsset getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return data.hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null)
                view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.search_listview_cell, viewGroup, false);

            BorrowAsset borrowAsset = getItem(i);

            ((TextView)view.findViewById(R.id.search_cell_title)).setText(borrowAsset.getAssetno() + " | " + borrowAsset.getName());
            ((TextView)view.findViewById(R.id.search_cell_brand_value)).setText(borrowAsset.getBrand());
            ((TextView)view.findViewById(R.id.search_cell_model_value)).setText(borrowAsset.getModel());
            ((TextView)view.findViewById(R.id.search_cell_category_value)).setText(borrowAsset.getCategory());
            ((TextView)view.findViewById(R.id.search_cell_location_value)).setText(borrowAsset.getLocation());
            ((TextView)view.findViewById(R.id.search_cell_epc_value)).setText(borrowAsset.getEpc());
            (((TextView)view.findViewById(R.id.search_cell_return_date_value))).setVisibility(View.GONE);

            ((view.findViewById(R.id.search_cell_generic))).setVisibility(View.GONE);
            ((view.findViewById(R.id.search_cell_status_in_library))).setVisibility(View.GONE);
            ((view.findViewById(R.id.search_cell_status_in_borrowed))).setVisibility(View.GONE);
            ((view.findViewById(R.id.search_cell_status_disposed))).setVisibility(View.GONE);

           // ((TextView)view.findViewById(R.id.last_asset_no)).setText (getItem(i).getLastAssetNo());
            //((ViewGroup)((TextView)view.findViewById(R.id.last_asset_no)).getParent()).setVisibility(View.VISIBLE);

            Log.i("position", "position " + view);

            int position = ((TabLayout) RenewBorrowListItemFragment.this.view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

            (((view.findViewById(R.id.borrow_tick)))).setVisibility(View.GONE);
            (((view.findViewById(R.id.missing_take)))).setVisibility(View.GONE);
            ((view.findViewById(R.id.abnormal_take))).setVisibility(View.GONE);

            ((ViewGroup)view.findViewById(R.id.search_cell_title).getParent()).setVisibility(View.GONE);
            ((ViewGroup)view.findViewById(R.id.search_cell_brand_value).getParent()).setVisibility(View.GONE);
            ((ViewGroup)view.findViewById(R.id.search_cell_model_value).getParent()).setVisibility(View.GONE);
            ((ViewGroup)view.findViewById(R.id.search_cell_category_value).getParent()).setVisibility(View.GONE);
            ((ViewGroup)view.findViewById(R.id.search_cell_location_value).getParent()).setVisibility(View.GONE);
            ((ViewGroup)(view.findViewById(R.id.search_cell_return_date_value)).getParent()).setVisibility(View.GONE);

            if(position == 0 || position == 1 || (borrowAsset.getAssetno() != null && borrowAsset.getAssetno().length() > 0) ) {
                ((ViewGroup)view.findViewById(R.id.search_cell_title).getParent()).setVisibility(View.VISIBLE);
                ((ViewGroup)view.findViewById(R.id.search_cell_brand_value).getParent()).setVisibility(View.VISIBLE);
                ((ViewGroup)view.findViewById(R.id.search_cell_model_value).getParent()).setVisibility(View.VISIBLE);
                ((ViewGroup)view.findViewById(R.id.search_cell_category_value).getParent()).setVisibility(View.VISIBLE);
                ((ViewGroup)view.findViewById(R.id.search_cell_location_value).getParent()).setVisibility(View.VISIBLE);
            }

            if (position == 0) {
                (( (view.findViewById(R.id.borrow_tick)))).setVisibility(View.VISIBLE);
            } else if (position == 1) {
                 if(epcFoundHashMap.get(borrowAsset.getEpc()) != null || barcodeFoundHashMap.get(borrowAsset.getAssetno()) != null) {
                     if (epcFoundHashMap.get(borrowAsset.getEpc()) != null && epcFoundHashMap.get(borrowAsset.getEpc()).equals( new Boolean(true))) {
                         Log.i("epcFoundHashMap", "epcFoundHashMap yes " + borrowAsset.getAssetno() );
                         ( (view.findViewById(R.id.borrow_tick))).setVisibility(View.VISIBLE);
                     } else if (barcodeFoundHashMap.get(borrowAsset.getAssetno()) != null && barcodeFoundHashMap.get(borrowAsset.getAssetno()).equals( new Boolean(true))) {
                         Log.i("epcFoundHashMap", "epcFoundHashMap yes"  + borrowAsset.getAssetno() );
                         ( (view.findViewById(R.id.borrow_tick))).setVisibility(View.VISIBLE);
                     } else {
                         Log.i("epcFoundHashMap", "epcFoundHashMap no"  + borrowAsset.getAssetno() );
                         ((view.findViewById(R.id.missing_take))).setVisibility(View.VISIBLE);
                       }
                 } else {
                     ((view.findViewById(R.id.missing_take))).setVisibility(View.VISIBLE);
                 }
            } else if (position == 2) {
                ( (view.findViewById(R.id.abnormal_take))).setVisibility(View.VISIBLE);
            }
            //}

            List<AssetsDetail> assets =  Realm.getDefaultInstance().where(AssetsDetail.class)
                    .contains("userid", Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID, ""))
                    .contains("companyid",Hawk.get(InternalStorage.Setting.COMPANY_ID, ""))
                    .equalTo("assetNo" ,borrowAsset.getAssetno())
                    .findAll()
                    .sort("ordering") ;

            if(assets.size() > 0) {

                if (assets.get(0).getStatusid().length() > 0) {
                    String text = getStatusString(assets.get(0).getStatusid());

                    ((ViewGroup)(view.findViewById(R.id.search_cell_generic)).getParent()).setVisibility(View.VISIBLE);

                    ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.search_cell_generic)).setText(text);

                    if(assets.get(0).getStatusid().equals("2"))
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(MainActivity.mContext.getResources().getColor(R.color.colorPrimary));
                    else if(assets.get(0).getStatusid().equals("3") || assets.get(0).getStatusid().equals("4"))
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(MainActivity.mContext.getResources().getColor(android.R.color.holo_orange_light));
                    else if(assets.get(0).getStatusid().equals("5") || assets.get(0).getStatusid().equals("6") || assets.get(0).getStatusid().equals("7") || assets.get(0).getStatusid().equals("8") || assets.get(0).getStatusid().equals("9999"))
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setBackgroundColor(MainActivity.mContext.getResources().getColor(android.R.color.holo_red_dark));
                    else {
                        ((TextView) view.findViewById(R.id.search_cell_generic)).setVisibility(View.GONE);
                    }

                    Log.i("case1", "case1 " + text + " " + assets.get(0).getStatusid());
                }
            }

            final int tempI = i;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getItem(tempI).getAssetno() != null && getItem(tempI).getAssetno().length() > 0){
                        AssetsDetailWithTabFragment.ASSET_NO = (getItem(tempI).getAssetno());

                        ((MainActivity) getActivity()).replaceFragment(new AssetsDetailWithTabFragment());
                    }

                }
            });
            return view;
        }
    }

    public void handleNoResult(List<BorrowAsset> data) {
        if (data == null || data.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        } else {
            noResult.setVisibility(View.GONE);
        }


        if ( data.size() > 0) {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(BORROW_NAME + " (" + data.size() + ")");
        } else {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(BORROW_NAME);
        }

        ((TextView)view.findViewById(R.id.toolbar_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    String companyId = Hawk.get(InternalStorage.Setting.COMPANY_ID,"");
    String userid = Hawk.get(InternalStorage.OFFLINE_CACHE.USER_ID,"");

    public ArrayList getData() {
        int position = ((TabLayout) view.findViewById(R.id.tab_layout)).getSelectedTabPosition();

        ArrayList<BorrowAsset> data = new ArrayList<>();

        if(position == 0) {
            if(borrowedAssets.isEmpty()) {
                borrowedAssets.addAll(Realm.getDefaultInstance().copyFromRealm(
                        Realm.getDefaultInstance().where(BorrowAsset.class)
                                .equalTo("companyid", companyId)
                                .equalTo("userid", userid)
                                .equalTo("borrowno", BORROW_NO)
                                .equalTo("borrowed", true)
                                .sort("timeString")
                                .findAll()
                        )
                );
            } else {
                borrowedAssets.clear();
                borrowedAssets.addAll(Realm.getDefaultInstance().copyFromRealm(
                        Realm.getDefaultInstance().where(BorrowAsset.class)
                                .equalTo("companyid", companyId)
                                .equalTo("userid", userid)
                                .equalTo("borrowno", BORROW_NO)
                                .equalTo("borrowed", true)
                                .beginGroup()
                                .contains("assetno", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("name", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("category", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("location", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("epc", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("LastAssetNo", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .endGroup()
                                .sort("timeString")
                                .findAll()
                        )
                );
            }
            return borrowedAssets;
        }

        if(position == 1) {
            if(searchingAssets.isEmpty()) {
                searchingAssets.addAll(Realm.getDefaultInstance().copyFromRealm(
                        Realm.getDefaultInstance().where(BorrowAsset.class)
                                .equalTo("companyid", companyId)
                                .equalTo("userid", userid)
                                .equalTo("borrowno", BORROW_NO)
                                .equalTo("borrowed", false)
                                .sort("timeString")
                                .equalTo("type", 1)
                                .findAll()
                        )
                );

                for (int i = 0; i < searchingAssets.size(); i++) {
                    //if(searchingAssets.get(i).getEpc() != null && !searchingAssets.get(i).getEpc().isEmpty()) {
                    //} else{

                    if(searchingAssets.get(i).getType() == 1 && epcFoundHashMap.get(searchingAssets.get(i).getEpc()) == null) {
                        epcFoundHashMap.put(searchingAssets.get(i).getEpc(), new Boolean(false));
                        Log.i("epcFoundHashMap", "epcFoundHashMap " + searchingAssets.get(i).getEpc());
                    }


                    if(searchingAssets.get(i).getType() == 1 && barcodeFoundHashMap.get(searchingAssets.get(i).getAssetno()) == null) {
                        barcodeFoundHashMap.put(searchingAssets.get(i).getAssetno(), new Boolean(false));
                        Log.i("barcodeFoundHashMap", "barcodeFoundHashMap " + searchingAssets.get(i).getAssetno());
                    }
                    //}
                }
            } else {
                searchingAssets.clear();
                searchingAssets.addAll(Realm.getDefaultInstance().copyFromRealm(
                        Realm.getDefaultInstance().where(BorrowAsset.class)
                                .equalTo("companyid", companyId)
                                .equalTo("userid", userid)
                                .equalTo("borrowno", BORROW_NO)
                                .equalTo("borrowed", false)
                                .beginGroup()
                                .contains("assetno", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("name", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("category", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("location", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("epc", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .or()
                                .contains("LastAssetNo", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                                .endGroup()
                                .sort("timeString")
                                .equalTo("type", 1)
                                .findAll()
                        )
                );

            }

            if(searchingAssets.size() == 0 && ((EditText) view.findViewById(R.id.edittext)).getText().toString().length() == 0 ){
                ((TabLayout) view.findViewById(R.id.tab_layout)).getTabAt(0).select();
                ((TabLayout) view.findViewById(R.id.tab_layout)).setVisibility(View.GONE);
            }

            return searchingAssets;
        }

        Log.i("data", "getData " + data.size());
        abnormalAssets.clear();
        abnormalAssets.addAll(Realm.getDefaultInstance().copyFromRealm(
                Realm.getDefaultInstance().where(BorrowAsset.class)
                        .equalTo("companyid", companyId)
                        .equalTo("userid", userid)
                        .equalTo("tempAsset", true)
                        .equalTo("borrowed", false)
                        .beginGroup()
                        .contains("assetno", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                        .or()
                        .contains("name", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                        .or()
                        .contains("category", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                        .or()
                        .contains("location", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                        .or()
                        .contains("epc", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                        .or()
                        .contains("LastAssetNo", ((EditText) view.findViewById(R.id.edittext)).getText().toString())
                        .endGroup()
                        .sort("timeString")
                        .findAll()
                )
        );
        return abnormalAssets;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RFIDDataUpdateEvent event) {
        Log.i("event", "event " + event.getData());
        if (playerN != null)
            playerN.start();
        //if (playerO != null)
        //    playerO.start();

        boolean changed = false;
        for(int i = 0; i < event.getData().size(); i++) {
            if(event.getData().get(i).trim().length() > 0) {

                if (epcFoundHashMap.get(event.getData().get(i)) != null) {
                    Log.i("epcFoundHashMap", "put epcFoundHashMap " + event.getData().get(i) + " " + new Boolean(true));

                    epcFoundHashMap.put(event.getData().get(i), new Boolean(true));
                    changed = true;
                } else {

                    if(abnormalHashMap.get(event.getData().get(i)) == null) {
                        abnormalHashMap.put(event.getData().get(i), new Boolean(true));

                        RealmResults<AssetsDetail> assetsDetails = Realm.getDefaultInstance().where(AssetsDetail.class)
                                .equalTo("companyid", companyId)
                                .contains("userid", userid)
                                .equalTo("epc", event.getData().get(i))
                                .findAll();

                        Log.i("abnormal", "abnormal " + assetsDetails.size());

                        Realm.getDefaultInstance().beginTransaction();

                        if (assetsDetails.size() > 0) {
                            AssetsDetail ad = assetsDetails.get(0);

                            BorrowAsset borrowAsset = new BorrowAsset();
                            borrowAsset.setName(ad.getName());
                            borrowAsset.setAssetno(ad.getAssetNo());
                            borrowAsset.setBrand(ad.getBrand());
                            borrowAsset.setCategory(ad.getCategory());
                            borrowAsset.setLocation(ad.getLocation());
                            borrowAsset.setEpc(ad.getEpc());
                            borrowAsset.setEpc(event.getData().get(i));
                            borrowAsset.setTempAsset(true);
                            borrowAsset.setCompanyid(companyId);
                            borrowAsset.setUserid(userid);
                            borrowAsset.setTimeString(new Date().getTime());

                            Realm.getDefaultInstance().insertOrUpdate(borrowAsset);
                            abnormalAssets.add(borrowAsset);
                        } else {
                            BorrowAsset borrowAsset = new BorrowAsset();
                            borrowAsset.setEpc(event.getData().get(i));
                            borrowAsset.setTempAsset(true);

                            borrowAsset.setCompanyid(companyId);
                            borrowAsset.setUserid(userid);
                            borrowAsset.setTimeString(new Date().getTime());

                            Realm.getDefaultInstance().insertOrUpdate(borrowAsset);
                            abnormalAssets.add(borrowAsset);
                        }

                        Realm.getDefaultInstance().commitTransaction();

                        changed = true;
                    }
                }
            }
        }

        if(changed) {
            handleNoResult(getData());
            adapter.notifyDataSetChanged();
        }
    }

    public String getStatusString(String statusid) {
        String language = Hawk.get(InternalStorage.Setting.LANGUAGE, "");

        Log.i("getStatusString" , "getStatusString " + language + " " +statusid);
        if(language == null) {
            language = "en";
        }

        if(statusid == null) {

        } else {
            if(statusid.equals("1")) {
                if(language.equals("en")) {
                    return "In Storage";
                } else if(language.equals("cn")) {
                    return "入库中";
                } else if(language.equals("zh")) {
                    return "入庫中";
                }
            } else if(statusid.equals("2")) {
                if(language.equals("en")) {
                    return "In Library";
                } else if(language.equals("cn")) {
                    return "在库";
                } else if(language.equals("zh")) {
                    return "在庫";
                }
            }else if(statusid.equals("3")) {
                if(language.equals("en")) {
                    return "On Loan";
                } else if(language.equals("cn")) {
                    return "已借出";
                } else if(language.equals("zh")) {
                    return "已借出";
                }
            }else if(statusid.equals("4")) {
                if(language.equals("en")) {
                    return "To Be Lent";
                } else if(language.equals("cn")) {
                    return "待借出";
                } else if(language.equals("zh")) {
                    return "待借出";
                }
            }else if(statusid.equals("5")) {
                if(language.equals("en")) {
                    return "Delete";
                } else if(language.equals("cn")) {
                    return "删除";
                } else if(language.equals("zh")) {
                    return "刪除";
                }
            }else if(statusid.equals("6")) {
                if(language.equals("en")) {
                    return "Lose";
                } else if(language.equals("cn")) {
                    return "丢失";
                } else if(language.equals("zh")) {
                    return "丟失";
                }
            }else if(statusid.equals("7")) {
                if(language.equals("en")) {
                    return "Cancellation";
                } else if(language.equals("cn")) {
                    return "注销";
                } else if(language.equals("zh")) {
                    return "註銷";
                }
            }else if(statusid.equals("8")) {
                if(language.equals("en")) {
                    return "Destruction In Progress";
                } else if(language.equals("cn")) {
                    return "销毁中";
                } else if(language.equals("zh")) {
                    return "銷毀中";
                }
            }else if(statusid.equals("9")) {
                if(language.equals("en")) {
                    return "Abnormal";
                } else if(language.equals("cn")) {
                    return "异常";
                } else if(language.equals("zh")) {
                    return "異常";
                }
            }else if(statusid.equals("10")) {
                if(language.equals("en")) {
                    return "Not in storage";
                } else if(language.equals("cn")) {
                    return "不在库";
                } else if(language.equals("zh")) {
                    return "不在庫";
                }
            }
        }
        return "";
    }

}