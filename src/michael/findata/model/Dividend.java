package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç4:58
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Dividend {
	private int id;

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name = "id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
	@Id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private Date announcementDate;

	@Column(name = "announcement_date", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public Date getAnnouncementDate() {
		return announcementDate;
	}

	public void setAnnouncementDate(Date announcementDate) {
		this.announcementDate = announcementDate;
	}

	private float amount;

	@Column(name = "amount", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	private Date paymentDate;

	@Column(name = "payment_date", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	private float bonus;

	@Column(name = "bonus", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public float getBonus() {
		return bonus;
	}

	public void setBonus(float bonus) {
		this.bonus = bonus;
	}

	private float split;

	@Column(name = "split", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public float getSplit() {
		return split;
	}

	public void setSplit(float split) {
		this.split = split;
	}

	private double totalAmount;

	@Column(name = "total_amount", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Dividend dividend = (Dividend) o;

//		if (id != dividend.id) return false;
		if (announcementDate != null ? !announcementDate.equals(dividend.announcementDate) : dividend.announcementDate != null) return false;
		if (stock != null ? !stock.equals(dividend.stock) : dividend.stock != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = stock.hashCode();
		result = 31 * result + (announcementDate != null ? announcementDate.hashCode() : 0);
		return result;
	}

	private Stock stock;

	@javax.persistence.JoinColumn(name = "stock_id", nullable = false, insertable = true, updatable = false)
	@ManyToOne
	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}
}
