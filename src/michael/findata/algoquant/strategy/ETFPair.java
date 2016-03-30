package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import org.joda.time.DateTime;

import static michael.findata.algoquant.strategy.Pair.PairStatus.*;

/**
 * Created by nicky on 2015/12/4.
 */
public class ETFPair extends Pair {

	public ETFPair(int id, Product toShort, Product toLong,
				   double slope, double stdev) {
		super(id, toShort, toLong, slope, stdev);
	}

	public ETFPair(int id, Product toShort, Product toLong,
				   double slope, double stdev,
				   double shortOpen, double longOpen,
				   long shortPositionHeld, long longPositionHeld,
				   DateTime dateOpened, double thresholdOpen, double maxAmountPossibleOpen) {
		super(id, toShort, toLong, slope, stdev, shortOpen, longOpen, shortPositionHeld, longPositionHeld, dateOpened, thresholdOpen, maxAmountPossibleOpen);
	}

	public ETFPair(int id, Product toShort, Product toLong,
				   double slope, double stdev,
				   double shortOpen, double longOpen,
				   long shortPositionHeld, long longPositionHeld,
				   DateTime dateOpened, double thresholdOpen, double maxAmountPossibleOpen,
				   double shortClose, double longClose,
				   DateTime dateClosed, double thresholdClose, double maxAmountPossibleClose,
				   PairStatus status, double minResidual) {
		super(id, toShort, toLong, slope, stdev, shortOpen, longOpen, shortPositionHeld, longPositionHeld, dateOpened, thresholdOpen, maxAmountPossibleOpen,
				shortClose, longClose, dateClosed, thresholdClose, maxAmountPossibleClose, status, minResidual);
	}

	@Override
	public double feeEstimate() {
		int age = closureAge();
		return 4 * 0.0004 + (age==0?1:age) * 0.0835 / 360;
	}

	@Override
	public ETFPair copy() {
		if (status == OPENED) {
			return new ETFPair(
					id,
					toShort, toLong,
					slope, stdev,
					shortOpen, longOpen,
					shortPositionHeld, longPositionHeld,
					dateOpened, thresholdOpen, maxAmountPossibleOpen);
		} else if (status == CLOSED || status == FORCED) {
			return new ETFPair(
					id,
					toShort, toLong,
					slope, stdev,
					shortOpen, longOpen,
					shortPositionHeld, longPositionHeld,
					dateOpened, thresholdOpen, maxAmountPossibleOpen,
					shortClose, longClose, dateClosed, thresholdClose, maxAmountPossibleClose,
					status, minResidual);
		} else {
			return null;
		}
	}
}