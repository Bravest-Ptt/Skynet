package bravest.ptt.skynet.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import bravest.ptt.skynet.R;
import bravest.ptt.skynet.activity.HomeActivity;
import bravest.ptt.skynet.core.util.VpnServiceHelper;
import bravest.ptt.skynet.db.SkyNetDbUtils;
import bravest.ptt.skynet.db.observer.FilterHistoryObserver;
import bravest.ptt.skynet.event.VPNEvent;
import bravest.ptt.skynet.view.RippleBackground;
import de.greenrobot.event.EventBus;

/**
 * Created by 123 on 2017/8/29.
 */

public class MainFragment extends BaseFragment {
    private FilterHistoryObserver mFilterHistoryObserver;

    private TextView mVpnSwitch;
    private RippleBackground mRippleView;
    private TextView mFilterCountText;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FilterHistoryObserver.WHAT_FILTER_HISTORY_CHANGED:
                    if (mFilterCountText != null) {
                        mFilterCountText.setText(String.valueOf(msg.arg1));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFilterHistoryObserver != null) {
            getActivity().getContentResolver().unregisterContentObserver(mFilterHistoryObserver);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initViews() {
        mVpnSwitch = (TextView) getRootView().findViewById(R.id.start_vpn_service);
        mVpnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean currentStatus = VpnServiceHelper.vpnRunningStatus();
                if (currentStatus) { //如果是关闭操作，立即更新界面
                    changeButtonStatus(false);
                } else {
                    Snackbar.make(v, "Sky Net Started", Snackbar.LENGTH_LONG).show();
                }
                VpnServiceHelper.changeVpnRunningStatus(getActivity(), !currentStatus);
            }
        });

        mRippleView = (RippleBackground) getRootView().findViewById(R.id.rippleView);
        mFilterCountText = (TextView) getRootView().findViewById(R.id.filter_count);
        initFilterCount();
    }

    private void initFilterCount() {
        Cursor cursor = getActivity().getContentResolver()
                .query(SkyNetDbUtils.FilterHistory.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            mFilterCountText.setText(String.valueOf(cursor.getCount()));
            cursor.close();
        }
    }

    @Override
    protected void initVariables() {
        mFilterHistoryObserver = new FilterHistoryObserver(getActivity(), mHandler);
        Uri uri = SkyNetDbUtils.FilterHistory.CONTENT_URI;
        getActivity().getContentResolver().registerContentObserver(uri, false, mFilterHistoryObserver);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(VPNEvent event) {
        boolean selected = event.isEstablished();

        switch (event.getStatus()) {
            case STARTING:
                Toast.makeText(getActivity(), "Starting...", Toast.LENGTH_LONG).show();
                break;
            default:
                changeButtonStatus(selected);
                break;
        }

    }

    public void changeButtonStatus(boolean isRunning) {
        mRippleView.setSelected(isRunning);
        mVpnSwitch.setSelected(isRunning);
        mVpnSwitch.setText(isRunning ? "Close" : "Start");
    }

    @Override
    public void onMenuSelected(int id) {
    }
}
