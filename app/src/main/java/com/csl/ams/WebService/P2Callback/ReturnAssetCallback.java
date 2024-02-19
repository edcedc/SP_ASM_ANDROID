package com.csl.ams.WebService.P2Callback;

import android.util.Log;

import com.csl.ams.Entity.SPEntityP3.ReturnAsset;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;
import com.csl.ams.Event.UpdateFailEvent;
import com.csl.ams.MainActivity;
import com.csl.ams.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReturnAssetCallback implements Callback<List<ReturnAsset>> {
    private int type;

    public ReturnAssetCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public ReturnAssetCallback(int type) {
        this.type = type;
        EventBus.getDefault().post(new CallbackStartEvent());
        Log.i("CONTINUOUS", "CONTINUOUS case 10 a start");
    }

    @Override
    public void onResponse(Call<List<ReturnAsset>> call, Response<List<ReturnAsset>> response) {
        Log.i("onResponse", "onResponse" + response.toString());

        if(response.code() == 200) {
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(response.body());
            callbackResponseEvent.type = type;
            EventBus.getDefault().post(callbackResponseEvent);
        } else {
            EventBus.getDefault().post(new UpdateFailEvent());
            EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
        }
    }

    @Override
    public void onFailure(Call<List<ReturnAsset>> call, Throwable t) {
        Log.i("onFailure", "onFailure" + t.toString() + " " + call.request().url());
        EventBus.getDefault().post(new UpdateFailEvent());
        EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
    }
}
