package net.kemitix.wiser.assertions;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;
import org.junit.Test;

import javax.mail.Message;

/**
 * Regression test for issue #1.
 *
 * @see https://github.com/kemitix/wiser-assertions/issues/1
 * @author pcampbell
 */
public class Issue1Test extends AbstractWiserTest {

    @Test
    public void shouldParseNestedMultiPartEmails() {
        //given
        final Email email = new Email();
        email.addRecipient("Jonjo McKay", "jonjo.mckay@manywho.com",
                Message.RecipientType.TO);
        email.setFromAddress("ManyWho", "no-reply@manywho.com");
        email.setSubject("New activity");
        email.setText("Hi Jonjo McKay,\n\nA new message was just posted in a "
                + "stream you follow on ManyWho. The message was:\n\nLance "
                + "Drake Mandrell: \"This is a test message\"\n\nJoin the flow "
                + "at https://flow.manywho.com to read the stream and reply.\n"
                + "\nManyWho Email Bot");
        Mailer mailer = new Mailer(getSession());
        //when
        mailer.sendMail(email);
        //then
        getAssertions().withContent("Hi Jonjo McKay");
    }

}
