package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.MarketConditionHandler;
import org.joda.time.DateTime;

public class DummyStrategy implements Strategy, MarketConditionHandler{
	@Override
	public void onMarketConditionUpdate(DateTime now, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		System.out.println("Dummy Strategy: MarketCondition updated.");
		mc.depths().forEach((product, depth) -> {
			System.out.println(depth);
		});
		System.out.println("Ending @ "+System.currentTimeMillis());
	}
}
