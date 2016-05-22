package michael.findata.model;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "pair_instance")
@Access(AccessType.FIELD)
public class PairInstance implements Comparable {

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	@Basic
	@Column(name = "status",columnDefinition="char(6)")
	@Enumerated(EnumType.STRING)
	private PairStatus status;

	@Basic
	@Column(name = "date_opened")
	private Timestamp dateOpened;

	@Basic
	@Column(name = "date_closed")
	private Timestamp dateClosed;

	@Basic
	@Column(name = "short_open")
	private Double shortOpen;

	@Basic
	@Column(name = "long_open")
	private Double longOpen;

	@Basic
	@Column(name = "threshold_open")
	private Double thresholdOpen;

	@Basic
	@Column(name = "max_amount_possible_open")
	private Double maxAmountPossibleOpen;

	@Basic
	@Column(name = "short_close")
	private Double shortClose;

	@Basic
	@Column(name = "long_close")
	private Double longClose;

	@Basic
	@Column(name = "threshold_close")
	private Double thresholdClose;

	@Basic
	@Column(name = "max_amount_possible_close")
	private Double maxAmountPossibleClose;

	@Basic
	@Column(name = "short_volume_held")
	private Integer shortVolumeHeld;

	@Basic
	@Column(name = "long_volume_held")
	private Integer longVolumeHeld;

	@Basic
	@Column(name = "min_res")
	private Double minResidual;

	@Basic
	@Column(name = "max_res")
	private Double maxResidual;

	@Basic
	@Column(name = "min_res_date")
	private Timestamp minResidualDate;

	@Basic
	@Column(name = "max_res_date")
	private Timestamp maxResidualDate;

	@Basic
	@Column(name = "res_open")
	private Double residualOpen;

	@Basic
	@Column(name = "res_close")
	private Double residualClose;

	@ManyToOne
	@JoinColumn(name = "pair_stats_id", nullable = false, insertable = true, updatable = false)
	private PairStats stats;

	@Basic
	@Column(name = "openable_on")
	private Date openableDate;

	@Basic
	@Column(name = "force_closure_on_or_after")
	private Date forceClosureDate;

	@Transient
	private String codeToShort = null;

	public PairInstance() {	}

	@Basic
	@Access(AccessType.PROPERTY)
	@Column(name = "code_to_short", columnDefinition = "char(9)")
	public String getCodeToShort () {
		if (codeToShort == null) {
			codeToShort = stats.getPair().getStockToShort().getCode();
		}
		return codeToShort;
	}

	public void setCodeToShort (String codeToShort) {
		this.codeToShort = codeToShort;
	}

	@Transient
	private String codeToLong = null;

	@Basic
	@Access(AccessType.PROPERTY)
	@Column(name = "code_to_long", columnDefinition = "char(9)")
	public String getCodeToLong() {
		if (codeToLong == null) {
			codeToLong = stats.getPair().getStockToLong().getCode();
		}
		return codeToLong;
	}

	public void setCodeToLong(String codeToLong) {
		this.codeToLong = codeToLong;
	}

	@Transient
	private String nameToShort = null;

	@Access(AccessType.PROPERTY)
	@Column(name = "name_to_short", columnDefinition = "char(14)")
	public String getNameToShort() {
		if (nameToShort == null) {
			nameToShort = stats.getPair().getStockToShort().getName();
		}
		return nameToShort;
	}

	public void setNameToShort(String nameToShort) {
		this.nameToShort = nameToShort;
	}

	@Transient
	private String nameToLong = null;

	@Access(AccessType.PROPERTY)
	@Column(name = "name_to_long", columnDefinition = "char(14)")
	public String getNameToLong() {
		if (nameToLong == null) {
			nameToLong = stats.getPair().getStockToLong().getName();
		}
		return nameToLong;
	}

