INSERT INTO User_Info (user_id, username)
VALUES (1, 'Alex'),
       (2, 'Bruno'),
       (3, 'Miguel');

INSERT INTO Session (session_id, user_id, created_at, gateway_topic)
VALUES (1001, 1, '2023-05-01 10:00:00', 'topic1'),
       (1002, 2, '2023-05-02 14:30:00', 'topic2'),
       (1003, 1, '2023-05-03 09:15:00', 'topic3');

INSERT INTO Active_Session (session_id, updated_at)
VALUES (1001, '2023-05-04 11:30:00'),
       (1002, '2023-05-05 16:45:00');

INSERT INTO Subscription (session_id, topic, key)
VALUES (1001, 'topic1', 'key1'),
       (1001, 'topic2', 'key2'),
       (1002, 'topic2', 'key3'),
       (1003, 'topic3', NULL);

INSERT INTO Permission (user_id, topic, key)
VALUES (1, 'topic1', 'permission1'),
       (1, 'topic2', 'permission2'),
       (2, 'topic2', 'permission3'),
       (3, 'topic3', NULL);
