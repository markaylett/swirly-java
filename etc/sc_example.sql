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

INSERT INTO market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('EURUSD.JUN15', 'EURUSD June 15', 'EURUSD',
              TO_DAYS('2015-06-19') + 1721060, TO_DAYS('2015-06-18') + 1721060, 0)
;

INSERT INTO market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('EURUSD.SEP15', 'EURUSD Sept 15', 'EURUSD',
              TO_DAYS('2015-09-18') + 1721060, TO_DAYS('2015-09-17') + 1721060, 0)
;

INSERT INTO market_t (mnem, display, contr, settlDay, expiryDay, state)
       VALUES ('GBPUSD.JUN15', 'GBPUSD June 15', 'GBPUSD',
              TO_DAYS('2015-06-19') + 1721060, TO_DAYS('2015-06-18') + 1721060, 0)
;

INSERT INTO User_t (email, pass)
       VALUES ('mark.aylett@gmail.com', 'test')
;

INSERT INTO User_t (email, pass)
       VALUES ('goska.aylett@gmail.com', 'test')
;

INSERT INTO User_t (email, pass)
       VALUES ('toby.aylett@gmail.com', 'test')
;

INSERT INTO User_t (email, pass)
       VALUES ('emily.aylett@gmail.com', 'test')
;

INSERT INTO User_t (email, pass)
       VALUES ('info@swirlycloud.com', 'test')
;

INSERT INTO User_t (email, pass)
       VALUES ('ram.mac@gmail.com', 'test')
;

INSERT INTO UserGroup_t (userId, groupId)
       VALUES (
          (SELECT Id FROM User_t WHERE email = 'mark.aylett@gmail.com'),
          (SELECT id FROM Group_t WHERE mnem = 'admin')
       )
;

INSERT INTO UserGroup_t (userId, groupId)
       VALUES (
          (SELECT id FROM User_t WHERE email = 'mark.aylett@gmail.com'),
          (SELECT id FROM Group_t WHERE mnem = 'tomcat')
       )
;

INSERT INTO UserGroup_t (userId, groupId)
       VALUES (
          (SELECT id FROM User_t WHERE email = 'ram.mac@gmail.com'),
          (SELECT id FROM Group_t WHERE mnem = 'admin')
       )
;

INSERT INTO UserGroup_t (userId, groupId)
       VALUES (
          (SELECT id FROM User_t WHERE email = 'ram.mac@gmail.com'),
          (SELECT id FROM Group_t WHERE mnem = 'tomcat')
       )
;

INSERT INTO Trader_t (mnem, display, email)
       VALUES ('MARAYL', 'Mark Aylett', 'mark.aylett@gmail.com')
;

INSERT INTO Trader_t (mnem, display, email)
       VALUES ('GOSAYL', 'Goska Aylett', 'goska.aylett@gmail.com')
;

INSERT INTO Trader_t (mnem, display, email)
       VALUES ('TOBAYL', 'Toby Aylett', 'toby.aylett@gmail.com')
;

INSERT INTO Trader_t (mnem, display, email)
       VALUES ('EMIAYL', 'Emily Aylett', 'emily.aylett@gmail.com')
;

INSERT INTO Trader_t (mnem, display, email)
       VALUES ('SWIRLY', 'Swirly Cloud', 'info@swirlycloud.com')
;

INSERT INTO Trader_t (mnem, display, email)
       VALUES ('RAMMAC', 'Ram Macharaj', 'ram.mac@gmail.com')
;

COMMIT
;
