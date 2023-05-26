package com.csl.ams.WebService.P2Callback;

import android.util.Log;

import com.csl.ams.Entity.SPEntityP3.StocktakeList;
import com.csl.ams.Entity.SpEntity.StockTakeNoList;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StocktakeListCallback implements Callback<List<StocktakeList>> {
    private int id = -1;

    public StocktakeListCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    public StocktakeListCallback(int id) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.id = id;
    }

    @Override
    public void onResponse(Call<List<StocktakeList>> call, Response<List<StocktakeList>> response) {
        Log.i("onResponse", "onResponse" + response.toString());

        if(response.code() == 200) {
            if(id == -1) {
                EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
            } else {
                CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(response.body());
                callbackResponseEvent.type = id;

                EventBus.getDefault().post(callbackResponseEvent);
            }
        } else {
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<List<StocktakeList>> call, Throwable t) {

        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}