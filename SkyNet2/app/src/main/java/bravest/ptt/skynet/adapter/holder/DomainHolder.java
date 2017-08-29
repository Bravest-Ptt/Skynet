package bravest.ptt.skynet.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import bravest.ptt.skynet.R;

/**
 * Created by 123 on 2017/8/30.
 */

public class DomainHolder extends RecyclerView.ViewHolder {

    public TextView domain;
    public TextView time;

    public DomainHolder(View itemView) {
        super(itemView);

        domain = (TextView) itemView.findViewById(R.id.item_domain);
        time = (TextView) itemView.findViewById(R.id.item_time);
    }
}
