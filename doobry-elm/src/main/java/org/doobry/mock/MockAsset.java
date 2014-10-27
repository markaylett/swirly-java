/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.doobry.domain.Asset;
import org.doobry.domain.AssetType;
import org.doobry.function.NullaryFunction;
import org.doobry.util.Queue;

public final class MockAsset {
    private static final Map<String, NullaryFunction<Asset>> FACTORIES = new HashMap<String, NullaryFunction<Asset>>();

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
        put(i++, "EUR", "Euro Member Countries, Euro", AssetType.CURRENCY);
        put(i++, "GBP", "United Kingdom, Pounds", AssetType.CURRENCY);
        put(i++, "AUD", "Australia, Dollars", AssetType.CURRENCY);
        put(i++, "NZD", "New Zealand, Dollars", AssetType.CURRENCY);
        put(i++, "USD", "United States of America, Dollars", AssetType.CURRENCY);
        put(i++, "CAD", "Canada, Dollars", AssetType.CURRENCY);
        put(i++, "CHF", "Switzerland, Francs", AssetType.CURRENCY);
        put(i++, "TRY", "Turkey, New Lira", AssetType.CURRENCY);
        put(i++, "SGD", "Singapore, Dollars", AssetType.CURRENCY);
        put(i++, "RON", "Romania, New Lei", AssetType.CURRENCY);
        put(i++, "PLN", "Poland, Zlotych", AssetType.CURRENCY);
        put(i++, "ILS", "Israel, New Shekels", AssetType.CURRENCY);
        put(i++, "DKK", "Denmark, Kroner", AssetType.CURRENCY);
        put(i++, "ZAR", "South Africa, Rand", AssetType.CURRENCY);
        put(i++, "NOK", "Norway, Krone", AssetType.CURRENCY);
        put(i++, "SEK", "Sweden, Kronor", AssetType.CURRENCY);
        put(i++, "HKD", "Hong Kong, Dollars", AssetType.CURRENCY);
        put(i++, "MXN", "Mexico, Pesos", AssetType.CURRENCY);
        put(i++, "CZK", "Czech Republic, Koruny", AssetType.CURRENCY);
        put(i++, "THB", "Thailand, Baht", AssetType.CURRENCY);
        put(i++, "JPY", "Japan, Yen", AssetType.CURRENCY);
        put(i++, "HUF", "Hungary, Forint", AssetType.CURRENCY);
        put(i++, "ZC", "Corn", AssetType.COMMODITY);
        put(i++, "ZS", "Soybeans", AssetType.COMMODITY);
        put(i++, "ZW", "Wheat", AssetType.COMMODITY);
    }

    private MockAsset() {
    }

    public static Asset newAsset(String mnem) {
        return FACTORIES.get(mnem).call();
    }

    public static Asset[] newAssetArray() {
        int i = 0;
        final Asset[] arr = new Asset[FACTORIES.size()];
        for (final Entry<String, NullaryFunction<Asset>> entry : FACTORIES.entrySet()) {
            arr[i++] = entry.getValue().call();
        }
        return arr;
    }

    public static Asset newAssetList() {
        final Queue q = new Queue();
        for (final Entry<String, NullaryFunction<Asset>> entry : FACTORIES.entrySet()) {
            q.insertBack(entry.getValue().call());
        }
        return (Asset) q.getFirst();
    }
}
