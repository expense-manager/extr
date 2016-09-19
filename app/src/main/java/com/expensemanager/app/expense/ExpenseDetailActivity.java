package com.expensemanager.app.expense;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.category_picker.CategoryPickerFragment;
import com.expensemanager.app.expense.photo.ExpensePhotoAdapter;
import com.expensemanager.app.expense.photo.ExpensePhotoFragment;
import com.expensemanager.app.helpers.DatePickerFragment;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.helpers.PhotoSourceAdapter;
import com.expensemanager.app.helpers.TimePickerFragment;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.EAction;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.ExpensePhoto;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.PhotoSource;
import com.expensemanager.app.models.User;
import com.expensemanager.app.profile.ProfileActivity;
import com.expensemanager.app.service.Constant;
import com.expensemanager.app.service.ExpenseBuilder;
import com.expensemanager.app.service.PermissionsManager;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.SyncPhoto;
import com.expensemanager.app.service.enums.EIcon;
import com.jakewharton.rxbinding.widget.RxAdapterView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ExpenseDetailActivity extends BaseActivity {
    private static final String TAG = ExpenseDetailActivity.class.getSimpleName();

    public static final String DATE_PICKER = "date_picker";
    public static final String TIME_PICKER = "time_picker";
    private static final String EXPENSE_ID = "EXPENSE_ID";
    private static final int PROGRESS_BAR_DISPLAY_LENGTH = 6000;

    private Handler handler;
    private ArrayList<byte[]> photoList;
    private ExpensePhotoAdapter newExpensePhotoAdapter;
    private Uri outputFileUri;
    private String photoFileName;
    private AlertDialog.Builder choosePhotoSource;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;
    private String expenseId;
    private Expense expense;
    private Category category;
    private Calendar calendar;
    private boolean isEditable = false;
    private String groupId;
    private String loginUserId;
    private Group group;
    private User createdBy;
    private boolean isDeleteAction = false;

    private ArrayList<ExpensePhoto> expensePhotos;
    private ExpensePhotoAdapter expensePhotoAdapter;
    private BottomSheetDialog bottomSheetDialog;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_extra_image_view_id) ImageView extraImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.expense_detail_activity_user_info_relative_layout_id) RelativeLayout userInfoRelativeLayout;
    @BindView(R.id.expense_detail_activity_user_photo_image_view_id) ImageView userPhotoImageView;
    @BindView(R.id.expense_detail_activity_fullname_text_view_id) TextView fullNameTextView;
    @BindView(R.id.expense_detail_activity_email_text_view_id) TextView emailTextView;
    @BindView(R.id.expense_detail_activity_amount_text_view_id) EditText amountTextView;
    @BindView(R.id.expense_detail_activity_note_text_view_id) EditText noteTextView;
    @BindView(R.id.expense_detail_activity_grid_view_id) GridView photoGridView;
    @BindView(R.id.expense_detail_activity_new_photo_grid_view_id) GridView newPhotoGridView;
    @BindView(R.id.expense_detail_activity_delete_button_id) Button deleteButton;
    @BindView(R.id.expense_detail_activity_progress_bar_id) ProgressBar progressBar;
    @BindView(R.id.expense_detail_activity_category_hint_text_view_id) TextView categoryHintTextView;
    @BindView(R.id.expense_detail_activity_category_relative_layout_id) RelativeLayout categoryRelativeLayout;
    @BindView(R.id.expense_detail_activity_category_color_image_view_id) CircleImageView categoryColorImageView;
    @BindView(R.id.expense_detail_activity_icon_image_view_id) ImageView iconImageView;
    @BindView(R.id.expense_detail_activity_category_name_text_view_id) TextView categoryNameTextView;
    @BindView(R.id.expense_detail_activity_category_amount_text_view_id) TextView categoryAmountTextView;
    @BindView(R.id.expense_detail_activity_expense_date_text_view_id) TextView expenseDateTextView;
    @BindView(R.id.expense_detail_activity_expense_time_text_view_id) TextView expenseTimeTextView;

    public static void newInstance(Context context, String id) {
        Intent intent = new Intent(context, ExpenseDetailActivity.class);
        intent.putExtra(EXPENSE_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_detail_activity);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);
        loginUserId = sharedPreferences.getString(User.USER_ID, null);

        group = Group.getGroupById(groupId);

        expenseId = getIntent().getStringExtra(EXPENSE_ID);
        expense = Expense.getExpenseById(expenseId);
        if (expense != null) {
            createdBy = User.getUserById(expense.getUserId());
            category = Category.getCategoryById(expense.getCategoryId());
        }

        setupToolbar();
        setupDateAndTime();
        setupNewPhoto();

        SyncExpense.getExpensePhotoByExpenseId(expenseId, true).continueWith(onGetExpensePhotoSuccess, Task.UI_THREAD_EXECUTOR);
    }

    private void invalidateViews() {
        Log.d(TAG, "invalidateViews()");

        if (expense == null) {
            return;
        }

        photoGridView.setFocusable(false);
        newPhotoGridView.setFocusable(false);

        amountTextView.setText(String.valueOf(expense.getAmount()));
        setupCategory();
        noteTextView.setText(String.valueOf(expense.getNote()));

        if (createdBy != null && Member.getAllAcceptedMembersByGroupId(groupId).size() > 1) {
            userInfoRelativeLayout.setVisibility(View.VISIBLE);

            Helpers.loadProfilePhoto(userPhotoImageView, createdBy.getPhotoUrl());
            fullNameTextView.setText(createdBy.getFullname());
            emailTextView.setText(createdBy.getEmail());
            userPhotoImageView.setOnClickListener(v -> ProfileActivity.newInstance(this, createdBy.getId()));
        } else {
            userInfoRelativeLayout.setVisibility(View.GONE);
        }

        deleteButton.setOnClickListener(v -> delete());

        if (loginUserId.equals(expense.getUserId()) || loginUserId.equals(group.getUserId())) {
            editTextView.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        } else {
            editTextView.setVisibility(View.GONE);
        }

        saveTextView.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        // If set GONE, newPhotoGridView would not show up after click edit again
        newPhotoGridView.setVisibility(isEditable ? View.VISIBLE : View.INVISIBLE);

        setupExpensePhoto();
        setupEditableViews(isEditable);
    }

    private void showActionSheet() {
        ArrayList<EAction> actionsList = new ArrayList<>();
        actionsList.add(new EAction(R.string.edit, R.mipmap.ic_launcher));
        actionsList.add(new EAction(R.string.save, R.mipmap.ic_launcher));
        actionsList.add(new EAction(R.string.add, R.mipmap.ic_launcher));
        actionsList.add(new EAction(R.string.delete, R.mipmap.ic_launcher));

        ActionSheetAdapter adapter = new ActionSheetAdapter(actionsList);
        adapter.setOnItemClickListener(new ActionSheetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ActionSheetAdapter.ItemHolder item, int position) {
                Log.d(TAG, "clicked position:" + position);
                bottomSheetDialog.dismiss();
            }
        });

        View view = getLayoutInflater().inflate(R.layout.action_sheet, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void setupDateAndTime() {
        calendar = Calendar.getInstance();
        calendar.setTime(expense.getExpenseDate());
        formatDateAndTime(calendar.getTime());
        expenseDateTextView.setOnClickListener(v -> {
            if (isEditable) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerFragment datePickerFragment = DatePickerFragment
                    .newInstance(year, month, day);
                datePickerFragment.setListener(onDateSetListener);
                datePickerFragment.show(getSupportFragmentManager(), DATE_PICKER);
            }
        });

        expenseTimeTextView.setOnClickListener(v -> {
            if (isEditable) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerFragment timePickerFragment = TimePickerFragment
                    .newInstance(hour, minute);
                timePickerFragment.setListener(onTimeSetListener);
                timePickerFragment.show(getSupportFragmentManager(), TIME_PICKER);
            }
        });
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(year, monthOfYear, dayOfMonth);
            formatDateAndTime(calendar.getTime());
        }
    };

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            formatDateAndTime(calendar.getTime());
        }
    };

    private void formatDateAndTime(Date date) {
        // Create format
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        // Parse date and set text
        expenseDateTextView.setText(dateFormat.format(date));
        expenseTimeTextView.setText(timeFormat.format(date));
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        titleTextView.setText(getString(R.string.title_activity_expense_detail));
        backImageView.setOnClickListener(v -> close());
        editTextView.setOnClickListener(v -> {
            setEditMode(true);
//            showActionSheet(); // Example of bottom sheet
        });
        saveTextView.setOnClickListener(v -> save());
    }

    private void setupExpensePhoto() {
        RealmResults<ExpensePhoto> photos = ExpensePhoto.getExpensePhotoByExpenseId(expense.getId());
        Log.d(TAG, "setupExpensePhoto: " + photos.size());

        if (photos.isEmpty()) {
            return;
        }

        expensePhotos = new ArrayList<>();
        expensePhotos.addAll(photos);
        expensePhotoAdapter = new ExpensePhotoAdapter(this, null, expensePhotos);
        photoGridView.setAdapter(expensePhotoAdapter);

        RxAdapterView.itemClicks(photoGridView)
                .throttleLast(300, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.d(TAG, "Photo clicked at position: " + integer);
                        ExpensePhotoFragment expensePhotoFragment = ExpensePhotoFragment
                                .newInstance(expense.getId(), integer);
                        expensePhotoFragment.show(getSupportFragmentManager(), ExpensePhotoFragment.class.getSimpleName());
                    }
                });

        photoGridView.setOnItemLongClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            Log.d(TAG, "Photo long clicked at position: " + position);

            if (isEditable && expensePhotos != null && expensePhotos.size() > position) {
                ExpensePhoto expensePhoto = expensePhotos.get(position);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_photo_title)
                        .setMessage(R.string.delete_photo_message)
                        .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
                            SyncPhoto.deleteExpensePhoto(expensePhoto.getId(), expensePhoto.getFileName());
                            ExpensePhoto.delete(expensePhoto.getExpenseId(), expensePhoto.getFileName());
                            expensePhotos.remove(position);
                            expensePhotoAdapter.notifyDataSetChanged();
                        })
                        .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
                        .show();
            }

            return true;
        });
    }

    private void setupCategory() {
        loadCategory(category);

        categoryHintTextView.setOnClickListener(v -> {
            selectCategory();
        });
        categoryRelativeLayout.setOnClickListener(v -> {
            selectCategory();
        });
    }

    private void selectCategory() {
        if (isEditable) {
            CategoryPickerFragment categoryPickerFragment = CategoryPickerFragment
                .newInstance();
            categoryPickerFragment.setListener(categoryPickerListener);
            categoryPickerFragment.show(getSupportFragmentManager(), CategoryPickerFragment.class.getSimpleName());
        }
    }

    private CategoryPickerFragment.CategoryPickerListener categoryPickerListener = new CategoryPickerFragment.CategoryPickerListener() {
        @Override
        public void onFinishExpenseCategoryDialog(Category category) {
            loadCategory(category);
        }
    };

    private void loadCategory(Category category) {
        if (category == null) {
            // Show category hint
            categoryHintTextView.setVisibility(View.VISIBLE);
            categoryRelativeLayout.setVisibility(View.INVISIBLE);
        } else {
            // Hide category hint
            categoryHintTextView.setVisibility(View.INVISIBLE);
            categoryRelativeLayout.setVisibility(View.VISIBLE);

            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
            categoryColorImageView.setImageDrawable(colorDrawable);
            categoryNameTextView.setText(category.getName());

            EIcon eIcon = EIcon.instanceFromName(category.getIcon());
            if (eIcon != null) {
                iconImageView.setImageResource(eIcon.getValueRes());
                iconImageView.setVisibility(View.VISIBLE);
            } else {
                iconImageView.setVisibility(View.INVISIBLE);
            }
        }
        // Update category
        this.category = category;
    }

    private void setupEditableViews(boolean isEditable) {
        amountTextView.setFocusable(isEditable);
        amountTextView.setFocusableInTouchMode(isEditable);
        amountTextView.setClickable(isEditable);

        noteTextView.setFocusable(isEditable);
        noteTextView.setFocusableInTouchMode(isEditable);
        noteTextView.setClickable(isEditable);

        if (isEditable) {
            amountTextView.requestFocus();
            amountTextView.setSelection(amountTextView.length());
        }
    }

    private void setEditMode(boolean isEditable) {
        this.isEditable = isEditable;
        invalidateViews();
    }

    private Continuation<JSONObject, Void> onGetExpensePhotoSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            Log.d(TAG, "onGetExpensePhotoSuccess");
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in getting expense photo.", task.getError());
            }

            Log.d(TAG, "onGetExpensePhotoSuccess Expense photos: " + expense.getPhotos());
            invalidateViews();

            return null;
        }
    };

    private Continuation<Void, Void> onUpdateSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            Log.d(TAG, "onUpdateSuccess");

            if (photoList.size() == 0) {
                progressBar.setVisibility(View.GONE);
            }

            if (task.isFaulted()) {
                Log.e(TAG, "Error in updating expense.", task.getError());
            }

            Log.d(TAG, "Update expense success.");

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            expense.setSynced(true);
            realm.copyToRealmOrUpdate(expense);
            realm.commitTransaction();
            realm.close();

            return null;
        }
    };

    private void save() {
        double amount;
        try {
            amount = Double.valueOf(amountTextView.getText().toString());
            amount = Helpers.formatNumToDouble(amount);
            if (amount <= 0) {
                Toast.makeText(this, "Amount cannot be zero.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot convert amount to double.", e);
            Toast.makeText(this, "Incorrect amount format.", Toast.LENGTH_SHORT).show();
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
        expense.setAmount(amount);
        expense.setNote(noteTextView.getText().toString());
        expense.setCategoryId(category != null ? category.getId() : null);
        expense.setGroupId(groupId);
        expense.setExpenseDate(calendar.getTime());
        expense.setSynced(false);
        realm.copyToRealmOrUpdate(expense);
        realm.commitTransaction();
        realm.close();

        ExpenseBuilder expenseBuilder = new ExpenseBuilder();
        expenseBuilder.setExpense(expense);

        if (photoList.size() >= 2) {
            photoList.remove(photoList.size() - 1);
            expenseBuilder.setPhotoList(photoList);
        }

        progressBar.setVisibility(View.VISIBLE);
        SyncExpense.update(expenseBuilder).continueWith(onUpdateSuccess, Task.UI_THREAD_EXECUTOR);

        isEditable = false;
        closeSoftKeyboard();
        invalidateViews();
    }

    private void setupNewPhoto() {
        // Photo
        photoList = new ArrayList<>();
        Bitmap cameraIconBitmap = getCameraIconBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        cameraIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] sampledInputData = stream.toByteArray();
        photoList.add(sampledInputData);
        newExpensePhotoAdapter = new ExpensePhotoAdapter(this, photoList, null);
        newPhotoGridView.setAdapter(newExpensePhotoAdapter);
        newPhotoGridView.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            if (position == photoList.size() - 1) {
                setPhotoSourcePicker();
            }
        });

        newPhotoGridView.setOnItemLongClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            if (position == photoList.size() - 1) {
                return false;
            }
            photoList.remove(position);
            newExpensePhotoAdapter.notifyDataSetChanged();
            return true;
        });
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
                    Log.d(TAG, "Choose photo from library");
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
            startActivityForResult(intent, Constant.TAKE_PHOTO_CODE);
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
                            byte[] cameraBytes = photoList.get(photoList.size() - 1);
                            photoList.remove(photoList.size() - 1);
                            photoList.add(sampledInputData);
                            photoList.add(cameraBytes);
                            Log.d(TAG, "photoList size: " + photoList.size());
                            newExpensePhotoAdapter.notifyDataSetChanged();
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

    private Continuation<Void, Void> onDeleteSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in deleting expense.", task.getError());
            }

            isDeleteAction = true;
            Log.d(TAG, "Delete expense success.");
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
                .setTitle(R.string.delete_expense_title)
                .setMessage(R.string.delete_expense_message)
                .setPositiveButton(R.string.delete, (DialogInterface dialog, int which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    SyncExpense.delete(expense.getId()).continueWith(onDeleteSuccess, Task.UI_THREAD_EXECUTOR);
                })
                .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void close() {
        removeHandler();
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isDeleteAction) {
            Expense.delete(expense.getId());
        }
    }
    @Override
    public void onBackPressed() {
        close();
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> {
            Log.d(TAG, "RealmChangeListener");

            progressBar.setVisibility(View.GONE);
            invalidateViews();
        });

        invalidateViews();
        handler = new Handler();
        handler.postDelayed(progressBarRunnable, PROGRESS_BAR_DISPLAY_LENGTH);
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();

        removeHandler();
    }

    private void removeHandler() {
        if (handler != null) {
            handler.removeCallbacks(progressBarRunnable);
            handler = null;
        }
    }

    private Runnable progressBarRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "progressBarRunnable");
            progressBar.setVisibility(View.GONE);
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
}
