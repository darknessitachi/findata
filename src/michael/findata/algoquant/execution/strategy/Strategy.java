package michael.findata.algoquant.execution.strategy;

import michael.findata.model.Stock;

import java.util.Collection;

public interface Strategy extends com.numericalmethod.algoquant.execution.strategy.Strategy {
	void onStop ();
	Collection<Stock> getTargetSecurities ();
}