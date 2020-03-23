package net.kemitix.wiser.assertions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.subethamail.wiser.Wiser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Properties;

import javax.mail.Session;

abstract class AbstractWiserTest {

    private int port;

    private Wiser wiser;

    @BeforeEach
    public void setUp() throws IOException {
        port = findFreePort();
        wiser = new Wiser(port);
        wiser.start();
    }

    private int findFreePort() throws IOException {
        try(final ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(0));
            return serverSocket.getLocalPort();
        }
    }

    @AfterEach
    public void tearDown() {
        wiser.stop();
    }

    /**
     * Instantiates the WiserAssertions.
     *
     * @return the wiser assertions
     */
    protected WiserAssertions assertReceivedMessage() {
        return WiserAssertions.assertReceivedMessage(wiser);
    }

    protected Session getSession() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", "localhost");
        properties.setProperty("mail.smtp.port", "" + port);
        return Session.getInstance(properties);
    }

    /**
     * The test mail server port.
     *
     * @return the port he test mail server is running on
     */
    protected int getPort() {
        return port;
    }
}
