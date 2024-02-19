package com.csl.ams.WebService.Callback;

import android.util.Log;

import com.csl.ams.Entity.ImageReturn;
import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageReturnCallback implements Callback<ArrayList<ImageReturn>> {
    public ImageReturnCallback() {
        EventBus.getDefault().post(new CallbackStartEvent());
    }

    @Override
    public void onResponse(Call<ArrayList<ImageReturn>> call, Response<ArrayList<ImageReturn>> response) {
        Log.i("onResponse", "onResponse " + response.toString() + response.code() + " " + response.body());

        if(response.code() == 200) {
            try {
                Log.i("response", "response case 1 " + response.body().get(0).getVersion());
            } catch (Exception e) {
                e.printStackTrace();
            }
            EventBus.getDefault().post(new CallbackResponseEvent(response.body()));
        } else {
            //Log.i("response", "response case 2 " );
            //EventBus.getDefault().post(new CallbackFailEvent(response.message()));
        }
    }

    @Override
    public void onFailure(Call<ArrayList<ImageReturn>> call, Throwable t) {
        Log.i("onFailure", "onFailure" + t.toString() + " " + call.request().url());
    }

}
