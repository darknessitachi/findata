package michael.findata.algoquant.execution.strategy;

import michael.findata.model.Stock;
import org.springframework.data.repository.Repository;

import java.util.Collection;

public interface Strategy extends com.numericalmethod.algoquant.execution.strategy.Strategy {
	void onStop ();
	void setRepository (Repository repository);
	void trySave ();
	String notification ();
	Collection<Stock> getTargetSecurities ();
}