package com.expensemanager.app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.PhotoSource;

import java.util.ArrayList;

/**
 * Created by Zhaolong Zhong on 9/10/16.
 */

public class PhotoSourceAdapter extends ArrayAdapter<PhotoSource> {
    private static final String TAG = PhotoSourceAdapter.class.getSimpleName();

    public PhotoSourceAdapter(Context context, ArrayList<PhotoSource> photoSources) {
        super(context, 0, photoSources);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoSource photoSource = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.photo_source_item, parent, false);
        }

        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.photo_source_item_icon_image_view_id);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.photo_source_item_title_text_view_id);

        iconImageView.setImageResource(photoSource.getIconResId());
        titleTextView.setText(photoSource.getTitle());

        return convertView;
    }
}
