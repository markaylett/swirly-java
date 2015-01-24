/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import static com.swirlycloud.twirly.math.MathUtil.linearInterp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

public final class IrCurve {

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
    private DayCount dayCount;
    private GregDate spotDate;
    private double[] xs, ys;

    private static GregDate parseDate(char[] ch, int start, int length) {
        final int year = Integer.parseInt(new String(ch, start + 0, 4));
        final int mon = Integer.parseInt(new String(ch, start + 5, 2)) - 1;
        final int mday = Integer.parseInt(new String(ch, start + 8, 2));
        return new GregDate(year, mon, mday);
    }

    private static DayCount parseDayCount(char[] ch, int start, int length) {
        final String s = new String(ch, start, length);
        if ("ACT/360".equals(s)) {
            return DayCount.ACTUAL360;
        } else if ("ACT/365".equals(s)) {
            return DayCount.ACTUAL365FIXED;
        } else {
            throw new IllegalArgumentException(
                    String.format("invalid day-count convention '%s'", s));
        }
    }

    private final class Handler extends DefaultHandler {

        private Element element;
        private boolean deposits = false;

        // Curve Point.
        private String tenor;
        private GregDate maturityDate;
        private double parRate;

        private final List<IrPoint> irPoints = new ArrayList<>();

        private final void createArrays() {
            final int n = irPoints.size();
            xs = new double[n];
            ys = new double[n];
            for (int i = 0; i < n; ++i) {
                final IrPoint cp = irPoints.get(i);
                xs[i] = dayCount.diffYears(spotDate, cp.getMaturityDate());
                ys[i] = cp.getParRate();
            }
        }

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
                dayCount = parseDayCount(ch, start, length);
                break;
            case deposits:
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
            switch (element) {
            case baddayconvention:
                break;
            case calendar:
                break;
            case calendars:
                break;
            case currency:
                break;
            case curvepoint:
                break;
            case daycountconvention:
                break;
            case deposits:
                deposits = true;
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
        }

        @Override
        public final void endElement(String uri, String localName, String qName)
                throws SAXException {
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
                    irPoints.add(new IrPoint(tenor, maturityDate, parRate));
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
                createArrays();
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

    public final void parse(InputStream is) throws ParserConfigurationException, SAXException,
            IOException {
        final SAXParser parser = newSaxParser();
        parser.parse(is, new Handler());
    }

    public final void parse(String uri) throws ParserConfigurationException, SAXException,
            IOException {
        final SAXParser parser = newSaxParser();
        parser.parse(uri, new Handler());
    }

    public final void parse(File f) throws ParserConfigurationException, SAXException, IOException {
        final SAXParser parser = newSaxParser();
        parser.parse(f, new Handler());
    }

    public final void parse(InputSource is) throws ParserConfigurationException, SAXException,
            IOException {
        final SAXParser parser = newSaxParser();
        parser.parse(is, new Handler());
    }

    public final boolean parse(String ccy, GregDate gd) throws IOException,
            ParserConfigurationException, SAXException {
        boolean success = false;
        final URL url = new URL(String.format(
                "https://www.markit.com/news/InterestRates_%s_%d.zip", ccy, gd.toIso()));
        try (final ZipInputStream is = new ZipInputStream(url.openStream())) {
            ZipEntry entry = is.getNextEntry();
            while (entry != null) {
                if (entry.getName().startsWith("InterestRates_")) {
                    final SAXParser parser = newSaxParser();
                    parser.parse(is, new Handler());
                    success = true;
                    break;
                }
                entry = is.getNextEntry();
            }
        }
        return success;
    }

    public final GregDate getEffectiveAsOf() {
        return effectiveAsOf;
    }

    public final String getCcy() {
        return ccy;
    }

    public final DayCount getDayCount() {
        return dayCount;
    }

    public final GregDate getSpotDate() {
        return spotDate;
    }

    public final double getTime(GregDate gd) {
        return dayCount.diffYears(spotDate, gd);
    }

    public final double ir(double t) {
        return linearInterp(xs, ys, t);
    }

    public final double ir(GregDate gd) {
        return ir(getTime(gd));
    }

    public double fr(double t1, double t2, IrCalc irc) {
        final double r1 = ir(t1);
        final double r2 = ir(t2);
        return irc.fr(r1, t1, r2, t2);
    }

    public double fr(GregDate gd1, GregDate gd2, IrCalc irc) {
        return fr(getTime(gd1), getTime(gd2), irc);
    }

    public double fv(double t1, double t2, IrCalc irc) {
        final double r1 = ir(t1);
        final double r2 = ir(t2);
        return irc.fv(r1, t1, r2, t2);
    }

    public double fv(GregDate gd1, GregDate gd2, IrCalc irc) {
        return fv(getTime(gd1), getTime(gd2), irc);
    }
}
