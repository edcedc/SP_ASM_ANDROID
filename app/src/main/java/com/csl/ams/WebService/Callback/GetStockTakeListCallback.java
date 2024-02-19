package com.csl.ams.WebService.Callback;

import com.csl.ams.Entity.StockTakeList;
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


public class GetStockTakeListCallback implements Callback<List<StockTakeList>> {
    public int type;

    public GetStockTakeListCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public GetStockTakeListCallback(int type) {
        this.type = type;
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<List<StockTakeList>> call, Response<List<StockTakeList>> response) {
        if(response.code() == 200) {
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(response.body());
            callbackResponseEvent.type = type;
            EventBus.getDefault().post(callbackResponseEvent);
        } else {
            EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
        }
    }

    @Override
    public void onFailure(Call<List<StockTakeList>> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
    }
}

