package com.csl.ams.WebService.Callback;

import com.csl.ams.Entity.SPEntityP2.AssetsDetail;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetAssetDetailCallback implements Callback<AssetsDetail> {
    public GetAssetDetailCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<AssetsDetail> call, Response<AssetsDetail> response) {
        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));

    }

    @Override
    public void onFailure(Call<AssetsDetail> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
