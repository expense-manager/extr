package com.expensemanager.app.category;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.category.color_picker.ColorPickerSheetAdapter;
import com.expensemanager.app.category.icon_picker.IconPickerSheetAdapter;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.enums.EColor;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.enums.EIcon;

import org.json.JSONObject;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

import static com.expensemanager.app.R.id.recyclerView;

public class NewCategoryActivity extends AppCompatActivity {
    private static final String TAG = NewCategoryActivity.class.getSimpleName();

    private static final int COLUMN = 6;

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

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.new_category_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.new_category_activity_color_image_view_id) CircleImageView colorImageView;
    @BindView(R.id.new_category_activity_icon_image_view_id) ImageView iconImageView;
    @BindView(R.id.new_category_activity_preview_color_image_view_id) CircleImageView previewColorImageView;
    @BindView(R.id.new_category_activity_preview_icon_image_view_id) ImageView previewIconImageView;
    @BindView(R.id.new_category_activity_icon_relative_layout_id) RelativeLayout iconRelativeLayout;
    @BindView(R.id.progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NewCategoryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_category_activity);
        ButterKnife.bind(this);

        setupToolbar();

        category = new Category();
        groupId = Helpers.getCurrentGroupId();

        // Get used color set
        usedColors = Helpers.getUsedColorSet(groupId);
        // Get a random unused color
        currentColor = Helpers.getRandomColor(usedColors);
        colors = EColor.getAllColors();

        usedIcons = Helpers.getUsedIconSet(groupId);
        currentIcon = Helpers.getRandomIcon(usedIcons);
        icons = EIcon.getAllIcons();

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(currentColor));
        colorImageView.setImageDrawable(colorDrawable);
        previewColorImageView.setImageDrawable(colorDrawable);

        EIcon eIcon = EIcon.instanceFromName(currentIcon);
        if (eIcon != null) {
            iconImageView.setImageResource(eIcon.getValueRes());
            previewIconImageView.setImageResource(eIcon.getValueRes());
        }

        colorImageView.setOnClickListener(v -> selectColor());
        iconRelativeLayout.setOnClickListener(v -> selectIcon());
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        titleTextView.setText(getString(R.string.title_activity_new_category));
        saveTextView.setVisibility(View.VISIBLE);
        backImageView.setImageResource(R.drawable.ic_window_close);
        saveTextView.setText(R.string.create);

        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
        saveTextView.setOnClickListener(v -> save());
    }

    private void selectColor() {
        closeSoftKeyboard();
        showColorSheet();
    }

    private void selectIcon() {
        closeSoftKeyboard();
        showIconSheet();
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

            usedColors.remove(currentColor);
            usedColors.add(colors.get(position));
            currentColor = colors.get(position);
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(currentColor));
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

            EIcon eIcon = EIcon.instanceFromName(currentIcon);
            if (eIcon != null) {
                iconImageView.setImageResource(eIcon.getValueRes());
                previewIconImageView.setImageResource(eIcon.getValueRes());
            }
        }
    };

    private void save() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);
        String groupId = sharedPreferences.getString(Group.ID_KEY, null);
        if (loginUserId == null || groupId == null) {
            Log.i(TAG, "Error getting login user id or group id.");
            return;
        }
        category.setUserId(loginUserId);
        category.setGroupId(groupId);

        String uuid = UUID.randomUUID().toString();
        category.setId(uuid);
        category.setName(nameEditText.getText().toString());
        category.setColor(currentColor);
        category.setIcon(currentIcon);

        progressBar.setVisibility(View.VISIBLE);
        SyncCategory.create(category).continueWith(onCreateSuccess, Task.UI_THREAD_EXECUTOR);
        closeSoftKeyboard();
    }

    private Continuation<JSONObject, Void> onCreateSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in creating new category.", task.getError());
            }

            JSONObject result = task.getResult();
            String categoryId = result.optString(Category.OBJECT_ID_JSON_KEY);

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            category.setId(categoryId);
            realm.copyToRealmOrUpdate(category);
            realm.commitTransaction();
            realm.close();

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

    private void close() {
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        close();
    }
}
