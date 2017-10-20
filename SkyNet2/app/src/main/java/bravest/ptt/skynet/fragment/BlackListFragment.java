package bravest.ptt.skynet.fragment;

import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import bravest.ptt.skynet.event.SelectEvent;
import bravest.ptt.skynet.utils.SkyNetUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by 123 on 2017/8/29.
 */

public class BlackListFragment extends BaseFragment {
    MultiChoiceRecyclerView mMultiChoiceRecyclerView;

    private boolean mSelected = false;

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
                if (itemSelectedCount > 0) {
                    mSelected = true;
                    EventBus.getDefault().post(new SelectEvent(SelectEvent.Status.SELECTED));
                }
            }

            @Override
            public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {
                if (itemSelectedCount <= 0) {
                    mSelected = false;
                    EventBus.getDefault().post(new SelectEvent(SelectEvent.Status.UNSELECTED));
                }
            }

            @Override
            public void OnSelectAll(int itemSelectedCount, int allItemCount) {
                if (itemSelectedCount > 0) {
                    mSelected = true;
                    EventBus.getDefault().post(new SelectEvent(SelectEvent.Status.SELECTED));
                } else {
                    mSelected = false;
                    EventBus.getDefault().post(new SelectEvent(SelectEvent.Status.UNSELECTED));
                }
            }

            @Override
            public void OnDeselectAll(int itemSelectedCount, int allItemCount) {
                if (itemSelectedCount <= 0) {
                    mSelected = false;
                    EventBus.getDefault().post(new SelectEvent(SelectEvent.Status.UNSELECTED));
                }
            }
        });
    }

    @Override
    public void onMenuSelected(int id) {
        if (id == R.id.action_add) {
            addBlackDomain();
        } else if (id == R.id.action_remove) {
            Toast.makeText(getActivity(), "remove", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isSelected() {
        return mSelected;
    }

    private void addBlackDomain() {
        final int DP_MARGIN = 16;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        final LinearLayout layout = new LinearLayout(getActivity());
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.setOrientation(LinearLayout.VERTICAL);
        params.setMargins(SkyNetUtils.dp2px(getActivity(), DP_MARGIN), 0, SkyNetUtils.dp2px(getActivity(), DP_MARGIN), 0);
        final EditText editText = new EditText(getActivity());
        layout.addView(editText, params);

        SkyNetUtils.popSoftInput(getActivity(), editText);
        //build dialog
        builder.setTitle("添加拦截黑名单")
                .setView(layout)
                .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
