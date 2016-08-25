package com.expensemanager.app.expense;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.ExpensePhoto;
import com.expensemanager.app.service.Constant;
import com.expensemanager.app.service.SyncPhoto;

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

        if (photoList != null && photoList.size() >= 1) {
            byte[] photoBytes = photoList.get(position);
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            photoViewHolder.photoImageView.setImageBitmap(bitmap);
        } else {
//                Bitmap cameraIconBitmap = getCameraIconBitmap();
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                cameraIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] sampledInputData = stream.toByteArray();
//                Bitmap bitmap = BitmapFactory.decodeByteArray(sampledInputData, 0, sampledInputData.length);
//                photoViewHolder.photoImageView.setImageBitmap(bitmap);
//                photoViewHolder.photoImageView.setVisibility(View.GONE);
//                //todo: add photo in edit mode

                ExpensePhoto expensePhoto = expensePhotos.get(position);
                String photoUrl = Constant.BASE_FILE_URL + expensePhoto.getFileName();
                Glide.with(context).load(photoUrl).fitCenter().into(photoViewHolder.photoImageView);

                if (expensePhotos != null && position != expensePhotos.size() - 1) {
                    photoViewHolder.photoImageView.setOnLongClickListener(v -> {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.delete_photo)
                                .setMessage(R.string.delete_photo_message)
                                .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
                                    SyncPhoto.deleteExpensePhoto(expensePhoto.getId(), expensePhoto.getFileName());
                                    ExpensePhoto.delete(expensePhoto.getExpenseId(), expensePhoto.getFileName());
                                    expensePhotos.remove(position);
                                    notifyDataSetChanged();
                                })
                                .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
                                .show();

                        return false;
                    });
                }
        }

        return view;
    }

    public static class PhotoViewHolder {
        @BindView(R.id.expense_photo_grid_view_image_view_id) ImageView photoImageView;

        private View itemView;

        public PhotoViewHolder(View view) {
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }

    private Bitmap getCameraIconBitmap() {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_camera_alt_white_24dp);
        Bitmap originalBitmap = ((BitmapDrawable)drawable).getBitmap();
        Paint paint = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(filter);
        Bitmap cameraIconBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(cameraIconBitmap);
        canvas.drawBitmap(originalBitmap, -2, 2, paint);

        return Helpers.padBitmap(cameraIconBitmap, 20, 20);
    }
}