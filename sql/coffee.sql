START TRANSACTION
;

USE swirly;

INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('ETB', 'Ethiopia, Birr', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('WYCA', 'Yirgachefe A', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('WWNA', 'Wenago A', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('WKCA', 'Kochere A', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('WGAA', 'Gelena Abaya A', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('WYCB', 'Yirgachefe B', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('WWNB', 'Wenago B', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('WKCB', 'Kochere B', 1)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('WGAB', 'Gelena Abaya B', 1)
;

INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('WYCA', 'Yirgachefe A', 'WYCA', 'ETB', 1, 1, 1, 1, 0, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('WWNA', 'Wenago A', 'WWNA', 'ETB', 1, 1, 1, 1, 0, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('WKCA', 'Kochere A', 'WKCA', 'ETB', 1, 1, 1, 1, 0, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('WGAA', 'Gelena Abaya A', 'WGAA', 'ETB', 1, 1, 1, 1, 0, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('WYCB', 'Yirgachefe B', 'WYCB', 'ETB', 1, 1, 1, 1, 0, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('WWNB', 'Wenago B', 'WWNB', 'ETB', 1, 1, 1, 1, 0, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('WKCB', 'Kochere B', 'WKCB', 'ETB', 1, 1, 1, 1, 0, 1, 10);
;
INSERT INTO Contr_t (mnem, display, asset, ccy, lotNumer, lotDenom,
       tickNumer, tickDenom, pipDp, minLots, maxLots)
       VALUES ('WGAB', 'Gelena Abaya B', 'WGAB', 'ETB', 1, 1, 1, 1, 0, 1, 10);
;

INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('WYCA', 'Yirgachefe A', 'WYCA', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('WWNA', 'Wenago A', 'WWNA', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('WKCA', 'Kochere A', 'WKCA', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('WGAA', 'Gelena Abaya A', 'WGAA', 0, 0, 0)
;

COMMIT
;
