/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.function.UnaryFunction;
import com.swirlycloud.twirly.intrusive.MnemRbTree;
import com.swirlycloud.twirly.rec.Asset;
import com.swirlycloud.twirly.rec.AssetType;

public final class MockAsset {

    private static List<UnaryFunction<Asset, Factory>> LIST = new ArrayList<>();
    private static Map<String, UnaryFunction<Asset, Factory>> MAP = new HashMap<>();

    private static void put(final @NonNull String mnem, final String display,
            final @NonNull AssetType type) {
        final UnaryFunction<Asset, Factory> fn = new UnaryFunction<Asset, Factory>() {
            @Override
            public final Asset call(Factory factory) {
                return factory.newAsset(mnem, display, type);
            }
        };
        LIST.add(fn);
        MAP.put(mnem, fn);
    }

    static {
        // Forex.
        put("CHF", "Switzerland, Francs", AssetType.CURRENCY);
        put("EUR", "Euro Member Countries, Euro", AssetType.CURRENCY);
        put("GBP", "United Kingdom, Pounds", AssetType.CURRENCY);
        put("JPY", "Japan, Yen", AssetType.CURRENCY);
        put("USD", "United States of America, Dollars", AssetType.CURRENCY);
        put("ZAR", "South Africa, Rand", AssetType.CURRENCY);
        // Coal.
        put("CAP", "Central Appalachia Coal", AssetType.COMMODITY);
        put("NAP", "Northern Appalachia Coal", AssetType.COMMODITY);
        put("ILB", "Illinois Basin Coal", AssetType.COMMODITY);
        put("PRB", "Powder River Basin Coal", AssetType.COMMODITY);
        put("UIB", "Uinta Basin Coal", AssetType.COMMODITY);
        // Coffee.
        put("ETB", "Ethiopia, Birr", AssetType.CURRENCY);
        put("WYCA", "Yirgachefe A", AssetType.COMMODITY);
        put("WWNA", "Wenago A", AssetType.COMMODITY);
        put("WKCA", "Kochere A", AssetType.COMMODITY);
        put("WGAA", "Gelena Abaya A", AssetType.COMMODITY);
        // US Corporates.
        put("CSCO", "Cisco Systems Inc", AssetType.CORPORATE);
        put("DIS", "Walt Disney", AssetType.CORPORATE);
        put("IBM", "Ibm Corp", AssetType.CORPORATE);
        put("INTC", "Intel Corp", AssetType.CORPORATE);
        put("MSFT", "Microsoft Corp", AssetType.CORPORATE);
        put("VIA", "Viacom Inc", AssetType.CORPORATE);
        put("VOD", "Vodafone Group Plc", AssetType.CORPORATE);
        put("VZ", "Verizon Com", AssetType.CORPORATE);
     }

    private MockAsset() {
    }

    @SuppressWarnings("null")
    public static @NonNull Asset newAsset(String mnem, Factory factory) {
        return MAP.get(mnem).call(factory);
    }

    public static @NonNull MnemRbTree selectAsset(Factory factory) {
        final MnemRbTree t = new MnemRbTree();
        for (final UnaryFunction<Asset, Factory> entry : LIST) {
            final Asset asset = entry.call(factory);
            assert asset != null;
            t.insert(asset);
        }
        return t;
    }

    public static void selectAsset(Factory factory, UnaryCallback<Asset> cb) {
        for (final UnaryFunction<Asset, Factory> entry : LIST) {
            cb.call(entry.call(factory));
        }
    }
}
