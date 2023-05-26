package com.csl.ams.WebService.Callback;

import android.util.Log;

import com.csl.ams.Entity.Asset;
import com.csl.ams.Entity.Tray;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;
import com.csl.ams.SystemFragment.DownloadFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrayListCallBack implements Callback<List<Tray>> {
    private int type;

    public TrayListCallBack(int type) {
        this.type = type;
    }

    @Override
    public void onResponse(Call<List<Tray>> call, Response<List<Tray>> response) {
        if(response.code() == 200) {
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(response.body());
            callbackResponseEvent.type = DownloadFragment.TRAY_LIST;
            EventBus.getDefault().post(callbackResponseEvent);
        } else {
            //EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<List<Tray>> call, Throwable t) {
        //EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
