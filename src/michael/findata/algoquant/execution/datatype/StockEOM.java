package michael.findata.algoquant.execution.datatype;

import com.numericalmethod.algoquant.execution.component.simulator.event.Event;
import com.numericalmethod.algoquant.execution.datatype.OHLC;

/**
 * End of minute
 */
public class StockEOM extends OHLC implements Event {

	private final int volume;

	public StockEOM(double open,
					double high,
					double low,
					double close,
					int volume) {
		super(open, high, low, close);

		this.volume = volume;
	}

	public int volume() {
		return volume;
	}
}