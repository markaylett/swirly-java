/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    private transient SAXParserFactory factory;

    private static final class Handler extends DefaultHandler {

        @Override
        public final void characters(char[] ch, int start, int length) throws SAXException {
            System.out.println(new String(ch, start, length));
        }

        @Override
        public final void startDocument() throws SAXException {
            System.out.println("startDocument");
        }

        @Override
        public final void endDocument() throws SAXException {
            System.out.println("endDocument");
        }

        @Override
        public final void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            System.out.println("startElement: " + qName);
        }

        @Override
        public final void endElement(String uri, String localName, String qName)
                throws SAXException {
            System.out.println("endElement: " + qName);
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
}
