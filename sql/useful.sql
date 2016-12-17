-- find record that exists in minute price data but not in daily price data
SELECT DISTINCT (stock.code) FROM stock_price_minute spm
  LEFT OUTER JOIN stock_price sp ON spm.stock_id = sp.stock_id AND spm.date = sp.date
  INNER JOIN stock ON stock.id = spm.stock_id
WHERE sp.id IS NULL AND spm.date >= '2016-06-18 00:00:00'
      AND stock.latest_season IS NOT NULL AND NOT is_ignored;

-- remove the data found in the above step
UPDATE stock_price_minute spm
  LEFT OUTER JOIN stock_price sp ON spm.stock_id = sp.stock_id AND spm.date = sp.date
  INNER JOIN stock ON stock.id = spm.stock_id
SET minute_data = null
WHERE sp.id IS NULL AND spm.date >= '2015-07-29 00:00:00'
      AND stock.latest_season IS NOT NULL;
DELETE FROM stock_price_minute WHERE minute_data IS NULL;

-- find record that exists in daily price data but not in minute price data
SELECT DISTINCT (stock.code) from stock_price_minute spm
  RIGHT OUTER JOIN stock_price sp ON spm.stock_id = sp.stock_id AND spm.date = sp.date
  INNER JOIN stock ON stock.id = sp.stock_id
WHERE spm.id IS NULL AND sp.date >= '2016-06-18 00:00:00'
      AND stock.latest_season IS NOT NULL AND NOT is_ignored;

-- find minute data for a stock
SELECT stock_price_minute.* FROM stock_price_minute, stock WHERE stock_id = stock.id AND stock.code = '002815' ORDER BY date DESC LIMIT 300;

-- find daily data for a stock
SELECT stock_price.* FROM stock_price, stock WHERE stock_id = stock.id AND stock.code = '002815' ORDER BY date DESC LIMIT 104;
