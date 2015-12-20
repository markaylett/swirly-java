START TRANSACTION
;
DROP DATABASE IF EXISTS swirly
;
CREATE DATABASE swirly
;
USE swirly
;

SET foreign_key_checks = 1
;

CREATE TABLE User_t (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(64) NOT NULL UNIQUE,
  pass VARCHAR(32) NULL
)
ENGINE = InnoDB;

CREATE TABLE Group_t (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO Group_t (id, mnem) VALUES (0, 'tomcat')
;

INSERT INTO Group_t (id, mnem) VALUES (1, 'user')
;

INSERT INTO Group_t (id, mnem) VALUES (2, 'trader')
;

INSERT INTO Group_t (id, mnem) VALUES (3, 'admin')
;

CREATE TABLE UserGroup_t (
  userId INT NOT NULL,
  groupId INT NOT NULL,
  PRIMARY KEY (userId, groupId),
  FOREIGN KEY (userId) REFERENCES User_t (id),
  FOREIGN KEY (groupId) REFERENCES Group_t (id)
)
ENGINE = InnoDB;

CREATE VIEW UserGroup_v AS
  SELECT
    u.email email,
    g.mnem group_
  FROM UserGroup_t ug
  INNER JOIN User_t u
  ON ug.userId = u.id
  INNER JOIN Group_t g
  ON ug.groupId = g.id
;

CREATE TABLE State_t (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO State_t (id, mnem) VALUES (1, 'NEW')
;
INSERT INTO State_t (id, mnem) VALUES (2, 'REVISE')
;
INSERT INTO State_t (id, mnem) VALUES (3, 'CANCEL')
;
INSERT INTO State_t (id, mnem) VALUES (4, 'TRADE')
;
INSERT INTO State_t (id, mnem) VALUES (5, 'PECAN')
;

CREATE TABLE Side_t (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO Side_t (id, mnem) VALUES (1, 'BUY')
;
INSERT INTO Side_t (id, mnem) VALUES (-1, 'SELL')
;

CREATE TABLE Direct_t (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO Direct_t (id, mnem) VALUES (1, 'PAID')
;
INSERT INTO Direct_t (id, mnem) VALUES (-1, 'GIVEN')
;

CREATE TABLE Role_t (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO Role_t (id, mnem) VALUES (1, 'MAKER')
;
INSERT INTO Role_t (id, mnem) VALUES (2, 'TAKER')
;

CREATE TABLE AssetType_t (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO AssetType_t (id, mnem) VALUES (1, 'COMMODITY')
;
INSERT INTO AssetType_t (id, mnem) VALUES (2, 'CORPORATE')
;
INSERT INTO AssetType_t (id, mnem) VALUES (3, 'CURRENCY')
;
INSERT INTO AssetType_t (id, mnem) VALUES (4, 'EQUITY')
;
INSERT INTO AssetType_t (id, mnem) VALUES (5, 'GOVERNMENT')
;
INSERT INTO AssetType_t (id, mnem) VALUES (6, 'INDEX')
;

CREATE TABLE Asset_t (
  mnem CHAR(16) NOT NULL PRIMARY KEY,
  display VARCHAR(64) NOT NULL UNIQUE,
  typeId INT NOT NULL,

  FOREIGN KEY (typeId) REFERENCES AssetType_t (id)
)
ENGINE = InnoDB;

CREATE TABLE Contr_t (
  mnem CHAR(16) NOT NULL PRIMARY KEY,
  display VARCHAR(64) NOT NULL,
  asset CHAR(16) NOT NULL,
  ccy CHAR(16) NOT NULL,
  lotNumer INT NOT NULL,
  lotDenom INT NOT NULL,
  tickNumer INT NOT NULL,
  tickDenom INT NOT NULL,
  pipDp INT NOT NULL,
  minLots BIGINT NOT NULL DEFAULT 1,
  maxLots BIGINT NOT NULL,

  FOREIGN KEY (asset) REFERENCES Asset_t (mnem),
  FOREIGN KEY (ccy) REFERENCES Asset_t (mnem)
)
ENGINE = InnoDB;

CREATE TABLE Market_t (
  mnem CHAR(16) NOT NULL PRIMARY KEY,
  display VARCHAR(64) NOT NULL,
  contr CHAR(16) NOT NULL,
  settlDay INT NULL DEFAULT NULL,
  expiryDay INT NULL DEFAULT NULL,
  state INT NOT NULL DEFAULT 0,
  lastLots BIGINT NULL DEFAULT NULL,
  lastTicks BIGINT NULL DEFAULT NULL,
  lastTime BIGINT NULL DEFAULT NULL,

  FOREIGN KEY (contr) REFERENCES Contr_t (mnem)
)
ENGINE = InnoDB;

CREATE TABLE Trader_t (
  mnem CHAR(16) NOT NULL PRIMARY KEY,
  display VARCHAR(64) NOT NULL,
  email VARCHAR(64) NOT NULL,
  FOREIGN KEY (email) REFERENCES User_t (email)
)
ENGINE = InnoDB;

DELIMITER //
CREATE TRIGGER afterInsertOnUser
  AFTER INSERT ON User_t
  FOR EACH ROW
  BEGIN
    INSERT INTO UserGroup_t (
      userId,
      groupId
    ) VALUES (
      NEW.id,
      1
    );
  END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER beforeDeleteOnUser
  BEFORE DELETE on User_t
  FOR EACH ROW
  BEGIN
    DELETE FROM Trader_t
    WHERE email = OLD.email;
    DELETE FROM UserGroup_t
    WHERE userId = OLD.id;
  END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER beforeInsertOnTrader
  BEFORE INSERT ON Trader_t
  FOR EACH ROW
  BEGIN
    -- FIXME: allow user to specify password.
    INSERT IGNORE INTO User_t (
      email,
      pass
    ) VALUES (
      NEW.email,
      'test'
    );
    INSERT INTO UserGroup_t (
      userId,
      groupId
    ) VALUES (
      (SELECT id FROM User_t WHERE email = NEW.email),
      2
    );
  END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER afterDeleteOnTrader
  AFTER DELETE on Trader_t
  FOR EACH ROW
  BEGIN
    DELETE FROM UserGroup_t
    WHERE userId = (SELECT id FROM User_t WHERE email = OLD.email)
    AND groupId = 2;
  END //
DELIMITER ;

CREATE TABLE Order_t (
  trader CHAR(16) NOT NULL,
  market CHAR(16) NOT NULL,
  contr CHAR(16) NOT NULL,
  settlDay INT NULL DEFAULT NULL,
  id BIGINT NOT NULL,
  ref VARCHAR(64) NULL DEFAULT NULL,
  quoteId BIGINT NULL DEFAULT NULL,
  stateId INT NOT NULL,
  sideId INT NOT NULL,
  lots BIGINT NOT NULL,
  ticks BIGINT NOT NULL,
  resd BIGINT NOT NULL,
  exec BIGINT NOT NULL,
  cost BIGINT NOT NULL,
  lastLots BIGINT NULL DEFAULT NULL,
  lastTicks BIGINT NULL DEFAULT NULL,
  minLots BIGINT NOT NULL DEFAULT 1,
  archive TINYINT(1) NOT NULL DEFAULT 0,
  pecan TINYINT(1) NOT NULL DEFAULT 0,
  created BIGINT NOT NULL,
  modified BIGINT NOT NULL,

  PRIMARY KEY (market, id),
  CONSTRAINT orderTraderRefUnq UNIQUE (Trader, ref),

  FOREIGN KEY (trader) REFERENCES Trader_t (mnem),
  FOREIGN KEY (market) REFERENCES Market_t (mnem),
  FOREIGN KEY (contr) REFERENCES Contr_t (mnem),
  FOREIGN KEY (stateId) REFERENCES State_t (id),
  FOREIGN KEY (sideId) REFERENCES Side_t (id)
)
ENGINE = InnoDB;

CREATE INDEX orderTraderIdx ON Order_t (trader);
CREATE INDEX orderResdIdx ON Order_t (resd);
CREATE INDEX orderArchiveIdx ON Order_t (archive);

CREATE TABLE Exec_t (
  trader CHAR(16) NOT NULL,
  market CHAR(16) NOT NULL,
  contr CHAR(16) NOT NULL,
  settlDay INT NULL DEFAULT NULL,
  id BIGINT NOT NULL,
  ref VARCHAR(64) NULL DEFAULT NULL,
  orderId BIGINT NULL DEFAULT NULL,
  quoteId BIGINT NULL DEFAULT NULL,
  stateId INT NOT NULL,
  sideId INT NOT NULL,
  lots BIGINT NOT NULL,
  ticks BIGINT NOT NULL,
  resd BIGINT NOT NULL,
  exec BIGINT NOT NULL,
  cost BIGINT NOT NULL,
  lastLots BIGINT NULL DEFAULT NULL,
  lastTicks BIGINT NULL DEFAULT NULL,
  minLots BIGINT NOT NULL DEFAULT 1,
  matchId BIGINT NULL DEFAULT NULL,
  roleId INT NULL DEFAULT NULL,
  cpty CHAR(16) NULL DEFAULT NULL,
  archive TINYINT(1) NOT NULL DEFAULT 0,
  created BIGINT NOT NULL,
  modified BIGINT NOT NULL,

  PRIMARY KEY (market, id),

  FOREIGN KEY (market, orderId) REFERENCES Order_t (market, id),
  FOREIGN KEY (trader) REFERENCES Trader_t (mnem),
  FOREIGN KEY (contr) REFERENCES Contr_t (mnem),
  FOREIGN KEY (stateId) REFERENCES State_t (id),
  FOREIGN KEY (sideId) REFERENCES Side_t (id),
  FOREIGN KEY (roleId) REFERENCES Role_t (id),
  FOREIGN KEY (cpty) REFERENCES Trader_t (mnem)
)
ENGINE = InnoDB;

CREATE INDEX execTraderIdx ON Exec_t (trader);
CREATE INDEX execStateIdx ON Exec_t (stateId);
CREATE INDEX execArchiveIdx ON Exec_t (archive);

DELIMITER //
CREATE TRIGGER beforeInsertOnExec
  BEFORE INSERT ON Exec_t
  FOR EACH ROW
  BEGIN
    IF NEW.orderId IS NOT NULL THEN
      IF NEW.stateId = 1 THEN
        INSERT INTO Order_t (
          trader,
          market,
          contr,
          settlDay,
          id,
          ref,
          quoteId,
          stateId,
          sideId,
          lots,
          ticks,
          resd,
          exec,
          cost,
          lastLots,
          lastTicks,
          minLots,
          archive,
          pecan,
          created,
          modified
        ) VALUES (
          NEW.trader,
          NEW.market,
          NEW.contr,
          NEW.settlDay,
          NEW.orderId,
          NEW.ref,
          NEW.quoteId,
          NEW.stateId,
          NEW.sideId,
          NEW.lots,
          NEW.ticks,
          NEW.resd,
          NEW.exec,
          NEW.cost,
          NEW.lastLots,
          NEW.lastTicks,
          NEW.minLots,
          CASE WHEN NEW.quoteId != 0 THEN 1 ELSE 0 END,
          CASE WHEN NEW.stateId = 5 THEN 1 ELSE 0 END,
          NEW.created,
          NEW.modified
        );
      ELSE
        UPDATE Order_t
        SET
          stateId = NEW.stateId,
          lots = NEW.lots,
          resd = NEW.resd,
          exec = NEW.exec,
          cost = NEW.cost,
          lastLots = NEW.lastLots,
          lastTicks = NEW.lastTicks,
          pecan = CASE WHEN NEW.stateId = 5 THEN 1 ELSE pecan END,
          modified = NEW.modified
        WHERE id = NEW.orderId;
      END IF;
      IF NEW.stateId = 4 THEN
        UPDATE Market_t
        SET
          lastLots = NEW.lastLots,
          lastTicks = NEW.lastTicks,
          lastTime = NEW.created
        WHERE mnem = NEW.market;
      END IF;
    END IF;
  END //
DELIMITER ;

CREATE TABLE Quote_t (
  market CHAR(16) NOT NULL PRIMARY KEY,
  id BIGINT NOT NULL
)
ENGINE = InnoDB;

CREATE VIEW Asset_v AS
  SELECT
    a.mnem,
    a.display,
    t.mnem type
  FROM Asset_t a
  LEFT OUTER JOIN AssetType_t t
  ON a.typeId = t.id
;

CREATE VIEW Contr_v AS
  SELECT
    c.mnem,
    c.display,
    a.type,
    c.asset,
    c.ccy,
    c.lotNumer,
    c.lotDenom,
    c.tickNumer,
    c.tickDenom,
    c.pipDp,
    c.minLots,
    c.maxLots
  FROM Contr_t c
  LEFT OUTER JOIN Asset_v a
  ON c.asset = a.mnem
;

CREATE VIEW Market_v AS
  SELECT
    m.mnem,
    m.display,
    m.contr,
    m.settlDay,
    m.expiryDay,
    m.state,
    m.lastLots,
    m.lastTicks,
    m.lastTime,
    MAX(e.orderId) maxOrderId,
    MAX(e.id) maxExecId,
    q.id maxQuoteId
  FROM Market_t m
  LEFT OUTER JOIN Exec_t e
  ON m.mnem = e.market
  LEFT OUTER JOIN Quote_t q
  ON m.mnem = q.market
  GROUP BY m.mnem
;

CREATE VIEW Order_v AS
  SELECT
    o.trader,
    o.market,
    o.contr,
    o.settlDay,
    o.id,
    o.ref,
    o.quoteId,
    s.mnem state,
    a.mnem side,
    o.lots,
    o.ticks,
    o.resd,
    o.exec,
    o.cost,
    o.lastLots,
    o.lastTicks,
    o.minLots,
    o.pecan,
    o.created,
    o.modified
  FROM Order_t o
  LEFT OUTER JOIN State_t s
  ON o.stateId = s.id
  LEFT OUTER JOIN Side_t a
  ON o.sideId = a.id
;

CREATE VIEW Exec_v AS
  SELECT
    e.trader,
    e.contr,
    e.settlDay,
    e.id,
    e.ref,
    e.orderId,
    e.quoteId,
    s.mnem state,
    a.mnem side,
    e.lots,
    e.ticks,
    e.resd,
    e.exec,
    e.cost,
    e.lastLots,
    e.lastTicks,
    e.minLots,
    e.matchId,
    r.mnem role,
    e.cpty,
    e.archive,
    e.created,
    e.modified
  FROM Exec_t e
  LEFT OUTER JOIN State_t s
  ON e.stateId = s.id
  LEFT OUTER JOIN Side_t a
  ON e.sideId = a.id
  LEFT OUTER JOIN Role_t r
  ON e.roleId = r.id
;

CREATE VIEW Posn_v AS
  SELECT
    e.trader,
    e.contr,
    e.settlDay,
    e.sideId,
    SUM(e.lastLots) lots,
    SUM(e.lastLots * e.lastTicks) cost
  FROM Exec_t e
  WHERE e.stateId = 4
  GROUP BY e.trader, e.contr, e.settlDay, e.sideId
;

COMMIT
;
