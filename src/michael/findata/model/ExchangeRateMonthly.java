package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "exchange_rate_monthly")
public class ExchangeRateMonthly {
	private int id;

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@javax.persistence.Column(name = "id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
	@Id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private String currency;

	@javax.persistence.Column(name = "currency", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	private int year;

	@javax.persistence.Column(name = "year", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	private int month;

	@javax.persistence.Column(name = "month", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	private float rate;

	@javax.persistence.Column(name = "rate", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ExchangeRateMonthly that = (ExchangeRateMonthly) o;

//		if (id != that.id) return false;
		if (month != that.month) return false;
		if (year != that.year) return false;
		if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result =  (currency != null ? currency.hashCode() : 0);
		result = 31 * result + year;
		result = 31 * result + month;
		return result;
	}
}
