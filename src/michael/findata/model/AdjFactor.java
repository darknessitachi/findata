package michael.findata.model;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by nicky on 2015/11/28.
 */
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