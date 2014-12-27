/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.swirlycloud.twirly.date.GregDate;

public final class YieldCurve {

    private enum Element {
        baddayconvention, //
        calendar, //
        calendars, //
        currency, //
        curvepoint, //
        daycountconvention, //
        deposits, //
        effectiveasof, //
        fixeddaycountconvention, //
        fixedpaymentfrequency, //
        floatingdaycountconvention, //
        floatingpaymentfrequency, //
        interestRateCurve, //
        maturitydate, //
        parrate, //
        snaptime, //
        spotdate, //
        swaps, //
        tenor //
    }

    private transient SAXParserFactory factory;
    private GregDate effectiveAsOf;
    private String ccy;
    private String dayCountConvention;
    private GregDate spotDate;
    private final Map<String, CurvePoint> curvePoints = new HashMap<>();

    private static GregDate parseDate(char[] ch, int start, int length) {
        final int year = Integer.parseInt(new String(ch, start + 0, 4));
        final int mon = Integer.parseInt(new String(ch, start + 5, 2)) - 1;
        final int mday = Integer.parseInt(new String(ch, start + 8, 2));
        return new GregDate(year, mon, mday);
    }

    private final class Handler extends DefaultHandler {

        private Element element;
        private boolean deposits = false;

        // Curve Point.
        private String tenor;
        private GregDate maturityDate;
        private double parRate;

        @Override
        public final void characters(char[] ch, int start, int length) throws SAXException {
            if (element == null) {
                return;
            }
            switch (element) {
            case baddayconvention:
                break;
            case calendar:
                break;
            case calendars:
                break;
            case currency:
                ccy = new String(ch, start, length);
                break;
            case curvepoint:
                break;
            case daycountconvention:
                dayCountConvention = new String(ch, start, length);
                break;
            case deposits:
                deposits = true;
                break;
            case effectiveasof:
                effectiveAsOf = parseDate(ch, start, length);
                break;
            case fixeddaycountconvention:
                break;
            case fixedpaymentfrequency:
                break;
            case floatingdaycountconvention:
                break;
            case floatingpaymentfrequency:
                break;
            case interestRateCurve:
                break;
            case maturitydate:
                maturityDate = parseDate(ch, start, length);
                break;
            case parrate:
                parRate = Double.parseDouble(new String(ch, start, length));
                break;
            case snaptime:
                break;
            case spotdate:
                if (deposits) {
                    spotDate = parseDate(ch, start, length);
                }
                break;
            case swaps:
                break;
            case tenor:
                tenor = new String(ch, start, length);
                break;
            }
        }

        @Override
        public final void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            element = Element.valueOf(qName);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (Element.valueOf(qName)) {
            case baddayconvention:
                break;
            case calendar:
                break;
            case calendars:
                break;
            case currency:
                break;
            case curvepoint:
                if (deposits) {
                    curvePoints.put(tenor, new CurvePoint(tenor, maturityDate, parRate));
                }
                break;
            case daycountconvention:
                break;
            case deposits:
                deposits = false;
                break;
            case effectiveasof:
                break;
            case fixeddaycountconvention:
                break;
            case fixedpaymentfrequency:
                break;
            case floatingdaycountconvention:
                break;
            case floatingpaymentfrequency:
                break;
            case interestRateCurve:
                break;
            case maturitydate:
                break;
            case parrate:
                break;
            case snaptime:
                break;
            case spotdate:
                break;
            case swaps:
                break;
            case tenor:
                break;
            }
            element = null;
        }
    }

    private final SAXParser newSaxParser() throws ParserConfigurationException, SAXException {
        if (factory == null) {
            factory = SAXParserFactory.newInstance();
        }
        return factory.newSAXParser();
    }

    public final void parse(InputStream is) throws IOException, ParserConfigurationException,
            SAXException {
        final SAXParser parser = newSaxParser();
        parser.parse(is, new Handler());
    }

    public final void parse(String uri) throws IOException, ParserConfigurationException,
            SAXException {
        final SAXParser parser = newSaxParser();
        parser.parse(uri, new Handler());
    }

    public final void parse(File f) throws IOException, ParserConfigurationException, SAXException {
        final SAXParser parser = newSaxParser();
        parser.parse(f, new Handler());
    }

    public final void parse(InputSource is) throws IOException, ParserConfigurationException,
            SAXException {
        final SAXParser parser = newSaxParser();
        parser.parse(is, new Handler());
    }

    public final void parse(String ccy, GregDate date) throws Exception {
        final URL url = new URL(String.format(
                "https://www.markit.com/news/InterestRates_%s_%d.zip", ccy, date.toIso()));
        try (final ZipInputStream is = new ZipInputStream(url.openStream())) {
            ZipEntry entry = is.getNextEntry();
            while (entry != null) {
                if (entry.getName().startsWith("InterestRates_")) {
                    final SAXParser parser = newSaxParser();
                    parser.parse(is, new Handler());
                    break;
                }
                entry = is.getNextEntry();
            }
        }
    }

    public final GregDate getEffectiveAsOf() {
        return effectiveAsOf;
    }

    public final String getCcy() {
        return ccy;
    }

    public final String getDayCountConvention() {
        return dayCountConvention;
    }

    public final GregDate getSpotDate() {
        return spotDate;
    }

    public final CurvePoint getCurvePoint(String tenor) {
        return curvePoints.get(tenor);
    }
}
