/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.itest;

import static com.swirlycloud.twirly.fx.PriceUtil.fwdPrice;

import java.io.IOException;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.swirlycloud.twirly.date.GregDate;
import com.swirlycloud.twirly.fx.EcbRates;
import com.swirlycloud.twirly.fx.IrCalc;
import com.swirlycloud.twirly.fx.IrCurve;

public final class FwdPrice {
    private static IrCurve parseCurve(String ccy, GregDate gd)
            throws IOException, ParserConfigurationException, SAXException {
        final IrCurve irc = new IrCurve();
        for (;;) {
            if (irc.parse(ccy, gd)) {
                break;
            }
            gd = gd.addDays(-1);
        }
        return irc;

    }

    public static void main(String[] args) throws Exception {

        final GregDate today = GregDate.valueOf(Calendar.getInstance());

        final EcbRates ecbRates = new EcbRates();
        ecbRates.parse();

        final double spotPrice = ecbRates.getRate("GBP", "USD");
        System.out.printf("spot: %.6f\n", spotPrice);

        final IrCurve gbpCurve = parseCurve("GBP", today);
        final IrCurve usdCurve = parseCurve("USD", today);

        final GregDate spotDate = today.addDays(2);
        GregDate settlDate = spotDate;
        for (int i = 0; i < 12; ++i) {
            settlDate = settlDate.addMonths(1);
            final double gbpFv = gbpCurve.fv(spotDate, settlDate, IrCalc.SIMPLE_INTEREST);
            final double gbpFr = gbpCurve.fr(spotDate, settlDate, IrCalc.SIMPLE_INTEREST);
            final double usdFv = usdCurve.fv(spotDate, settlDate, IrCalc.SIMPLE_INTEREST);
            final double usdFr = usdCurve.fr(spotDate, settlDate, IrCalc.SIMPLE_INTEREST);
            final double fwdPrice = fwdPrice(spotPrice, gbpFv, usdFv);
            System.out.printf("%2dM: %.6f/%.6f => %.6f\n", i + 1, gbpFr, usdFr,
                    fwdPrice - spotPrice);
        }
    }
}
