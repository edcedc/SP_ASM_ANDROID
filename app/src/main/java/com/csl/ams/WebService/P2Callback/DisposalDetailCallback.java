package com.csl.ams.WebService.P2Callback;

import android.util.Log;

import com.csl.ams.Entity.SPEntityP3.DisposalDetailResponse;
import com.csl.ams.Entity.SPEntityP3.DisposalListItem;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;
import com.csl.ams.MainActivity;
import com.csl.ams.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DisposalDetailCallback implements Callback<DisposalDetailResponse> {
    private int type;

    public DisposalDetailCallback(int type) {
        this.type = type;
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public DisposalDetailCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<DisposalDetailResponse> call, Response<DisposalDetailResponse> response) {
        Log.i("onResponse", "onResponseonResponseonResponse " + response.body() + " " + call.request().url());

        if(response.code() == 200) {
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(response.body());
            callbackResponseEvent.type = type;
            EventBus.getDefault().post(callbackResponseEvent);
        } else {
            EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
        }
    }

    @Override
    public void onFailure(Call<DisposalDetailResponse> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
    }
}
