package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;


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

	private Float amount;

	@Column(name = "amount", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
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

	private Float bonus;

	@Column(name = "bonus", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getBonus() {
		return bonus;
	}

	public void setBonus(Float bonus) {
		this.bonus = bonus;
	}

	private Float split;

	@Column(name = "split", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getSplit() {
		return split;
	}

	public void setSplit(Float split) {
		this.split = split;
	}

	private Double totalAmount;

	@Column(name = "total_amount", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !getClass().equals(o.getClass())) return false;
		Dividend dividend = (Dividend) o;

		if (paymentDate != null ? !paymentDate.equals(dividend.paymentDate) : dividend.paymentDate != null) return false;
		if (stock != null ? !stock.equals(dividend.stock) : dividend.stock != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = stock.hashCode();
		result = 31 * result + (paymentDate != null ? paymentDate.hashCode() : 0);
		return result;
	}

	private Stock stock;

	@ManyToOne
	@JoinColumn(name = "stock_id", nullable = false, insertable = true, updatable = false)
	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}
}