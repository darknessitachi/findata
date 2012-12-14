package michael.findata.external;

import java.util.Date;

public class SecurityDividendRecord {
	private Date announcementDate;
	private float amount;
	private float bonus;
	private float split;
	private Date paymentDate;
	private double total_amount;

	public SecurityDividendRecord(Date announcementDate, float amount, float bonus, float split, Date paymentDate, double total_amount) {
		this.announcementDate = announcementDate;
		this.amount = amount;
		this.bonus = bonus;
		this.split = split;
		this.paymentDate = paymentDate;
		this.total_amount = total_amount;
	}

	public Date getAnnouncementDate() {
		return announcementDate;
	}

	public void setAnnouncementDate(Date announcementDate) {
		this.announcementDate = announcementDate;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public float getBonus() {
		return bonus;
	}

	public void setBonus(float bonus) {
		this.bonus = bonus;
	}

	public float getSplit() {
		return split;
	}

	public void setSplit(float split) {
		this.split = split;
	}

	public double getTotal_amount() {
		return total_amount;
	}

	public void setTotal_amount(double total_amount) {
		this.total_amount = total_amount;
	}
}