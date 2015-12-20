/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.fx;

import static com.swirlycloud.swirly.mock.MockEcbRates.newEcbRates;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.swirlycloud.swirly.date.GregDate;

public final class EcbRatesTest {
    private static final double DELTA = 0.0000001;

    @Test
    public final void test() throws ParserConfigurationException, SAXException, IOException {

        final EcbRates ecbRates = newEcbRates();

        assertEquals(new GregDate(2014, 11, 29), ecbRates.getDate());
        assertEquals(1.2197, ecbRates.getRate("EUR", "USD"), DELTA);
        assertEquals(120.48864474870871, ecbRates.getRate("USD", "JPY"), DELTA);
        assertEquals(1.555343024738587, ecbRates.getRate("GBP", "USD"), DELTA);
        assertEquals(0.9861441338033944, ecbRates.getRate("USD", "CHF"), DELTA);

        assertEquals(1.0, ecbRates.getRate("EUR", "USD") * ecbRates.getRate("USD", "CHF")
                * ecbRates.getRate("CHF", "EUR"), DELTA);
    }
}
