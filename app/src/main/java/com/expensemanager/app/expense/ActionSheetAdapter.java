package com.expensemanager.app.expense;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.EAction;

import java.util.ArrayList;

/**
 * Created by Zhaolong Zhong on 9/10/16.
 */

public class ActionSheetAdapter extends RecyclerView.Adapter<ActionSheetAdapter.ItemHolder> {
    private static final String TAG = ActionSheetAdapter.class.getSimpleName();

    private ArrayList<EAction> actions;
    private OnItemClickListener onItemClickListener;

    public ActionSheetAdapter(ArrayList<EAction> actions) {
        this.actions = actions;
    }

    @Override
    public ActionSheetAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.action_sheet_item, parent, false);
        return new ItemHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(ActionSheetAdapter.ItemHolder holder, int position) {
        holder.bind(actions.get(position));
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ItemHolder item, int position);
    }

    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ActionSheetAdapter adapter;
        TextView textView;
        ImageView imageView;

        public ItemHolder(View itemView, ActionSheetAdapter parent) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.adapter = parent;

            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }

        public void bind(EAction item) {
            textView.setText(item.getTitleId());
        }

        @Override
        public void onClick(View v) {
            final OnItemClickListener listener = adapter.getOnItemClickListener();
            if (listener != null) {
                listener.onItemClick(this, getAdapterPosition());
            }
        }
    }
}
