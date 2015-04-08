START TRANSACTION
;
DROP DATABASE IF EXISTS twirly
;
CREATE DATABASE twirly
;
USE twirly
;

SET foreign_key_checks = 1
;

CREATE TABLE RealmUser (
  id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(64) NOT NULL UNIQUE,
  pass VARCHAR(32) NULL
)
ENGINE = InnoDB;

CREATE TABLE RealmRole (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO RealmRole (id, mnem) VALUES (0, 'tomcat')
;

INSERT INTO RealmRole (id, mnem) VALUES (1, 'user')
;

INSERT INTO RealmRole (id, mnem) VALUES (2, 'trader')
;

INSERT INTO RealmRole (id, mnem) VALUES (3, 'admin')
;

CREATE TABLE RealmUserRole (
  user INT NOT NULL,
  role INT NOT NULL,
  PRIMARY KEY (user, role),
  FOREIGN KEY (user) REFERENCES RealmUser (id),
  FOREIGN KEY (role) REFERENCES RealmRole (id)
)
ENGINE = InnoDB;

CREATE VIEW RealmUserRoleV AS
  SELECT
    u.email email,
    r.mnem role
  FROM RealmUserRole ur
  INNER JOIN RealmUser u
  ON ur.user = u.id
  INNER JOIN RealmRole r
  ON ur.role = r.id
;

CREATE TABLE State (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO State (id, mnem) VALUES (1, 'NEW')
;
INSERT INTO State (id, mnem) VALUES (2, 'REVISE')
;
INSERT INTO State (id, mnem) VALUES (3, 'CANCEL')
;
INSERT INTO State (id, mnem) VALUES (4, 'TRADE')
;

CREATE TABLE Action (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO Action (id, mnem) VALUES (1, 'BUY')
;
INSERT INTO Action (id, mnem) VALUES (-1, 'SELL')
;

CREATE TABLE Direct (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO Direct (id, mnem) VALUES (1, 'PAID')
;
INSERT INTO Direct (id, mnem) VALUES (-1, 'GIVEN')
;

CREATE TABLE Role (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO Role (id, mnem) VALUES (1, 'MAKER')
;
INSERT INTO Role (id, mnem) VALUES (2, 'TAKER')
;

CREATE TABLE AssetType (
  id INT NOT NULL PRIMARY KEY,
  mnem CHAR(16) NOT NULL UNIQUE
)
ENGINE = InnoDB;

INSERT INTO AssetType (id, mnem) VALUES (1, 'COMMODITY')
;
INSERT INTO AssetType (id, mnem) VALUES (2, 'CORPORATE')
;
INSERT INTO AssetType (id, mnem) VALUES (3, 'CURRENCY')
;
INSERT INTO AssetType (id, mnem) VALUES (4, 'EQUITY')
;
INSERT INTO AssetType (id, mnem) VALUES (5, 'GOVERNMENT')
;
INSERT INTO AssetType (id, mnem) VALUES (6, 'INDEX')
;

CREATE TABLE Asset (
  mnem CHAR(16) NOT NULL PRIMARY KEY,
  display VARCHAR(64) NOT NULL UNIQUE,
  typeId INT NOT NULL,

  FOREIGN KEY (typeId) REFERENCES AssetType (id)
)
ENGINE = InnoDB;

CREATE TABLE Contr (
  mnem CHAR(16) NOT NULL PRIMARY KEY,
  display VARCHAR(64) NOT NULL,
  asset CHAR(16) NOT NULL,
  ccy CHAR(16) NOT NULL,
  tickNumer INT NOT NULL,
  tickDenom INT NOT NULL,
  lotNumer INT NOT NULL,
  lotDenom INT NOT NULL,
  pipDp INT NOT NULL,
  minLots BIGINT NOT NULL,
  maxLots BIGINT NOT NULL,

  FOREIGN KEY (asset) REFERENCES Asset (mnem),
  FOREIGN KEY (ccy) REFERENCES Asset (mnem)
)
ENGINE = InnoDB;

CREATE TABLE Market (
  mnem CHAR(16) NOT NULL PRIMARY KEY,
  display VARCHAR(64) NOT NULL,
  contr CHAR(16) NOT NULL,
  settlDay INT NOT NULL,
  expiryDay INT NOT NULL,
  lastTicks BIGINT NULL DEFAULT NULL,
  lastLots BIGINT NULL DEFAULT NULL,
  lastTime BIGINT NULL DEFAULT NULL,

  FOREIGN KEY (contr) REFERENCES Contr (mnem)
)
ENGINE = InnoDB;

CREATE TABLE Trader (
  mnem CHAR(16) NOT NULL PRIMARY KEY,
  display VARCHAR(64) NOT NULL,
  email VARCHAR(64) NOT NULL,
  FOREIGN KEY (email) REFERENCES RealmUser (email)
)
ENGINE = InnoDB;

DELIMITER //
CREATE TRIGGER beforeInsertOnTrader
  BEFORE INSERT ON Trader
  FOR EACH ROW
  BEGIN
    -- FIXME: allow user to specify password.
    INSERT IGNORE INTO RealmUser (
      email,
      pass
    ) VALUES (
      NEW.email,
      'test'
    );
    INSERT INTO RealmUserRole (
      user,
      role
    ) VALUES (
      (SELECT id FROM RealmUser WHERE email = NEW.email),
      2
    );
  END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER afterDeleteOnTrader
  AFTER DELETE on Trader
  FOR EACH ROW
  BEGIN
    DELETE FROM RealmUserRole
    WHERE user = (SELECT id FROM RealmUser WHERE email = OLD.email)
    AND role = 2;
  END //
DELIMITER ;

CREATE TABLE Order_ (
  id BIGINT NOT NULL,
  trader CHAR(16) NOT NULL,
  market CHAR(16) NOT NULL,
  contr CHAR(16) NOT NULL,
  settlDay INT NOT NULL,
  ref VARCHAR(64) NULL,
  stateId INT NOT NULL,
  actionId INT NOT NULL,
  ticks BIGINT NOT NULL,
  lots BIGINT NOT NULL,
  resd BIGINT NOT NULL,
  exec BIGINT NOT NULL,
  cost BIGINT NOT NULL,
  lastTicks BIGINT NULL DEFAULT NULL,
  lastLots BIGINT NULL DEFAULT NULL,
  minLots BIGINT NOT NULL,
  archive TINYINT(1) NOT NULL DEFAULT 0,
  created BIGINT NOT NULL,
  modified BIGINT NOT NULL,

  PRIMARY KEY (market, id),
  CONSTRAINT orderTraderRefUnq UNIQUE (Trader, ref),

  FOREIGN KEY (trader) REFERENCES Trader (mnem),
  FOREIGN KEY (market) REFERENCES Market (mnem),
  FOREIGN KEY (contr) REFERENCES Contr (mnem),
  FOREIGN KEY (stateId) REFERENCES State (id),
  FOREIGN KEY (actionId) REFERENCES Action (id)
)
ENGINE = InnoDB;

CREATE INDEX orderResdIdx ON Order_ (resd);
CREATE INDEX orderArchiveIdx ON Order_ (archive);

CREATE TABLE Exec (
  id BIGINT NOT NULL,
  orderId BIGINT NOT NULL,
  trader CHAR(16) NOT NULL,
  market CHAR(16) NOT NULL,
  contr CHAR(16) NOT NULL,
  settlDay INT NOT NULL,
  ref VARCHAR(64) NULL,
  stateId INT NOT NULL,
  actionId INT NOT NULL,
  ticks BIGINT NOT NULL,
  lots BIGINT NOT NULL,
  resd BIGINT NOT NULL,
  exec BIGINT NOT NULL,
  cost BIGINT NOT NULL,
  lastTicks BIGINT NULL DEFAULT NULL,
  lastLots BIGINT NULL DEFAULT NULL,
  minLots BIGINT NOT NULL,
  matchId BIGINT NULL DEFAULT NULL,
  roleId INT NULL DEFAULT NULL,
  cpty CHAR(16) NULL DEFAULT NULL,
  archive TINYINT(1) NOT NULL DEFAULT 0,
  created BIGINT NOT NULL,
  modified BIGINT NOT NULL,

  PRIMARY KEY (market, id),

  FOREIGN KEY (market, orderId) REFERENCES Order_ (market, id),
  FOREIGN KEY (trader) REFERENCES Trader (mnem),
  FOREIGN KEY (contr) REFERENCES Contr (mnem),
  FOREIGN KEY (stateId) REFERENCES State (id),
  FOREIGN KEY (actionId) REFERENCES Action (id),
  FOREIGN KEY (roleId) REFERENCES Role (id),
  FOREIGN KEY (cpty) REFERENCES Trader (mnem)
)
ENGINE = InnoDB;

CREATE INDEX execStateIdx ON Exec (stateId);
CREATE INDEX execArchiveIdx ON Exec (archive);

DELIMITER //
CREATE TRIGGER beforeInsertOnExec
  BEFORE INSERT ON Exec
  FOR EACH ROW
  BEGIN
    IF NEW.stateId = 1 THEN
      INSERT INTO Order_ (
        id,
        trader,
        market,
        contr,
        settlDay,
        ref,
        stateId,
        actionId,
        ticks,
        lots,
        resd,
        exec,
        cost,
        lastTicks,
        lastLots,
        minLots,
        archive,
        created,
        modified
      ) VALUES (
        NEW.orderId,
        NEW.trader,
        NEW.market,
        NEW.contr,
        NEW.settlDay,
        NEW.ref,
        NEW.stateId,
        NEW.actionId,
        NEW.ticks,
        NEW.lots,
        NEW.resd,
        NEW.exec,
        NEW.cost,
        NEW.lastTicks,
        NEW.lastLots,
        NEW.minLots,
        NEW.archive,
        NEW.created,
        NEW.modified
      );
    ELSE
      UPDATE Order_
      SET
        stateId = NEW.stateId,
        lots = NEW.lots,
        resd = NEW.resd,
        exec = NEW.exec,
        cost = NEW.cost,
        lastTicks = NEW.lastTicks,
        lastLots = NEW.lastLots,
        modified = NEW.modified
      WHERE id = NEW.orderId;
    END IF;
    IF NEW.stateId = 4 THEN
      UPDATE Market
      SET
        lastTicks = NEW.lastTicks,
        lastLots = NEW.lastLots,
        lastTime = NEW.created
      WHERE mnem = NEW.market;
    END IF;
  END //
DELIMITER ;

CREATE VIEW AssetV AS
  SELECT
    a.mnem,
    a.display,
    t.mnem type
  FROM Asset a
  LEFT OUTER JOIN AssetType t
  ON a.typeId = t.id
;

CREATE VIEW ContrV AS
  SELECT
    c.mnem,
    c.display,
    a.type,
    c.asset,
    c.ccy,
    c.tickNumer,
    c.tickDenom,
    c.lotNumer,
    c.lotDenom,
    c.pipDp,
    c.minLots,
    c.maxLots
  FROM Contr c
  LEFT OUTER JOIN AssetV a
  ON c.asset = a.mnem
;

CREATE VIEW MarketV AS
  SELECT
    m.mnem,
    m.display,
    m.contr,
    m.settlDay,
    m.expiryDay,
    m.lastTicks,
    m.lastLots,
    m.lastTime,
    MAX(e.orderId) maxOrderId,
    MAX(e.id) maxExecId
  FROM Market m
  LEFT OUTER JOIN Exec e
  ON m.mnem = e.market
  GROUP BY m.mnem
;

CREATE VIEW OrderV AS
  SELECT
    o.id,
    o.trader,
    o.market,
    o.contr,
    o.settlDay,
    o.ref,
    s.mnem state,
    a.mnem action,
    o.ticks,
    o.lots,
    o.resd,
    o.exec,
    o.cost,
    o.lastTicks,
    o.lastLots,
    o.minLots,
    o.created,
    o.modified
  FROM Order_ o
  LEFT OUTER JOIN State s
  ON o.stateId = s.id
  LEFT OUTER JOIN Action a
  ON o.actionId = a.id
;

CREATE VIEW ExecV AS
  SELECT
    e.id,
    e.orderId,
    e.trader,
    e.contr,
    e.settlDay,
    e.ref,
    s.mnem state,
    a.mnem action,
    e.ticks,
    e.lots,
    e.resd,
    e.exec,
    e.cost,
    e.lastTicks,
    e.lastLots,
    e.minLots,
    e.matchId,
    r.mnem role,
    e.cpty,
    e.archive,
    e.created,
    e.modified
  FROM Exec e
  LEFT OUTER JOIN State s
  ON e.stateId = s.id
  LEFT OUTER JOIN Action a
  ON e.actionId = a.id
  LEFT OUTER JOIN Role r
  ON e.roleId = r.id
;

CREATE VIEW PosnV AS
  SELECT
    e.trader,
    e.contr,
    e.settlDay,
    e.actionId,
    SUM(e.lastLots * e.lastTicks) cost,
    SUM(e.lastLots) lots
  FROM Exec e
  WHERE e.stateId = 4
  GROUP BY e.trader, e.contr, e.settlDay, e.actionId
;

COMMIT
;
