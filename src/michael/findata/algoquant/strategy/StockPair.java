package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import org.joda.time.DateTime;

import java.util.Date;

import static michael.findata.algoquant.strategy.Pair.PairStatus.CLOSED;
import static michael.findata.algoquant.strategy.Pair.PairStatus.FORCED;
import static michael.findata.algoquant.strategy.Pair.PairStatus.OPENED;

public class StockPair extends Pair {

	public StockPair(int id, Product toShort, Product toLong, double slope, double stdev, double correlco, double adf_p) {
		super(id, toShort, toLong, slope, stdev, correlco, adf_p);
	}

	public StockPair(int id, Product toShort, Product toLong,
					 double slope, double stdev, double correlco, double adf_p,
					 double shortOpen, double longOpen,
					 long shortPositionHeld, long longPositionHeld,
					 DateTime dateOpened, double thresholdOpen, double maxAmountPossibleOpen, double minResidual, double maxResidual, DateTime minResDate, DateTime maxResDate) {
		super(id, toShort, toLong, slope, stdev, correlco, adf_p, shortOpen, longOpen, shortPositionHeld, longPositionHeld, dateOpened, thresholdOpen, maxAmountPossibleOpen, minResidual, maxResidual, minResDate, maxResDate);
	}

	public StockPair(int id, Product toShort, Product toLong,
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
	public StockPair copy() {
		if (status == OPENED) {
			return new StockPair(
					id,
					toShort, toLong,
					slope, stdev, correlco, adf_p,
					shortOpen, longOpen,
					shortPositionHeld, longPositionHeld,
					dateOpened, thresholdOpen, maxAmountPossibleOpen, minResidual, maxResidual, minResDate, maxResDate);
		} else if (status == CLOSED || status == FORCED) {
			return new StockPair(
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