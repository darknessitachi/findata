package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pair_stats")
@Access(AccessType.FIELD)
public class PairStats {

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	@ManyToOne
	@JoinColumn(name = "pair_id", nullable = false, insertable = true, updatable = false)
	private Pair pair;

	@Basic
	@Column(name = "training_start")
	private Date trainingStart;

	@Basic
	@Column(name = "training_end")
	private Date trainingEnd;

	@Basic
	@Column(name = "time_series_type", columnDefinition = "char(6)")
	@Enumerated(EnumType.STRING)
	private TimeSeriesType timeSeriesType;

	@Basic
	@Column(name = "slope")
	private double slope;

	@Basic
	@Column(name = "stdev")
	private double stdev;

	@Basic
	@Column(name = "correlco")
	private double correlco;

	@Basic
	@Column(name = "adf_p")
	private double adfp; // the p value in ADF test

	@Basic
	@Column(name = "adf_p_ma")
	private double adfpma; // the adfp moving average

	@Transient
	private String codeToShort = null;

	@Basic
	@Access(AccessType.PROPERTY)
	@Column(name = "code_to_short", columnDefinition = "char(9)")
	public String getCodeToShort () {
		if (codeToShort == null) {
			codeToShort = pair.getStockToShort().getCode();
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
			codeToLong = pair.getStockToLong().getCode();
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
			nameToShort = pair.getStockToShort().getName();
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
			nameToLong = pair.getStockToLong().getName();
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

	public Pair getPair() {
		return pair;
	}

	public void setPair(Pair pair) {
		this.pair = pair;
	}

	public Date getTrainingStart() {
		return trainingStart;
	}

	public void setTrainingStart(Date trainingStart) {
		this.trainingStart = trainingStart;
	}

	public Date getTrainingEnd() {
		return trainingEnd;
	}

	public void setTrainingEnd(Date trainingEnd) {
		this.trainingEnd = trainingEnd;
	}

	public double getSlope() {
		return slope;
	}

	public void setSlope(double slope) {
		this.slope = slope;
	}

	public double getStdev() {
		return stdev;
	}

	public void setStdev(double stdev) {
		this.stdev = stdev;
	}

	public double getCorrelco() {
		return correlco;
	}

	public void setCorrelco(double correlco) {
		this.correlco = correlco;
	}

	public double getAdfp() {
		return adfp;
	}

	public void setAdfp(double adf_p) {
		this.adfp = adf_p;
	}

	public TimeSeriesType getTimeSeriesType() {
		return timeSeriesType;
	}

	public void setTimeSeriesType(TimeSeriesType timeSeriesType) {
		this.timeSeriesType = timeSeriesType;
	}

	public double getAdfpma() {
		return adfpma;
	}

	public void setAdfpma(double adfpma) {
		this.adfpma = adfpma;
	}

	public enum TimeSeriesType {

		DAY ("DAY"), HOUR ("HOUR"), MINUTE ("MINUTE");

		private String name;

		TimeSeriesType (String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Override
	public boolean equals (Object another) {
		if (another == null || ! (another instanceof PairStats)) {
			return false;
		} else {
			PairStats anotherStats = (PairStats) another;
			return	pair.equals(anotherStats.pair) &&
					trainingStart.equals(anotherStats.trainingStart) &&
					trainingEnd.equals(anotherStats.trainingEnd) &&
					getTimeSeriesType().equals(anotherStats.getTimeSeriesType());
		}
	}

	@Override
	public int hashCode () {
		return pair.hashCode() + trainingStart.hashCode() + trainingEnd.hashCode() + timeSeriesType.hashCode();
	}

	@Override
	public String toString () {
		return new StringBuilder().append(codeToShort).append(" ").append(nameToShort).append("->").append(codeToLong).append(" ")
				.append(nameToLong).append("[").append(trainingStart).append("::").append(trainingEnd).append("]").toString();
	}
}