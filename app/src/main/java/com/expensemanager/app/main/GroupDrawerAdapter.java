package com.expensemanager.app.main;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDrawerAdapter extends RecyclerView.Adapter<GroupDrawerAdapter.DrawerViewHolder> {

    public final static int TYPE_HEADER = 0;
    public final static int TYPE_MENU = 1;


    private Context context;
    private ArrayList<Group> groups;
    private User user;
    private DrawerViewHolder headerHolder;

    private OnItemSelecteListener mListener;

    public GroupDrawerAdapter(Context context, ArrayList<Group> groups, User user) {
        this.context = context;
        this.groups = groups;
        this.user = user;
    }

    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == TYPE_HEADER){

            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);

        }else{

            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_item_group, parent, false);
        }

        return new DrawerViewHolder(view, viewType);
    }


    @Override
    public void onBindViewHolder(DrawerViewHolder holder, int position) {
        int type = getItemViewType(position);
        if(type == TYPE_HEADER) {
            headerHolder = holder;
            loadUser(user);

            Typeface font= Typeface.createFromAsset(context.getAssets(),
                "fonts/materialdrawerfont-font-v5.0.0.ttf");
            holder.groupSwitcherTextView.setTypeface(font);
            holder.groupSwitcherTextView.setText("\uE5C5");

            holder.groupSwitcherTextView.clearAnimation();
            ViewCompat.animate(holder.groupSwitcherTextView).rotation(180).start();

            holder.accountPhotoImageView.setOnClickListener(v -> {
                ProfileActivity.newInstance(context, null);
            });
        }else{
            Group group = groups.get(position - 1);
            holder.titleTextView.setText(group.getName());
            User user = User.getUserById(group.getUserId());
            if (user != null) {
                Glide.with(context)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.profile_place_holder_image)
                    .dontAnimate()
                    .into(holder.iconImageView);
            }
        }

    }

    public void loadUser(User user) {
        if (user != null) {
            Glide.with(context)
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.profile_place_holder_image)
                .dontAnimate()
                .into(headerHolder.accountPhotoImageView);
            headerHolder.accountNameTextView.setText(user.getFullname());
            headerHolder.accountEmailTextView.setText(user.getEmail());

            String fullname = user.getFullname();

            if (fullname != null && !fullname.isEmpty()) {
                headerHolder.accountNameTextView.setText(fullname);
            } else {
                headerHolder.accountNameTextView.setText(context.getString(R.string.app_name));
            }
        }
    }

    @Override
    public int getItemCount() {
        return groups.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return  TYPE_HEADER;
        }
        return TYPE_MENU;
    }

    public void add(Group group) {
        groups.add(group);
        notifyItemChanged(groups.size() - 1);
    }

    public void addAll(List<Group> groups) {
        this.groups.addAll(groups);
        notifyDataSetChanged();
    }

    public void clear() {
        groups.clear();
        notifyDataSetChanged();
    }

    class DrawerViewHolder extends RecyclerView.ViewHolder{
        // DrawerHeader
        CircleImageView accountPhotoImageView;
        TextView accountNameTextView;
        TextView accountEmailTextView;
        TextView groupSwitcherTextView;

        // DrawerItem
        TextView titleTextView;
        CircleImageView iconImageView;

        public DrawerViewHolder(View itemView, int viewType) {
            super(itemView);

            if(viewType == TYPE_HEADER){
                accountPhotoImageView = (CircleImageView) itemView.findViewById(R.id.drawer_account_header_acount_photo_id);
                accountNameTextView = (TextView) itemView.findViewById(R.id.drawer_header_name_id);
                accountEmailTextView = (TextView) itemView.findViewById(R.id.drawer_header_email_id);
                groupSwitcherTextView = (TextView) itemView.findViewById(R.id.drawer_header_group_switcher_text_view_id);
            }else if(viewType == TYPE_MENU){
                titleTextView = (TextView) itemView.findViewById(R.id.drawer_name_text_view_id);
                iconImageView = (CircleImageView) itemView.findViewById(R.id.drawer_icon_image_view_id);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemSelected(view, getAdapterPosition());

                }
            });
        }

    }

    public void setOnItemClickLister(OnItemSelecteListener mListener) {
        this.mListener = mListener;
    }

   public interface OnItemSelecteListener{
        public void onItemSelected(View v, int position);
    }

}