package com.csl.ams.fragments;

import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.csl.ams.MainActivity;
import com.csl.ams.R;
import com.csl.ams.adapters.AxzonAdapter;

public class AxzonFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    AxzonAdapter mAdapter;

    private String[] tabs = { "Scan/Select", "Read" };
    private String[] tabsXerxes = { "Logger", "Security" };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InventoryRfidiMultiFragment fragment = (InventoryRfidiMultiFragment) mAdapter.fragment0;
        switch (item.getItemId()) {
            case R.id.menuAction_1:
                fragment.clearTagsList();
                return true;
            case R.id.menuAction_2:
                fragment.sortTagsList();
                return true;
            case R.id.menuAction_3:
                fragment.saveTagsList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle("A"); //"Axzon");

        boolean bXervesTag = false;
        if (MainActivity.mDid != null) if (MainActivity.mDid.matches("E282405")) bXervesTag = true;

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        mAdapter = AxzonAdapter.newinstance(getActivity().getSupportFragmentManager(), (bXervesTag ? 4 : 2));
        viewPager = (ViewPager) getActivity().findViewById(R.id.OperationsPager);
        viewPager.setAdapter(mAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        for (String tab_name : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab_name));
        }
        if (bXervesTag) {
            for (String tab_name : tabsXerxes) {
                tabLayout.addTab(tabLayout.newTab().setText(tab_name));
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onPause() {
        if (mAdapter.fragment0 != null) if (mAdapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment0.onPause();
        if (mAdapter.fragment1 != null) if (mAdapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment1.onPause();
        if (mAdapter.fragment2 != null) if (mAdapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment2.onPause();
        if (mAdapter.fragment3 != null) if (mAdapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment3.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mAdapter.fragment0 != null) if (mAdapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment0.onStop();
        if (mAdapter.fragment1 != null) if (mAdapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment1.onStop();
        if (mAdapter.fragment2 != null) if (mAdapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment2.onStop();
        if (mAdapter.fragment3 != null) if (mAdapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment3.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mAdapter.fragment0 != null) if (mAdapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment0.onDestroyView();
        if (mAdapter.fragment1 != null) if (mAdapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment1.onDestroyView();
        if (mAdapter.fragment2 != null) if (mAdapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment2.onDestroyView();
        if (mAdapter.fragment3 != null) if (mAdapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment3.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (MainActivity.selectFor != -1) {
            MainActivity.mCs108Library4a.setSelectCriteriaDisable(1);
            MainActivity.mCs108Library4a.setSelectCriteriaDisable(2);
            MainActivity.selectFor = -1;
        }
        if (mAdapter.fragment0 != null) if (mAdapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment0.onDestroy();
        if (mAdapter.fragment1 != null) if (mAdapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment1.onDestroy();
        if (mAdapter.fragment2 != null) if (mAdapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment2.onDestroy();
        if (mAdapter.fragment3 != null) if (mAdapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment3.onDestroy();
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (mAdapter.fragment0 != null) if (mAdapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment0.onDetach();
        if (mAdapter.fragment1 != null) if (mAdapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment1.onDetach();
        if (mAdapter.fragment2 != null) if (mAdapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment2.onDetach();
        if (mAdapter.fragment3 != null) if (mAdapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) mAdapter.fragment3.onDetach();
        super.onDetach();
    }

    public AxzonFragment() { super("AxzonFragment"); }
}
