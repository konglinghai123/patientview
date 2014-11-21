package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.api.config.TestCommonConfig;
import org.patientview.api.model.Email;
import org.patientview.api.service.impl.EmailServiceImpl;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.MessagingException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestCommonConfig.class, EmailServiceImpl.class})
public class EmailServiceTest {

    @Autowired
    private EmailServiceImpl emailService;

    @Ignore("Email should be tested manually")
    @Test
    public void testSendEmail() throws MailException, MessagingException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        Email email = new Email();
        email.setBody("test body");
        email.setRecipients(new String[]{"nadim@solidstategroup.com","jamesr@solidstategroup.com","andrewmoffatt777@hotmail.com"});
        email.setSubject("test subject");
        email.setSender("no-reply@solidstategroup.com");
        Assert.assertTrue("should have sent email", emailService.sendEmail(email));
    }
}
