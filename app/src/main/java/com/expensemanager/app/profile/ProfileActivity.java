package com.expensemanager.app.profile;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.helpers.PhotoSourceAdapter;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.PhotoSource;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.Constant;
import com.expensemanager.app.service.PermissionsManager;
import com.expensemanager.app.service.ProfileBuilder;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.expensemanager.app.service.Constant.TAKE_PHOTO_CODE;

public class ProfileActivity extends BaseActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private static final String USER_ID = "userId";
    private static final String IS_EDITABLE = "isEditable";

    private boolean isPlaceholder;
    private ArrayList<byte[]> photoList;
    private Uri outputFileUri;
    private String photoFileName;
    private AlertDialog.Builder choosePhotoSource;
    private Drawable cameraIconHolder;
    private User currentUser;
    private byte[] profileImage;
    private boolean isEditable = false;
    private String userId;
    private String loginUserId;
    private long syncTimeInMillis;
    private String syncTimeKey;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_right_title_text_view_id) TextView editTextView;
    @BindView(R.id.profile_activity_profile_photo_image_view_id) ImageView profilePhotoImageView;
    @BindView(R.id.profile_activity_change_photo_text_view_id) TextView changePhotoTextView;
    @BindView(R.id.profile_activity_first_name_edit_text_id) EditText firstNameEditText;
    @BindView(R.id.profile_activity_last_name_edit_text_id) EditText lastNameEditText;
    @BindView(R.id.profile_activity_email_edit_text_id) EditText emailEditText;
    @BindView(R.id.profile_activity_mobile_edit_text_id) EditText mobileEditText;
    @BindView(R.id.profile_activity_progress_bar_id) ProgressBar progressBar;
    @BindView(R.id.swipeContainer_id) SwipeRefreshLayout swipeContainer;

    public static void newInstance(Context context, String userId) {
        ProfileActivity.newInstance(context, userId, false);
    }

    public static void newInstance(Context context, String userId, boolean isEditable) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(IS_EDITABLE, isEditable);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ButterKnife.bind(this);

        // todo: handle redirect back from email
        String uriPath = getIntent().getDataString();
        Log.d(TAG, "token: uriPath=" + uriPath);

        String groupId = Helpers.getCurrentGroupId();
        loginUserId = Helpers.getLoginUserId();
        syncTimeKey = Helpers.getSyncTimeKey(TAG, groupId);
        syncTimeInMillis = Helpers.getSyncTimeInMillis(syncTimeKey);

        userId = getIntent().getStringExtra(USER_ID);
        isEditable = getIntent().getBooleanExtra(IS_EDITABLE, false);

        if (userId == null || userId.isEmpty()) {
            userId = loginUserId;
        }

        if (isEditable && !userId.equals(loginUserId)) {
            isEditable = false;
        }

        currentUser = User.getUserById(userId);
        photoList = new ArrayList<>();
        cameraIconHolder = new BitmapDrawable(getResources(), Helpers.getCameraIconBitmap(this));

        if (currentUser != null) {
            Log.d(TAG, "current user name: " + currentUser.getFullname());
        }

        setupToolbar();
        setupSwipeToRefresh();
        setupEditableViews(isEditable);

    }

    private void invalidateViews() {
        if (currentUser == null) {
            return;
        }

        if (isEditable) {
            editTextView.setText(getString(R.string.save));
            editTextView.setOnClickListener(v -> save());
            titleTextView.setText(getString(R.string.cancel));
            titleTextView.setOnClickListener(v -> setEditMode(false));
            changePhotoTextView.setVisibility(View.VISIBLE);
        } else {
            editTextView.setText(getString(R.string.edit));
            editTextView.setOnClickListener(v -> setEditMode(true));
            titleTextView.setText(getString(R.string.profile));
            titleTextView.setOnClickListener(v -> close());
            changePhotoTextView.setVisibility(View.INVISIBLE);
        }

        String photoUrl = currentUser.getPhotoUrl();
        isPlaceholder = !(photoUrl != null && photoUrl.length() > 0);

        Log.i(TAG, "new invalidate: " + currentUser.getPhotoUrl());
        // todo: glide disallow putting bitmap into imageview

        if (photoList.size() > 0) {
            Helpers.loadProfilePhoto(profilePhotoImageView, Arrays.toString(photoList.get(0)));
        } else {
            Helpers.loadProfilePhoto(profilePhotoImageView, currentUser.getPhotoUrl());
        }

        profilePhotoImageView.setOnClickListener(v -> {
            updateProfileImage();
        });

        changePhotoTextView.setOnClickListener(v -> updateProfileImage());

        profilePhotoImageView.setOnLongClickListener(v -> {
            // todo: allow to delete profile image
            //deleteProfileImage();
            return true;
        });

        setupEditableViews(isEditable);

        firstNameEditText.setText(currentUser.getFirstName());
        lastNameEditText.setText(currentUser.getLastName());
        emailEditText.setText(currentUser.getEmail());
        mobileEditText.setText(currentUser.getPhone());

        firstNameEditText.setOnClickListener(v -> requestFocus(v));
        lastNameEditText.setOnClickListener(v -> requestFocus(v));
        emailEditText.setOnClickListener(v -> requestFocus(v));
        mobileEditText.setOnClickListener(v -> requestFocus(v));

        // todo: update user by id
        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncUser.getLoginUser().continueWith(onResponseReturned, Task.UI_THREAD_EXECUTOR);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(this, syncTimeKey, syncTimeInMillis);
        }
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        titleTextView.setText(getString(R.string.profile));
        editTextView.setText(getString(R.string.edit));
        if (loginUserId.equals(userId)) {
            editTextView.setVisibility(View.VISIBLE);
        } else {
            editTextView.setVisibility(View.INVISIBLE);
        }
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    private void setupSwipeToRefresh() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncUser.getLoginUser().continueWith(onResponseReturned, Task.UI_THREAD_EXECUTOR);
            }
        });

        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    private void setPhotoSourcePicker() {
        choosePhotoSource = new AlertDialog.Builder(this);

        PhotoSourceAdapter photoSourceAdapter = Helpers.getPhotoSourceAdapter(this);

        choosePhotoSource.setAdapter(photoSourceAdapter, (DialogInterface dialog, int which) -> {
            PhotoSource photoSource = photoSourceAdapter.getItem(which);
            if (photoSource == null) {
                checkCameraPermission();
                return;
            }

            switch (photoSource.getTitle()) {
                case Constant.TAKE_PHOTO:
                    Log.d(TAG, "Take a photo");
                    checkCameraPermission();
                    break;
                case Constant.PICK_PHOTO:
                    Log.d(TAG, "Choose from library");
                    checkExternalStoragePermission();
                    break;
            }
        });

        choosePhotoSource.show();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFileName = Helpers.dateToString(new Date(), getString(R.string.photo_date_format_string));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        }
    }

    private void openGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Constant.PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.TAKE_PHOTO_CODE) {
                Log.d(TAG, "TAKE_PHOTO_CODE");

                outputFileUri = getPhotoFileUri(photoFileName);
                cropPhoto(outputFileUri);
            } else if (requestCode == Constant.PICK_PHOTO_CODE) {
                Log.d(TAG, "PICK_PHOTO_CODE");

                Uri photoUri = data.getData();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(photoUri);
                    byte[] inputData = Helpers.getBytesFromInputStream(inputStream);

                    String directoryName = Helpers.dateToString(new Date(), getString(R.string.photo_date_format_string));
                    final File outputFileRoot = new File(Environment.getExternalStorageDirectory() + File.separator + directoryName + File.separator);
                    outputFileRoot.mkdirs();
                    final File outputFile = new File(outputFileRoot, directoryName);
                    outputFileUri = Uri.fromFile(outputFileRoot);

                    OutputStream out;
                    out = new FileOutputStream(outputFile);
                    out.write(inputData);
                    out.close();

                    outputFileUri = Uri.fromFile(outputFile);
                    cropPhoto(outputFileUri);

                } catch (IOException e) {
                    Log.e(TAG, "Error in getting photo data.", e);
                }
            } else if (requestCode == Constant.CROP_PHOTO_CODE) {
                Log.d(TAG, "CROP_PHOTO_CODE");

                Uri photoUri = data.getData();
                if (photoUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(photoUri);
                        try {
                            byte[] inputData = Helpers.getBytesFromInputStream(inputStream);
                            Bitmap bitmap = Helpers.decodeSampledBitmapFromByteArray(inputData, 0, 400, 400);
                            int dimension = Helpers.getCenterCropDimensionForBitmap(bitmap);
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] sampledInputData = stream.toByteArray();
                            photoList.clear();
                            photoList.add(sampledInputData);
                            invalidateViews();
                            Log.d(TAG, "photoList size: " + photoList.size());
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading image byte data from uri");
                        }
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "Error file with uri " + photoUri + " not found", e);
                    }
                } else {
                    Toast.makeText(this, "Cannot get cropped photo data!", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == Constant.POST_PHOTO_CODE) {
                Log.d(TAG, "POST_PHOTO_CODE");
            }
        }
    }

    private void cropPhoto(Uri photoUri) {
        //call the standard crop action intent (the user device may not support it)
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri
        cropIntent.setDataAndType(photoUri, "image/*");

        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 400);
        cropIntent.putExtra("outputY", 400);
        //retrieve data on return
        cropIntent.putExtra("return-data", true);

        //start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, Constant.CROP_PHOTO_CODE);
    }

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        Log.d(TAG, "getPhotoFileUri: isExternalStorageAvailable() : " + isExternalStorageAvailable());
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(
                    this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    /**
     * Returns true if external storage for photos is available
     */
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }
    private void updateProfileImage() {
        if (isEditable) {
            setPhotoSourcePicker();
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
        profilePhotoImageView.setImageDrawable(cameraIconHolder);
        isPlaceholder = true;
    }

    private void setupEditableViews(boolean isEditable) {
        firstNameEditText.setFocusable(isEditable);
        firstNameEditText.setFocusableInTouchMode(isEditable);
        firstNameEditText.setClickable(isEditable);

        lastNameEditText.setFocusable(isEditable);
        lastNameEditText.setFocusableInTouchMode(isEditable);
        lastNameEditText.setClickable(isEditable);

        emailEditText.setFocusable(isEditable);
        emailEditText.setFocusableInTouchMode(isEditable);
        emailEditText.setClickable(isEditable);

        mobileEditText.setFocusable(isEditable);
        mobileEditText.setFocusableInTouchMode(isEditable);
        mobileEditText.setClickable(isEditable);
    }

    private void requestFocus(View v) {
        EditText editText = (EditText)v;
        editText.requestFocus();
        editText.setSelection(editText.length());
    }

    private void setEditMode(boolean isEditable) {
        this.isEditable = isEditable;
        invalidateViews();
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

    private void save() {
        String email = emailEditText.getText().toString().trim();
        String phone = mobileEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();

        // todo: validate username, email in server
        if (!Helpers.isValidEmail(email)) {
            Toast.makeText(this, "Invalid email.", Toast.LENGTH_SHORT).show();
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        ProfileBuilder profileBuilder = new ProfileBuilder();
        profileBuilder.setUser(currentUser);
        profileBuilder.setPhotoList(photoList);

        realm.copyToRealmOrUpdate(currentUser);
        realm.commitTransaction();
        realm.close();

        SyncUser.update(profileBuilder).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);
        progressBar.setVisibility(View.VISIBLE);
        changePhotoTextView.setVisibility(View.GONE);
        isEditable = !isEditable;
        invalidateViews();
        Helpers.closeSoftKeyboard(this);
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

            if (swipeContainer != null) {
                swipeContainer.setRefreshing(false);
            }
            // Update profile page
            invalidateViews();
            return null;
        }
    };

    private void checkCameraPermission() {
        PermissionsManager.verifyCameraPermissionGranted(this, (boolean isGranted) -> {
            if (isGranted) {
                launchCamera();
            } else {
                Log.d(TAG, "Permission is not granted.");
            }
        });
    }

    private void checkExternalStoragePermission() {
        PermissionsManager.verifyExternalStoragePermissionGranted(this, (boolean isGranted) -> {
            if (isGranted) {
                openGallery();
            } else {
                Log.d(TAG, "Permission is not granted.");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> {
            Log.d(TAG, "RealmChangeListener");
            invalidateViews();
        });

        invalidateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
