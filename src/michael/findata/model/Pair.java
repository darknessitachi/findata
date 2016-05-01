package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "pair")
@Access(AccessType.FIELD)
public class Pair {

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	@ManyToOne
	@JoinColumn(name = "short_id", nullable = false, insertable = true, updatable = false)
	private Stock stockToShort;

	@ManyToOne
	@JoinColumn(name = "long_id", nullable = false, insertable = true, updatable = false)
	private Stock stockToLong;

	@Column(name = "max_amount_allowed")
	private double maxAmountAllowed;

	@Column(name = "enabled")
	private boolean enabled;

	@Transient
	private String codeToShort = null;

	@Access(AccessType.PROPERTY)
	@Column(name = "code_to_short", columnDefinition = "char(9)")
	public String getCodeToShort () {
		if (codeToShort == null) {
			codeToShort = stockToShort.getCode();
		}
		return codeToShort;
	}

	public void setCodeToShort (String codeToShort) {
		this.codeToShort = codeToShort;
	}

	@Transient
	private String codeToLong = null;

	@Access(AccessType.PROPERTY)
	@Column(name = "code_to_long", columnDefinition = "char(9)")
	public String getCodeToLong() {
		if (codeToLong == null) {
			codeToLong = stockToLong.getCode();
		}
		return codeToLong;
	}

	public void setCodeToLong(String codeToLong) {
		this.codeToLong = codeToLong;
	}

	@Override
	public boolean equals (Object another) {
		if (another == null) {
			return false;
		}
		if (another instanceof Pair) {
			Pair anotherPair = (Pair) another;
			return 	anotherPair.getCodeToShort().equals(getCodeToShort()) &&
					anotherPair.getCodeToLong().equals(getCodeToLong());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode () {
		return getCodeToLong().hashCode()+getCodeToShort().hashCode();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Stock getStockToShort() {
		return stockToShort;
	}

	public void setStockToShort(Stock stock) {
		this.stockToShort = stock;
	}

	public Stock getStockToLong() {
		return stockToLong;
	}

	public void setStockToLong(Stock stock) {
		this.stockToLong = stock;
	}

	public double getMaxAmountAllowed() {
		return maxAmountAllowed;
	}

	public void setMaxAmountAllowed(double maxAmountAllowed) {
		this.maxAmountAllowed = maxAmountAllowed;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}