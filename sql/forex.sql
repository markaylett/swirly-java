START TRANSACTION
;

USE twirly;

INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('AUD', 'Australia, Dollars', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('CAD', 'Canada, Dollars', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('CHF', 'Switzerland, Francs', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('CZK', 'Czech Republic, Koruny', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('DKK', 'Denmark, Kroner', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('EUR', 'Euro Member Countries, Euro', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('GBP', 'United Kingdom, Pounds', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('HKD', 'Hong Kong, Dollars', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('HUF', 'Hungary, Forint', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('ILS', 'Israel, New Shekels', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('JPY', 'Japan, Yen', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('MXN', 'Mexico, Pesos', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('NOK', 'Norway, Krone', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('NZD', 'New Zealand, Dollars', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('PLN', 'Poland, Zlotych', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('RON', 'Romania, New Lei', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('SEK', 'Sweden, Kronor', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('SGD', 'Singapore, Dollars', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('THB', 'Thailand, Baht', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('TRY', 'Turkey, New Lira', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('USD', 'United States of America, Dollars', 3)
;
INSERT INTO Asset_t (mnem, display, typeId)
       VALUES ('ZAR', 'South Africa, Rand', 3)
;

INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('AUDUSD', 'AUDUSD', 'AUD', 'USD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURCHF', 'EURCHF', 'EUR', 'CHF', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURCZK', 'EURCZK', 'EUR', 'CZK', 1, 100, 1000000, 1, 2, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURDKK', 'EURDKK', 'EUR', 'DKK', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURGBP', 'EURGBP', 'EUR', 'GBP', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURHUF', 'EURHUF', 'EUR', 'HUF', 1, 100, 1000000, 1, 2, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURJPY', 'EURJPY', 'EUR', 'JPY', 1, 100, 1000000, 1, 2, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURNOK', 'EURNOK', 'EUR', 'NOK', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURPLN', 'EURPLN', 'EUR', 'PLN', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURRON', 'EURRON', 'EUR', 'RON', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURSEK', 'EURSEK', 'EUR', 'SEK', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURUSD', 'EURUSD', 'EUR', 'USD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('GBPUSD', 'GBPUSD', 'GBP', 'USD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('NZDUSD', 'NZDUSD', 'NZD', 'USD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDCAD', 'USDCAD', 'USD', 'CAD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDCHF', 'USDCHF', 'USD', 'CHF', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDHKD', 'USDHKD', 'USD', 'HKD', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDILS', 'USDILS', 'USD', 'ILS', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDJPY', 'USDJPY', 'USD', 'JPY', 1, 100, 1000000, 1, 2, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDMXN', 'USDMXN', 'USD', 'MXN', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDSGD', 'USDSGD', 'USD', 'SGD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDTHB', 'USDTHB', 'USD', 'THB', 1, 100, 1000000, 1, 2, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDTRY', 'USDTRY', 'USD', 'TRY', 1, 1000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr_t (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDZAR', 'USDZAR', 'USD', 'ZAR', 1, 1000, 1000000, 1, 3, 1, 10)
;

INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('EURUSD', 'EURUSD', 'EURUSD', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('GBPUSD', 'GBPUSD', 'GBPUSD', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('USDCHF', 'USDCHF', 'USDCHF', 0, 0, 0)
;
INSERT INTO Market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('USDJPY', 'USDJPY', 'USDJPY', 0, 0, 0)
;

COMMIT
;
