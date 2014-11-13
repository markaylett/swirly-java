/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.doobry.domain.AssetType;
import org.doobry.domain.Contr;
import org.doobry.function.NullaryFunction;
import org.doobry.util.Queue;

public final class MockContr {

    private static final Map<String, NullaryFunction<Contr>> FACTORIES = new TreeMap<>();

    private static void put(final long id, final String mnem, final String display,
            final AssetType assetType, final String asset, final String ccy, final int tickNumer,
            final int tickDenom, final int lotNumer, final int lotDenom, final int pipDp,
            final long minLots, final long maxLots) {
        FACTORIES.put(mnem, new NullaryFunction<Contr>() {
            @Override
            public final Contr call() {
                return new Contr(id, mnem, display, assetType, asset, ccy, tickNumer, tickDenom,
                        lotNumer, lotDenom, pipDp, minLots, maxLots);
            }
        });
    }

    static {
        int i = 1;
        put(i++, "AUDUSD", "AUDUSD", AssetType.CURRENCY, "AUD", "USD", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "EURCHF", "EURCHF", AssetType.CURRENCY, "EUR", "CHF", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "EURCZK", "EURCZK", AssetType.CURRENCY, "EUR", "CZK", 1, 100, 1000000, 1, 2, 1, 10);
        put(i++, "EURDKK", "EURDKK", AssetType.CURRENCY, "EUR", "DKK", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "EURGBP", "EURGBP", AssetType.CURRENCY, "EUR", "GBP", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "EURHUF", "EURHUF", AssetType.CURRENCY, "EUR", "HUF", 1, 100, 1000000, 1, 2, 1, 10);
        put(i++, "EURJPY", "EURJPY", AssetType.CURRENCY, "EUR", "JPY", 1, 100, 1000000, 1, 2, 1, 10);
        put(i++, "EURNOK", "EURNOK", AssetType.CURRENCY, "EUR", "NOK", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "EURPLN", "EURPLN", AssetType.CURRENCY, "EUR", "PLN", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "EURRON", "EURRON", AssetType.CURRENCY, "EUR", "RON", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "EURSEK", "EURSEK", AssetType.CURRENCY, "EUR", "SEK", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "EURUSD", "EURUSD", AssetType.CURRENCY, "EUR", "USD", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "GBPUSD", "GBPUSD", AssetType.CURRENCY, "GBP", "USD", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "NZDUSD", "NZDUSD", AssetType.CURRENCY, "NZD", "USD", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "USDCAD", "USDCAD", AssetType.CURRENCY, "USD", "CAD", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "USDCHF", "USDCHF", AssetType.CURRENCY, "USD", "CHF", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "USDHKD", "USDHKD", AssetType.CURRENCY, "USD", "HKD", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "USDILS", "USDILS", AssetType.CURRENCY, "USD", "ILS", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "USDJPY", "USDJPY", AssetType.CURRENCY, "USD", "JPY", 1, 100, 1000000, 1, 2, 1, 10);
        put(i++, "USDMXN", "USDMXN", AssetType.CURRENCY, "USD", "MXN", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "USDSGD", "USDSGD", AssetType.CURRENCY, "USD", "SGD", 1, 10000, 1000000, 1, 4, 1,
                10);
        put(i++, "USDTHB", "USDTHB", AssetType.CURRENCY, "USD", "THB", 1, 100, 1000000, 1, 2, 1, 10);
        put(i++, "USDTRY", "USDTRY", AssetType.CURRENCY, "USD", "TRY", 1, 1000, 1000000, 1, 4, 1,
                10);
        put(i++, "USDZAR", "USDZAR", AssetType.CURRENCY, "USD", "ZAR", 1, 1000, 1000000, 1, 3, 1,
                10);
        put(i++, "ZC", "ZC", AssetType.COMMODITY, "ZC", "USD", 1, 400, 5000, 1, 2, 1, 10);
        put(i++, "ZS", "ZS", AssetType.COMMODITY, "ZS", "USD", 1, 400, 5000, 1, 2, 1, 10);
        put(i++, "ZW", "ZW", AssetType.COMMODITY, "ZW", "USD", 1, 400, 5000, 1, 2, 1, 10);
    }

    private MockContr() {
    }

    public static Contr newContr(String mnem) {
        return FACTORIES.get(mnem).call();
    }

    public static Contr newContrList() {
        final Queue q = new Queue();
        for (final Entry<String, NullaryFunction<Contr>> entry : FACTORIES.entrySet()) {
            q.insertBack(entry.getValue().call());
        }
        return (Contr) q.getFirst();
    }
}
