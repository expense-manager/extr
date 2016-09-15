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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
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
import com.expensemanager.app.helpers.DatePickerFragment;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.helpers.PhotoSourceAdapter;
import com.expensemanager.app.helpers.TimePickerFragment;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.PhotoSource;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.Constant;
import com.expensemanager.app.service.ExpenseBuilder;
import com.expensemanager.app.service.PermissionsManager;
import com.expensemanager.app.service.SyncExpense;

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

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.expensemanager.app.service.Constant.CROP_PHOTO_CODE;
import static com.expensemanager.app.service.Constant.PICK_PHOTO_CODE;
import static com.expensemanager.app.service.Constant.TAKE_PHOTO_CODE;

public class NewExpenseActivity extends BaseActivity {
    public static final String TAG = NewExpenseActivity.class.getSimpleName();

    public static final String DATE_PICKER = "date_picker";
    public static final String TIME_PICKER = "time_picker";

    private ArrayList<byte[]> photoList;
    private ExpensePhotoAdapter expensePhotoAdapter;
    private Uri outputFileUri;
    private String photoFileName;
    private AlertDialog.Builder choosePhotoSource;
    private Calendar calendar;
    private Expense expense;
    private Category category;
    private Runnable pendingRunnable;
    private Handler handler;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.new_expense_activity_amount_text_view_id) TextView amountTextView;
    @BindView(R.id.new_expense_activity_note_text_view_id) TextView noteTextView;
    @BindView(R.id.new_expense_activity_grid_view_id) GridView photoGridView;
    @BindView(R.id.progress_bar_id) ProgressBar progressBar;
    @BindView(R.id.new_expense_activity_category_hint_text_view_id) TextView categoryHintTextView;
    @BindView(R.id.new_expense_activity_category_relative_layout_id) RelativeLayout categoryRelativeLayout;
    @BindView(R.id.new_expense_activity_category_color_image_view_id) CircleImageView categoryColorImageView;
    @BindView(R.id.new_expense_activity_category_name_text_view_id) TextView categoryNameTextView;
    @BindView(R.id.new_expense_activity_expense_date_text_view_id) TextView expenseDateTextView;
    @BindView(R.id.new_expense_activity_expense_time_text_view_id) TextView expenseTimeTextView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NewExpenseActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_expense_activity);
        ButterKnife.bind(this);

        handler = new Handler();
        expense = new Expense();
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
        setupToolbar();
        setupCategory();
        setupDateAndTime();
        setupPhoto();
    }

    private void setupDateAndTime() {
        calendar = Calendar.getInstance();
        formatDateAndTime(calendar.getTime());
        expenseDateTextView.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(year, month, day);
            datePickerFragment.setListener(onDateSetListener);
            datePickerFragment.show(getSupportFragmentManager(), DATE_PICKER);
        });

        expenseTimeTextView.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerFragment timePickerFragment = TimePickerFragment.newInstance(hour, minute);
            timePickerFragment.setListener(onTimeSetListener);
            timePickerFragment.show(getSupportFragmentManager(), TIME_PICKER);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        // Parse date and set text
        expenseDateTextView.setText(dateFormat.format(date));
        expenseTimeTextView.setText(timeFormat.format(date));
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
        CategoryPickerFragment categoryPickerFragment = CategoryPickerFragment.newInstance();
        categoryPickerFragment.setListener(categoryPickerListener);
        categoryPickerFragment.show(getSupportFragmentManager(), CategoryPickerFragment.class.getSimpleName());
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
        }
        // Update category
        this.category = category;
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.title_activity_new_expense));
        saveTextView.setVisibility(View.VISIBLE);
        backImageView.setImageResource(R.drawable.ic_window_close);
        saveTextView.setText(R.string.create);

        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
        saveTextView.setOnClickListener(v -> save());
    }

    private void setupPhoto() {
        // Photo
        photoList = new ArrayList<>();
        Bitmap cameraIconBitmap = getCameraIconBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        cameraIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] sampledInputData = stream.toByteArray();
        photoList.add(sampledInputData);
        expensePhotoAdapter = new ExpensePhotoAdapter(this, photoList, null);
        photoGridView.setAdapter(expensePhotoAdapter);
        photoGridView.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            if (position == photoList.size() - 1) {
                setPhotoSourcePicker();
            }
        });

        photoGridView.setOnItemLongClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            if (position == photoList.size() - 1) {
                return false;
            }
            photoList.remove(position);
            expensePhotoAdapter.notifyDataSetChanged();
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
            } else if (requestCode == PICK_PHOTO_CODE) {
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
            } else if (requestCode == CROP_PHOTO_CODE) {
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
                            expensePhotoAdapter.notifyDataSetChanged();
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

    private void save() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);
        String groupId = sharedPreferences.getString(Group.ID_KEY, null);
        if (loginUserId == null || groupId == null) {
            Log.i(TAG, "Error getting login user id or group id.");
            return;
        }
        expense.setUserId(loginUserId);
        expense.setGroupId(groupId);

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
        expense.setAmount(amount);

        if (noteTextView.getText().length() > 0) {
            expense.setNote(noteTextView.getText().toString());
        } else {
            expense.setNote("");
        }

        expense.setCategoryId(category != null ? category.getId() : null);
        expense.setExpenseDate(calendar.getTime());

        ExpenseBuilder expenseBuilder = new ExpenseBuilder();
        expenseBuilder.setExpense(expense);
        photoList.remove(photoList.size() - 1);
        expenseBuilder.setPhotoList(photoList);

        progressBar.setVisibility(View.VISIBLE);
        SyncExpense.create(expenseBuilder).continueWith(onCreateSuccess, Task.UI_THREAD_EXECUTOR);
        closeSoftKeyboard();
    }

    private Continuation<JSONObject, Void> onCreateSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in creating new expense.", task.getError());
            }

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

    private void closeWithUnSavedChangesCheck() {
        if (amountTextView.getText().length() == 0 && noteTextView.getText().length() == 0 && photoList.size() <= 1) {
            close();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.unsaved_changes)
                .setMessage(R.string.unsaved_changes_message)
                .setPositiveButton(R.string.discard, (DialogInterface dialog, int which) -> close())
                .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void close() {
        closeSoftKeyboard();

        pendingRunnable = new Runnable() {
            @Override
            public void run() {
                finish();
                overridePendingTransition(0, R.anim.right_out);
            }
        };

        // Wait soft keyboard to close
        handler.postDelayed(pendingRunnable, 50);
        pendingRunnable = null;
    }

    @Override
    public void onBackPressed() {
        closeWithUnSavedChangesCheck();
    }

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
