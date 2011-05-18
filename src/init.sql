-- Must use utf-8 encoding for these tables

CREATE TABLE stock (
	id INT AUTO_INCREMENT,
	code VARCHAR(20),
	name VARCHAR(20),
	current_price FLOAT,
	number_of_shares FLOAT,
	latest_year INT,
	latest_season VARCHAR(255),
	UNIQUE (code),
	PRIMARY KEY (id)
);

CREATE TABLE stock_price (
	id INT AUTO_INCREMENT,
	stock_id INT,
	price_date DATE,
	price FLOAT,
	FOREIGN KEY (stock_id) REFERENCES stock(id),
	PRIMARY KEY (id)
);

-- CREATE TABLE fin_sheet (
-- 	id INT AUTO_INCREMENT,
-- 	name VARCHAR(20),
-- 	PRIMARY KEY (id),
-- 	UNIQUE (name)
-- );

-- CREATE TABLE fin_field (
-- 	id INT AUTO_INCREMENT,
-- 	fin_sheet_id INT,
-- 	name VARCHAR(255),
-- 	PRIMARY KEY (id),
-- 	FOREIGN KEY (fin_sheet_id) REFERENCES fin_sheet(id),
-- 	UNIQUE (name)
-- );

-- CREATE TABLE fin_period (
-- 	id INT AUTO_INCREMENT,
-- 	name VARCHAR(10),
-- 	annual BOOLEAN,
-- 	PRIMARY KEY (id),
-- 	UNIQUE (name)
-- );

-- CREATE TABLE source (
-- 	id INT AUTO_INCREMENT,
-- 	name VARCHAR(255),
-- 	url VARCHAR(255),
-- 	PRIMARY KEY (id)
-- );

-- CREATE TABLE source_fin_sheet (
-- 	id INT AUTO_INCREMENT,
-- 	source_id INT,
-- 	fin_sheet_id INT,
-- 	text_pattern VARCHAR(255),
-- 	PRIMARY KEY (id),
-- 	FOREIGN KEY (source_id) REFERENCES source(id),
-- 	FOREIGN KEY (fin_sheet_id) REFERENCES fin_sheet(id)
-- );

CREATE TABLE fin_data (
	id INT AUTO_INCREMENT,
	stock_id INT,
-- 	fin_period_id INT,
-- 	fin_sheet_id INT,
	fin_year INT,
	fin_season INT(1),
	fin_sheet VARCHAR(255),
	source VARCHAR(255),
	name VARCHAR(255),
	value DOUBLE,
	order_ INT,
	PRIMARY KEY (id),
	FOREIGN KEY (stock_id) REFERENCES stock(id),
-- 	FOREIGN KEY (fin_period_id) REFERENCES fin_period(id),
-- 	FOREIGN KEY (fin_sheet_id) REFERENCES fin_sheet(id),
-- 	FOREIGN KEY (source_id) REFERENCES source(id),
	UNIQUE (stock_id, fin_year, fin_season, source, fin_sheet, name)
);

-- INSERT INTO fin_sheet (name) VALUES ('balance');
-- INSERT INTO fin_sheet (name) VALUES ('profit&loss');
-- INSERT INTO fin_sheet (name) VALUES ('cashflow');

-- INSERT INTO fin_period (name, annual) VALUE ('1992.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('1993.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('1994.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('1995.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('1996.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('1997.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('1998.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('1999.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2000.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2001.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2002.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2003.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2004.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2005.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2006.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2007.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2008.12', true);
-- INSERT INTO fin_period (name, annual) VALUE ('2009.12', true);

-- INSERT INTO fin_period (name, annual) VALUE ('2010.03', false);
-- INSERT INTO fin_period (name, annual) VALUE ('2010.06', false);
-- INSERT INTO fin_period (name, annual) VALUE ('2010.09', false);
-- INSERT INTO fin_period (name, annual) VALUE ('2010.12', true);

-- INSERT INTO source (name, url) VALUES ('hexun2008', 'http://stockdata.stock.hexun.com/2008/[fin_sheet].aspx?stockid=[stock]&accountdate=[fin_period]');
-- INSERT INTO source_fin_sheet (source_id, fin_sheet_id, text_pattern) VALUES (1,1, 'zcfz');
-- INSERT INTO source_fin_sheet (source_id, fin_sheet_id, text_pattern) VALUES (1,2, 'lr');
-- INSERT INTO source_fin_sheet (source_id, fin_sheet_id, text_pattern) VALUES (1,3, 'xjll');
