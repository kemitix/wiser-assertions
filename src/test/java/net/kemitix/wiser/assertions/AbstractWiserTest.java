package net.kemitix.wiser.assertions;

import org.junit.After;
import org.junit.Before;
import org.subethamail.wiser.Wiser;

import java.util.Properties;

import javax.mail.Session;

/**
 * Abstract base class for wiser tests.
 *
 * @author pcampbell
 */
public abstract class AbstractWiserTest {

    /**
     * Test mail server port.
     */
    protected static final int PORT = 12345;

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
     * Clean up after each test.
     */
    @After
    public void tearDown() {
        wiser.stop();
    }

    /**
     * Instantiates the WiserAssertions.
     *
     * @return the wiser assertions
     */
    protected WiserAssertions getAssertions() {
        return WiserAssertions.assertReceivedMessage(wiser);
    }

    protected Session getSession() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "localhost");
        properties.setProperty("mail.smtp.port", "" + WiserAssertionsTest.PORT);
        Session session = Session.getDefaultInstance(properties);
        return session;
    }

}
