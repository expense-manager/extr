package com.expensemanager.app.service.email;

import java.util.Properties;

/**
 * Created by Zhaolong Zhong on 9/1/16.
 */

public class GmailProvider implements MailProvider {

    private Properties props;

    public GmailProvider() {
        props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");
    }

    public Properties getProperties() {
        return props;
    }

}
