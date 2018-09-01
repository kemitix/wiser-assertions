/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Paul Campbell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.kemitix.wiser.assertions;

import net.kemitix.mon.maybe.Maybe;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Provides a set of assertions for checking the status of any messages received
 * by subethamail's Wiser.
 * <pre>
 * <code>
 * {@literal @}Before
 *  public void setUp() throws IOException {
 *      wiser = new Wiser(PORT);
 *      wiser.start();
 *  }
 *
 * {@literal @}After
 *  public void tearDown() {
 *      wiser.stop();
 *  }
 *
 * {@literal @}Test
 *  public void testMail() {
 *      //given ...
 *      //when ...
 *      //then
 *      WiserAssertions.assertReceivedMessage(wiser)
 *                     .from(sender)
 *                     .to(recipient_alpha)
 *                     .to(recipient_beta)
 *                     .withSubjectContains(subject_prefix)
 *                     .withSubjectContains(subject_suffix)
 *                     .withContentContains(message_element_1)
 *                     .withContentContains(message_element_2)
 *                     .withContentContains(message_element_3);
 *  }
 * </code>
 * </pre>
 */
@SuppressWarnings("methodcount")
public final class WiserAssertions {

    private static final String ERROR_MESSAGE_SUBJECT = "No message with subject [{0}] found!";
    private static final String ERROR_MESSAGE_CONTENT_CONTAINS = "No message with content containing [{0}] found!";
    private static final String ERROR_MESSAGE_CONTENT = "No message with content [{0}] found!";
    private static final String ERROR_MESSAGE_TO = "No message to [{0}] found!";

    /**
     * The messages received by Wiser.
     */
    private final List<WiserMessage> messages;

    /**
     * Private constructor.
     *
     * @param wiserMessages the messages to be tested by the assertions
     */
    private WiserAssertions(final List<WiserMessage> wiserMessages) {
        this.messages = wiserMessages;
    }

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
     * Checks that there was at least one email received that was sent from the
     * {@code sender}.
     *
     * @param sender email address to search for
     *
     * @return the {@code WiserAssertions} instance
     */
    public WiserAssertions from(final String sender) {
        messageMatches(m -> m.getEnvelopeSender().equals(sender))
                .orElseThrow(assertionError("No message from [{0}] found!", sender));
        return this;
    }

    private Optional<WiserMessage> messageMatches(final Predicate<WiserMessage> predicate) {
        return messages.stream()
                .filter(predicate)
                .findAny();
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
    private static Supplier<AssertionError> assertionError(final String errorMessage, final Object... args) {
        return () -> new AssertionError(MessageFormat.format(errorMessage, args));
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
        messageMatches(m -> m.getEnvelopeReceiver().equals(recipient))
                .orElseThrow(assertionError(ERROR_MESSAGE_TO, recipient));
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
        messageMatches(m -> subject(m).equals(subject))
                .orElseThrow(assertionError(ERROR_MESSAGE_SUBJECT, subject));
        return this;
    }

    private String subject(final WiserMessage wiserMessage) {
        try {
            return wiserMessage.getMimeMessage().getSubject();
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Invalid email message", e);
        }
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
        messageMatches(m -> subject(m).contains(subject))
                .orElseThrow(assertionError(ERROR_MESSAGE_SUBJECT, subject));
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
        messageMatches(m -> messageBody(m).trim().equals(content.trim()))
                .orElseThrow(assertionError(ERROR_MESSAGE_CONTENT, content));
        return this;
    }

    private String messageBody(final WiserMessage m) {
        try {
            return messageBody(m.getMimeMessage().getContent());
        } catch (IOException | MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String messageBody(final Object content) {
        return contentAsString(content)
                .or(() -> contentAsMimeMessage(content))
                .or(() -> contentAsMultiPartMime(content))
                .orElseThrow(() -> new RuntimeException("Unexpected MimeMessage content"));
    }

    private Maybe<String> contentAsString(final Object content) {
        if (content instanceof String) {
            return Maybe.just((String) content);
        }
        return Maybe.nothing();
    }

    private Maybe<String> contentAsMimeMessage(final Object content) {
        if (content instanceof MimeMessage) {
            return Maybe.just(content.toString());
        }
        return Maybe.nothing();
    }

    private Maybe<String> contentAsMultiPartMime(final Object content) {
        if (content instanceof MimeMultipart) {
            try {
                return Maybe.just(getMimeMultipartAsString((MimeMultipart) content));
            } catch (MessagingException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Maybe.nothing();
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
        messageMatches((WiserMessage m) -> messageBody(m).trim().contains(content))
                .orElseThrow(assertionError(ERROR_MESSAGE_CONTENT_CONTAINS, content));
        return this;
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
            Object content = mimeMultipart.getBodyPart(i).getContent();
            if (content instanceof MimeMultipart) {
                sb.append(getMimeMultipartAsString((MimeMultipart) content));
            } else {
                sb.append(content);
            }
        }
        return sb.toString();
    }

}
