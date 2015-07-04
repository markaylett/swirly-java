/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.AssetType;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.node.SlNode;

public final class MockAsset {
    private static final List<NullaryFunction<Asset>> LIST = new ArrayList<>();
    private static final Map<String, NullaryFunction<Asset>> MAP = new HashMap<>();

    private static void put(final @NonNull String mnem, final String display,
            final @NonNull AssetType type) {
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
    }

    private MockAsset() {
    }

    @SuppressWarnings("null")
    public static @NonNull Asset newAsset(String mnem) {
        return MAP.get(mnem).call();
    }

    public static SlNode selectAsset() {
        final SlQueue q = new SlQueue();
        for (final NullaryFunction<Asset> entry : LIST) {
            q.insertBack(entry.call());
        }
        return q.getFirst();
    }

    public static void selectAsset(UnaryCallback<Asset> cb) {
        for (final NullaryFunction<Asset> entry : LIST) {
            cb.call(entry.call());
        }
    }
}
