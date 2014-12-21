/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.AssetType;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;

public final class MockAsset {
    private static final Map<String, NullaryFunction<Asset>> FACTORIES = new TreeMap<>();

    private static void put(final long id, final String mnem, final String display,
            final AssetType type) {
        FACTORIES.put(mnem, new NullaryFunction<Asset>() {
            @Override
            public final Asset call() {
                return new Asset(id, mnem, display, type);
            }
        });
    }

    static {
        int i = 1;
        put(i++, "AUD", "Australia, Dollars", AssetType.CURRENCY);
        put(i++, "CAD", "Canada, Dollars", AssetType.CURRENCY);
        put(i++, "CHF", "Switzerland, Francs", AssetType.CURRENCY);
        put(i++, "CZK", "Czech Republic, Koruny", AssetType.CURRENCY);
        put(i++, "DKK", "Denmark, Kroner", AssetType.CURRENCY);
        put(i++, "EUR", "Euro Member Countries, Euro", AssetType.CURRENCY);
        put(i++, "GBP", "United Kingdom, Pounds", AssetType.CURRENCY);
        put(i++, "HKD", "Hong Kong, Dollars", AssetType.CURRENCY);
        put(i++, "HUF", "Hungary, Forint", AssetType.CURRENCY);
        put(i++, "ILS", "Israel, New Shekels", AssetType.CURRENCY);
        put(i++, "JPY", "Japan, Yen", AssetType.CURRENCY);
        put(i++, "MXN", "Mexico, Pesos", AssetType.CURRENCY);
        put(i++, "NOK", "Norway, Krone", AssetType.CURRENCY);
        put(i++, "NZD", "New Zealand, Dollars", AssetType.CURRENCY);
        put(i++, "PLN", "Poland, Zlotych", AssetType.CURRENCY);
        put(i++, "RON", "Romania, New Lei", AssetType.CURRENCY);
        put(i++, "SEK", "Sweden, Kronor", AssetType.CURRENCY);
        put(i++, "SGD", "Singapore, Dollars", AssetType.CURRENCY);
        put(i++, "THB", "Thailand, Baht", AssetType.CURRENCY);
        put(i++, "TRY", "Turkey, New Lira", AssetType.CURRENCY);
        put(i++, "USD", "United States of America, Dollars", AssetType.CURRENCY);
        put(i++, "ZAR", "South Africa, Rand", AssetType.CURRENCY);
        put(i++, "ZC", "Corn", AssetType.COMMODITY);
        put(i++, "ZS", "Soybeans", AssetType.COMMODITY);
        put(i++, "ZW", "Wheat", AssetType.COMMODITY);
    }

    private MockAsset() {
    }

    public static Asset newAsset(String mnem) {
        return FACTORIES.get(mnem).call();
    }

    public static void selectAsset(UnaryCallback<Asset> cb) {
        for (final Entry<String, NullaryFunction<Asset>> entry : FACTORIES.entrySet()) {
            cb.call(entry.getValue().call());
        }
    }
}
