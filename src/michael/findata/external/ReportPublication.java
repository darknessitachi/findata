package michael.findata.external;

import java.util.Date;

public class ReportPublication {
	private Date date;
	private String code;
	private String name;
	private int year;
	private int season;

	public ReportPublication () {
	}

	public ReportPublication (Date date, String code, String name, int year, int season) {
		this.name = name;
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

	@Override
	public boolean equals (Object obj) {
		if (obj instanceof ReportPublication) {
			ReportPublication rpb = (ReportPublication) obj;
			boolean stockEquals = false;
			if (code == null) {
				if (name == null) {
					return false;
				} else {
					stockEquals = name.equals(rpb.getName());
				}
			} else {
				stockEquals = code.equals(rpb.getCode());
			}
			return stockEquals && rpb.getYear() == year && rpb.getSeason() == season;
		} else {
			return false;
		}
	}

	public int hashCode () {
		int hash = 0;
		if (code == null) {
			if (name == null) {
				hash += 0;
			} else {
				hash += name.hashCode();
			}
		} else {
			hash += code.hashCode();
		}
		return hash*33 + year*10 + season;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}