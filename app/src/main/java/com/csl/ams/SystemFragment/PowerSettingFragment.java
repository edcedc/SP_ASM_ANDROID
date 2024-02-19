package com.csl.ams.SystemFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.csl.ams.Event.DialogEvent;
import com.csl.ams.InternalStorage;
import com.csl.ams.MainActivity;
import com.csl.ams.NewHandHeld.MyUtil;
import com.csl.ams.R;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import rfid.uhfapi_y2007.entities.AntennaPowerStatus;
import rfid.uhfapi_y2007.entities.Flag;
import rfid.uhfapi_y2007.entities.Session;
import rfid.uhfapi_y2007.entities.SessionInfo;
import rfid.uhfapi_y2007.protocol.vrp.MsgPowerConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgQValueConfig;
import rfid.uhfapi_y2007.protocol.vrp.MsgReaderCapabilityQuery;
import rfid.uhfapi_y2007.protocol.vrp.MsgRfidStatusQuery;
import rfid.uhfapi_y2007.protocol.vrp.MsgSessionConfig;

public class PowerSettingFragment extends BaseFragment{
    SeekBar seekBar;
    TextView powerText;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
//            MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{(byte) Integer.parseInt((int) (Hawk.get("power_stocktake", 100f) / 100f * 32) + "")});
            MsgPowerConfig pMsg = new MsgPowerConfig(new byte[]{Hawk.get(InternalStorage.Rfid.POWER)});
            MyUtil.reader.Send(pMsg);
            SessionInfo si = new SessionInfo();

            //si.Session = Session.values()[0];
            //si.Flag = Flag.values()[2];
            si.Session = Session.values()[0];
            si.Flag = Flag.values()[2];

            MsgSessionConfig msgS = new MsgSessionConfig(si);
            MyUtil.reader.Send(msgS);

            byte q = 4;
            MsgQValueConfig msg = new MsgQValueConfig(q);
            MyUtil.reader.Send(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
        view = LayoutInflater.from(MainActivity.mContext).inflate(R.layout.power_setting_fragment_layout, null);
        powerText = view.findViewById(R.id.power);

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)MainActivity.mContext).onBackPressed();
            }
        });

        view.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)MainActivity.mContext).mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        seekBar = view.findViewById(R.id.seekbar);
        MsgReaderCapabilityQuery msg = new MsgReaderCapabilityQuery();
        if(MyUtil.reader.Send(msg)) {
            int antCount = msg.getReceivedMessage().getAntennaCount();
            int minPower = msg.getReceivedMessage().getMinPowerValue();
            int maxPower = msg.getReceivedMessage().getMaxPowerValue();

            if (antCount > 0) {
                String[] powers = new String[maxPower - minPower + 1];
                for (int j = minPower; j <= maxPower; j++) {
                    powers[j - minPower] = j + "";
                }

                MsgRfidStatusQuery msgState = new MsgRfidStatusQuery();
                if (MyUtil.reader.Send(msgState))
                {
                    AntennaPowerStatus[] aps = msgState.getReceivedMessage().getAntennas();
                    for (AntennaPowerStatus a : aps)
                    {
                        if(a.AntennaNO == 1) {
                            // for (int i = 0; i < spPower.getAdapter().getCount(); i++) {
                            //if (spPower.getAdapter().getItem(i).toString().equals(a.PowerValue + "")) {
                            //    spPower.setSelection(i);
                            //    break;
                            //}
                            //}
                            int i = Hawk.get(InternalStorage.Rfid.POWER, 32);
                            if (i != 0){
                                powerText.setText(" ( " + i + " ) ");
                                seekBar.setProgress((i));
                            }else {
                                powerText.setText(" ( " + a.PowerValue + " ) ");
                                seekBar.setProgress((int)(a.PowerValue));
                            }
                            break;
                        }
                    }
                }
            }
        }
        ((TextView)view.findViewById(R.id.toolbar_title)).setText(getString(R.string.home_settings));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.i("value", "value " + (int)(seekBar.getProgress() / 100f * 32));

                powerText.setText(" ( " + + progress + " ) ");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle b){
        super.onCreateView(li,vg,b);
        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = seekBar.getProgress();
                byte[] bytes = new byte[]{(byte) progress};
                MsgPowerConfig pMsg = new MsgPowerConfig(bytes);
                if (!MyUtil.reader.Send(pMsg)) {
                    //errStr += "\r\n天线功率设置失败！" + pMsg.getErrorInfo().getErrMsg();
                    Log.i("error", "error");
                    DialogEvent event = new DialogEvent(getString(R.string.app_name), getString(R.string.fail));
                    EventBus.getDefault().post(event);
                } else {
                    Hawk.put(InternalStorage.Rfid.POWER, progress);
                    DialogEvent event = new DialogEvent(getString(R.string.app_name), getString(R.string.success));
                    EventBus.getDefault().post(event);
                }
            }
        });

        return view;
    }
}
