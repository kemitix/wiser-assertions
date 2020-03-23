package net.kemitix.wiser.assertions;

import org.junit.jupiter.api.Test;
import org.subethamail.wiser.Wiser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
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
public class WiserAssertionsTest extends AbstractWiserTest {

    private static final Logger LOG
            = Logger.getLogger(WiserAssertionsTest.class.getName());

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
        try {
            MimeMessage message = new MimeMessage(getSession());
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
        assertReceivedMessage().withContent(body);
    }

    /**
     * Test {@link WiserAssertions#withContent(java.lang.String)} where the
     * content of the email does not match.
     */
    @Test
    public void testContentNotMatches() {
        //given
        final String body = "message body";
        //when
        sendMimeMultipartMessage("from", "to", "subject", body);
        //then
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() ->
                        assertReceivedMessage()
                                .withContent("Other body"));
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
        assertReceivedMessage().withContentContains("age bo");
    }

    /**
     * Test {@link WiserAssertions#withContentContains(String)} where the
     * content of the email does not match.
     */
    @Test
    public void testContentContainsNotMatches() {
        //given
        final String body = "message body";
        //when
        sendMimeMultipartMessage("from", "to", "subject", body);
        //then
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() ->
                        assertReceivedMessage()
                                .withContentContains("agebo"));
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
        assertReceivedMessage().from(from);
    }

    /**
     * Test {@link WiserAssertions#from(java.lang.String)} can detect when mail
     * is not sent from a user.
     */
    @Test
    public void testFromNotMatches() {
        //given
        final String from = "bob@a.com";
        //when
        sendMimeMultipartMessage(from, "to", "subject", "body");
        //then
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() ->
                        assertReceivedMessage()
                                .from("lisa@c.com"));
    }

    /**
     * Test {@link WiserAssertions#assertReceivedMessage(Wiser)} creates and
     * returns a WiserAssertions instance.
     */
    @Test
    public void testInstantiate() {
        assertThat(assertReceivedMessage())
                .isNotNull();
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
        assertReceivedMessage().withSubjectContains(fragment);
    }

    /**
     * Test {@link WiserAssertions#withSubjectContains(java.lang.String)} where
     * the subject does not contain the expected fragment.
     */
    @Test
    public void testSubjectContainsNotMatches() {
        //given
        final String fragment = "foo";
        //when
        sendMimeMultipartMessage("from", "to", "subject tail", "body");
        //then
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() ->
                        assertReceivedMessage()
                                .withSubjectContains(fragment));
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
        assertReceivedMessage().withSubject(subject);
    }

    /**
     * Test {@link WiserAssertions#withSubject(java.lang.String)} where the
     * message does not have the subject expected.
     */
    @Test
    public void testSubjectNotMatches() {
        //given
        final String subject = "message subject";
        //when
        sendMimeMultipartMessage("from", "to", subject, "body");
        //then
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() ->
                        assertReceivedMessage()
                                .withSubject("other subject"));
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
        assertReceivedMessage().to(to);
    }

    /**
     * Test {@link WiserAssertions#from(java.lang.String)} can detect when mail
     * is not sent from a user.
     *
     */
    @Test
    public void testToNotMatches() {
        //given
        final String to = "carl@b.com";
        //when
        sendMimeMultipartMessage("from", to, "subject", "body");
        //then
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() ->
                        assertReceivedMessage()
                                .to("bob@a.com"));
    }

}
