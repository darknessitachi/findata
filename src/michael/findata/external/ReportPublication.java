package michael.findata.external;

import java.util.Date;

public class ReportPublication {
	private Date date;
	private String code;
	private int year;
	private int season;

	public ReportPublication (Date date, String code, int year, int season) {
		this.date = date;
		this.code = code;
		this.year = year;
		this.season = season;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}
}
