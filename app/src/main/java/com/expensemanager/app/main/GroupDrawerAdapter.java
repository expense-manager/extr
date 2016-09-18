package com.expensemanager.app.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.SyncMember;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class GroupDrawerAdapter extends RecyclerView.Adapter<GroupDrawerAdapter.DrawerViewHolder> {
    private static final String TAG = MainActivity.class.getSimpleName();

    public final static int TYPE_HEADER = 0;
    public final static int TYPE_MENU = 1;
    public final static int TYPE_NEW = 2;
    public final static int TYPE_SELECT_HINT = 3;


    private Context context;
    private ArrayList<Member> members;
    private User user;
    private DrawerViewHolder headerHolder;

    private OnItemSelecteListener mListener;

    public GroupDrawerAdapter(Context context, ArrayList<Member> members, User user) {
        this.context = context;
        this.members = members;
        this.user = user;
    }

    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == TYPE_HEADER) {
            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);

        } else if (viewType == TYPE_MENU) {
            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_item_group, parent, false);
        } else if (viewType == TYPE_NEW) {
            view = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.drawer_item_group_new, parent, false);
        } else {
            view = LayoutInflater
            .from(parent.getContext()).inflate(R.layout.drawer_item_group_hint, parent, false);
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
        } else if (type == TYPE_MENU){
            // Reset views
            holder.selectImageView.setVisibility(View.INVISIBLE);
            holder.joinTextView.setVisibility(View.INVISIBLE);

            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences_session_key), 0);
            String groupId = sharedPreferences.getString(Group.ID_KEY, null);

            Member member = members.get(position - 2);
            Group group = member.getGroup();

            holder.titleTextView.setText(group.getName());
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(group.getColor()));
            holder.iconImageView.setImageDrawable(colorDrawable);
            holder.iconCharTextView.setText(group.getName().substring(0, 1).toUpperCase());

            Log.i(TAG, member.getGroup().getName() + " accept status in group adapter: " + member.isAccepted());
            if (!member.isAccepted()) {
                holder.joinTextView.setVisibility(View.VISIBLE);
                holder.joinTextView.setOnClickListener(v -> joinGroup(member));
            } else if (group.getId().equals(groupId)) {
                // Set drawable color dynamically
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_check);
                drawable.setColorFilter(Color.parseColor(group.getColor()), PorterDuff.Mode.SRC_ATOP);
                holder.selectImageView.setImageDrawable(drawable);
                holder.selectImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void joinGroup(Member member) {
        Log.i(TAG, member.getGroup().getName());
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        member.setAccepted(true);
        realm.copyToRealmOrUpdate(member);
        realm.commitTransaction();
        realm.close();

        SyncMember.update(member).continueWith(onUpdateMemberFinished, Task.UI_THREAD_EXECUTOR);;
    }

    private Continuation<JSONObject, Void> onUpdateMemberFinished = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            String groupId = task.getResult().getString(Member.GROUP_ID_KEY);
            if (groupId != null) {
                Log.i(TAG, "group id: " + groupId);
                // Sync all categories of current group
                SyncCategory.getAllCategoriesByGroupId(groupId);
                // Sync all expenses of current group
                SyncExpense.getAllExpensesByGroupId(groupId);
                // Sync all members of current group
                SyncMember.getMembersByGroupId(groupId);
            }

            return null;
        }
    };

    public void loadUser(User user) {
        if (user != null) {
            Helpers.loadProfilePhoto(headerHolder.accountPhotoImageView, user.getPhotoUrl());
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
        return members.size() + 3;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return  TYPE_HEADER;
        } else if (position == 1) {
            return TYPE_SELECT_HINT;
        } else if (position <= members.size() + 1) {
            return TYPE_MENU;
        } else {
            return TYPE_NEW;
        }
    }

    public void add(Member member) {
        members.add(member);
        notifyItemChanged(members.size() - 2);
    }

    public void addAll(List<Member> members) {
        this.members.addAll(members);
        notifyDataSetChanged();
    }

    public void clear() {
        members.clear();
        notifyDataSetChanged();
    }

    class DrawerViewHolder extends RecyclerView.ViewHolder{
        // DrawerHeader
        ImageView accountPhotoImageView;
        TextView accountNameTextView;
        TextView accountEmailTextView;
        TextView groupSwitcherTextView;

        // DrawerItem
        TextView titleTextView;
        TextView iconCharTextView;
        TextView joinTextView;
        CircleImageView iconImageView;
        ImageView selectImageView;

        public DrawerViewHolder(View itemView, int viewType) {
            super(itemView);

            if(viewType == TYPE_HEADER){
                accountPhotoImageView = (ImageView) itemView.findViewById(R.id.drawer_account_header_account_photo_id);
                accountNameTextView = (TextView) itemView.findViewById(R.id.drawer_header_name_id);
                accountEmailTextView = (TextView) itemView.findViewById(R.id.drawer_header_email_id);
                groupSwitcherTextView = (TextView) itemView.findViewById(R.id.drawer_header_group_switcher_text_view_id);
            }else if(viewType == TYPE_MENU){
                titleTextView = (TextView) itemView.findViewById(R.id.drawer_name_text_view_id);
                iconCharTextView = (TextView) itemView.findViewById(R.id.drawer_icon_char_text_view_id);
                joinTextView = (TextView) itemView.findViewById(R.id.drawer_join_text_view_id);
                iconImageView = (CircleImageView) itemView.findViewById(R.id.drawer_icon_image_view_id);
                selectImageView = (ImageView) itemView.findViewById(R.id.drawer_icon_select_image_view_id);
            }

            if (viewType == TYPE_SELECT_HINT) {
                return;
            }

            itemView.setOnClickListener(v -> mListener.onItemSelected(v, getAdapterPosition()));
        }

    }

    public void setOnItemClickLister(OnItemSelecteListener mListener) {
        this.mListener = mListener;
    }

   public interface OnItemSelecteListener{
        public void onItemSelected(View v, int position);
    }

}