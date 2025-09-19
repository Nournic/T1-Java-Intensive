DELETE FROM transactions;
DELETE FROM payments;
DELETE FROM cards;
DELETE FROM accounts;

INSERT INTO accounts (id, client_id, product_id, balance, interest_rate, is_recalc, card_exist, status) VALUES
(401, 101, 201, 150000.75, 0.01, 'true', 'true', 'ACTIVE'),
(402, 102, 202, -25000.50, 0.21, 'true', 'true', 'ACTIVE'),
(403, 103, 203, 750000.00, 0.08, 'true', 'false', 'ACTIVE'),
(404, 104, 203, 12000.25, 0.05, 'false', 'false', 'ARRESTED'),
(405, 109, 201, 5000.00, 0.00, 'false', 'true', 'ACTIVE');

INSERT INTO cards (id, account_id, card_id, payment_system, status) VALUES
(501, 401, 9876543210987654, 'VISA', 'ACTIVE'),
(502, 402, 8765432109876543, 'MASTERCARD', 'ACTIVE'),
(503, 405, 7654321098765432, 'MIR', 'BLOCKED');

INSERT INTO payments (id, account_id, payment_date, amount, is_credit, payed_at, type) VALUES
(601, 401, '2024-06-01T10:00:00', 5000.00, 'false', '2024-06-01T10:00:05', 'SINGLE'),
(602, 401, '2024-06-05T18:00:00', 80000.00, 'true', '2024-06-05T18:00:10', 'RECURRING'),
(603, 402, '2024-06-03T15:30:00', 1250.70, 'false', '2024-06-03T15:30:08', 'SINGLE');

INSERT INTO transactions (id, account_id, card_id, type, amount, status, timestamp) VALUES
(701, 401, 501, 'PAYMENT', 450.50, 'COMPLETE', '2024-06-10T14:45:10'),
(702, 401, NULL, 'DEPOSIT', 80000.00, 'COMPLETE', '2024-06-05T18:00:15'),
(703, 402, 502, 'PAYMENT', 1250.70, 'PROCESSING', '2024-06-11T10:20:00'),
(704, 405, 503, 'WITHDRAWAL', 1000.00, 'CANCELLED', '2024-06-11T11:00:00'),
(705, 404, NULL, 'TRANSFER', 2000.00, 'BLOCKED', '2024-06-09T09:00:00');