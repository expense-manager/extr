package com.expensemanager.app.expense.filter;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= MemberFilterAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_CHAR = 0;
    private static final int VIEW_TYPE_DEFAULT = 1;

    private ArrayList<Member> members;
    private Member member;
    private boolean isMemberFiltered;
    private Context context;

    public MemberFilterAdapter(Context context, ArrayList<Member> members,
            boolean isMemberFiltered, Member member) {
        this.context = context;
        this.members = members;
        this.isMemberFiltered = isMemberFiltered;
        this.member = member;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.members.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                View view = inflater.inflate(R.layout.member_item_filter, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;

            default:
                View defaultView = inflater.inflate(R.layout.category_picker_item, parent, false);
                viewHolder = new ViewHolderDefault(defaultView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_DEFAULT:
                ViewHolderDefault viewHolderDefault = (ViewHolderDefault) viewHolder;
                configureViewHolderDefault(viewHolderDefault, position);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        // Reset views
        viewHolder.selectImageView.setVisibility(View.INVISIBLE);

        User user = members.get(position).getUser();

        Helpers.loadIconPhoto(viewHolder.photoImageView, user.getPhotoUrl());

        viewHolder.nameTextView.setText(user.getFullname());

        if (isMemberFiltered && user.getId().equals(member.getUserId())) {
            viewHolder.selectImageView.setVisibility(View.VISIBLE);
        }
    }

    public void clear() {
        members.clear();
        notifyDataSetChanged();
    }

    public void add(Member member) {
        this.members.add(member);
        notifyItemChanged(members.size() - 1);
    }


    public void addAll(List<Member> members) {
        if (members == null) {
            return;
        }

        this.members.addAll(members);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.member_item_filter_photo_image_view_id) ImageView photoImageView;
        @BindView(R.id.member_item_filter_name_text_view_id) TextView nameTextView;
        @BindView(R.id.member_item_filter_select_image_view_id) ImageView selectImageView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
