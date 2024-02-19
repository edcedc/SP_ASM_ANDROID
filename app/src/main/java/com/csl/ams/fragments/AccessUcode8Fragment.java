package com.csl.ams.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.csl.ams.MainActivity;
import com.csl.ams.R;

public class AccessUcode8Fragment extends CommonFragment {
    final boolean DEBUG = true;
    RadioButton radioButtonSelectEpc, radioButtonSelectEpcTid, radioButtonSelectEpcBrand;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_access_ucode8, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        radioButtonSelectEpc = (RadioButton) getActivity().findViewById(R.id.accessUC8SelectEpc);
        radioButtonSelectEpcTid = (RadioButton) getActivity().findViewById(R.id.accessUC8SelectEpcTid);
        radioButtonSelectEpcBrand = (RadioButton) getActivity().findViewById(R.id.accessUC8SelectEpcBrand);

        MainActivity.mCs108Library4a.setSameCheck(false);
    }

    @Override
    public void onDestroy() {
        MainActivity.mCs108Library4a.setSameCheck(true);
        MainActivity.mCs108Library4a.setInvBrandId(false);
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        super.onDestroy();
    }

    boolean userVisibleHint = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            userVisibleHint = true;
            MainActivity.mCs108Library4a.appendToLog("AccessUcode8Fragment is now VISIBLE");
            //            setNotificationListener();
        } else {
            if (radioButtonSelectEpc != null && radioButtonSelectEpcTid != null && radioButtonSelectEpcBrand != null) {
                if (radioButtonSelectEpc.isChecked()) {
                    MainActivity.mCs108Library4a.appendToLog("Selected EPC");
                    MainActivity.mDid = "E2806894A";
                }
                if (radioButtonSelectEpcTid.isChecked()) {
                    MainActivity.mCs108Library4a.appendToLog("Selected EPC+TID");
                    MainActivity.mDid = "E2806894B";
                }
                if (radioButtonSelectEpcBrand.isChecked()) {
                    MainActivity.mCs108Library4a.appendToLog("Selected EPC+BRAND");
                    MainActivity.mDid = "E2806894C";
                }
            }
            userVisibleHint = false;
            MainActivity.mCs108Library4a.appendToLog("AccessUcode8Fragment is now INVISIBLE");
        }
    }

    public AccessUcode8Fragment() {
        super("AccessUcode8Fragment");
    }
}
