package com.csl.ams.WebService.Callback;

import com.csl.ams.Entity.StockTakeList;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetStockTakeListSingleCallback implements Callback<StockTakeList> {
        public GetStockTakeListSingleCallback() {
            EventBus.getDefault().post(new CallbackStartEvent());
        }

        @Override
        public void onResponse(Call<StockTakeList> call, Response<StockTakeList> response) {
            if(response.code() == 200)
                EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
            else
                EventBus.getDefault().post(new CallbackFailEvent(response.message()));

        }

        @Override
        public void onFailure(Call<StockTakeList> call, Throwable t) {

        }

}

