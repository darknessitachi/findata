package michael.findata.external;

import java.util.Date;

public class SecurityShareNumberChange {
	private Date changeDate;
	private Number numberOfShares;

	public SecurityShareNumberChange (Date date, Number numberOfShares) {
		setChangeDate(date);
		this.setNumberOfShares(numberOfShares);
	}

	public Date getChangeDate () {
		return  changeDate;
	}

	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	public Number getNumberOfShares() {
		return numberOfShares;
	}

	public void setNumberOfShares(Number numberOfShares) {
		this.numberOfShares = numberOfShares;
	}
}