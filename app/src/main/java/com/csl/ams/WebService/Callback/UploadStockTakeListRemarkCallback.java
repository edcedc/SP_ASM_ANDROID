package com.csl.ams.WebService.Callback;

import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;
import com.csl.ams.Request.StockTakeListItemRemarkRequest;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadStockTakeListRemarkCallback implements Callback<StockTakeListItemRemarkRequest> {
    public UploadStockTakeListRemarkCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<StockTakeListItemRemarkRequest> call, Response<StockTakeListItemRemarkRequest> response) {
         if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else
            EventBus.getDefault().post(new CallbackFailEvent(response.message()));

    }

    @Override
    public void onFailure(Call<StockTakeListItemRemarkRequest> call, Throwable t) {
        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}