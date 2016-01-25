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

    /**
     * Test {@link WiserAssertions#withContentContains(String)} where the nested
     * multi-part message contains the expected text.
     */
    @Test
    public void shouldParseNestedMultiPartEmails() {
        //given
        final Email email = new Email();
        email.addRecipient("Carl", "carl@b.com",
                Message.RecipientType.TO);
        email.setFromAddress("Bob", "bob@a.com");
        email.setSubject("Subject");
        email.setText("Hi Carl,\n\nA new message was just posted.");
        Mailer mailer = new Mailer(getSession());
        //when
        mailer.sendMail(email);
        //then
        getAssertions().withContentContains("Hi Carl");
    }

}
