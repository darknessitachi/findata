package michael.findata.algoquant.strategy;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.strategy.Strategy;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import com.numericalmethod.algoquant.execution.strategy.handler.MarketConditionHandler;
import org.joda.time.DateTime;

public class DummyStrategy implements Strategy, MarketConditionHandler, DepthHandler {
	@Override
	public void onMarketConditionUpdate(DateTime now, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		System.out.println("Dummy Strategy: marketCondition updated @ "+now);
		mc.depths().forEach((product, depth) -> {
			System.out.println(depth);
		});
		System.out.println("Ending @ "+System.currentTimeMillis());
	}

	@Override
	public void onDepthUpdate(DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		System.out.println("Dummy Strategy: depth updated @ "+now);
		System.out.println(depth);
		System.out.println("Together with these marketConditions.");
		mc.depths().forEach((product, d) -> {
			System.out.println(d);
		});
		System.out.println("Ending @ "+System.currentTimeMillis());
	}
}