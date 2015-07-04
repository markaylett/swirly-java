START TRANSACTION
;

USE twirly;

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
       VALUES ('RAMMAC', 'Ram Macharaj', 'ram.mac@gmail.com')
;

COMMIT
;
