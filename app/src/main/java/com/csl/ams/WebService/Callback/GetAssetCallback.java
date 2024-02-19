package com.csl.ams.WebService.Callback;


import com.csl.ams.Entity.Asset;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetAssetCallback implements Callback<Asset> {
    public GetAssetCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<Asset> call, Response<Asset> response) {
        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));

    }

    @Override
    public void onFailure(Call<Asset> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
