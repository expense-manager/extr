package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.MainActivity;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private String email;
    private String password;
    private String confirmPassword;
    private String fullname;

    @BindView(R.id.sign_up_activity_sign_up_button_id) Button signUpButton;
    @BindView(R.id.sign_up_activity_email_or_phone_number_edit_text_id) EditText emailEditText;
    @BindView(R.id.sign_up_activity_password_edit_text_id) EditText passwordEditText;
    @BindView(R.id.sign_up_activity_confirm_password_edit_text_id) EditText confirmPasswordEditText;
    @BindView(R.id.sign_up_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.sign_up_activity_error_text_view_id) TextView errorMessageTextView;
    @BindView(R.id.sign_up_activity_mismatch_image_view_id) ImageView mismatchImageView;
    @BindView(R.id.sign_up_activity_clear_email_image_view_id) ImageView clearEmailImageView;
    @BindView(R.id.sign_up_activity_clear_password_image_view_id) ImageView clearPasswordImageView;
    @BindView(R.id.sign_up_activity_clear_name_image_view_id) ImageView clearNameImageView;
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

        emailEditText.addTextChangedListener(emailTextWatcher);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
        confirmPasswordEditText.addTextChangedListener(confirmPasswordTextWatcher);
        nameEditText.addTextChangedListener(nameTextWatcher);
        loginLinearLayout.setOnClickListener(v -> {
            LoginActivity.newInstance(this);
            finish();
        });
    }

    @OnClick({R.id.sign_up_activity_clear_email_image_view_id, R.id.sign_up_activity_clear_password_image_view_id,
        R.id.sign_up_activity_clear_name_image_view_id})
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.sign_up_activity_clear_email_image_view_id:
                emailEditText.setText("");
                break;
            case R.id.sign_up_activity_clear_password_image_view_id:
                passwordEditText.setText("");
                break;
            case R.id.sign_up_activity_clear_name_image_view_id:
                nameEditText.setText("");
                break;
        }
    }

    public void signUp(View v) {
        if (stepOneRelativeLayout.getVisibility() == View.VISIBLE) {
            setStepTwo();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            SyncUser.signUp(email, password, fullname).continueWith(onSignUpSuccess, Task.UI_THREAD_EXECUTOR);
        }
    }

    private void setStepTwo() {
        closeSoftKeyboard();
        titleTextView.setText(R.string.sign_up_title_step_two);
        stepOneRelativeLayout.setVisibility(View.GONE);
        stepTwoRelativeLayout.setVisibility(View.VISIBLE);
        signUpButton.setText(R.string.sign_up);
        resetStepTwo();
    }

    private void resetStepTwo() {
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
        nameEditText.setText("");
        setButton();
    }

    private void setStepOne() {
        closeSoftKeyboard();
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
                SyncUser.login(email, password).onSuccess(onLoginSuccess, Task.UI_THREAD_EXECUTOR);
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
                closeSoftKeyboard();
            } else {
                MainActivity.newInstance(SignUpActivity.this);
            }

            return null;
        }
    };

    private TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            clearEmailImageView.setVisibility(email != null && email.length() != 0 ? View.VISIBLE : View.GONE);
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
        mismatchImageView.setVisibility(confirmPassword != null && confirmPassword.length() > 0 ? View.VISIBLE : View.GONE);
        mismatchImageView.setImageResource(password.equals(confirmPassword) ? R.drawable.ic_check : R.drawable.ic_alert_circle_outline);
    }

    private TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setButton();
            clearNameImageView.setVisibility(fullname != null && fullname.length() != 0 ? View.VISIBLE : View.GONE);
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
            int color = ContextCompat.getColor(this, R.color.blue);
            signUpButton.setTextColor(color);
        }
    }

    private boolean isValidUserInfo() {
        getSignUpInfo();

        // Validate SignUp Step 1
        if (stepOneRelativeLayout.getVisibility() == View.VISIBLE) {
            if (email == null || email.length() < 7) {
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

            if (fullname == null || fullname.length() < 5) {
                return false;
            }
        }

        return true;
    }

    private void getSignUpInfo() {
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        confirmPassword = confirmPasswordEditText.getText().toString().trim();
        fullname = nameEditText.getText().toString().trim();
    }

    public void closeSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (inputMethodManager != null && view != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
