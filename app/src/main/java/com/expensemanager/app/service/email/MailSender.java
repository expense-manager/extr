package com.expensemanager.app.service.email;

import android.os.AsyncTask;
import android.util.Log;

import java.security.Security;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by Zhaolong Zhong on 9/1/16.
 *
 * Reference: http://stackoverflow.com/a/2033124/2427211
 */

public class MailSender extends javax.mail.Authenticator {

    private OnMailSentListener listener;

    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailSender(String user, String password) {
        this.user = user;
        this.password = password;

        MailProvider mailProvider = new GmailProvider();
        session = Session.getDefaultInstance(mailProvider.getProperties(), this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public void sendMail(Mail mail) {
        sendMail(mail, null);
    }

    public void sendMail(Mail mail, OnMailSentListener listener) {
        this.listener = listener;
        new MailTask().execute(mail);
    }

    public interface OnMailSentListener {
        void onSuccess();
        void onError(Exception error);
    }

    private class MailTask extends AsyncTask<Mail, Void, Void> {

        private Exception error;

        @Override
        protected Void doInBackground(Mail... params) {
            Mail mail = params[0];

            try {
                MimeMessage message = new MimeMessage(session);

                message.setSender(new InternetAddress(mail.getSender()));
                message.setSubject(mail.getSubject());

                for(Recipient recipient : mail.getRecipients()) {
                    Message.RecipientType recipientType = null;
                    switch (recipient.getType()) {
                        case TO:
                            recipientType = Message.RecipientType.TO;
                            break;
                        case CC:
                            recipientType = Message.RecipientType.CC;
                            break;
                        case BCC:
                            recipientType = Message.RecipientType.BCC;
                            break;
                    }
                    message.addRecipient(recipientType, new InternetAddress(recipient.getAddress()));
                }

                Multipart multipart = new MimeMultipart();

//                if(mail.getText() != null) {
//                    MimeBodyPart textBodyPart = new MimeBodyPart();
//                    ByteArrayDataSource textDatasource = new ByteArrayDataSource(mail.getText().getBytes(), "text/plain");
//                    DataHandler handler = new DataHandler(textDatasource);
//                    textBodyPart.setDataHandler(handler);
//                    multipart.addBodyPart(textBodyPart);
//                }
//
//                if(mail.getHtml() != null) {
//                    MimeBodyPart htmlBodyPart = new MimeBodyPart();
//                    ByteArrayDataSource htmlDatasource = new ByteArrayDataSource(mail.getHtml().getBytes(), "text/html");
//                    DataHandler handler = new DataHandler(htmlDatasource);
//                    htmlBodyPart.setDataHandler(handler);
//                    multipart.addBodyPart(htmlBodyPart);
//                }
//
//                message.setContent(multipart);

                message.setContent(mail.getText(), "text/html; charset=utf-8");

                Transport.send(message);

            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error sending mail", e);
                error = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(listener != null) {
                if(error != null) {
                    listener.onError(error);
                } else {
                    listener.onSuccess();
                }
            }
        }
    }
}
