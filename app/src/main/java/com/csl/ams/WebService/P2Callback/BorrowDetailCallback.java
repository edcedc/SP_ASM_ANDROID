package com.csl.ams.WebService.P2Callback;

import android.util.Log;

import com.csl.ams.Entity.SPEntityP3.BorrowDetailResponse;
import com.csl.ams.Entity.SPEntityP3.DisposalDetailResponse;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;
import com.csl.ams.MainActivity;
import com.csl.ams.R;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BorrowDetailCallback implements Callback<BorrowDetailResponse> {
    private int type;

    public BorrowDetailCallback(int type) {
        this.type = type;
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public BorrowDetailCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<BorrowDetailResponse> call, Response<BorrowDetailResponse> response) {
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
    public void onFailure(Call<BorrowDetailResponse> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
    }
}
