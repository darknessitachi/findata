package michael.findata.external;

import java.sql.Date;

public class SecurityTimeSeriesDatum {
	private Date date;
	private int open; // opening price * 1000
	private int high; // highest of the day * 1000
	private int low; // lowest of the day * 1000
	private int close; // closing price * 1000

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getOpen() {
		return open;
	}

	public void setOpen(int open) {
		this.open = open;
	}

	public int getHigh() {
		return high;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	public int getLow() {
		return low;
	}

	public void setLow(int low) {
		this.low = low;
	}

	public int getClose() {
		return close;
	}

	public void setClose(int close) {
		this.close = close;
	}
}
