package com.expensemanager.app.expense;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.service.Constant;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/20/16.
 */

public class ExpensePhotoAdapter extends BaseAdapter {
    private static final String TAG = ExpensePhotoAdapter.class.getSimpleName();

    private Context context;
    public ArrayList<byte[]> photoList;
    public ArrayList<String> photoNameList;

    public ExpensePhotoAdapter(Context context, ArrayList<byte[]> photoList, ArrayList<String> photoNameList) {
        this.context = context;
        this.photoList = photoList;
        this.photoNameList = photoNameList;
    }

    public int getCount() {
        return photoList != null ? photoList.size() : photoNameList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_photo_grid_view_item, null);
        }

        PhotoViewHolder photoViewHolder = new PhotoViewHolder(view);

        if (photoList != null && photoList.size() >= 1) {
            byte[] photoBytes = photoList.get(position);
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            photoViewHolder.photoImageView.setImageBitmap(bitmap);
        } else {
            String photoUrl = Constant.BASE_FILE_URL + photoNameList.get(position);
            Glide.with(context).load(photoUrl).fitCenter().into(photoViewHolder.photoImageView);
        }

        return view;
    }

    public static class PhotoViewHolder {
        @BindView(R.id.expense_photo_grid_view_image_view_id) ImageView photoImageView;

        public PhotoViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}