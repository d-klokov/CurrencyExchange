DROP TABLE IF EXISTS currencies;
CREATE TABLE IF NOT EXISTS currencies (
    id INTEGER PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    sign TEXT NOT NULL
);
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('USD', 'United States dollar', '$');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('EUR', 'Euro', '€');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('RUB', 'Russian Ruble', '₽');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('JPY', 'Yen', '¥');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('CNY', 'Yuan Renminbi', '¥');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('GBP', 'Pound Sterling', '£');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('UAH', 'Hryvnia', '₴');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('KZT', 'Tenge', 'лв');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('NOK', 'Norwegian Krone', 'kr');
INSERT OR IGNORE INTO currencies (code, full_name, sign) VALUES ('SEK', 'Swedish Krona', 'kr');

DROP TABLE IF EXISTS exchange_rates;
CREATE TABLE IF NOT EXISTS exchange_rates (
    id INTEGER PRIMARY KEY,
    base_currency_id INTEGER,
    target_currency_id INTEGER,
    rate DECIMAL(10, 6),
    FOREIGN KEY (base_currency_id) REFERENCES currencies (id),
    FOREIGN KEY (target_currency_id) REFERENCES currencies (id),
    UNIQUE (base_currency_id, target_currency_id)
);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (1, 2, 0.9270);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (1, 3, 80.0473);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (1, 4, 133.84);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (1, 5, 7.0498);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (6, 1, 1.2405);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (7, 1, 36.8791);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (8, 1, 446.027);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (9, 1, 0.0909);
INSERT OR IGNORE INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (2, 3, 85.784);
