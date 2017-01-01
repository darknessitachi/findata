package michael.findata.algoquant.execution.strategy.handler;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.strategy.handler.StrategyHandler;
import michael.findata.model.Dividend;
import org.joda.time.DateTime;

// Event handler for dividend/split etc
public interface DividendHandler extends StrategyHandler {

	// new dividend/split happens
	default void onDividend(DateTime now,
						   Dividend dividend,
						   MarketCondition mc,
						   TradeBlotter blotter,
						   Broker broker) {
	}
}