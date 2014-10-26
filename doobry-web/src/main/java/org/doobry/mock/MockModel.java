package org.doobry.mock;

import org.doobry.domain.Asset;
import org.doobry.domain.Contr;
import org.doobry.domain.Exec;
import org.doobry.domain.Model;
import org.doobry.domain.Order;
import org.doobry.domain.Party;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;
import org.doobry.domain.RecType;

public final class MockModel implements Model {

    public static final Asset[] ASSET_ARRAY = new Asset[] { //
    MockAsset.EUR, //
            MockAsset.GBP, //
            MockAsset.AUD, //
            MockAsset.NZD, //
            MockAsset.USD, //
            MockAsset.CAD, //
            MockAsset.CHF, //
            MockAsset.TRY, //
            MockAsset.SGD, //
            MockAsset.RON, //
            MockAsset.PLN, //
            MockAsset.ILS, //
            MockAsset.DKK, //
            MockAsset.ZAR, //
            MockAsset.NOK, //
            MockAsset.SEK, //
            MockAsset.HKD, //
            MockAsset.MXN, //
            MockAsset.CZK, //
            MockAsset.THB, //
            MockAsset.JPY, //
            MockAsset.HUF, //
            MockAsset.ZC, //
            MockAsset.ZS, //
            MockAsset.ZW //
    };

    public static final Contr[] CONTR_ARRAY = new Contr[] { //
    MockContr.EURUSD, //
            MockContr.GBPUSD, //
            MockContr.AUDUSD, //
            MockContr.NZDUSD, //
            MockContr.USDCAD, //
            MockContr.USDCHF, //
            MockContr.USDTRY, //
            MockContr.USDSGD, //
            MockContr.EURRON, //
            MockContr.EURPLN, //
            MockContr.USDILS, //
            MockContr.EURDKK, //
            MockContr.USDZAR, //
            MockContr.EURNOK, //
            MockContr.EURSEK, //
            MockContr.USDHKD, //
            MockContr.USDMXN, //
            MockContr.EURCZK, //
            MockContr.USDTHB, //
            MockContr.USDJPY, //
            MockContr.EURHUF, //
            MockContr.EURGBP, //
            MockContr.EURCHF, //
            MockContr.EURJPY, //
            MockContr.ZC_USD, //
            MockContr.ZS_USD, //
            MockContr.ZW_USD //
    };

    public static final Party[] PARTY_ARRAY = new Party[] { //
    MockParty.WRAMIREZ, //
            MockParty.SFLORES, //
            MockParty.JWRIGHT, //
            MockParty.VCAMPBEL, //
            MockParty.GWILSON, //
            MockParty.BJONES, //
            MockParty.TLEE, //
            MockParty.EEDWARDS, //
            MockParty.RALEXAND, //
            MockParty.JTHOMAS, //
            MockParty.DBRA, //
            MockParty.DBRB //
    };

    @Override
    public final Rec[] readRec(RecType type) {
        Rec[] arr = null;
        switch (type) {
        case ASSET:
            arr = ASSET_ARRAY;
            break;
        case CONTR:
            arr = CONTR_ARRAY;
            break;
        case PARTY:
            arr = PARTY_ARRAY;
            break;
        }
        return arr;
    }

    @Override
    public final Order[] readOrder() {
        return new Order[] {};
    }

    @Override
    public final Exec[] readTrade() {
        return new Exec[] {};
    }

    @Override
    public final Posn[] readPosn() {
        return new Posn[] {};
    }
}
