package bravest.ptt.skynet.fragment;

import android.widget.Toast;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceRecyclerView;
import com.davidecirillo.multichoicerecyclerview.listeners.MultiChoiceSelectionListener;

import java.util.ArrayList;
import java.util.Calendar;

import bravest.ptt.skynet.R;
import bravest.ptt.skynet.activity.HomeActivity;
import bravest.ptt.skynet.adapter.DomainAdapter;
import bravest.ptt.skynet.adapter.entity.Record;
import bravest.ptt.skynet.app.App;

/**
 * Created by 123 on 2017/8/29.
 */

public class BlackListFragment extends BaseFragment {
    MultiChoiceRecyclerView mMultiChoiceRecyclerView;

    private ArrayList<Record> mData = new ArrayList<>();

    private static final String[] domains = {
            "dn.ad.xiaomi.com",
            "chuantu.biz",
            "mg.baotoushanghui.com",
            "m1g816.nokia333.com",
            "pic - cdn.casee.cn",
            "adcontent.videoegg.com",
            "core.videoegg.com",
            "beacon.videoegg.com",
            "mconf.videoegg.com",
            "ds.ethicalads.net",
            "ethicalads.net",
            "d.ethicalads.net",
            "atic.adtaily.com",
            "ssets.adtaily.com",
            "os.gw.youmi.net",
            "at.gw.youmi.net",
            "banner.img.",
            "static.youmi.net",
            "os.wall.youmi.net",
            "r.youmi.net",
            "bea4.v.fwmrm.net"
    };


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blacklist;
    }

    @Override
    protected void initViews() {
        setUpMultiChoiceRecyclerView();
    }

    @Override
    protected void initVariables() {
        for (String a : domains) {
            Record record = new Record();
            record.domain = a;
            record.time = Calendar.getInstance().getTime().toLocaleString();
            mData.add(record);
        }
    }

    private void setUpMultiChoiceRecyclerView() {
        mMultiChoiceRecyclerView
                = (MultiChoiceRecyclerView) getRootView().findViewById(R.id.multiChoiceRecyclerView);
        mMultiChoiceRecyclerView.setRecyclerColumnNumber(1);

        mMultiChoiceRecyclerView.setMultiChoiceToolbar(
                (HomeActivity)getActivity(),
                ((HomeActivity)getActivity()).mToolbar,
                getString(R.string.app_name),
                "item selected",
                R.color.colorPrimary, R.color.colorPrimaryDark);
        DomainAdapter domainAdapter = new DomainAdapter(mData, App.getInstance().getApplicationContext());
        mMultiChoiceRecyclerView.setAdapter(domainAdapter);
        mMultiChoiceRecyclerView.setMultiChoiceSelectionListener(new MultiChoiceSelectionListener() {
            @Override
            public void OnItemSelected(int selectedPosition, int itemSelectedCount, int allItemCount) {
                
            }

            @Override
            public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {

            }

            @Override
            public void OnSelectAll(int itemSelectedCount, int allItemCount) {

            }

            @Override
            public void OnDeselectAll(int itemSelectedCount, int allItemCount) {

            }
        });
    }

    @Override
    public void onMenuSelected(int id) {
        if (id == R.id.action_add) {
            Toast.makeText(getActivity(), "add", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_remove) {
            Toast.makeText(getActivity(), "remove", Toast.LENGTH_SHORT).show();
        }
    }
}
