package com.expensemanager.app.category;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.category.color_picker.ColorPickerFragment;
import com.expensemanager.app.category.color_picker.ColorPickerSheetAdapter;
import com.expensemanager.app.category.icon_picker.IconPickerSheetAdapter;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.enums.EColor;
import com.expensemanager.app.service.enums.EIcon;

import java.util.List;
import java.util.Set;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

import static com.expensemanager.app.R.id.recyclerView;

public class CategoryDetailActivity extends AppCompatActivity {
    private static final String TAG = CategoryDetailActivity.class.getSimpleName();

    private static final int COLUMN = 6;
    private static final String CATEGORY_ID = "CATEGORY_ID";

    private String groupId;
    private Category category;

    private Set<String> usedColors;
    private String currentColor;
    private BottomSheetDialog colorSheetDialog;
    private List<String> colors;

    private Set<String> usedIcons;
    private String currentIcon;
    private BottomSheetDialog iconSheetDialog;
    private List<String> icons;

    private boolean isEditable = false;
    private boolean isDeleteAction = false;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.category_detail_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.category_detail_activity_delete_button_id) Button deleteButton;
    @BindView(R.id.category_detail_activity_color_image_view_id) CircleImageView colorImageView;
    @BindView(R.id.category_detail_activity_icon_image_view_id) ImageView iconImageView;
    @BindView(R.id.category_detail_activity_preview_color_image_view_id) CircleImageView previewColorImageView;
    @BindView(R.id.category_detail_activity_preview_icon_image_view_id) ImageView previewIconImageView;
    @BindView(R.id.category_detail_activity_icon_relative_layout_id) RelativeLayout iconRelativeLayout;
    @BindView(R.id.category_detail_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context, String id) {
        Intent intent = new Intent(context, CategoryDetailActivity.class);
        intent.putExtra(CATEGORY_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_detail_activity);
        ButterKnife.bind(this);

        groupId = Helpers.getCurrentGroupId();
        String categoryId = getIntent().getStringExtra(CATEGORY_ID);
        category = Category.getCategoryById(categoryId);

        usedColors = Helpers.getUsedColorSet(groupId);
        currentColor = category.getColor();
        colors = EColor.getAllColors();

        usedIcons = Helpers.getUsedIconSet(groupId);
        currentIcon = category.getIcon();
        icons = EIcon.getAllIcons();

        setupToolbar();
        invalidateViews();
    }

    private void invalidateViews() {
        nameEditText.setText(category.getName());

        currentColor = category.getColor();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
        colorImageView.setImageDrawable(colorDrawable);
        previewColorImageView.setImageDrawable(colorDrawable);

        currentIcon = category.getIcon();
        EIcon eIcon = EIcon.instanceFromName(currentIcon);
        if (eIcon != null) {
            iconImageView.setImageResource(eIcon.getValueRes());
            previewIconImageView.setImageResource(eIcon.getValueRes());
        }

        colorImageView.setOnClickListener(v -> selectColor());
        iconRelativeLayout.setOnClickListener(v -> selectIcon());
        deleteButton.setOnClickListener(v -> delete());

        editTextView.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveTextView.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);

        setupEditableViews(isEditable);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.title_activity_category_detail));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
        editTextView.setOnClickListener(v -> setEditMode(true));
        saveTextView.setOnClickListener(v -> save());
    }

    private void selectColor() {
        if (isEditable) {
//            ColorPickerFragment colorPickerFragment = ColorPickerFragment
//                .newInstance(currentColor);
//            colorPickerFragment.setListener(colorPickerListener);
//            colorPickerFragment.show(getSupportFragmentManager(), ColorPickerFragment.class.getSimpleName());

            closeSoftKeyboard();
            showColorSheet();
        }
    }

    private void selectIcon() {
        if (isEditable) {
            closeSoftKeyboard();
            showIconSheet();
        }
    }

    private ColorPickerFragment.ColorPickerListener colorPickerListener = new ColorPickerFragment.ColorPickerListener() {
        @Override
        public void onFinishCategoryColorDialog(String color) {
            usedColors.remove(currentColor);
            usedColors.add(color);
            currentColor = color;
            colorImageView.setBackgroundColor(Color.parseColor(color));
            previewColorImageView.setBackgroundColor(Color.parseColor(color));
        }
    };

    private void setupEditableViews(boolean isEditable) {
        nameEditText.setFocusable(isEditable);
        nameEditText.setFocusableInTouchMode(isEditable);
        nameEditText.setClickable(isEditable);

        if (isEditable) {
            nameEditText.requestFocus();
            nameEditText.setSelection(nameEditText.length());
        }
    }

    private void setEditMode(boolean isEditable) {
        this.isEditable = isEditable;
        invalidateViews();
    }

    private void showColorSheet() {
        ColorPickerSheetAdapter colorPickerSheetAdapter = new ColorPickerSheetAdapter(this, currentColor, colors, usedColors);
        colorPickerSheetAdapter.setOnItemClickListener(onColorClickListener);

        View colorSheetView = getLayoutInflater().inflate(R.layout.color_sheet, null);
        RecyclerView colorRecyclerView = (RecyclerView) colorSheetView.findViewById(recyclerView);
        colorRecyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, COLUMN);
        colorRecyclerView.setLayoutManager(gridLayoutManager);
        colorRecyclerView.setAdapter(colorPickerSheetAdapter);

        colorSheetDialog = new BottomSheetDialog(this);
        colorSheetDialog.setContentView(colorSheetView);
        colorSheetDialog.show();
    }

    private ColorPickerSheetAdapter.OnItemClickListener onColorClickListener = new ColorPickerSheetAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(ColorPickerSheetAdapter.ViewHolder item, int position) {
            colorSheetDialog.dismiss();

//            usedColors.remove(currentColor);
//            usedColors.add(colors.get(position));
//            currentColor = colors.get(position);
//            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(currentColor));
//            colorImageView.setImageDrawable(colorDrawable);

            usedColors.remove(currentColor);
            usedColors.add(colors.get(position));
            currentColor = colors.get(position);
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(colors.get(position)));
            colorImageView.setImageDrawable(colorDrawable);
            previewColorImageView.setImageDrawable(colorDrawable);
        }
    };

    private void showIconSheet() {
        IconPickerSheetAdapter iconPickerSheetAdapter = new IconPickerSheetAdapter(this, currentIcon, icons, usedIcons, currentColor);
        iconPickerSheetAdapter.setOnItemClickListener(onIconClickListener);

        View iconSheetView = getLayoutInflater().inflate(R.layout.icon_sheet, null);
        RecyclerView iconRecyclerView = (RecyclerView) iconSheetView.findViewById(recyclerView);
        iconRecyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, COLUMN);
        iconRecyclerView.setLayoutManager(gridLayoutManager);
        iconRecyclerView.setAdapter(iconPickerSheetAdapter);

        iconSheetDialog = new BottomSheetDialog(this);
        iconSheetDialog.setContentView(iconSheetView);
        iconSheetDialog.show();
    }

    private IconPickerSheetAdapter.OnItemClickListener onIconClickListener = new IconPickerSheetAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(IconPickerSheetAdapter.ViewHolder item, int position) {
            iconSheetDialog.dismiss();

            usedIcons.remove(currentIcon);
            usedIcons.add(icons.get(position));
            currentIcon = icons.get(position);

            if (EIcon.instanceFromName(currentIcon) != null) {
                iconImageView.setImageResource(EIcon.instanceFromName(currentIcon).getValueRes());
                previewIconImageView.setImageResource(EIcon.instanceFromName(currentIcon).getValueRes());
            }
        }
    };

    private Continuation<Void, Void> onUpdateSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in updating category.", task.getError());
            }

            Log.d(TAG, "Update category success.");

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(category);
            realm.commitTransaction();
            realm.close();

            return null;
        }
    };

    private void save() {
        String name = nameEditText.getText().toString();

        if (name.length() == 0) {
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);
        String groupId = sharedPreferences.getString(Group.ID_KEY, null);

        if (loginUserId == null || groupId == null) {
            Log.i(TAG, "Error getting login user id or group id.");
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        category.setName(name);
        category.setColor(currentColor);
        category.setIcon(currentIcon);
        category.setGroupId(groupId);
        realm.copyToRealmOrUpdate(category);
        realm.commitTransaction();
        realm.close();

        SyncCategory.update(category).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);

        progressBar.setVisibility(View.VISIBLE);
        closeSoftKeyboard();
        isEditable = false;
        invalidateViews();
    }

    private Continuation<Void, Void> onDeleteSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in deleting category.", task.getError());
            }

            isDeleteAction = true;
            Log.d(TAG, "Delete category success.");
            close();
            return null;
        }
    };

    public void closeSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (inputMethodManager != null && view != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void delete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_category_title)
                .setMessage(R.string.delete_category_message)
                .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    SyncCategory.delete(category.getId()).continueWith(onDeleteSuccess, Task.UI_THREAD_EXECUTOR);
                })
                .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
                .show();
    }

    private void close() {
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isDeleteAction) {
            Category.delete(category.getId());
        }
    }

    @Override
    public void onBackPressed() {
        close();
    }
}
