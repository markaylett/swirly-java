/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.mock;

import org.doobry.domain.Asset;
import org.doobry.domain.AssetType;

public final class MockAsset {
    private MockAsset() {
    }

    public static final Asset EUR = new Asset(1, "EUR", "Euro Member Countries, Euro",
            AssetType.CURRENCY);
    public static final Asset GBP = new Asset(2, "GBP", "United Kingdom, Pounds",
            AssetType.CURRENCY);
    public static final Asset AUD = new Asset(3, "AUD", "Australia, Dollars", AssetType.CURRENCY);
    public static final Asset NZD = new Asset(4, "NZD", "New Zealand, Dollars", AssetType.CURRENCY);
    public static final Asset USD = new Asset(5, "USD", "United States of America, Dollars",
            AssetType.CURRENCY);
    public static final Asset CAD = new Asset(6, "CAD", "Canada, Dollars", AssetType.CURRENCY);
    public static final Asset CHF = new Asset(7, "CHF", "Switzerland, Francs", AssetType.CURRENCY);
    public static final Asset TRY = new Asset(8, "TRY", "Turkey, New Lira", AssetType.CURRENCY);
    public static final Asset SGD = new Asset(9, "SGD", "Singapore, Dollars", AssetType.CURRENCY);
    public static final Asset RON = new Asset(10, "RON", "Romania, New Lei", AssetType.CURRENCY);
    public static final Asset PLN = new Asset(11, "PLN", "Poland, Zlotych", AssetType.CURRENCY);
    public static final Asset ILS = new Asset(12, "ILS", "Israel, New Shekels", AssetType.CURRENCY);
    public static final Asset DKK = new Asset(13, "DKK", "Denmark, Kroner", AssetType.CURRENCY);
    public static final Asset ZAR = new Asset(14, "ZAR", "South Africa, Rand", AssetType.CURRENCY);
    public static final Asset NOK = new Asset(15, "NOK", "Norway, Krone", AssetType.CURRENCY);
    public static final Asset SEK = new Asset(16, "SEK", "Sweden, Kronor", AssetType.CURRENCY);
    public static final Asset HKD = new Asset(17, "HKD", "Hong Kong, Dollars", AssetType.CURRENCY);
    public static final Asset MXN = new Asset(18, "MXN", "Mexico, Pesos", AssetType.CURRENCY);
    public static final Asset CZK = new Asset(19, "CZK", "Czech Republic, Koruny",
            AssetType.CURRENCY);
    public static final Asset THB = new Asset(20, "THB", "Thailand, Baht", AssetType.CURRENCY);
    public static final Asset JPY = new Asset(21, "JPY", "Japan, Yen", AssetType.CURRENCY);
    public static final Asset HUF = new Asset(22, "HUF", "Hungary, Forint", AssetType.CURRENCY);
    public static final Asset ZC = new Asset(23, "ZC", "Corn", AssetType.COMMODITY);
    public static final Asset ZS = new Asset(24, "ZS", "Soybeans", AssetType.COMMODITY);
    public static final Asset ZW = new Asset(25, "ZW", "Wheat", AssetType.COMMODITY);
}
