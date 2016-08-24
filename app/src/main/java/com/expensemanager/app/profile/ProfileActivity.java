package com.expensemanager.app.profile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 8/22/16.
 */

public class ProfileActivity extends BaseActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private static final String USER_ID = "userId";
    public static final String NEW_PHOTO = "Take a photo";
    public static final String LIBRARY_PHOTO = "Choose from library";
    public static final int SELECT_PICTURE_REQUEST_CODE = 1;

    private boolean isDefault;
    private boolean isPlaceholder;
    private Uri outputFileUri;
    private Drawable cameraIconHolder;
    private AlertDialog.Builder choosePhotoSource;
    private User currentUser;
    private byte[] profileImage;
    private boolean isEditable = false;
    private String userId;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.profile_activity_profile_photo_image_view_id) ImageView profilePhotoImageView;
    @BindView(R.id.profile_activity_fullname_edit_text_id) EditText fullnameEditText;
    @BindView(R.id.profile_activity_cancel_button_id) Button cancelButton;
    @BindView(R.id.profile_activity_edit_button_id) Button editButton;
    @BindView(R.id.profile_activity_save_button_id) Button saveButton;
    @BindView(R.id.profile_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context, String userId) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);

        userId = getIntent().getStringExtra(USER_ID);

        if (userId == null || userId.isEmpty()) {
            userId = loginUserId;
        }

        currentUser = User.getUserById(userId);
        // Convert bitmap to drawable
        cameraIconHolder = new BitmapDrawable(getResources(), getCameraIconBitmap());

        if (currentUser != null) {
            Log.d(TAG, "current user name: " + currentUser.getFullname());
        }

        setupToolbar();

        invalidateViews();

        SyncUser.getLoginUser().continueWith(onResponseReturned, Task.UI_THREAD_EXECUTOR);
    }

    private void invalidateViews() {
        if (currentUser == null) {
            return;
        }

        editButton.setOnClickListener(v -> setEditMode(true));
        cancelButton.setOnClickListener(v -> setEditMode(false));
        saveButton.setOnClickListener(v -> save());

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        cancelButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);

        String photoUrl = currentUser.getPhotoUrl();
        isPlaceholder = photoUrl != null && photoUrl.length() > 0 ? false : true;
        isDefault = true;

        Log.i(TAG, "new invalidate: " + currentUser.getPhotoUrl());
        // todo: glide disallow putting bitmap into imageview
        if (photoUrl != null && photoUrl.length() > 0) {
            Glide.with(this)
                .load(currentUser.getPhotoUrl())
                .placeholder(cameraIconHolder)
                .fitCenter()
                .into(profilePhotoImageView);
        }

        fullnameEditText.setText(currentUser.getFullname());

        setupEditableViews(isEditable);

        fullnameEditText.setOnClickListener(v -> {
            isDefault = false;
        });

        profilePhotoImageView.setOnClickListener(v -> {
            updateProfileImage();
        });
        profilePhotoImageView.setOnLongClickListener(v -> {
            // todo: allow to delete profile image
            //deleteProfileImage();
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data.getData();
                }

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    try {
                        byte[] inputData = Helpers.getBytesFromInputStream(inputStream);
                        Bitmap bitmap = Helpers.decodeSampledBitmapFromByteArray(inputData, 0, 400, 400);
                        int dimension = Helpers.getCenterCropDimensionForBitmap(bitmap);
                        bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        profileImage = stream.toByteArray();

                        bitmap = BitmapFactory.decodeByteArray(profileImage, 0, profileImage.length);
                        profilePhotoImageView.setImageBitmap(bitmap);

                        isPlaceholder = false;
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading image byte data from uri");
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Error file with uri " + selectedImageUri + " not found", e);
                }
            }
        }
    }

    private void updateProfileImage() {
        if (isEditable) {
            openImageIntent();
        }
    }

    private void deleteProfileImage() {
        if (isPlaceholder) {
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_profile_image)
            .setMessage(R.string.delete_profile_image_message)
            .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> onDeleteProfileImage())
            .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
            .show();
    }

    private void onDeleteProfileImage() {
        isDefault = false;
        profilePhotoImageView.setImageDrawable(cameraIconHolder);
        isPlaceholder = true;
    }

    private void setupEditableViews(boolean isEditable) {
        fullnameEditText.setFocusable(isEditable);
        fullnameEditText.setFocusableInTouchMode(isEditable);
        fullnameEditText.setClickable(isEditable);

        if (isEditable) {
            fullnameEditText.requestFocus();
            fullnameEditText.setSelection(fullnameEditText.length());
        }
    }

    private void setEditMode(boolean isEditable) {
        this.isEditable = isEditable;
        invalidateViews();
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        titleTextView.setText(getString(R.string.profile));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }

        return super.onOptionsItemSelected(item);
    }

    private Bitmap getCameraIconBitmap() {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_camera_alt_white_24dp);
        Bitmap originalBitmap = ((BitmapDrawable)drawable).getBitmap();
        Paint paint = new Paint();
        ColorFilter filter = new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.gray), PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(filter);
        Bitmap cameraIconBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(cameraIconBitmap);
        canvas.drawBitmap(originalBitmap, -2, 2, paint);

        return Helpers.padBitmap(cameraIconBitmap, 20, 20);
    }

    private void openImageIntent() {
        // Path to save image
        String directoryName = Helpers.dateToString(new Date(), getString(R.string.photo_date_format_string));
        final File root = new File(
            Environment.getExternalStorageDirectory() + File.separator + directoryName + File.separator);
        root.mkdirs();
        final File sdImageMainDirectory = new File(root, directoryName);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // todo: request runtime permission
        // Take a photo
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCamera = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCamera) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Choose a photo
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.photo_select_source));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
        startActivityForResult(chooserIntent, SELECT_PICTURE_REQUEST_CODE);
    }

    private void save() {
        String name = fullnameEditText.getText().toString();

        // Check name input
        if (name == null || name.length() == 0) {
            Toast.makeText(this, "Invalid name.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProfileBuilder profileBuilder = new ProfileBuilder()
            .setUser(currentUser)
            .setProfileImage(profileImage);

        SyncUser.update(profileBuilder).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);

        progressBar.setVisibility(View.VISIBLE);
        isEditable = !isEditable;
        invalidateViews();
        closeSoftKeyboard();
    }

    private Continuation<JSONObject, Void> onUpdateSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in updating expense.", task.getError());
            }

            Log.d(TAG, "Update current user success.");
            // todo: fix problem: not able to update image immediately
            // Update user object
            SyncUser.getLoginUser().continueWith(onResponseReturned, Task.UI_THREAD_EXECUTOR);

            return null;
        }
    };

    private Continuation<Void, Void> onResponseReturned = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, task.getError().toString());
                return null;
            }
            // Update profile page
            invalidateViews();
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
}
