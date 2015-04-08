package net.kemitix.wiser.assertions;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.mail.internet.MimeMessage;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * Taken from the WiserAssetions class from Rafal Browiec at
 * http://blog.codeleak.pl/2014/09/testing-mail-code-in-spring-boot.html
 */
public class WiserAssertions {

    private final List<WiserMessage> messages;

    public static WiserAssertions assertReceivedMessage(Wiser wiser) {
        return new WiserAssertions(wiser.getMessages());
    }

    private WiserAssertions(List<WiserMessage> messages) {
        this.messages = messages;
    }

    public WiserAssertions from(String from) {
        findFirstOrElseThrow(m -> m.getEnvelopeSender().equals(from),
                assertionError("No message from [{0}] found!", from));
        return this;
    }

    public WiserAssertions to(String to) {
        findFirstOrElseThrow(m -> m.getEnvelopeReceiver().equals(to),
                assertionError("No message to [{0}] found!", to));
        return this;
    }

    public WiserAssertions withSubject(String subject) {
        Predicate<WiserMessage> predicate = m -> subject.equals(unchecked(getMimeMessage(m)::getSubject));
        findFirstOrElseThrow(predicate,
                assertionError("No message with subject [{0}] found!", subject));
        return this;
    }

    public WiserAssertions withSubjectContains(String subject) {
        Predicate<WiserMessage> predicate = m -> unchecked(getMimeMessage(m)::getSubject).contains(subject);
        findFirstOrElseThrow(predicate,
                assertionError("No message with subject [{0}] found!", subject));
        return this;
    }

    public WiserAssertions withContent(String content) {
        findFirstOrElseThrow(m -> {
            ThrowingSupplier<String> contentAsString
                    = () -> ((String) getMimeMessage(m).getContent()).trim();
            return content.equals(unchecked(contentAsString));
        }, assertionError("No message with content [{0}] found!", content));
        return this;
    }

    public WiserAssertions withContentContains(String content) {
        StringBuilder messageContent = new StringBuilder();
        findFirstOrElseThrow((WiserMessage m) -> {
            ThrowingSupplier<String> contentAsString
                    = () -> ((String) getMimeMessage(m).getContent()).trim();
            messageContent.append(unchecked(contentAsString));
            return unchecked(contentAsString).contains(content);
        }, assertionError("No message with content containing [{0}] found! Was {1}", content, messageContent));
        return this;
    }

    private void findFirstOrElseThrow(Predicate<WiserMessage> predicate, Supplier<AssertionError> exceptionSupplier) {
        messages.stream().filter(predicate)
                .findFirst().orElseThrow(exceptionSupplier);
    }

    private MimeMessage getMimeMessage(WiserMessage wiserMessage) {
        return unchecked(wiserMessage::getMimeMessage);
    }

    private static Supplier<AssertionError> assertionError(String errorMessage, Object... args) {
        return () -> new AssertionError(MessageFormat.format(errorMessage, args));
    }

    public static <T> T unchecked(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public interface ThrowingSupplier<T> {

        T get() throws Throwable;
    }
}
