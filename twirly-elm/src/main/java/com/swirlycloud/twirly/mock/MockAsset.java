/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.AssetType;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;

public final class MockAsset {
    private static final List<NullaryFunction<Asset>> LIST = new ArrayList<>();
    private static final Map<String, NullaryFunction<Asset>> MAP = new HashMap<>();

    private static void put(final String mnem, final String display, final AssetType type) {
        final NullaryFunction<Asset> fn = new NullaryFunction<Asset>() {
            @Override
            public final Asset call() {
                return new Asset(mnem, display, type);
            }
        };
        LIST.add(fn);
        MAP.put(mnem, fn);
    }

    static {
        put("AUD", "Australia, Dollars", AssetType.CURRENCY);
        put("CAD", "Canada, Dollars", AssetType.CURRENCY);
        put("CHF", "Switzerland, Francs", AssetType.CURRENCY);
        put("CZK", "Czech Republic, Koruny", AssetType.CURRENCY);
        put("DKK", "Denmark, Kroner", AssetType.CURRENCY);
        put("EUR", "Euro Member Countries, Euro", AssetType.CURRENCY);
        put("GBP", "United Kingdom, Pounds", AssetType.CURRENCY);
        put("HKD", "Hong Kong, Dollars", AssetType.CURRENCY);
        put("HUF", "Hungary, Forint", AssetType.CURRENCY);
        put("ILS", "Israel, New Shekels", AssetType.CURRENCY);
        put("JPY", "Japan, Yen", AssetType.CURRENCY);
        put("MXN", "Mexico, Pesos", AssetType.CURRENCY);
        put("NOK", "Norway, Krone", AssetType.CURRENCY);
        put("NZD", "New Zealand, Dollars", AssetType.CURRENCY);
        put("PLN", "Poland, Zlotych", AssetType.CURRENCY);
        put("RON", "Romania, New Lei", AssetType.CURRENCY);
        put("SEK", "Sweden, Kronor", AssetType.CURRENCY);
        put("SGD", "Singapore, Dollars", AssetType.CURRENCY);
        put("THB", "Thailand, Baht", AssetType.CURRENCY);
        put("TRY", "Turkey, New Lira", AssetType.CURRENCY);
        put("USD", "United States of America, Dollars", AssetType.CURRENCY);
        put("ZAR", "South Africa, Rand", AssetType.CURRENCY);
        put("CAP", "Central Appalachia Coal", AssetType.COMMODITY);
        put("NAP", "Northern Appalachia Coal", AssetType.COMMODITY);
        put("ILB", "Illinois Basin Coal", AssetType.COMMODITY);
        put("PRB", "Powder River Basin Coal", AssetType.COMMODITY);
        put("UIB", "Uinta Basin Coal", AssetType.COMMODITY);
    }

    private MockAsset() {
    }

    public static Asset newAsset(String mnem) {
        return MAP.get(mnem).call();
    }

    public static void selectAsset(UnaryCallback<Asset> cb) {
        for (final NullaryFunction<Asset> entry : LIST) {
            cb.call(entry.call());
        }
    }
}
