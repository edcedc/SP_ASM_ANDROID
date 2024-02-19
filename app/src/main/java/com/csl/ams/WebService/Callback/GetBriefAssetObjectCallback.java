package com.csl.ams.WebService.Callback;

import android.util.Log;

import com.csl.ams.Entity.SPEntityP2.BriefAsset;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetBriefAssetObjectCallback implements Callback<BriefAsset> {
    public GetBriefAssetObjectCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    boolean ignore;
    public GetBriefAssetObjectCallback(boolean ignore) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.ignore = ignore;
    }


    @Override
    public void onResponse(Call<BriefAsset> call, Response<BriefAsset> response) {
        Log.i("onResponse", "onResponse GetBriefAssetObjectCallback " + response.raw().toString().toString());
        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else {
            if(!ignore)
                EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<BriefAsset> call, Throwable t) {
        Log.i("onFailure", "onFailure GetBriefAssetObjectCallback");

        if(!ignore)
            EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
