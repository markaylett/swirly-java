/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.domain.Asset;
import com.swirlycloud.twirly.domain.AssetType;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.function.NullaryFunction;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.intrusive.MnemRbTree;

public final class MockAsset {

    private final Factory factory;
    private final List<NullaryFunction<Asset>> list = new ArrayList<>();
    private final Map<String, NullaryFunction<Asset>> map = new HashMap<>();

    private final void put(final @NonNull String mnem, final String display,
            final @NonNull AssetType type) {
        final NullaryFunction<Asset> fn = new NullaryFunction<Asset>() {
            @Override
            public final Asset call() {
                return factory.newAsset(mnem, display, type);
            }
        };
        list.add(fn);
        map.put(mnem, fn);
    }

    public MockAsset(Factory factory) {
        this.factory = factory;
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

    @SuppressWarnings("null")
    public final @NonNull Asset newAsset(String mnem) {
        return map.get(mnem).call();
    }

    public final @NonNull MnemRbTree selectAsset() {
        final MnemRbTree t = new MnemRbTree();
        for (final NullaryFunction<Asset> entry : list) {
            final Asset asset = entry.call();
            assert asset != null;
            t.insert(asset);
        }
        return t;
    }

    public final void selectAsset(UnaryCallback<Asset> cb) {
        for (final NullaryFunction<Asset> entry : list) {
            cb.call(entry.call());
        }
    }
}
