package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.main.MainActivity;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends BaseActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    public static final String IS_FIRST_TIME = "is_first_time";

    private String emailOrPhone;
    private String password;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String phone;

    @BindView(R.id.sign_up_activity_sign_up_button_id) Button signUpButton;
    @BindView(R.id.sign_up_activity_email_or_phone_number_edit_text_id) EditText emailOrPhoneEditText;
    @BindView(R.id.sign_up_activity_password_edit_text_id) EditText passwordEditText;
    @BindView(R.id.sign_up_activity_confirm_password_edit_text_id) EditText confirmPasswordEditText;
    @BindView(R.id.sign_up_activity_first_name_edit_text_id) EditText firstNameEditText;
    @BindView(R.id.sign_up_activity_last_name_edit_text_id) EditText lastNameEditText;
    @BindView(R.id.sign_up_activity_error_text_view_id) TextView errorMessageTextView;
    @BindView(R.id.sign_up_activity_mismatch_image_view_id) ImageView mismatchImageView;
    @BindView(R.id.sign_up_activity_clear_email_image_view_id) ImageView clearEmailImageView;
    @BindView(R.id.sign_up_activity_clear_password_image_view_id) ImageView clearPasswordImageView;
    @BindView(R.id.sign_up_activity_clear_first_name_image_view_id) ImageView clearFirstNameImageView;
    @BindView(R.id.sign_up_activity_clear_last_name_image_view_id) ImageView clearLastNameImageView;
    @BindView(R.id.sign_up_activity_error_relative_layout_id) RelativeLayout errorMessageRelativeLayout;
    @BindView(R.id.sign_up_activity_step_one_relative_layout_id) RelativeLayout stepOneRelativeLayout;
    @BindView(R.id.sign_up_activity_step_two_relative_layout_id) RelativeLayout stepTwoRelativeLayout;
    @BindView(R.id.sign_up_activity_title_text_view_id) TextView titleTextView;
    @BindView(R.id.sign_up_activity_login_linear_layout_id) LinearLayout loginLinearLayout;
    @BindView(R.id.progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        ButterKnife.bind(this);

        invalidateViews();
    }

    private void invalidateViews() {
        signUpButton.setEnabled(false);
        signUpButton.setOnClickListener(this::signUp);

        emailOrPhoneEditText.addTextChangedListener(emailOrPhoneTextWatcher);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
        confirmPasswordEditText.addTextChangedListener(confirmPasswordTextWatcher);
        firstNameEditText.addTextChangedListener(firstNameTextWatcher);
        lastNameEditText.addTextChangedListener(lastNameTextWatcher);
        loginLinearLayout.setOnClickListener(v -> {
            LoginActivity.newInstance(this);
            finish();
        });
    }

    @OnClick({R.id.sign_up_activity_clear_email_image_view_id, R.id.sign_up_activity_clear_password_image_view_id,
        R.id.sign_up_activity_clear_first_name_image_view_id, R.id.sign_up_activity_clear_last_name_image_view_id})
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.sign_up_activity_clear_email_image_view_id:
                emailOrPhoneEditText.setText("");
                break;
            case R.id.sign_up_activity_clear_password_image_view_id:
                passwordEditText.setText("");
                break;
            case R.id.sign_up_activity_clear_first_name_image_view_id:
                firstNameEditText.setText("");
                break;
            case R.id.sign_up_activity_clear_last_name_image_view_id:
                lastNameEditText.setText("");
                break;
        }
    }

    public void signUp(View v) {
        emailOrPhone = emailOrPhoneEditText.getText().toString();

        if (!Helpers.isValidEmail(emailOrPhone) && !Helpers.isValidPhoneNumber(emailOrPhone)) {
            Toast.makeText(this, "Invalid email or phone number.", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Helpers.isValidEmail(emailOrPhone) && Helpers.isValidPhoneNumber(emailOrPhone)) {
            phone = emailOrPhone;
        } else if (Helpers.isValidEmail(emailOrPhone) && Helpers.isValidPhoneNumber(emailOrPhone)) {
            phone = "";
        }

        if (stepOneRelativeLayout.getVisibility() == View.VISIBLE) {
            // todo: a better way to handle this is to do a network request to check if username exists or not
            if (emailOrPhone.contains("@")) {
                SyncUser.getAllUsersByUserEmail(emailOrPhone).continueWith(onQueryUserFinished, Task.UI_THREAD_EXECUTOR);
            } else {
                SyncUser.getAllUsersByUserPhoneNumber(emailOrPhone).continueWith(onQueryUserFinished, Task.UI_THREAD_EXECUTOR);
            }
        } else {
            progressBar.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(phone)) {
                SyncUser.signUp(emailOrPhone, password, firstName, lastName, null).continueWith(onSignUpSuccess, Task.UI_THREAD_EXECUTOR);
            } else {
                SyncUser.signUp(emailOrPhone, password, firstName, lastName, phone).continueWith(onSignUpSuccess, Task.UI_THREAD_EXECUTOR);
            }
        }
    }

    private Continuation<JSONObject, Void> onQueryUserFinished = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            if (task.isFaulted()) {
                Exception exception = task.getError();
                Log.e(TAG, "Error in downloading all users.", exception);
                throw  exception;
            }

            JSONObject result = task.getResult();
            if (result == null) {
                throw new Exception("Empty response.");
            }

            Log.d(TAG, "Sign up check Users: " + result);

            // Check if username already used
            if (result.getJSONArray("results").length() != 0) {
                errorMessageTextView.setText(getString(R.string.username_used));
                errorMessageRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                setStepTwo();
                errorMessageRelativeLayout.setVisibility(View.INVISIBLE);
            }

            return null;
        }
    };

    private void setStepTwo() {
        Helpers.closeSoftKeyboard(this);
        titleTextView.setText(R.string.sign_up_title_step_two);
        stepOneRelativeLayout.setVisibility(View.GONE);
        stepTwoRelativeLayout.setVisibility(View.VISIBLE);
        signUpButton.setText(R.string.sign_up);
        resetStepTwo();
    }

    private void resetStepTwo() {
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        setButton();
        mismatchImageView.setVisibility(View.INVISIBLE);
    }

    private void setStepOne() {
        Helpers.closeSoftKeyboard(this);
        titleTextView.setText(R.string.sign_up_title_step_one);
        stepTwoRelativeLayout.setVisibility(View.GONE);
        stepOneRelativeLayout.setVisibility(View.VISIBLE);
        signUpButton.setText(R.string.next);
        setButton();
    }

    private Continuation<JSONObject, Void> onSignUpSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error in sign up.", task.getError());
                progressBar.setVisibility(View.GONE);
            }

            if (task.getResult().has("error")) {
                // Clear password
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
                // Show error messasge
                errorMessageTextView.setText(task.getResult().getString("error"));
                errorMessageRelativeLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                SyncUser.login(emailOrPhone, password).onSuccess(onLoginSuccess, Task.UI_THREAD_EXECUTOR);
            }
            return null;
        }
    };

    private Continuation<JSONObject, Void> onLoginSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);

            if (task.isFaulted()) {
                Log.e(TAG, "Error in login. ", task.getError());
            }

            if (task.getResult().has("error")) {
                // Clear password
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
                // Show error messasge
                errorMessageTextView.setText(task.getResult().getString("error"));
                errorMessageRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                intent.putExtra(IS_FIRST_TIME, true);
                // Make sure main activity at the top on stack, no other activity in the backstack.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            return null;
        }
    };

    private TextWatcher emailOrPhoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            clearEmailImageView.setVisibility(emailOrPhone != null && emailOrPhone.length() != 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            clearPasswordImageView.setVisibility(password != null && password.length() != 0 ? View.VISIBLE : View.GONE);
            setMismatchSign();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher confirmPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            setMismatchSign();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void setMismatchSign() {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            mismatchImageView.setVisibility(View.INVISIBLE);
            return;
        }

        if (password != null && confirmPassword.equals(password)) {
            mismatchImageView.setImageResource(R.drawable.ic_check);
        } else {
            mismatchImageView.setImageResource(R.drawable.ic_alert_circle_outline);
        }

        mismatchImageView.setVisibility(View.VISIBLE);
    }

    private TextWatcher firstNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            clearFirstNameImageView.setVisibility(firstName != null && firstName.length() != 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher lastNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            Log.d(TAG, "onTextChanged");
            clearLastNameImageView.setVisibility(lastName != null && lastName.length() != 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void setButton() {
        signUpButton.setEnabled(isValidUserInfo());
        if (signUpButton.isEnabled()) {
            int color = ContextCompat.getColor(this, R.color.white);
            signUpButton.setTextColor(color);
        } else {
            int color = ContextCompat.getColor(this, R.color.colorPrimary);
            signUpButton.setTextColor(color);
        }
    }

    private boolean isValidUserInfo() {
        getSignUpInfo();

        // Validate SignUp Step 1
        if (stepOneRelativeLayout.getVisibility() == View.VISIBLE) {
            if (emailOrPhone == null || emailOrPhone.length() < 7) {
                return false;
            }
        } else {
        // Validate SignUp Step 2
            if (password == null || password.length() < 6) {
                return false;
            }

            if (!password.equals(confirmPassword)) {
                return false;
            }

            if (firstName == null || firstName.length() < 2) {
                return false;
            }

            if (lastName == null || lastName.length() < 2) {
                return false;
            }
        }

        return true;
    }

    private void getSignUpInfo() {
        emailOrPhone = emailOrPhoneEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        confirmPassword = confirmPasswordEditText.getText().toString().trim();
        firstName = firstNameEditText.getText().toString().trim();
        lastName = lastNameEditText.getText().toString().trim();
    }

    @Override
    public void onBackPressed() {
        if (stepTwoRelativeLayout.getVisibility() == View.VISIBLE) {
            // Clear error message if exists
            errorMessageRelativeLayout.setVisibility(View.INVISIBLE);
            // Back to step one
            setStepOne();
        } else {
            finish();
        }
    }
}
