package michael.findata.model;

import org.joda.time.DateTime;

import java.util.Date;

public class AdjFactor {
	public DateTime paymentDate;
	public double factor;

	public AdjFactor(DateTime paymentDate, double factor) {
		this.paymentDate = paymentDate;
		this.factor = factor;
	}

	public AdjFactor(Date paymentDate, double factor) {
		this.paymentDate = new DateTime(paymentDate);
		this.factor = factor;
	}
}