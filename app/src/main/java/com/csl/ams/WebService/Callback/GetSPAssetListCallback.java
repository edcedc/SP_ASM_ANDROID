package com.csl.ams.WebService.Callback;

import android.util.Log;

import com.csl.ams.Entity.SPEntityP2.AssetsDetail;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetSPAssetListCallback implements Callback<List<AssetsDetail>> {
    public GetSPAssetListCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    private String type;
    public GetSPAssetListCallback(String type) {
        this.type = type;
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<List<AssetsDetail>> call, Response<List<AssetsDetail>> response) {
        Log.i("onResponse","onResponse" + response.toString() + " " + response.body());
        if(response.code() == 200) {
            EventBus.getDefault().post(new CallbackResponseEvent(type, response.body()));
        } else {
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<List<AssetsDetail>> call, Throwable t) {
        Log.i("onFailure", "onFailure" + t.toString() + " " + call.request().url());

        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
