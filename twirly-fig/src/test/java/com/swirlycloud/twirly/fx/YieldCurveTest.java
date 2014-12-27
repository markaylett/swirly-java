/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.swirlycloud.twirly.date.GregDate;

public final class YieldCurveTest {
    private static final double DELTA = 0.0000001;
    private static final String EXAMPLE = "<?xml version=\"1.0\" standalone=\"yes\" ?><interestRateCurve><effectiveasof>2014-12-25</effectiveasof><currency>GBP</currency><baddayconvention>M</baddayconvention><deposits><daycountconvention>ACT/365</daycountconvention><snaptime>2014-12-24T16:00:00.000Z</snaptime><spotdate>2014-12-25</spotdate><calendars><calendar>none</calendar></calendars><curvepoint><tenor>1M</tenor><maturitydate>2015-01-26</maturitydate><parrate>0.005016</parrate></curvepoint><curvepoint><tenor>2M</tenor><maturitydate>2015-02-25</maturitydate><parrate>0.00524</parrate></curvepoint><curvepoint><tenor>3M</tenor><maturitydate>2015-03-25</maturitydate><parrate>0.005596</parrate></curvepoint><curvepoint><tenor>6M</tenor><maturitydate>2015-06-25</maturitydate><parrate>0.006823</parrate></curvepoint><curvepoint><tenor>1Y</tenor><maturitydate>2015-12-25</maturitydate><parrate>0.009774</parrate></curvepoint></deposits><swaps><fixeddaycountconvention>ACT/365</fixeddaycountconvention><floatingdaycountconvention>ACT/365</floatingdaycountconvention><fixedpaymentfrequency>6M</fixedpaymentfrequency><floatingpaymentfrequency>6M</floatingpaymentfrequency><snaptime>2014-12-24T16:00:00.000Z</snaptime><spotdate>2014-12-25</spotdate><calendars><calendar>none</calendar></calendars><curvepoint><tenor>2Y</tenor><maturitydate>2016-12-25</maturitydate><parrate>0.00973</parrate></curvepoint><curvepoint><tenor>3Y</tenor><maturitydate>2017-12-25</maturitydate><parrate>0.012</parrate></curvepoint><curvepoint><tenor>4Y</tenor><maturitydate>2018-12-25</maturitydate><parrate>0.01387</parrate></curvepoint><curvepoint><tenor>5Y</tenor><maturitydate>2019-12-25</maturitydate><parrate>0.01539</parrate></curvepoint><curvepoint><tenor>6Y</tenor><maturitydate>2020-12-25</maturitydate><parrate>0.01655</parrate></curvepoint><curvepoint><tenor>7Y</tenor><maturitydate>2021-12-25</maturitydate><parrate>0.01751</parrate></curvepoint><curvepoint><tenor>8Y</tenor><maturitydate>2022-12-25</maturitydate><parrate>0.01833</parrate></curvepoint><curvepoint><tenor>9Y</tenor><maturitydate>2023-12-25</maturitydate><parrate>0.01904</parrate></curvepoint><curvepoint><tenor>10Y</tenor><maturitydate>2024-12-25</maturitydate><parrate>0.01966</parrate></curvepoint><curvepoint><tenor>12Y</tenor><maturitydate>2026-12-25</maturitydate><parrate>0.02076</parrate></curvepoint><curvepoint><tenor>15Y</tenor><maturitydate>2029-12-25</maturitydate><parrate>0.02196</parrate></curvepoint><curvepoint><tenor>20Y</tenor><maturitydate>2034-12-25</maturitydate><parrate>0.02316</parrate></curvepoint><curvepoint><tenor>25Y</tenor><maturitydate>2039-12-25</maturitydate><parrate>0.02354</parrate></curvepoint><curvepoint><tenor>30Y</tenor><maturitydate>2044-12-25</maturitydate><parrate>0.02369</parrate></curvepoint></swaps></interestRateCurve>";

    @Test
    public final void test() throws IOException, ParserConfigurationException, SAXException {

        final InputSource is = new InputSource(new StringReader(EXAMPLE));
        final YieldCurve yc = new YieldCurve();
        //yc.parse("USD", GregDate.valueOfIso(20141224));
        yc.parse(is);
    }
}
