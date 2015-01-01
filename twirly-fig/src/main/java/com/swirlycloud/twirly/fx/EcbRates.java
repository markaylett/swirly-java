/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.swirlycloud.twirly.date.GregDate;

public class EcbRates {

    private transient SAXParserFactory factory;
    private GregDate date;
    private final Map<String, Double> rates = new HashMap<>();

    private final class Handler extends DefaultHandler {

        @Override
        public final void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            if (!"Cube".equals(qName)) {
                return;
            }
            int i = attributes.getIndex("currency");
            if (i >= 0) {
                // Example: <Cube currency='USD' rate='1.2450'/>
                final String ccy = attributes.getValue(i);
                final Double rate = Double.valueOf(attributes.getValue("rate"));
                rates.put(ccy, rate);
                return;
            }
            i = attributes.getIndex("time");
            if (i >= 0) {
                // Example: <Cube time='2014-12-12'>
                final String val = attributes.getValue(i);
                final int year = Integer.parseInt(val.substring(0, 4));
                final int mon = Integer.parseInt(val.substring(5, 7)) - 1;
                final int mday = Integer.parseInt(val.substring(8, 10));
                date = new GregDate(year, mon, mday);
            }
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

    public final void parse() throws IOException, ParserConfigurationException, SAXException {
        parse("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
    }

    public final GregDate getDate() {
        return date;
    }

    public final double getRate(String lhs, String rhs) {
        double r;
        if ("EUR".equals(lhs)) {
            r = rates.get(rhs);
        } else if ("EUR".equals(rhs)) {
            r = 1.0 / rates.get(lhs);
        } else {
            r = rates.get(rhs) / rates.get(lhs);
        }
        return r;
    }
}
