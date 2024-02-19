package com.csl.ams.SystemFragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.csl.ams.CustomTextWatcher;
import com.csl.ams.Entity.StockTakeList;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CustomTextWatcherEvent;
import com.csl.ams.Event.InsertEvent;
import com.csl.ams.Event.LoginDownloadProgressEvent;
import com.csl.ams.Event.NetworkInventoryDoneEvent;
import com.csl.ams.Event.ProgressEvent;
import com.csl.ams.Event.UpdateFailEvent;
import com.csl.ams.InternalStorage;
import com.csl.ams.MainActivity;
import com.csl.ams.R;
import com.csl.ams.SystemFragment.Adapter.AssetListAdapter;
import com.csl.ams.SystemFragment.Adapter.StockTakeListAdapter;
import com.csl.ams.WebService.Callback.GetStockTakeListCallback;
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

public class OldStockTakeFragment extends BaseFragment {
    public static int STOCK_TAKE_API = 11;
    public static String STOCK_TAKE_NO_EDITED = null;

    List<StockTakeList> data;
    StockTakeListAdapter stockTakeListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    ListView listView;
    View noResult;



}
