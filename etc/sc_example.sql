START TRANSACTION
;

USE twirly;

INSERT INTO Asset (mnem, display, typeId)
       VALUES ('EUR', 'Euro Member Countries, Euro', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('GBP', 'United Kingdom, Pounds', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('AUD', 'Australia, Dollars', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('NZD', 'New Zealand, Dollars', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('USD', 'United States of America, Dollars', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('CAD', 'Canada, Dollars', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('CHF', 'Switzerland, Francs', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('TRY', 'Turkey, New Lira', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('SGD', 'Singapore, Dollars', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('RON', 'Romania, New Lei', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('PLN', 'Poland, Zlotych', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('ILS', 'Israel, New Shekels', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('DKK', 'Denmark, Kroner', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('ZAR', 'South Africa, Rand', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('NOK', 'Norway, Krone', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('SEK', 'Sweden, Kronor', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('HKD', 'Hong Kong, Dollars', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('MXN', 'Mexico, Pesos', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('CZK', 'Czech Republic, Koruny', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('THB', 'Thailand, Baht', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('JPY', 'Japan, Yen', 3)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('HUF', 'Hungary, Forint', 3)
;

INSERT INTO Asset (mnem, display, typeId)
       VALUES ('ZC', 'Corn', 1)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('ZS', 'Soybeans', 1)
;
INSERT INTO Asset (mnem, display, typeId)
       VALUES ('ZW', 'Wheat', 1)
;

INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURUSD', 'EURUSD', 'EUR', 'USD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('GBPUSD', 'GBPUSD', 'GBP', 'USD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('AUDUSD', 'AUDUSD', 'AUD', 'USD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('NZDUSD', 'NZDUSD', 'NZD', 'USD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDCAD', 'USDCAD', 'USD', 'CAD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDCHF', 'USDCHF', 'USD', 'CHF', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDTRY', 'USDTRY', 'USD', 'TRY', 1, 1000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDSGD', 'USDSGD', 'USD', 'SGD', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURRON', 'EURRON', 'EUR', 'RON', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURPLN', 'EURPLN', 'EUR', 'PLN', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDILS', 'USDILS', 'USD', 'ILS', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURDKK', 'EURDKK', 'EUR', 'DKK', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDZAR', 'USDZAR', 'USD', 'ZAR', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURNOK', 'EURNOK', 'EUR', 'NOK', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURSEK', 'EURSEK', 'EUR', 'SEK', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDHKD', 'USDHKD', 'USD', 'HKD', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDMXN', 'USDMXN', 'USD', 'MXN', 1, 1000, 1000000, 1, 3, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURCZK', 'EURCZK', 'EUR', 'CZK', 1, 100, 1000000, 1, 2, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDTHB', 'USDTHB', 'USD', 'THB', 1, 100, 1000000, 1, 2, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('USDJPY', 'USDJPY', 'USD', 'JPY', 1, 100, 1000000, 1, 2, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURHUF', 'EURHUF', 'EUR', 'HUF', 1, 100, 1000000, 1, 2, 1, 10)
;
-- Crosses.
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURGBP', 'EURGBP', 'EUR', 'GBP', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURCHF', 'EURCHF', 'EUR', 'CHF', 1, 10000, 1000000, 1, 4, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('EURJPY', 'EURJPY', 'EUR', 'JPY', 1, 100, 1000000, 1, 2, 1, 10)
;

INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('ZC', 'ZC', 'ZC', 'USD', 1, 400, 5000, 1, 2, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('ZS', 'ZS', 'ZS', 'USD', 1, 400, 5000, 1, 2, 1, 10)
;
INSERT INTO Contr (mnem, display, asset, ccy, tickNumer,
       tickDenom, lotNumer, lotDenom, pipDp, minLots, maxLots)
       VALUES ('ZW', 'ZW', 'ZW', 'USD', 1, 400, 5000, 1, 2, 1, 10)
;

INSERT INTO Market (mnem, display, contr, settlDay, expiryDay)
       VALUES ('EURUSD.MAR14', 'EURUSD March 14', 'EURUSD',
              TO_DAYS('2014-03-14') + 1721060, TO_DAYS('2014-03-12') + 1721060);

INSERT INTO Trader (mnem, display, email)
       VALUES ('MARAYL', 'Mark Aylett', 'mark.aylett@gmail.com')
;

INSERT INTO Trader (mnem, display, email)
       VALUES ('GOSAYL', 'Goska Aylett', 'goska.aylett@gmail.com')
;

INSERT INTO Trader (mnem, display, email)
       VALUES ('TOBAYL', 'Toby Aylett', 'toby.aylett@gmail.com')
;

INSERT INTO Trader (mnem, display, email)
       VALUES ('EMIAYL', 'Emily Aylett', 'emily.aylett@gmail.com')
;

INSERT INTO Trader (mnem, display, email)
       VALUES ('SWIRLY', 'Swirly Cloud', 'info@swirlycloud.com')
;

COMMIT
;
