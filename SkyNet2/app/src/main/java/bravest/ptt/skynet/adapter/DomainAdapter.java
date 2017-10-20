package bravest.ptt.skynet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;

import java.util.ArrayList;

import bravest.ptt.skynet.R;
import bravest.ptt.skynet.adapter.entity.Record;
import bravest.ptt.skynet.adapter.holder.DomainHolder;

public class DomainAdapter extends MultiChoiceAdapter<DomainHolder> {

    private ArrayList<Record> mList;
    private Context mContext;

    public DomainAdapter(ArrayList<Record> list, Context context) {
        mList = list;
        mContext = context;
    }

    @Override
    public DomainHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DomainHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false));
    }

    @Override
    public void onBindViewHolder(DomainHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.domain.setText(mList.get(position).domain);
        holder.time.setText(mList.get(position).time);
    }

    /**
     * Override this method to implement a custom active/deactive state
     */
    @Override
    public void setActive(View view, boolean state) {

        View layout =view.findViewById(R.id.get_started_relative_layout);

        if(layout != null){
            if(state){
                layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            }else{
                layout.setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
