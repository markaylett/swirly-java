/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import static org.doobry.mock.MockAsset.*;

import org.doobry.domain.Contr;

public final class MockContr {
    private MockContr() {
    }

    public static final Contr EURUSD = new Contr(1, "EURUSD", "EURUSD", EUR, USD, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr GBPUSD = new Contr(2, "GBPUSD", "GBPUSD", GBP, USD, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr AUDUSD = new Contr(3, "AUDUSD", "AUDUSD", AUD, USD, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr NZDUSD = new Contr(4, "NZDUSD", "NZDUSD", NZD, USD, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr USDCAD = new Contr(5, "USDCAD", "USDCAD", USD, CAD, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr USDCHF = new Contr(6, "USDCHF", "USDCHF", USD, CHF, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr USDTRY = new Contr(7, "USDTRY", "USDTRY", USD, TRY, 1, 1000, 1000000,
            1, 4, 1, 10);
    public static final Contr USDSGD = new Contr(8, "USDSGD", "USDSGD", USD, SGD, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr EURRON = new Contr(9, "EURRON", "EURRON", EUR, RON, 1, 1000, 1000000,
            1, 3, 1, 10);
    public static final Contr EURPLN = new Contr(10, "EURPLN", "EURPLN", EUR, PLN, 1, 1000,
            1000000, 1, 3, 1, 10);
    public static final Contr USDILS = new Contr(11, "USDILS", "USDILS", USD, ILS, 1, 1000,
            1000000, 1, 3, 1, 10);
    public static final Contr EURDKK = new Contr(12, "EURDKK", "EURDKK", EUR, DKK, 1, 1000,
            1000000, 1, 3, 1, 10);
    public static final Contr USDZAR = new Contr(13, "USDZAR", "USDZAR", USD, ZAR, 1, 1000,
            1000000, 1, 3, 1, 10);
    public static final Contr EURNOK = new Contr(14, "EURNOK", "EURNOK", EUR, NOK, 1, 1000,
            1000000, 1, 3, 1, 10);
    public static final Contr EURSEK = new Contr(15, "EURSEK", "EURSEK", EUR, SEK, 1, 1000,
            1000000, 1, 3, 1, 10);
    public static final Contr USDHKD = new Contr(16, "USDHKD", "USDHKD", USD, HKD, 1, 1000,
            1000000, 1, 3, 1, 10);
    public static final Contr USDMXN = new Contr(17, "USDMXN", "USDMXN", USD, MXN, 1, 1000,
            1000000, 1, 3, 1, 10);
    public static final Contr EURCZK = new Contr(18, "EURCZK", "EURCZK", EUR, CZK, 1, 100, 1000000,
            1, 2, 1, 10);
    public static final Contr USDTHB = new Contr(19, "USDTHB", "USDTHB", USD, THB, 1, 100, 1000000,
            1, 2, 1, 10);
    public static final Contr USDJPY = new Contr(20, "USDJPY", "USDJPY", USD, JPY, 1, 100, 1000000,
            1, 2, 1, 10);
    public static final Contr EURHUF = new Contr(21, "EURHUF", "EURHUF", EUR, HUF, 1, 100, 1000000,
            1, 2, 1, 10);

    // Crosses.

    public static final Contr EURGBP = new Contr(22, "EURGBP", "EURGBP", EUR, GBP, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr EURCHF = new Contr(23, "EURCHF", "EURCHF", EUR, CHF, 1, 10000,
            1000000, 1, 4, 1, 10);
    public static final Contr EURJPY = new Contr(24, "EURJPY", "EURJPY", EUR, JPY, 1, 100, 1000000,
            1, 2, 1, 10);

    public static final Contr ZC_USD = new Contr(25, "ZC", "ZC", ZC, USD, 1, 400, 5000, 1, 2, 1, 10);
    public static final Contr ZS_USD = new Contr(26, "ZS", "ZS", ZS, USD, 1, 400, 5000, 1, 2, 1, 10);
    public static final Contr ZW_USD = new Contr(27, "ZW", "ZW", ZW, USD, 1, 400, 5000, 1, 2, 1, 10);
}
