package com.expensemanager.app.group.member;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.service.font.Font;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= MemberAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_CHAR = 0;
    private static final int VIEW_TYPE_DEFAULT = 1;
    private ArrayList<Member> members;
    private Context context;
    private int[] numOfChars;

    public MemberAdapter(Context context, ArrayList<Member> members) {
        this.context = context;
        this.members = members;
        numOfChars = new int[members.size()];
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.members.size() + numOfChars[members.size() - 1];
    }

    @Override
    public int getItemViewType(int position) {
        int memberPos = getMemberPosition(position);
        if (memberPos == -1) {
            return VIEW_TYPE_CHAR;
        }
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        switch (viewType) {
            case VIEW_TYPE_CHAR:
                view = inflater.inflate(R.layout.member_item_char, parent, false);
                break;
            case VIEW_TYPE_DEFAULT:
                view = inflater.inflate(R.layout.member_item_default, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.member_item_default, parent, false);
                break;
        }

        viewHolder = new ViewHolderDefault(view, viewType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int memberPosition;

        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_CHAR:
                memberPosition = getMemberPosition(position + 1);
                String nextName = members.get(memberPosition).getUser().getFullname();

                ViewHolderDefault viewHolderChar = (ViewHolderDefault) viewHolder;

                viewHolderChar.nameTextView.setText(String.valueOf(nextName.charAt(0)));
                viewHolderChar.nameTextView.setTypeface(EApplication.getInstance().getTypeface(Font.REGULAR));
                break;
            case VIEW_TYPE_DEFAULT:
                memberPosition = getMemberPosition(position);
                ViewHolderDefault viewHolderDefault = (ViewHolderDefault) viewHolder;
                configureViewHolderDefault(viewHolderDefault, memberPosition);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        User user = members.get(position).getUser();

        Helpers.loadIconPhoto(viewHolder.photoImageView, user.getPhotoUrl());

        viewHolder.nameTextView.setText(user.getFullname());

        // Set item click listener
        viewHolder.itemView.setOnClickListener(v -> {
            //todo: jump to profile activity
            ProfileActivity.newInstance(context, members.get(position).getUserId());
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    private int getMemberPosition(int listPosition) {
        int left = 0, right = members.size() - 1, mid;
        while (left <= right) {
            mid = left + (right - left) / 2;
            if (numOfChars[mid] + mid == listPosition) {
                return mid;
            } else if (numOfChars[mid] + mid < listPosition) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
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

        // Sort by member fullname
        if (members.size() > 1) {
            Collections.sort(members, new Comparator<Member>() {
                @Override
                public int compare(Member m1, Member m2) {
                    String name1 = m1.getUser().getFullname();
                    String name2 = m2.getUser().getFullname();

                    return name1.compareTo(name2);
                }
            });
        }

        numOfChars = new int[members.size()];
        numOfChars[0] = 1;

        for (int i = 1; i < members.size(); i++) {
            String prevName = members.get(i - 1).getUser().getFullname();
            String currName = members.get(i).getUser().getFullname();

            numOfChars[i] = numOfChars[i - 1];
            if (currName.charAt(0) != prevName.charAt(0)) {
                numOfChars[i]++;
            }
        }

        this.members.addAll(members);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        // Member item
        ImageView photoImageView;
        // Member item and char item
        TextView nameTextView;

        private View itemView;

        public ViewHolderDefault(View view, int viewType) {
            super(view);

            if (viewType == VIEW_TYPE_DEFAULT) {
                photoImageView = (ImageView) view.findViewById(R.id.member_item_default_photo_image_view_id);
                nameTextView = (TextView) view.findViewById(R.id.member_item_default_name_text_view_id);
            } else {
                nameTextView = (TextView) view.findViewById(R.id.member_item_char_name_text_view_id);
            }
            itemView = view;
        }
    }
}
