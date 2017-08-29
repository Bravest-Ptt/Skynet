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
import android.support.v4.app.FragmentContainer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import java.util.HashMap;

import bravest.ptt.skynet.R;
import bravest.ptt.skynet.animation.SupportAnimator;
import bravest.ptt.skynet.animation.ViewAnimationUtils;
import bravest.ptt.skynet.core.service.SkyNetVpnService;
import bravest.ptt.skynet.core.util.VpnServiceHelper;
import bravest.ptt.skynet.db.SkyNetDbUtils;
import bravest.ptt.skynet.db.observer.FilterHistoryObserver;
import bravest.ptt.skynet.event.VPNEvent;
import bravest.ptt.skynet.fragment.BaseFragment;
import bravest.ptt.skynet.fragment.BlackListFragment;
import bravest.ptt.skynet.fragment.HistoryFragment;
import bravest.ptt.skynet.fragment.MainFragment;
import bravest.ptt.skynet.view.RippleBackground;
import de.greenrobot.event.EventBus;

import static bravest.ptt.skynet.event.VPNEvent.Status.STARTING;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";

    private MainFragment mMainFragment;

    private BaseFragment mCurrentFragment;

    private HistoryFragment mHistoryFragment;

    private BlackListFragment mBlackListFragment;

    private FragmentManager mFragmentManager;

    private HashMap<Integer, BaseFragment> mFragmentMap;

    public Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        initData();
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

        mToolbar = toolbar;

        initFragments();
    }

    private void initData() {

    }

    private void initFragments() {
        //Init fragment
        mFragmentManager = getSupportFragmentManager();
        mFragmentMap = new HashMap<>();

        mMainFragment = new MainFragment();
        mHistoryFragment = new HistoryFragment();
        mBlackListFragment = new BlackListFragment();

        mFragmentMap.put(R.id.nav_home, mMainFragment);
        mFragmentMap.put(R.id.nav_history, mHistoryFragment);
        mFragmentMap.put(R.id.nav_blacklist, mBlackListFragment);
        //set default fragment
        setFragment(R.id.nav_home);
    }

    private void setFragment(int id) {
        BaseFragment current = mFragmentMap.get(id);
        invalidateOptionsMenu();
        if (current == null) {
            return;
        }

        //One fragment transaction @method commit can not be used twice, it will cause "commit already called".
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.content_view, current);
        mCurrentFragment = current;
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        Log.d(TAG, "onCreateOptionsMenu: ");
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: ");
        if (mCurrentFragment instanceof MainFragment) {
            menu.findItem(R.id.action_filter).setVisible(false);
            menu.findItem(R.id.action_add).setVisible(false);
            menu.findItem(R.id.action_remove).setVisible(false);
        } else if (mCurrentFragment instanceof HistoryFragment) {
            menu.findItem(R.id.action_filter).setVisible(true);
            menu.findItem(R.id.action_add).setVisible(false);
            menu.findItem(R.id.action_remove).setVisible(false);
        } else if (mCurrentFragment instanceof BlackListFragment) {
            menu.findItem(R.id.action_filter).setVisible(false);
            menu.findItem(R.id.action_add).setVisible(true);
            menu.findItem(R.id.action_remove).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        mCurrentFragment.onMenuSelected(id);
        return super.onOptionsItemSelected(item);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        setFragment(id);

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
                if (mMainFragment != null) mMainFragment.changeButtonStatus(false);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
