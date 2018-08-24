package net.kemitix.wiser.assertions;

import org.junit.Test;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Regression test for issue #6.
 *
 * @see https://github.com/kemitix/wiser-assertions/issues/6
 * @author pcampbell
 */
public class Issue6Test extends AbstractWiserTest {

    /**
     * Test {@link WiserAssertions#withContentContains(String)} where the
     * message is a Spring Mail {@link SimpleMailMessage}.
     */
    @Test
    public void shouldMatchContentContainsFromSimpleMailMessage() {
        //given
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("Carl <carl@b.com>");
        message.setFrom("Bob <bob@a.com>");
        message.setSubject("Subject");
        message.setText("Hi Carl,\n\nA new message was just posted.");
        final JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setPort(getPort());
        //when
        sender.send(message);
        //then
        getAssertions().from("bob@a.com").to("carl@b.com")
                .withSubject("Subject")
                .withContentContains("Hi Carl");
    }

}
