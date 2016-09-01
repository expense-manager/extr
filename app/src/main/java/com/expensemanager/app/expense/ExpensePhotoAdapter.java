package com.expensemanager.app.expense;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.expensemanager.app.R;
import com.expensemanager.app.models.ExpensePhoto;
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
    public ArrayList<ExpensePhoto> expensePhotos;

    public ExpensePhotoAdapter(Context context, ArrayList<byte[]> photoList, ArrayList<ExpensePhoto> expensePhotos) {
        this.context = context;
        this.photoList = photoList;
        this.expensePhotos = expensePhotos;
    }

    public int getCount() {
        return photoList != null ? photoList.size() : expensePhotos.size();
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
        photoViewHolder.progressBar.setVisibility(photoList != null && photoList.size() > 0 ? View.GONE : View.VISIBLE);

        if (photoList != null && photoList.size() >= 1) {
            byte[] photoBytes = photoList.get(position);
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            Log.i(TAG, bitmap.toString());
            photoViewHolder.photoImageView.setImageBitmap(bitmap);
        } else {
            ExpensePhoto expensePhoto = expensePhotos.get(position);
            String photoUrl = Constant.BASE_FILE_URL + expensePhoto.getFileName();

            Glide.with(photoViewHolder.photoImageView.getContext())
                    .load(photoUrl)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide onException.", e);
                            photoViewHolder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Log.d(TAG, "Glide onResourceReady");
                            photoViewHolder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .fitCenter()
                    .dontAnimate()
                    .into(photoViewHolder.photoImageView);
        }

        return view;
    }

    public static class PhotoViewHolder {
        @BindView(R.id.expense_photo_grid_view_image_view_id) ImageView photoImageView;
        @BindView(R.id.expense_photo_grid_view_progress_bar_id) ProgressBar progressBar;

        private View itemView;

        public PhotoViewHolder(View view) {
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}