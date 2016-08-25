package com.expensemanager.app.expense;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.models.ExpensePhoto;
import com.expensemanager.app.service.Constant;

import java.util.ArrayList;

/**
 * Created by Zhaolong Zhong on 8/25/16.
 */

public class ExpensePhotoPagerAdapter extends PagerAdapter {
    private static final String TAG = ExpensePhotoPagerAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<ExpensePhoto> expensePhotos;

    public ExpensePhotoPagerAdapter(Context context, ArrayList<ExpensePhoto> expensePhotos) {
        this.context = context;
        this.expensePhotos = expensePhotos;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.expense_photo_view_pager_item, collection, false);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.expense_photo_view_pager_item_image_view_id);
        String photoUrl = Constant.BASE_FILE_URL + expensePhotos.get(position).getFileName();

        Glide.with(context).load(photoUrl).fitCenter().into(imageView);

        collection.addView(rootView);

        return rootView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return expensePhotos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
