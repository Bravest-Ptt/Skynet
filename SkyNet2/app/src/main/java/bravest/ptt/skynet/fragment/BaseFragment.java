package bravest.ptt.skynet.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bravest.ptt.skynet.listener.MenuListener;

public abstract class BaseFragment extends Fragment implements MenuListener{

    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mView == null) {
            mView = inflater.inflate(getLayoutId(), container, false);
            initVariables();
            initViews();
        }
        return mView;
    }

    protected abstract int getLayoutId();
    protected abstract void initViews();
    protected abstract void initVariables();
    protected View getRootView() {
        return mView;
    }

}
