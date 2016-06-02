[![Build Status](https://travis-ci.org/kemitix/wiser-assertions.svg?branch=develop)](https://travis-ci.org/kemitix/wiser-assertions)
[![Coverage Status](https://coveralls.io/repos/github/kemitix/wiser-assertions/badge.svg?branch=develop)](https://coveralls.io/github/kemitix/wiser-assertions?branch=develop)

# wiser-assertions
Assertions for Wiser SMTP test server from subethamail

## Origin

Taken from Rafal Browiec [WiserAssertions] class.

## Usage

    @Before
    public void setUp() throws IOException {
        wiser = new Wiser(PORT);
        wiser.start();
    }

    @After
    public void tearDown() {
        wiser.stop();
    }

    @Test
    public void testMail() {
        //given
        ...

        //when
        ...

        //then
        WiserAssertions.assertReceivedMessage(wiser)
                .from(sender)
                .to(recipient_alpha)
                .to(recipient_beta)
                .withSubjectContains(subject_prefix)
                .withSubjectContains(subject_suffix)
                .withContentContains(message_element_1)
                .withContentContains(message_element_2)
                .withContentContains(message_element_3);
    }

[WiserAssertions]:http://blog.codeleak.pl/2014/09/testing-mail-code-in-spring-boot.html
