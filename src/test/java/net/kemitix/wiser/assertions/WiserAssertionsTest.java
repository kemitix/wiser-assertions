package net.kemitix.wiser.assertions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.subethamail.wiser.Wiser;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Tests for {@link WiserAssertions}.
 *
 * @author pcampbell
 */
public class WiserAssertionsTest {

    /**
     * Logger.
     */
    private static final Logger LOG
            = Logger.getLogger(WiserAssertionsTest.class.getName());

    /**
     * Test mail server.
     */
    private Wiser wiser;

    /**
     * Prepare each test.
     */
    @Before
    @SuppressWarnings("magicnumber")
    public void setUp() {
        wiser = new Wiser(PORT);
        wiser.start();
    }

    /**
     * Test mail server port.
     */
    private static final int PORT = 12345;

    /**
     * Clean up after each test.
     */
    @After
    public void tearDown() {
        wiser.stop();
    }

    /**
     * Sends a mime multipart message to the Wiser server.
     *
     * @param from    the sender
     * @param to      the recipient
     * @param subject the subject of the email
     * @param body    the body of the email
     */
    private void sendMimeMultipartMessage(
            final String from,
            final String to,
            final String subject,
            final String body) {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "localhost");
        properties.setProperty("mail.smtp.port", "" + PORT);
        Session session = Session.getDefaultInstance(properties);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject, "UTF-8");
            final Multipart mimeMultipart = new MimeMultipart();
            final MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setText(body);
            mimeMultipart.addBodyPart(mimeBodyPart);
            message.setContent(mimeMultipart);
            Transport.send(message);
        } catch (MessagingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Instantiates the WiserAssertions.
     *
     * @return the wiser assertions
     */
    private WiserAssertions getAssertions() {
        return WiserAssertions.assertReceivedMessage(wiser);
    }

    /**
     * Test {@link WiserAssertions#withContent(java.lang.String)} where the
     * content of the email matches.
     */
    @Test
    public void testContentMatches() {
        //given
        final String body = "message body";
        //when
        sendMimeMultipartMessage("from", "to", "subject", body);
        //then
        getAssertions().withContent(body);
    }

    /**
     * Test {@link WiserAssertions#withContent(java.lang.String)} where the
     * content of the email does not match.
     */
    @Test(expected = AssertionError.class)
    public void testContentNotMatches() {
        //given
        final String body = "message body";
        //when
        sendMimeMultipartMessage("from", "to", "subject", body);
        //then
        getAssertions().withContent("Other body");
    }

    /**
     * Test {@link WiserAssertions#withContentContains(String)} where the
     * content of the email matches.
     */
    @Test
    public void testContentContainsMatches() {
        //given
        final String body = "message body";
        //when
        sendMimeMultipartMessage("from", "to", "subject", body);
        //then
        getAssertions().withContentContains("age bo");
    }

    /**
     * Test {@link WiserAssertions#withContentContains(String)} where the
     * content of the email does not match.
     */
    @Test(expected = AssertionError.class)
    public void testContentContainsNotMatches() {
        //given
        final String body = "message body";
        //when
        sendMimeMultipartMessage("from", "to", "subject", body);
        //then
        getAssertions().withContentContains("agebo");
    }

    /**
     * Test {@link WiserAssertions#from(java.lang.String)} can detect when mail
     * is sent from a user.
     *
     * @throws java.io.IOException if error delivering test message
     */
    @Test
    public void testFromMatches() throws IOException {
        //given
        final String from = "bob@a.com";
        //when
        sendMimeMultipartMessage(from, "to", "subject", "body");
        //then
        getAssertions().from(from);
    }

    /**
     * Test {@link WiserAssertions#from(java.lang.String)} can detect when mail
     * is not sent from a user.
     */
    @Test(expected = AssertionError.class)
    public void testFromNotMatches() {
        //given
        final String from = "bob@a.com";
        //when
        sendMimeMultipartMessage(from, "to", "subject", "body");
        //then
        getAssertions().from("lisa@c.com");
    }

    /**
     * Test {@link WiserAssertions#assertReceivedMessage(Wiser)} creates and
     * returns a WiserAssertions instance.
     */
    @Test
    public void testInstantiate() {
        assertNotNull(getAssertions());
    }

    /**
     * Test {@link WiserAssertions#withSubjectContains(java.lang.String)} where
     * the subject contains the expected fragment.
     */
    @Test
    public void testSubjectContainsMatches() {
        //given
        final String fragment = "foo";
        //when
        sendMimeMultipartMessage(
                "from", "to", "subject " + fragment + " tail", "body");
        //then
        getAssertions().withSubjectContains(fragment);
    }

    /**
     * Test {@link WiserAssertions#withSubjectContains(java.lang.String)} where
     * the subject does not contain the expected fragment.
     */
    @Test(expected = AssertionError.class)
    public void testSubjectContainsNotMatches() {
        //given
        final String fragment = "foo";
        //when
        sendMimeMultipartMessage("from", "to", "subject tail", "body");
        //then
        getAssertions().withSubjectContains(fragment);
    }

    /**
     * Test {@link WiserAssertions#withSubject(java.lang.String)} where the
     * message has the subject expected.
     */
    @Test
    public void testSubjectMatches() {
        //given
        final String subject = "message subject";
        //when
        sendMimeMultipartMessage("from", "to", subject, "body");
        //then
        getAssertions().withSubject(subject);
    }

    /**
     * Test {@link WiserAssertions#withSubject(java.lang.String)} where the
     * message does not have the subject expected.
     */
    @Test(expected = AssertionError.class)
    public void testSubjectNotMatches() {
        //given
        final String subject = "message subject";
        //when
        sendMimeMultipartMessage("from", "to", subject, "body");
        //then
        getAssertions().withSubject("other subject");
    }

    /**
     * Test {@link WiserAssertions#from(java.lang.String)} can detect when mail
     * is sent to a user.
     */
    @Test
    public void testToMatches() {
        //given
        final String to = "carl@b.com";
        //when
        sendMimeMultipartMessage("from", to, "subject", "body");
        //then
        getAssertions().to(to);
    }

    /**
     * Test {@link WiserAssertions#from(java.lang.String)} can detect when mail
     * is not sent from a user.
     *
     */
    @Test(expected = AssertionError.class)
    public void testToNotMatches() {
        //given
        final String to = "carl@b.com";
        //when
        sendMimeMultipartMessage("from", to, "subject", "body");
        //then
        getAssertions().to("bob@a.com");
    }

}
