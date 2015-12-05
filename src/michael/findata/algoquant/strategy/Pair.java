package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import org.joda.time.DateTime;
import org.joda.time.Days;

import static michael.findata.algoquant.strategy.Pair.PairStatus.NEW;
import static michael.findata.algoquant.strategy.Pair.PairStatus.OPENED;

/**
 * Created by nicky on 2015/11/29.
 */
public abstract class Pair implements Comparable {

	public enum PairStatus {
		NEW,
		OPENED,
		CLOSED,
		FORCED
	}

	public Product toShort;
	public Product toLong;
	public double shortOpen;
	public double longOpen;
	public double thresholdOpen;
	public double maxAmountPossibleOpen;
	public double shortClose;
	public double longClose;
	public double thresholdClose;
	public double maxAmountPossibleClose;
	public DateTime dateOpened;
	public DateTime dateClosed;
	public double slope;
	public double stdev;
	public PairStatus status; //True means "short & long " position already taken, next thing to do is to settle it when price converges.
	public long shortPositionHeld;
	public long longPositionHeld;
	public double minResidual;

	public Pair (Product toShort, Product toLong, double slope, double stdev) {
		this.toShort = toShort;
		this.toLong = toLong;
		this.slope = slope;
		this.stdev = stdev;
		this.status = PairStatus.NEW;
		this.shortPositionHeld = 0;
		this.longPositionHeld = 0;
	}

	public Pair (Product toShort, Product toLong,
				 double slope, double stdev,
				 double shortOpen, double longOpen,
				 long shortPositionHeld, long longPositionHeld,
				 DateTime dateOpened, double thresholdOpen, double maxAmountPossibleOpen) {
		this.toShort = toShort;
		this.toLong = toLong;
		this.slope = slope;
		this.stdev = stdev;
		this.status = PairStatus.OPENED;
		this.shortPositionHeld = shortPositionHeld;
		this.longPositionHeld = longPositionHeld;
		this.dateOpened = dateOpened;
		this.shortOpen = shortOpen;
		this.longOpen = longOpen;
		this.thresholdOpen = thresholdOpen;
		this.maxAmountPossibleOpen = maxAmountPossibleOpen;
	}

	public Pair (Product toShort, Product toLong,
				 double slope, double stdev,
				 double shortOpen, double longOpen,
				 long shortPositionHeld, long longPositionHeld,
				 DateTime dateOpened, double thresholdOpen, double maxAmountPossibleOpen,
				 double shortClose, double longClose,
				 DateTime dateClosed, double thresholdClose, double maxAmountPossibleClose,
				 PairStatus status, double minResidual) {
		this.toShort = toShort;
		this.toLong = toLong;
		this.slope = slope;
		this.stdev = stdev;
		this.status = PairStatus.CLOSED;
		this.shortPositionHeld = shortPositionHeld;
		this.longPositionHeld = longPositionHeld;
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
	}

	public double profitPercentageEstimate () {
		return (shortOpen - shortClose) / shortOpen + (longClose - longOpen) / longOpen - feeEstimate();
	}

	public int closureAge () {
		return age(dateClosed);
	}

	public int age (DateTime now) {
		return Days.daysBetween(dateOpened.toLocalDate(), now.toLocalDate()).getDays();
	}

	public void reset () {
		this.status = PairStatus.NEW;
		this.shortPositionHeld = 0;
		this.longPositionHeld = 0;
		this.shortOpen = -1d;
		this.longOpen = -1d;
		this.shortClose = -1d;
		this.longClose = -1d;
		this.dateOpened = null;
		this.dateClosed = null;
		this.minResidual = -1000000d;
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
		if (o == null || !(o instanceof Pair)) return 0;
		Pair anotherPair = (Pair) o;
		if (status == NEW || anotherPair.status == NEW) return 0;
		DateTime thisDate = status == OPENED ? dateOpened : dateClosed;
		DateTime anotherDate = anotherPair.status == OPENED ? anotherPair.dateOpened : anotherPair.dateClosed;
		return thisDate.compareTo(anotherDate);
	}

	public abstract double feeEstimate();

	public abstract Pair copy ();
}