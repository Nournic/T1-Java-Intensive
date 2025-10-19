delete from client_products;
delete from users_roles;
delete from clients;
delete from users;
delete from products;
delete from roles;

INSERT INTO users (id, login, email, password) VALUES
(1, 'valerie', 'valerie@example.com', '{bcrypt}$2a$10$bFCRS8TMMpnkfyUisBAukuoH.JMVXXQrObUCcUQjjKucL8ndB5viW'),
(2, 'mark', 'mark@example.com', '{bcrypt}$2a$10$slsu4tZZVfRHW5DZOINkkun0g6UX09fU0.CPda52zvEyYBWLPeLhu'),
(3, 'sandra', 'sandra@example.com', '{bcrypt}$2a$10$DZelDQ4.WzXRjFZCpXvtcuPBeavy.FKxo5gKPZkHXff.U1gv07MxC'),
(4, 'hudson', 'hudson@example.com', '{bcrypt}$2a$10$DBhS6ubdJS788htu4jIiBOpbCGUnGhSu/mXQIIdlLd2VnwGKy7n16'),
(5, 'hill', 'hill@example.com', '{bcrypt}$2a$10$BwPLJ.Xqi2PAWuyOKTAZOOl7eyxPKuUiPBtutfFZBOPiHkmot0fra'),
(6, 'dorothy', 'dorothy@example.com', '{bcrypt}$2a$10$70bBo.3e0P2VkboOrR1Z3uQxwZLipA1Hg8doX4S53qVS5rj0J76CW'),
(7, 'marie', 'marie@example.com', '{bcrypt}$2a$10$dJkf05HEyga3nPUi1PawQuN4.NchMOqIAfDKKPNIU96ZUQt9EJ6ta'),
(8, 'bishop', 'bishop@example.com', '{bcrypt}$2a$10$oHXsR.qmZKKFJD/PgRNMoua3PoGr34VYhWKT2WlRTdh.E79NvHpay'),
(9, 'elliott', 'elliott@example.com', '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92sV3llgiV2UpjOud3yGm'),
(10, 'john', 'john@example.com', '{bcrypt}$2a$10$gaQt4fIlkQ3Og3X6AxBp0eT8EodrnBIMfCLvYLqNhWiuIdldnlBPa');

INSERT INTO clients (id, client_id, user_id, first_name, middle_name, last_name, date_of_birth, document_type, document_id, document_prefix, document_suffix) VALUES
(101, '770100000001', 1, 'Valerie', 'Anne', 'Williams', '1992-03-15', 'PASSPORT', '111222', '4510', NULL),
(102, '770100000002', 2, 'Mark', 'Steven', 'Johnson', '1985-11-20', 'PASSPORT', '222333', '4512', NULL),
(103, '770100000003', 3, 'Sandra', 'Elizabeth', 'Brown', '2005-01-30', 'BIRTH_CERT', '333444', 'I-AG', '2021'),
(104, '770100000004', 4, 'Hudson', 'James', 'Garcia', '1978-07-22', 'INT_PASSPORT', '444555', '71', NULL),
(105, '770100000005', 5, 'Hill', 'Thomas', 'Miller', '1999-09-09', 'PASSPORT', '555666', '4601', NULL),
(106, '770100000006', 6, 'Dorothy', 'Marie', 'Davis', '1964-04-12', 'PASSPORT', '666777', '4505', NULL),
(107, '770100000007', 7, 'Marie', 'Grace', 'Rodriguez', '1995-06-25', 'PASSPORT', '777888', '4515', NULL),
(108, '770100000008', 8, 'Bishop', 'Charles', 'Martinez', '1988-12-01', 'INT_PASSPORT', '888999', '75', NULL),
(109, '770100000009', 9, 'Elliott', 'Robert', 'Taylor', '2001-08-18', 'PASSPORT', '999000', '4605', NULL),
(110, '770100000010', 10, 'John', 'Michael', 'Smith', '1990-10-05', 'PASSPORT', '123789', '4511', NULL);

INSERT INTO products (id, name, product_key, create_date, product_id) VALUES
(201, 'Дебетовая карта "Классика"', 'DC', '2023-01-10T10:00:00', 'DC201'),
(202, 'Кредитная карта "Платинум"', 'CC', '2023-01-15T11:00:00', 'CC202'),
(203, 'Накопительный счет "Капитал"', 'NS', '2023-02-01T09:00:00', 'NS203'),
(204, 'Пенсионные накопления', 'PENS', '2023-02-20T14:00:00', 'PENS204'),
(205, 'Ипотечный кредит "Свой дом"', 'IPO', '2023-03-05T15:00:00', 'IPO205'),
(206, 'Автокредит "Драйв"', 'AC', '2023-03-10T12:00:00', 'AC206');

INSERT INTO client_products (id, client_id, product_id, open_date, close_date, status) VALUES
(301, 101, 201, '2024-02-15T14:30:00', NULL, 'ACTIVE'),
(302, 102, 202, '2024-03-20T11:00:00', NULL, 'ACTIVE'),
(303, 103, 203, '2024-04-01T10:15:00', NULL, 'ACTIVE'),
(304, 104, 201, '2023-05-10T18:00:00', '2024-05-10T18:00:00', 'CLOSED'),
(305, 104, 203, '2024-05-11T09:00:00', NULL, 'ACTIVE'),
(306, 105, 204, '2024-01-25T16:00:00', NULL, 'ACTIVE'),
(307, 106, 205, '2024-02-28T17:45:00', NULL, 'ACTIVE'),
(308, 107, 206, '2024-03-05T12:30:00', NULL, 'ACTIVE'),
(309, 108, 202, '2024-04-10T13:00:00', NULL, 'BLOCKED'),
(310, 109, 201, '2024-05-01T11:20:00', NULL, 'ACTIVE'),
(311, 110, 203, '2024-05-15T10:00:00', NULL, 'ACTIVE');

INSERT INTO roles (id, role_name) values
(1, 'ROLE_CURRENT_CLIENT'),
(2, 'ROLE_GRAND_EMPLOYEE'),
(3, 'ROLE_MASTER'),
(4, 'ROLE_BLOCKED_CLIENT');

INSERT INTO users_roles (user_id, roles_id) values
(1, 3),
(1, 1),
(2, 2),
(3, 1),
(4, 1),
(5, 1),
(6, 1),
(7, 1),
(8, 1),
(9, 1),
(10, 1);
