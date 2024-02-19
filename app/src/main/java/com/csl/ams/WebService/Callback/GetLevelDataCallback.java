package com.csl.ams.WebService.Callback;

import android.util.Log;

import com.csl.ams.Event.CallbackFailEvent;
import com.csl.ams.Event.CallbackResponseEvent;
import com.csl.ams.Event.CallbackStartEvent;
import com.csl.ams.MainActivity;
import com.csl.ams.R;
import com.csl.ams.Response.LevelData;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetLevelDataCallback implements Callback<List<LevelData>> {
    private int type;
    private int level;
    private String fatherno;

    public GetLevelDataCallback(int type, int level) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.type = type;
        this.level = level;
    }

    public GetLevelDataCallback(String fatherno, int type, int level) {
        EventBus.getDefault().post(new CallbackStartEvent());
        this.type = type;
        this.level = level;
        this.fatherno = fatherno;
    }

    @Override
    public void onResponse(Call<List<LevelData>> call, Response<List<LevelData>> response) {
        Log.i("GetLevelDataCallback", "GetLevelDataCallback " + response.body());
        if(response.code() == 200) {
            CallbackResponseEvent callbackResponseEvent = new CallbackResponseEvent(response.body());
            callbackResponseEvent.type = type;
            callbackResponseEvent.level = level;
            callbackResponseEvent.setFatherno(fatherno);

            if(response.body().size() == 0) {
                callbackResponseEvent.empty = true;
                List<LevelData> listData = new ArrayList<>();
                listData.add(new LevelData());
                callbackResponseEvent.setResponse(listData);
            }

            EventBus.getDefault().post(callbackResponseEvent);
        } else {
            EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
        }
    }

    @Override
    public void onFailure(Call<List<LevelData>> call, Throwable t) {
        Log.i("onFailure", "onFailure" + t.toString() + " " + call.request().url());

        EventBus.getDefault().post(new CallbackFailEvent(MainActivity.mContext.getString(R.string.no_internet)));
    }
}
