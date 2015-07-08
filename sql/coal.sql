START TRANSACTION
;

USE twirly;

INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('USD', 'United States of America, Dollars', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('CAP', 'Central Appalachia Coal', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('NAP', 'Northern Appalachia Coal', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('ILB', 'Illinois Basin Coal', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('PRB', 'Powder River Basin Coal', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('UIB', 'Uinta Basin Coal', 1)
;

INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('CAP', 'Central Appalachia Coal', 'CAP', 'USD', 1, 20, 1000, 1, 2, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('NAP', 'Northern Appalachia Coal', 'NAP', 'USD', 1, 20, 1000, 1, 2, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('ILB', 'Illinois Basin Coal', 'ILB', 'USD', 1, 20, 1000, 1, 2, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('PRB', 'Powder River Basin Coal', 'PRB', 'USD', 1, 20, 1000, 1, 2, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('UIB', 'Uinta Basin Coal', 'UIB', 'USD', 1, 20, 1000, 1, 2, 1, 10);
;

INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('CAP', 'Central Appalachia Coal', 'CAP', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('NAP', 'Northern Appalachia Coal', 'NAP', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('ILB', 'Illinois Basin Coal', 'ILB', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('PRB', 'Powder River Basin Coal', 'PRB', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('UIB', 'Uinta Basin Coal', 'UIB', 0, 0, 0)
;

COMMIT
;
