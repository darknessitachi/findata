package michael.findata.external;

import java.util.Date;

public class SecurityDividendRecord {
	private Date announcementDate;
	private float amount;
	private float bonus;
	private float bonus2;
	private Date paymentDate;
	private double total_amount;

	public SecurityDividendRecord(Date announcementDate, float amount, float bonus, float bonus2, Date paymentDate, double total_amount) {
		this.announcementDate = announcementDate;
		this.amount = amount;
		this.bonus = bonus;
		this.bonus2 = bonus2;
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

	public float getBonus2() {
		return bonus2;
	}

	public void setBonus2(float bonus2) {
		this.bonus2 = bonus2;
	}

	public double getTotal_amount() {
		return total_amount;
	}

	public void setTotal_amount(double total_amount) {
		this.total_amount = total_amount;
	}
}