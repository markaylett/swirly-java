
START TRANSACTION
;

USE swirly;

INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('USD', 'United States of America, Dollars', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('CSCO', 'Cisco Systems Inc', 2)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('DIS', 'Walt Disney', 2)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('IBM', 'Ibm Corp', 2)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('INTC', 'Intel Corp', 2)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('MSFT', 'Microsoft Corp', 2)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('VIA', 'Viacom Inc', 2)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('VOD', 'Vodafone Group Plc', 2)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('VZ', 'Verizon Com', 2)
;

INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('CSCO', 'Cisco Systems Inc', 'CSCO', 'USD', 1, 1, 1, 1000, 3, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('DIS', 'Walt Disney', 'DIS', 'USD', 1, 1, 1, 1000, 3, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('IBM', 'Ibm Corp', 'IBM', 'USD', 1, 1, 1, 1000, 3, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('INTC', 'Intel Corp', 'INTC', 'USD', 1, 1, 1, 1000, 3, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('MSFT', 'Microsoft Corp', 'MSFT', 'USD', 1, 1, 1, 1000, 3, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('VIA', 'Viacom Inc', 'VIA', 'USD', 1, 1, 1, 1000, 3, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('VOD', 'Vodafone Group Plc', 'VOD', 'USD', 1, 1, 1, 1000, 3, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('VZ', 'Verizon Com', 'VZ', 'USD', 1, 1, 1, 1000, 3, 1, 10);
;

INSERT INTO market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('CSCO.4.45.20', 'CSCO 4.450 `20', 'CSCO',
              TO_DAYS('2020-01-15') + 1721060, TO_DAYS('2020-01-14') + 1721060, 0);

INSERT INTO market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('DIS.1.125.17', 'DIS 1.125 `17', 'DIS',
              TO_DAYS('2017-02-15') + 1721060, TO_DAYS('2017-02-14') + 1721060, 0);

INSERT INTO market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('IBM.8.375.19', 'IBM 8.375 `19', 'IBM',
              TO_DAYS('2019-11-01') + 1721060, TO_DAYS('2019-10-31') + 1721060, 0);

INSERT INTO market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('INTC.2.7.22', 'INTC 2.700 `22', 'INTC',
              TO_DAYS('2022-12-15') + 1721060, TO_DAYS('2022-12-14') + 1721060, 0);

COMMIT
;
