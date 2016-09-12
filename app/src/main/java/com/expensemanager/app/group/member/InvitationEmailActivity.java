package com.expensemanager.app.group.member;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.service.Constant;
import com.expensemanager.app.service.email.Mail;
import com.expensemanager.app.service.email.MailSender;
import com.expensemanager.app.service.email.Recipient;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InvitationEmailActivity extends AppCompatActivity {
    private static final String TAG = InviteActivity.class.getSimpleName();

    public static final String GROUP_ID = "groupId";
    private String groupId;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_right_title_text_view_id) TextView sendTextView;
    @BindView(R.id.invite_activity_email_edit_text_id)
    EditText emailEditText;

    public static void newInstance(Context context, String id) {
        Intent intent = new Intent(context, InviteActivity.class);
        intent.putExtra(GROUP_ID, id);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.stay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_activity);
        ButterKnife.bind(this);

        setupToolbar();

        groupId = getIntent().getStringExtra(GROUP_ID);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.invite_new));
        sendTextView.setText(getString(R.string.send));
        sendTextView.setVisibility(View.VISIBLE);

        titleTextView.setOnClickListener(v -> finish());
        backImageView.setOnClickListener(v -> finish());
        sendTextView.setOnClickListener(v -> sendInvitationEmail());
    }

    private void sendInvitationEmail() {
        String recipientEmail = emailEditText.getText().toString().trim();
        Log.d(TAG, "Email: " + recipientEmail);

        String message = "Hi there, " +
            "<br><br>" +
            "Here is your invitation to Expense Manager: " + "<a href='http://zhaolongzhong.com?token=123456'>Expense Manager Invitation</a>" +
            "<br>For any other questions, simply reply to this email and we’ll respond!<br>" +
            "<br>" +
            "Cheers," +
            "<br>" +
            "The team at Expense Manager";

        MailSender mailSender = new MailSender(Constant.EMAIL_ACCOUNT, Constant.EMAIL_PASSWORD);
        Mail.MailBuilder builder = new Mail.MailBuilder();
        Mail mail = builder
            .setSender(Constant.EMAIL_ACCOUNT)
            .addRecipient(new Recipient(recipientEmail))
            .setSubject("Invitation from Expense Manager Team")
            .setText(message)
            .build();

        mailSender.sendMail(mail, new MailSender.OnMailSentListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "sendMail onSuccess");
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "sendMail onError", error);
            }
        });
    }
}
