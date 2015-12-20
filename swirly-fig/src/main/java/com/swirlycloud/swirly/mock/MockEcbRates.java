/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.mock;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.swirlycloud.swirly.fx.EcbRates;

public final class MockEcbRates {
    // http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
    private static final String MOCK = "<gesmes:Envelope xmlns:gesmes=\"http://www.gesmes.org/xml/2002-08-01\" xmlns=\"http://www.ecb.int/vocabulary/2002-08-01/eurofxref\">\n" //
            + "<gesmes:subject>Reference rates</gesmes:subject>\n" //
            + "<gesmes:Sender>\n" //
            + "<gesmes:name>European Central Bank</gesmes:name>\n" //
            + "</gesmes:Sender>\n" //
            + "<Cube>\n" //
            + "<Cube time=\"2014-12-29\">\n" //
            + "<Cube currency=\"USD\" rate=\"1.2197\"/>\n" //
            + "<Cube currency=\"JPY\" rate=\"146.96\"/>\n" //
            + "<Cube currency=\"BGN\" rate=\"1.9558\"/>\n" //
            + "<Cube currency=\"CZK\" rate=\"27.717\"/>\n" //
            + "<Cube currency=\"DKK\" rate=\"7.4404\"/>\n" //
            + "<Cube currency=\"GBP\" rate=\"0.78420\"/>\n" //
            + "<Cube currency=\"HUF\" rate=\"314.68\"/>\n" //
            + "<Cube currency=\"LTL\" rate=\"3.45280\"/>\n" //
            + "<Cube currency=\"PLN\" rate=\"4.3023\"/>\n" //
            + "<Cube currency=\"RON\" rate=\"4.4741\"/>\n" //
            + "<Cube currency=\"SEK\" rate=\"9.6234\"/>\n" //
            + "<Cube currency=\"CHF\" rate=\"1.2028\"/>\n" //
            + "<Cube currency=\"NOK\" rate=\"9.0475\"/>\n" //
            + "<Cube currency=\"HRK\" rate=\"7.6580\"/>\n" //
            + "<Cube currency=\"RUB\" rate=\"68.0650\"/>\n" //
            + "<Cube currency=\"TRY\" rate=\"2.8313\"/>\n" //
            + "<Cube currency=\"AUD\" rate=\"1.4964\"/>\n" //
            + "<Cube currency=\"BRL\" rate=\"3.2602\"/>\n" //
            + "<Cube currency=\"CAD\" rate=\"1.4183\"/>\n" //
            + "<Cube currency=\"CNY\" rate=\"7.5920\"/>\n" //
            + "<Cube currency=\"HKD\" rate=\"9.4632\"/>\n" //
            + "<Cube currency=\"IDR\" rate=\"15128.58\"/>\n" //
            + "<Cube currency=\"ILS\" rate=\"4.7916\"/>\n" //
            + "<Cube currency=\"INR\" rate=\"77.6660\"/>\n" //
            + "<Cube currency=\"KRW\" rate=\"1341.07\"/>\n" //
            + "<Cube currency=\"MXN\" rate=\"17.9281\"/>\n" //
            + "<Cube currency=\"MYR\" rate=\"4.2659\"/>\n" //
            + "<Cube currency=\"NZD\" rate=\"1.5644\"/>\n" //
            + "<Cube currency=\"PHP\" rate=\"54.558\"/>\n" //
            + "<Cube currency=\"SGD\" rate=\"1.6134\"/>\n" //
            + "<Cube currency=\"THB\" rate=\"40.223\"/>\n" //
            + "<Cube currency=\"ZAR\" rate=\"14.1557\"/>\n" //
            + "</Cube>\n" //
            + "</Cube>\n" //
            + "</gesmes:Envelope>";

    private MockEcbRates() {
    }

    public static EcbRates newEcbRates()
            throws ParserConfigurationException, SAXException, IOException {
        final InputSource is = new InputSource(new StringReader(MOCK));
        final EcbRates ecbRates = new EcbRates();
        ecbRates.parse(is);
        return ecbRates;
    }
}
