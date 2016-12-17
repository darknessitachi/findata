package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "exchange_rate_daily")
@Access(AccessType.FIELD)
public class ExchangeRateDaily {

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name = "id", nullable = false, length = 10)
	@Id
	private int id;

	@Basic
	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Basic
	@Column(name = "open", nullable = false, length = 8)
	private double open;

	@Basic
	@Column(name = "high", nullable = false, length = 8)
	private double high;

	@Basic
	@Column(name = "low", nullable = false, length = 8)
	private double low;

	@Basic
	@Column(name = "close", nullable = false, length = 8)
	private double close;

	@Basic
	@Column(name = "exchange_date", nullable = false)
	private Timestamp date;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ExchangeRateDaily that = (ExchangeRateDaily) o;

//		if (id != that.id) return false;
		if (!date.equals(that.date)) return false;
		if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result =  (currency != null ? currency.hashCode() : 0);
		result = 31 * result + date.hashCode();
		return result;
	}

	public int id() {
		return id;
	}

	public String currency() {
		return currency;
	}

	public void currency(String currency) {
		this.currency = currency;
	}

	public double rate() {
		return close;
	}

	public Timestamp date() {
		return date;
	}

	public void date(Timestamp date) {
		this.date = date;
	}

	public double open() {
		return open;
	}

	public void open(double open) {
		this.open = open;
	}

	public double high() {
		return high;
	}

	public void high(double high) {
		this.high = high;
	}

	public double low() {
		return low;
	}

	public void low(double low) {
		this.low = low;
	}

	public double close() {
		return close;
	}

	public void close(double close) {
		this.close = close;
	}
}