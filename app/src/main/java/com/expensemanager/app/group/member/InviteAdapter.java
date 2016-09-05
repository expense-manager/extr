package com.expensemanager.app.group.member;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.expensemanager.app.R;
import com.expensemanager.app.expense.ExpenseDetailActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.service.SyncMember;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class InviteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= InviteAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<User> users;
    private Context context;
    private Map<String, Member> existingMemberMap;
    private Group group;
    private User createdBy;

    public InviteAdapter(Context context, ArrayList<User> users, String loginUserId, String groupId) {
        this.context = context;
        this.users = users;
        createdBy = User.getUserById(loginUserId);
        group = Group.getGroupById(groupId);
        existingMemberMap = new HashMap<>();
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                View view = inflater.inflate(R.layout.member_item_invite, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.member_item_invite, parent, false);
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
        viewHolder.inviteTextView.setVisibility(View.INVISIBLE);
        viewHolder.sentTextView.setVisibility(View.INVISIBLE);
        viewHolder.acceptedTextView.setVisibility(View.INVISIBLE);

        User user = users.get(position);

        Log.i(TAG, user.getFirstName() + " photo: " + user.getPhotoUrl());
        Helpers.loadIconPhoto(viewHolder.userPhotoImageView, user.getPhotoUrl());

        viewHolder.userNameTextView.setText(user.getFullname());
        viewHolder.userEmailTextView.setText(user.getEmail());

        Member member = existingMemberMap.get(user.getId());
        if (member == null) {
            viewHolder.inviteTextView.setVisibility(View.VISIBLE);
            viewHolder.inviteTextView.setOnClickListener(v -> invite(user));
        } else if (member.isAccepted()) {
            viewHolder.acceptedTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.sentTextView.setVisibility(View.VISIBLE);
        }

        // Set item click listener
        viewHolder.itemView.setOnClickListener(v -> {
            // todo: show user info dialog fragment or profile activity
            if (member != null && member.getUserId() != null) {
                ProfileActivity.newInstance(context, member.getUserId());
                ((Activity) getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
            }
        });
    }

    private void invite(User user) {
        if (group == null || createdBy == null || user == null) {
            return;
        }

        Member member = new Member();
        member.setGroup(group);
        member.setCreatedBy(createdBy);
        member.setUser(user);
        member.setAccepted(false);

        SyncMember.create(member);
    }

    public void setExistingMembers(List<Member> members) {
        existingMemberMap.clear();
        for (Member member : members) {
            existingMemberMap.put(member.getUserId(), member);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<User> users) {
        if (users == null) {
            return;
        }

        this.users.addAll(users);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.member_item_invite_photo_image_view_id) ImageView userPhotoImageView;
        @BindView(R.id.member_item_invite_name_text_view_id) TextView userNameTextView;
        @BindView(R.id.member_item_invite_email_text_view_id) TextView userEmailTextView;
        @BindView(R.id.member_item_invite_invite_text_view_id) TextView inviteTextView;
        @BindView(R.id.member_item_invite_sent_text_view_id) TextView sentTextView;
        @BindView(R.id.member_item_invite_accepted_text_view_id) TextView acceptedTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
