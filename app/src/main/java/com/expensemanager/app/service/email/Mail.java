package com.expensemanager.app.service.email;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhaolong Zhong on 9/1/16.
 *
 * Reference:
 * https://github.com/enrichman/androidmail
 */

public class Mail {
    private static final String TAG = Mail.class.getSimpleName();

    private String sender;
    private List<Recipient> recipients;
    private String subject;
    private String text;
    private String html;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public static class MailBuilder {

        private String sender;
        private List<Recipient> recipients = new ArrayList<>();
        private String subject;
        private String text;
        private String html;

        public Mail build() {
            Mail mail = new Mail();
            mail.sender = this.sender;
            mail.recipients = this.recipients;
            mail.subject = this.subject;
            mail.text = this.text;
            mail.html = this.html;
            return mail;
        }

        public MailBuilder setSender(String sender) {
            this.sender = sender;
            return this;
        }

        public MailBuilder addRecipient(Recipient recipient) {
            this.recipients.add(recipient);
            return this;
        }

        public MailBuilder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public MailBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public MailBuilder setHtml(String html) {
            this.html = html;
            return this;
        }
    }
}
