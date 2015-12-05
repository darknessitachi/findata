package michael.findata.external;

import org.joda.time.DateTime;

import java.sql.Date;

public class SecurityTimeSeriesDatum {
//	private Date date;
//	private int minute;
	private DateTime dateTime;
	private int open; // opening price * 1000
	private int high; // highest of the day * 1000
	private int low; // lowest of the day * 1000
	private int close; // closing price * 1000
	private int volume; // volume (#shares)
	private float amount; // amount (#RMB)

//	public Date getDate() {
//		return date;
//	}
//
//	public void setDate(Date date) {
//		this.date = date;
//	}

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

//	public int getMinute() {
//		return minute;
//	}
//
//	public void setMinute(int minute) {
//		this.minute = minute;
//	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volumn) {
		this.volume = volumn;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}
}
