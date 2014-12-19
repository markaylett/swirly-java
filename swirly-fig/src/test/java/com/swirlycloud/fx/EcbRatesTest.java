/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.fx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.swirlycloud.date.GregDate;

public final class EcbRatesTest {
    private static final double DELTA = 0.0000001;
    // http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
    private static final String EXAMPLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" //
            + "<gesmes:Envelope xmlns:gesmes=\"http://www.gesmes.org/xml/2002-08-01\" xmlns=\"http://www.ecb.int/vocabulary/2002-08-01/eurofxref\">\n" //
            + "    <gesmes:subject>Reference rates</gesmes:subject>\n" //
            + "    <gesmes:Sender>\n" //
            + "        <gesmes:name>European Central Bank</gesmes:name>\n" //
            + "    </gesmes:Sender>\n" //
            + "    <Cube>\n" //
            + "        <Cube time='2014-12-18'>\n" //
            + "            <Cube currency='USD' rate='1.2285'/>\n" //
            + "            <Cube currency='JPY' rate='145.96'/>\n" //
            + "            <Cube currency='BGN' rate='1.9558'/>\n" //
            + "            <Cube currency='CZK' rate='27.606'/>\n" //
            + "            <Cube currency='DKK' rate='7.4393'/>\n" //
            + "            <Cube currency='GBP' rate='0.78650'/>\n" //
            + "            <Cube currency='HUF' rate='314.73'/>\n" //
            + "            <Cube currency='LTL' rate='3.45280'/>\n" //
            + "            <Cube currency='PLN' rate='4.2422'/>\n" //
            + "            <Cube currency='RON' rate='4.4713'/>\n" //
            + "            <Cube currency='SEK' rate='9.4361'/>\n" //
            + "            <Cube currency='CHF' rate='1.2052'/>\n" //
            + "            <Cube currency='NOK' rate='9.0645'/>\n" //
            + "            <Cube currency='HRK' rate='7.6675'/>\n" //
            + "            <Cube currency='RUB' rate='75.4850'/>\n" //
            + "            <Cube currency='TRY' rate='2.8533'/>\n" //
            + "            <Cube currency='AUD' rate='1.5005'/>\n" //
            + "            <Cube currency='BRL' rate='3.2777'/>\n" //
            + "            <Cube currency='CAD' rate='1.4248'/>\n" //
            + "            <Cube currency='CNY' rate='7.6364'/>\n" //
            + "            <Cube currency='HKD' rate='9.5292'/>\n" //
            + "            <Cube currency='IDR' rate='15440.50'/>\n" //
            + "            <Cube currency='ILS' rate='4.8515'/>\n" //
            + "            <Cube currency='INR' rate='77.4772'/>\n" //
            + "            <Cube currency='KRW' rate='1347.16'/>\n" //
            + "            <Cube currency='MXN' rate='17.8120'/>\n" //
            + "            <Cube currency='MYR' rate='4.2521'/>\n" //
            + "            <Cube currency='NZD' rate='1.5861'/>\n" //
            + "            <Cube currency='PHP' rate='54.972'/>\n" //
            + "            <Cube currency='SGD' rate='1.6149'/>\n" //
            + "            <Cube currency='THB' rate='40.376'/>\n" //
            + "            <Cube currency='ZAR' rate='14.1879'/>\n" //
            + "        </Cube>\n" //
            + "    </Cube>\n" //
            + "</gesmes:Envelope>";

    @Test
    public final void test() throws IOException, ParserConfigurationException, SAXException {

        final InputSource is = new InputSource(new StringReader(EXAMPLE));
        final EcbRates ecbRates = new EcbRates();
        ecbRates.parse(is);

        assertEquals(new GregDate(2014, 11, 18), ecbRates.getDate());
        assertNull(ecbRates.getRate("EUR"));
        assertEquals(1.2285, ecbRates.getRate("USD").doubleValue(), DELTA);
        assertEquals(145.96, ecbRates.getRate("JPY").doubleValue(), DELTA);
    }
}
