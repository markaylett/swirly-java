/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.swirlycloud.twirly.date.GregDate;

public final class YieldCurveTest {
    private static final double DELTA = 0.000000000001;
    private static final String EXAMPLE = "<?xml version=\"1.0\" standalone=\"yes\" ?>\n" //
            + "<interestRateCurve>\n" //
            + "  <effectiveasof>2014-12-25</effectiveasof>\n" //
            + "  <currency>GBP</currency>\n" //
            + "  <baddayconvention>M</baddayconvention>\n" //
            + "  <deposits>\n" //
            + "    <daycountconvention>ACT/365</daycountconvention>\n" //
            + "    <snaptime>2014-12-24T16:00:00.000Z</snaptime>\n" //
            + "    <spotdate>2014-12-25</spotdate>\n" //
            + "    <calendars>\n" //
            + "      <calendar>none</calendar>\n" //
            + "    </calendars>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>1M</tenor>\n" //
            + "      <maturitydate>2015-01-26</maturitydate>\n" //
            + "      <parrate>0.005016</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>2M</tenor>\n" //
            + "      <maturitydate>2015-02-25</maturitydate>\n" //
            + "      <parrate>0.00524</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>3M</tenor>\n" //
            + "      <maturitydate>2015-03-25</maturitydate>\n" //
            + "      <parrate>0.005596</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>6M</tenor>\n" //
            + "      <maturitydate>2015-06-25</maturitydate>\n" //
            + "      <parrate>0.006823</parrate>\n" //
            + "      </curvepoint><curvepoint>\n" //
            + "      <tenor>1Y</tenor>\n" //
            + "      <maturitydate>2015-12-25</maturitydate>\n" //
            + "      <parrate>0.009774</parrate>\n" //
            + "    </curvepoint>\n" //
            + "  </deposits>\n" //
            + "  <swaps>\n" //
            + "    <fixeddaycountconvention>ACT/365</fixeddaycountconvention>\n" //
            + "    <floatingdaycountconvention>ACT/365</floatingdaycountconvention>\n" //
            + "    <fixedpaymentfrequency>6M</fixedpaymentfrequency>\n" //
            + "    <floatingpaymentfrequency>6M</floatingpaymentfrequency>\n" //
            + "    <snaptime>2014-12-24T16:00:00.000Z</snaptime>\n" //
            + "    <spotdate>2014-12-25</spotdate>\n" //
            + "    <calendars>\n" //
            + "      <calendar>none</calendar>\n" //
            + "    </calendars>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>2Y</tenor>\n" //
            + "      <maturitydate>2016-12-25</maturitydate>\n" //
            + "      <parrate>0.00973</parrate>\n" //
            + "    </curvepoint><curvepoint>\n" //
            + "      <tenor>3Y</tenor\n" //
            + "      ><maturitydate>2017-12-25</maturitydate>\n" //
            + "      <parrate>0.012</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>4Y</tenor>\n" //
            + "      <maturitydate>2018-12-25</maturitydate>\n" //
            + "      <parrate>0.01387</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>5Y</tenor>\n" //
            + "      <maturitydate>2019-12-25</maturitydate>\n" //
            + "      <parrate>0.01539</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>6Y</tenor>\n" //
            + "      <maturitydate>2020-12-25</maturitydate>\n" //
            + "      <parrate>0.01655</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>7Y</tenor>\n" //
            + "      <maturitydate>2021-12-25</maturitydate>\n" //
            + "      <parrate>0.01751</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>8Y</tenor>\n" //
            + "      <maturitydate>2022-12-25</maturitydate>\n" //
            + "      <parrate>0.01833</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>9Y</tenor>\n" //
            + "      <maturitydate>2023-12-25</maturitydate>\n" //
            + "      <parrate>0.01904</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>10Y</tenor>\n" //
            + "      <maturitydate>2024-12-25</maturitydate>\n" //
            + "      <parrate>0.01966</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>12Y</tenor>\n" //
            + "      <maturitydate>2026-12-25</maturitydate>\n" //
            + "      <parrate>0.02076</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>15Y</tenor>\n" //
            + "      <maturitydate>2029-12-25</maturitydate>\n" //
            + "      <parrate>0.02196</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>20Y</tenor>\n" //
            + "      <maturitydate>2034-12-25</maturitydate>\n" //
            + "      <parrate>0.02316</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>25Y</tenor>\n" //
            + "      <maturitydate>2039-12-25</maturitydate>\n" //
            + "      <parrate>0.02354</parrate>\n" //
            + "    </curvepoint>\n" //
            + "    <curvepoint>\n" //
            + "      <tenor>30Y</tenor>\n" //
            + "      <maturitydate>2044-12-25</maturitydate>\n" //
            + "      <parrate>0.02369</parrate>\n" //
            + "    </curvepoint>\n" //
            + "  </swaps>\n" //
            + "</interestRateCurve>\n";

    @Test
    public final void test() throws IOException, ParserConfigurationException, SAXException {

        final InputSource is = new InputSource(new StringReader(EXAMPLE));
        final YieldCurve yc = new YieldCurve();
        //yc.parse("USD", GregDate.valueOfIso(20141224));
        yc.parse(is);

        assertEquals(new GregDate(2014, 11, 25), yc.getEffectiveAsOf());
        assertEquals("GBP", yc.getCcy());
        assertEquals("ACT/365", yc.getDayCountConvention());
        assertEquals(new GregDate(2014, 11, 25), yc.getSpotDate());

        CurvePoint cp = yc.getCurvePoint("1M");
        assertEquals("1M", cp.getTenor());
        assertEquals(new GregDate(2015, 0, 26), cp.getMaturityDate());
        assertEquals(0.005016, cp.getParRate(), DELTA);
    }
}
