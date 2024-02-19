package com.csl.ams.WebService.P2Callback;

import android.util.Log;

import com.csl.ams.Entity.SPEntityP2.BriefBorrowedList;
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

public class DisposalListCallback implements Callback<List<DisposalListItem>> {
    private int type;

    public DisposalListCallback(int type) {
        this.type = type;
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public DisposalListCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<List<DisposalListItem>> call, Response<List<DisposalListItem>> response) {
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
    public void onFailure(Call<List<DisposalListItem>> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
    }
}