	public void setNameToLong(String nameToLong) {
		this.nameToLong = nameToLong;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public PairStatus getStatus() {
		return status;
	}

	public void setStatus(PairStatus status) {
		this.status = status;
	}

	public Timestamp getDateOpened() {
		return dateOpened;
	}

	public void setDateOpened(Timestamp dateOpened) {
		this.dateOpened = dateOpened;
	}

	public Timestamp getDateClosed() {
		return dateClosed;
	}

	public void setDateClosed(Timestamp dateClosed) {
		this.dateClosed = dateClosed;
	}

	public Double getShortOpen() {
		return shortOpen;
	}

	public void setShortOpen(Double shortOpen) {
		this.shortOpen = shortOpen;
	}

	public Double getLongOpen() {
		return longOpen;
	}

	public void setLongOpen(Double longOpen) {
		this.longOpen = longOpen;
	}

	public Double getThresholdOpen() {
		return thresholdOpen;
	}

	public void setThresholdOpen(Double thresholdOpen) {
		this.thresholdOpen = thresholdOpen;
	}

	public Double getMaxAmountPossibleOpen() {
		return maxAmountPossibleOpen;
	}

	public void setMaxAmountPossibleOpen(Double maxAmountPossibleOpen) {
		this.maxAmountPossibleOpen = maxAmountPossibleOpen;
	}

	public Double getShortClose() {
		return shortClose;
	}

	public void setShortClose(Double shortClose) {
		this.shortClose = shortClose;
	}

	public Double getLongClose() {
		return longClose;
	}

	public void setLongClose(Double longCLose) {
		this.longClose = longCLose;
	}

	public Double getThresholdClose() {
		return thresholdClose;
	}

	public void setThresholdClose(Double thresholdClose) {
		this.thresholdClose = thresholdClose;
	}

	public Double getMaxAmountPossibleClose() {
		return maxAmountPossibleClose;
	}

	public void setMaxAmountPossibleClose(Double maxAmountPossibleClose) {
		this.maxAmountPossibleClose = maxAmountPossibleClose;
	}

	public Integer getShortVolumeHeld() {
		return shortVolumeHeld;
	}

	public void setShortVolumeHeld(Integer shortVolumeHeld) {
		this.shortVolumeHeld = shortVolumeHeld;
	}

	public Integer getLongVolumeHeld() {
		return longVolumeHeld;
	}

	public void setLongVolumeHeld(Integer longVolumeHeld) {
		this.longVolumeHeld = longVolumeHeld;
	}

	public Double getMinResidual() {
		return minResidual;
	}

	public void setMinResidual(Double minResidual) {
		this.minResidual = minResidual;
	}

	public Double getMaxResidual() {
		return maxResidual;
	}

	public void setMaxResidual(Double maxResidual) {
		this.maxResidual = maxResidual;
	}

	public Timestamp getMinResidualDate() {
		return minResidualDate;
	}

	public void setMinResidualDate(Timestamp minResidualDate) {
		this.minResidualDate = minResidualDate;
	}

	public Timestamp getMaxResidualDate() {
		return maxResidualDate;
	}

	public void setMaxResidualDate(Timestamp maxResidualDate) {
		this.maxResidualDate = maxResidualDate;
	}

	public Double getResidualOpen() {
		return residualOpen;
	}

	public void setResidualOpen(Double residualOpen) {
		this.residualOpen = residualOpen;
	}

	public Double getResidualClose() {
		return residualClose;
	}

	public void setResidualClose(Double residualClose) {
		this.residualClose = residualClose;
	}

	public PairStats getStats() {
		return stats;
	}

	public void setStats(PairStats stats) {
		this.stats = stats;
	}

	public Date getOpenableDate() {
		return openableDate;
	}

	public void setOpenableDate(Date openableOn) {
		this.openableDate = openableOn;
	}

	public Date getForceClosureDate() {
		return forceClosureDate;
	}

	public void setForceClosureDate(Date forceClosureOnOrAfter) {
		this.forceClosureDate = forceClosureOnOrAfter;
	}

	public enum PairStatus {

		NEW ("NEW"), OPENED ("OPENED"), CLOSED ("CLOSED"), FORCED ("FORCED");

		private String name;

		PairStatus (String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public Product toShort() {
		return stats.getPair().getStockToShort();
	}

	public Product toLong() {
		return stats.getPair().getStockToLong();
	}

	public LocalDate trainingStart() {
		return LocalDate.fromDateFields(stats.getTrainingStart());
	}

	public LocalDate trainingEnd() {
		return LocalDate.fromDateFields(stats.getTrainingEnd());
	}

	public double slope() {
		return stats.getSlope();
	};

	public double stdev() {
		return stats.getStdev();
	}

	//correlation coefficient obtained during training pass
	public double correlco() {
		return stats.getCorrelco();
	}

	// p value in adf test obtained during training pass
	public double adf_p() {
		return stats.getAdfp();
	}

	public int age (LocalDate now) {
		if (dateOpened == null) {
			return 0;
		}
		switch (status) {
			case OPENED:
			case CLOSED:
			case FORCED:
				return Days.daysBetween(LocalDate.fromDateFields(dateOpened), now).getDays();
			default:
				return 0;
		}
	}

	public int age (DateTime now) {
		return age (now.toLocalDate());
	}

	public PairInstance (int id, PairStats stats) {
		this.id = id;
		this.stats = stats;
		this.status = PairStatus.NEW;
		this.shortVolumeHeld = 0;
		this.longVolumeHeld = 0;
	}

	public PairInstance (int id, PairStats stats,
				 Double shortOpen, Double longOpen,
				 Integer shortPositionHeld, Integer longPositionHeld,
				 Timestamp dateOpened, Double thresholdOpen, Double maxAmountPossibleOpen,
				 Double minResidual, Double maxResidual, Timestamp minResDate, Timestamp maxResDate) {
		this.id = id;
		this.stats = stats;
		this.status = PairStatus.OPENED;
		this.shortVolumeHeld = shortPositionHeld;
		this.longVolumeHeld = longPositionHeld;
		this.dateOpened = dateOpened;
		this.shortOpen = shortOpen;
		this.longOpen = longOpen;
		this.thresholdOpen = thresholdOpen;
		this.maxAmountPossibleOpen = maxAmountPossibleOpen;
		this.minResidual = minResidual;
		this.maxResidual = maxResidual;
		this.minResidualDate = minResDate;
		this.maxResidualDate = maxResDate;
	}

	public PairInstance (int id, PairStats stats,
				 Double shortOpen, Double longOpen,
				 Integer shortPositionHeld, Integer longPositionHeld,
				 Timestamp dateOpened, Double thresholdOpen, Double maxAmountPossibleOpen,
				 Double shortClose, Double longClose,
				 Timestamp dateClosed, Double thresholdClose, Double maxAmountPossibleClose,
				 PairStatus status, Double minResidual, Double maxResidual, Timestamp minResDate, Timestamp maxResDate) {
		this.id = id;
		this.stats = stats;
		this.status = PairStatus.CLOSED;
		this.shortVolumeHeld = shortPositionHeld;
		this.longVolumeHeld = longPositionHeld;
		this.dateOpened = dateOpened;
		this.shortOpen = shortOpen;
		this.longOpen = longOpen;
		this.thresholdOpen = thresholdOpen;
		this.maxAmountPossibleOpen = maxAmountPossibleOpen;
		this.dateClosed = dateClosed;
		this.shortClose = shortClose;
		this.longClose = longClose;
		this.thresholdClose = thresholdClose;
		this.maxAmountPossibleClose = maxAmountPossibleClose;
		this.status = status;
		this.minResidual = minResidual;
		this.maxResidual = maxResidual;
		this.minResidualDate = minResDate;
		this.maxResidualDate = maxResDate;
	}

	public double profitPercentageEstimate () {
		try {
			return (shortOpen - shortClose) / shortOpen + (longClose - longOpen) / longOpen - feeEstimate();
		} catch (NullPointerException npe) {
			return 0;
		}
	}

	public int closureAge () {
		return age(LocalDate.fromDateFields(dateClosed));
	}

	public void reset () {
		this.status = PairStatus.NEW;
		this.shortVolumeHeld = 0;
		this.longVolumeHeld = 0;
		this.shortOpen = -1d;
		this.longOpen = -1d;
		this.shortClose = -1d;
		this.longClose = -1d;
		this.dateOpened = null;
		this.dateClosed = null;
		this.minResidual = -1000d;
		this.maxResidual = 1000d;
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * <p>
	 * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 * <p>
	 * <p>The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.
	 * <p>
	 * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
	 * all <tt>z</tt>.
	 * <p>
	 * <p>It is strongly recommended, but <i>not</i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates
	 * this condition should clearly indicate this fact.  The recommended
	 * language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 * <p>
	 * <p>In the foregoing description, the notation
	 * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
	 * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
	 * <tt>0</tt>, or <tt>1</tt> according to whether the value of
	 * <i>expression</i> is negative, zero or positive.
	 *
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 */
	@Override
	public int compareTo(Object o) {
		if (o == null || !(o instanceof PairInstance)) return 0;
		PairInstance anotherPair = (PairInstance) o;
		if (status == PairStatus.NEW || anotherPair.status == PairStatus.NEW) return 0;
		Timestamp thisDate = status == PairStatus.OPENED ? dateOpened : dateClosed;
		Timestamp anotherDate = anotherPair.status == PairStatus.OPENED ? anotherPair.dateOpened : anotherPair.dateClosed;
		return thisDate.compareTo(anotherDate);
	}

	public double feeEstimate() {
		int age = closureAge();
		double taxShort;
		double taxLong;
		if (toLong().symbol().startsWith("15") || toLong().symbol().startsWith("5")) {
			taxLong = 0d;
		} else {
			taxLong = 0.001d;
		}
		if (toShort().symbol().startsWith("15") || toShort().symbol().startsWith("5")) {
			taxShort = 0d;
		} else {
			taxShort = 0.001d;
		}
		return taxLong + taxShort + 4 * 0.0004 + (age==0?1:age) * 0.0835 / 360;
	}

	public PairInstance copy() {
		if (status == PairStatus.OPENED) {
			return new PairInstance(
					id, stats, shortOpen, longOpen, shortVolumeHeld, longVolumeHeld,
					dateOpened, thresholdOpen, maxAmountPossibleOpen,
					minResidual, maxResidual, minResidualDate, maxResidualDate);
		} else if (status == PairStatus.CLOSED || status == PairStatus.FORCED) {
			return new PairInstance(
					id, stats, shortOpen, longOpen, shortVolumeHeld, longVolumeHeld,
					dateOpened, thresholdOpen, maxAmountPossibleOpen,
					shortClose, longClose, dateClosed, thresholdClose, maxAmountPossibleClose, status,
					minResidual, maxResidual, minResidualDate, maxResidualDate);
		} else {
			return null;
		}
	}

	@Override
	public String toString () {
		return new StringBuilder().append(codeToShort).append(" ").append(nameToShort).append("->").append(codeToLong).append(" ")
				.append(nameToLong).append("<").append(openableDate).append("==").append(forceClosureDate).append(">").toString();
	}

	@Override
	public int hashCode () {
		return stats.hashCode()+99;
	}
}