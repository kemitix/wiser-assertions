package net.kemitix.wiser.assertions;

import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Provides a set of assertions for checking the status of any messages received
 * by subethamail's Wiser.
 *
 * <pre>
 * {@code
 * {@literal @}Before
 * public void setUp() throws IOException {
 *     wiser = new Wiser(PORT);
 *     wiser.start();
 * }
 *
 * {@literal @}After public void tearDown() { wiser.stop(); }
 *
 * {@literal @}Test public void testMail() { //given ...
 *
 * //when ...
 *
 * //then WiserAssertions.assertReceivedMessage(wiser) .from(sender)
 * .to(recipient_alpha) .to(recipient_beta) .withSubjectContains(subject_prefix)
 * .withSubjectContains(subject_suffix) .withContentContains(message_element_1)
 * .withContentContains(message_element_2)
 * .withContentContains(message_element_3); }
 * }
 * </pre>
 */
public final class WiserAssertions {

    /**
     * The messages received by Wiser.
     */
    private final List<WiserMessage> messages;

    /**
     * Creates an instance of {@code} WiserAssertions} ready to make assertions
     * on any messages received by the {@link Wiser} server.
     *
     * @param wiser the SMTP server instance
     *
     * @return an instance of {@code WiserAssertions}
     */
    public static WiserAssertions assertReceivedMessage(final Wiser wiser) {
        return new WiserAssertions(wiser.getMessages());
    }

    /**
     * Private constructor.
     *
     * @param wiserMessages the messages to be tested by the assertions
     */
    private WiserAssertions(final List<WiserMessage> wiserMessages) {
        this.messages = wiserMessages;
    }

    /**
     * Checks that there was at least one email received that was sent from the
     * {@code sender}.
     *
     * @param sender email address to search for
     *
     * @return the {@code WiserAssertions} instance
     */
    public WiserAssertions from(final String sender) {
        findFirstOrElseThrow(m -> m.getEnvelopeSender().equals(sender),
                assertionError("No message from [{0}] found!", sender));
        return this;
    }

    /**
     * Checks that there was at least one email received that was sent to the
     * {@code recipient}.
     *
     * @param recipient email address to search for
     *
     * @return the {@code WiserAssertions} instance
     */
    public WiserAssertions to(final String recipient) {
        findFirstOrElseThrow(m -> m.getEnvelopeReceiver().equals(recipient),
                assertionError("No message to [{0}] found!", recipient));
        return this;
    }

    /**
     * Checks that there was at least one email received that has the required
     * subject.
     *
     * @param subject the subject line to search for
     *
     * @return the {@code WiserAssertions} instance
     */
    public WiserAssertions withSubject(final String subject) {
        Predicate<WiserMessage> predicate
                = m -> subject.equals(unchecked(getMimeMessage(m)::getSubject));
        findFirstOrElseThrow(predicate,
                assertionError("No message with subject [{0}] found!",
                        subject));
        return this;
    }

    /**
     * Checks that there was at least one email received that has a subject that
     * contains the search text.
     *
     * @param subject the text to search for in the subject
     *
     * @return the {@code WiserAssertions} instance
     */
    public WiserAssertions withSubjectContains(final String subject) {
        Predicate<WiserMessage> predicate
                = m -> unchecked(getMimeMessage(m)::getSubject)
                .contains(subject);
        findFirstOrElseThrow(predicate,
                assertionError("No message with subject [{0}] found!",
                        subject));
        return this;
    }

    /**
     * Check that there was at least one email received that has a body that
     * matches the content.
     *
     * @param content the body of the email to search for
     *
     * @return the {@code WiserAssertions} instance
     */
    public WiserAssertions withContent(final String content) {
        findFirstOrElseThrow(m -> {
            ThrowingSupplier<String> contentAsString
                    = () -> getMimeMessageBody(m).trim();
            return content.equals(unchecked(contentAsString));
        }, assertionError("No message with content [{0}] found!", content));
        return this;
    }

    /**
     * Check that there was at least one email received that contains the search
     * text.
     *
     * @param content the text to search for in the body of the email
     *
     * @return the {@code WiserAssertions} instance
     */
    public WiserAssertions withContentContains(final String content) {
        StringBuilder messageContent = new StringBuilder();
        findFirstOrElseThrow((WiserMessage m) -> {
            ThrowingSupplier<String> contentAsString
                    = () -> getMimeMessageBody(m).trim();
            messageContent.append(unchecked(contentAsString));
            return unchecked(contentAsString).contains(content);
        }, assertionError(
                "No message with content containing [{0}] found! Was {1}",
                content, messageContent));
        return this;
    }

    /**
     * Returns the body of the message.
     *
     * @param message the message
     *
     * @return the body of the message
     *
     * @throws IOException        if error extracting the mime message
     * @throws MessagingException if the message type is not known
     */
    private String getMimeMessageBody(final WiserMessage message)
            throws IOException, MessagingException {
        Object content = getMimeMessage(message).getContent();
        if (content instanceof MimeMessage) {
            return content.toString();
        }
        if (content instanceof MimeMultipart) {
            return getMimeMultipartAsString((MimeMultipart) content);
        }
        throw new RuntimeException("Unexpected MimeMessage content");
    }

    /**
     * Checks that at least on message matches the predicate or the supplied
     * exception will be thrown.
     *
     * @param predicate         the condition a message must match
     * @param exceptionSupplier the supplier of the exception
     */
    private void findFirstOrElseThrow(
            final Predicate<WiserMessage> predicate,
            final Supplier<AssertionError> exceptionSupplier
    ) {
        messages.stream().filter(predicate)
                .findFirst().orElseThrow(exceptionSupplier);
    }

    /**
     * Returns the mime message within the {@link WiserMessage} converting any
     * {@link MessagingException}s into {@link RuntimeException}s.
     *
     * @param wiserMessage the message
     *
     * @return the mime message
     */
    private MimeMessage getMimeMessage(final WiserMessage wiserMessage) {
        return unchecked(wiserMessage::getMimeMessage);
    }

    /**
     * Returns a {@link Supplier} for an {@link AssertionError}.
     *
     * @param errorMessage the message for the exception
     * @param args         the parameters to insert into the message using
     *                     {@link MessageFormat}
     *
     * @return a supplier of an {@link AssertionError}
     */
    @SuppressWarnings(
            {"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
    private static Supplier<AssertionError> assertionError(
            final String errorMessage,
            final Object... args
    ) {
        return ()
                -> new AssertionError(MessageFormat.format(errorMessage, args));
    }

    /**
     * Convert any checked Exceptions into unchecked Exceptions.
     *
     * @param <T>      the item type to be returned after suppressing any
     *                 checked exceptions
     * @param supplier the source of the return value that could cause a checked
     *                 exception
     *
     * @return the product of the supplier
     */
    public static <T> T unchecked(final ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a {@link MimeMultipart} into a {@link String} stripping out the
     * mime part boundary and headers..
     *
     * @param mimeMultipart the message part to convert
     *
     * @return the message part as a string
     *
     * @throws MessagingException if the part is empty
     * @throws IOException        if there is another error
     */
    private String getMimeMultipartAsString(final MimeMultipart mimeMultipart)
            throws MessagingException, IOException {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            sb.append(mimeMultipart.getBodyPart(i).getContent());
        }
        return sb.toString();
    }

    /**
     * Interface for providing a value that could thrown an exception when
     * sought.
     *
     * @param <T> the type of value to be supplied
     */
    public interface ThrowingSupplier<T> {

        /**
         * Returns the value.
         *
         * @return the value
         *
         * @throws Throwable on error
         */
        T get() throws Throwable;
    }
}
