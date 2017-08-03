package bravest.ptt.skynet.activity;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import bravest.ptt.skynet.R;
import bravest.ptt.skynet.animation.SupportAnimator;
import bravest.ptt.skynet.animation.ViewAnimationUtils;
import bravest.ptt.skynet.core.service.SkyNetVpnService;
import bravest.ptt.skynet.core.util.VpnServiceHelper;
import bravest.ptt.skynet.db.SkyNetDbUtils;
import bravest.ptt.skynet.db.observer.FilterHistoryObserver;
import bravest.ptt.skynet.event.VPNEvent;
import bravest.ptt.skynet.view.RippleBackground;
import de.greenrobot.event.EventBus;

import static bravest.ptt.skynet.event.VPNEvent.Status.STARTING;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initVariables();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFilterHistoryObserver != null) {
            getContentResolver().unregisterContentObserver(mFilterHistoryObserver);
        }
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mVpnSwitch = (TextView) findViewById(R.id.start_vpn_service);
        mVpnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean currentStatus = VpnServiceHelper.vpnRunningStatus();
                if (currentStatus) { //如果是关闭操作，立即更新界面
                    changeButtonStatus(false);
                } else {
                    Snackbar.make(v, "Sky Net Started", Snackbar.LENGTH_LONG).show();
                }
                VpnServiceHelper.changeVpnRunningStatus(HomeActivity.this, !currentStatus);
            }
        });

        mRippleView = (RippleBackground) findViewById(R.id.rippleView);
        mFilterCountText = (TextView) findViewById(R.id.filter_count);
        initFilterCount();
    }

    private void initFilterCount() {
        Cursor cursor = getContentResolver()
                .query(SkyNetDbUtils.FilterHistory.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            mFilterCountText.setText(String.valueOf(cursor.getCount()));
            cursor.close();
        }
    }

    private void initVariables() {
        mFilterHistoryObserver = new FilterHistoryObserver(this, mHandler);
        Uri uri = SkyNetDbUtils.FilterHistory.CONTENT_URI;
        getContentResolver().registerContentObserver(uri, false, mFilterHistoryObserver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ad_filter) {
            // Handle the camera action
        } else if (id == R.id.nav_data_show) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VpnServiceHelper.START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                VpnServiceHelper.startVpnService(this);
            } else {
                Log.d(TAG, "you refuse the request");
                changeButtonStatus(false);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(VPNEvent event) {
        boolean selected = event.isEstablished();

        switch (event.getStatus()) {
            case STARTING:
                Toast.makeText(this, "Starting...", Toast.LENGTH_LONG).show();
                break;
            default:
                changeButtonStatus(selected);
                break;
        }

    }

    private void changeButtonStatus(boolean isRunning) {
        mRippleView.setSelected(isRunning);
        mVpnSwitch.setSelected(isRunning);
        mVpnSwitch.setText(isRunning ? "Close" : "Start");
    }
}
