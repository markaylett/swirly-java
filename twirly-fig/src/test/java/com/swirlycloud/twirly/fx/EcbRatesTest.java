/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import static com.swirlycloud.twirly.mock.MockEcbRates.newEcbRates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.swirlycloud.twirly.date.GregDate;

public final class EcbRatesTest {
    private static final double DELTA = 0.0000001;
    @Test
    public final void test() throws IOException, ParserConfigurationException, SAXException {

        final EcbRates ecbRates = newEcbRates();

        assertEquals(new GregDate(2014, 11, 29), ecbRates.getDate());
        assertNull(ecbRates.getRate("EUR"));
        assertEquals(1.2197, ecbRates.getRate("USD").doubleValue(), DELTA);
        assertEquals(146.96, ecbRates.getRate("JPY").doubleValue(), DELTA);
    }
}
