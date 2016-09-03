package com.expensemanager.app.group.member;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.models.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= MemberAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<User> users;
    private Context context;

    public MemberAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
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
                View view = inflater.inflate(R.layout.member_item_default, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.member_item_default, parent, false);
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
        User user = users.get(position);

        Glide.with(context)
            .load(user.getPhotoUrl())
            .placeholder(R.drawable.profile_place_holder_image)
            .into(viewHolder.photoImageView);

        viewHolder.nameTextView.setText(user.getFullname());

        // Set item click listener
        viewHolder.itemView.setOnClickListener(v -> {
            //todo: jump to profile activity
            //ProfileActivity.newInstance(context, users.get(position).getId());
            //((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    public void add(User user) {
        this.users.add(user);
        notifyItemChanged(users.size() - 1);
    }


    public void addAll(List<User> users) {
        if (users == null) {
            return;
        }

        this.users.addAll(users);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.member_item_default_photo_image_view_id) CircleImageView photoImageView;
        @BindView(R.id.member_item_default_name_text_view_id) TextView nameTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
