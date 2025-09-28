DELETE FROM payment_registries;
DELETE FROM product_registries;

INSERT INTO product_registries (id, client_id, account_id, product_id, interest_rate, open_date, amount, month_count) VALUES
(801, 106, 901, 205, 0.0950, '2024-02-28T17:45:00', 20000000, 12),
(802, 107, 902, 206, 0.1500, '2024-03-05T12:30:00', 5000000, 24);

INSERT INTO payment_registries (id, product_registry_id, payment_date, amount, interest_rate_amount, debt_amount, expired, payment_expiration_date) VALUES
(1001, 801, '2024-05-28T10:00:00', 35000.00, 20000.00, 15000.00, 'true', '2024-05-28T23:59:59'),
(1002, 801, '2024-06-28T10:00:00', 35000.00, 19800.00, 15200.00, 'false', '2024-06-28T23:59:59'),
(1003, 802, '2024-06-05T10:00:00', 25000.00, 10000.00, 15000.00, 'false', '2024-06-05T23:59:59');