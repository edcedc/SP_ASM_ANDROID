package com.csl.ams.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.csl.ams.MainActivity;
import com.csl.ams.R;

public class AxzonSelectorFragment extends CommonFragment {
    boolean bXerxesEnable = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, false);
        return inflater.inflate(R.layout.fragment_select_axzon, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle("Select Tag Type");
        }

        Button button_s2 = (Button) getActivity().findViewById(R.id.select_axzon_s2);
        button_s2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(2);
            }
        });
        Button button_s3 = (Button) getActivity().findViewById(R.id.select_axzon_s3);
        button_s3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(3);
            }
        });
        Button button_xx = (Button) getActivity().findViewById(R.id.select_axzon_xx);
        button_xx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(5);
            }
        });
        Button button_all = (Button) getActivity().findViewById(R.id.select_axzon_all);
        button_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAxzonFragment(0);
            }
        });
        if (bXerxesEnable == false) {
            button_xx.setVisibility(View.GONE);
            button_all.setText("All Magnus");
        }
    }

    public static AxzonSelectorFragment newInstance(boolean bXerxesEnable) {
        AxzonSelectorFragment myFragment = new AxzonSelectorFragment();
        myFragment.bXerxesEnable = bXerxesEnable;
        return myFragment;
    }
    public AxzonSelectorFragment() {
        super("AxzonSelectorFragment");
    }

    void gotoAxzonFragment(int tagType) {
        switch(tagType) {
            case 2:
                MainActivity.mDid = "E282402";
                break;
            case 3:
                MainActivity.mDid = "E282403";
                break;
            case 5:
                MainActivity.mDid = "E282405";
                break;
            default:
                MainActivity.mDid = "E28240";
                break;
        }
        MainActivity.mCs108Library4a.appendToLog("HelloABC: gotoAxzonFragment with tagType = " + tagType + ", MainActivity.mDid = " + MainActivity.mDid);

        MainActivity.mCs108Library4a.appendToLog("HelloABC: config is " + (MainActivity.config == null ? "null" : "Valid"));
        MainActivity.config.configPassword = "00000000";
        MainActivity.config.configPower = Integer.toString(300);
        MainActivity.config.config0 = Integer.toString(9);
        MainActivity.config.config1 = Integer.toString(21);
        MainActivity.config.config2 = Integer.toString(13);

        if (true) {
            Fragment fragment;
            if (bXerxesEnable) fragment = new AxzonFragment();
            else fragment = new MicronFragment();

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            Fragment fragment = AccessConfigFragment.newInstance(bXerxesEnable);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
}
