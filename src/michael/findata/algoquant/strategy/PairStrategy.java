package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.strategy.Strategy;

/**
 * Created by nicky on 2015/11/18.
 */
public class PairStrategy implements Strategy {
	public static class Pair {
		private Product toShort;
		private Product toLong;
		private double normalizationRatio; // price ratio falls into this threshold and we settle the position
		private double positionThreshold; // price ratio rises over this threshold and we set position
		private double settleRelaxation; // relax the settling a little bit (1 means we don't want to relax it)
		private boolean positionHeld; //True means "short & long " position already taken, next thing to do is to settle it when price converges.
		private long shortPositionHeld;
		private long longPositionHeld;

		public Pair (Product toShort, Product toLong, double normalizationRatio, double positionThreshold, double settleRelaxation, boolean positionHeld) {
			this.toShort = toShort;
			this.toLong = toLong;
			this.normalizationRatio = normalizationRatio;
			this.positionThreshold = positionThreshold;
			this.settleRelaxation = settleRelaxation;
			this.positionHeld = positionHeld;
		}

		public Product getToShort() {
			return toShort;
		}

		public Product getToLong() {
			return toLong;
		}

		public double getNormalizationRatio() {
			return normalizationRatio;
		}

		public double getPositionThreshold() {
			return positionThreshold;
		}

		public double getSettleRelaxation() {
			return settleRelaxation;
		}

		public boolean isPositionHeld() {
			return positionHeld;
		}

		public void setPositionHeld(boolean positionHeld) {
			this.positionHeld = positionHeld;
		}

		public long getShortPositionHeld() {
			return shortPositionHeld;
		}

		public void setShortPositionHeld(long shortPositionHeld) {
			this.shortPositionHeld = shortPositionHeld;
		}

		public long getLongPositionHeld() {
			return longPositionHeld;
		}

		public void setLongPositionHeld(long longPositionHeld) {
			this.longPositionHeld = longPositionHeld;
		}
	}
}