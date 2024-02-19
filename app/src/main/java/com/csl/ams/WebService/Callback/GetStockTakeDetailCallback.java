package com.csl.ams.WebService.Callback;

import android.util.Log;

import com.csl.ams.Entity.SpEntity.StockTakeDetail;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetStockTakeDetailCallback  implements Callback<StockTakeDetail> {
    public GetStockTakeDetailCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    private String id;

    public GetStockTakeDetailCallback(String id) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.id = id;
    }

    @Override
    public void onResponse(Call<StockTakeDetail> call, Response<StockTakeDetail> response) {
        Log.i("onResponse", "onResponse" + response.toString());

        if(response.code() == 200) {
            if(id == null) {
                EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
            } else {
                EventBus.getDefault().post(new CallbackResponseEvent(id, null, response.body()));
            }
        } else {
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<StockTakeDetail> call, Throwable t) {

        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}