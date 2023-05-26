package com.csl.ams.WebService.Callback;

import android.util.Log;

import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;
import com.csl.ams.Event.UpdateFailEvent;
import com.csl.ams.MainActivity;
import com.csl.ams.R;
import com.csl.ams.Response.ListingResponse;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetListingCallback implements Callback<ListingResponse> {
    public GetListingCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
        Log.i("onResponse", "onResponse" + response.toString());

        if(response.code() == 200)
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        else {
            EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));

            EventBus.getDefault().post(new UpdateFailEvent());

        }
    }

    @Override
    public void onFailure(Call<ListingResponse> call, Throwable t) {
        EventBus.getDefault().post(new UpdateFailEvent());

        EventBus.getDefault().post(new CallbackFailEvent(t.getMessage()));
    }
}
