package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import org.joda.time.DateTime;

import static michael.findata.algoquant.strategy.Pair.PairStatus.*;

/**
 * Created by nicky on 2015/12/4.
 */
public class ETFPair extends Pair {

	public ETFPair(int id, Product toShort, Product toLong,
				   double slope, double stdev, double correlco, double adf_p) {
		super(id, toShort, toLong, slope, stdev, correlco, adf_p);
	}

	public ETFPair(int id, Product toShort, Product toLong,
				   double slope, double stdev, double correlco, double adf_p,
				   double shortOpen, double longOpen,
				   long shortPositionHeld, long longPositionHeld,
				   DateTime dateOpened, double thresholdOpen, double maxAmountPossibleOpen, double minResidual, double maxResidual, DateTime minResDate, DateTime maxResDate) {
		super(id, toShort, toLong, slope, stdev, correlco, adf_p, shortOpen, longOpen, shortPositionHeld, longPositionHeld, dateOpened, thresholdOpen, maxAmountPossibleOpen, minResidual, maxResidual, minResDate, maxResDate);
	}

	public ETFPair(int id, Product toShort, Product toLong,
				   double slope, double stdev, double correlco, double adf_p,
				   double shortOpen, double longOpen,
				   long shortPositionHeld, long longPositionHeld,
				   DateTime dateOpened, double thresholdOpen, double maxAmountPossibleOpen,
				   double shortClose, double longClose,
				   DateTime dateClosed, double thresholdClose, double maxAmountPossibleClose,
				   PairStatus status, double minResidual, double maxResidual, DateTime minResDate, DateTime maxResDate) {
		super(id, toShort, toLong, slope, stdev, correlco, adf_p, shortOpen, longOpen, shortPositionHeld, longPositionHeld, dateOpened, thresholdOpen, maxAmountPossibleOpen,
				shortClose, longClose, dateClosed, thresholdClose, maxAmountPossibleClose, status, minResidual, maxResidual, minResDate, maxResDate);
	}

	@Override
	public ETFPair copy() {
		if (status == OPENED) {
			return new ETFPair(
					id,
					toShort, toLong,
					slope, stdev, correlco, adf_p,
					shortOpen, longOpen,
					shortPositionHeld, longPositionHeld,
					dateOpened, thresholdOpen, maxAmountPossibleOpen, minResidual, maxResidual, minResDate, maxResDate);
		} else if (status == CLOSED || status == FORCED) {
			return new ETFPair(
					id,
					toShort, toLong,
					slope, stdev, correlco, adf_p,
					shortOpen, longOpen,
					shortPositionHeld, longPositionHeld,
					dateOpened, thresholdOpen, maxAmountPossibleOpen,
					shortClose, longClose, dateClosed, thresholdClose, maxAmountPossibleClose,
					status, minResidual, maxResidual, minResDate, maxResDate);
		} else {
			return null;
		}
	}
}