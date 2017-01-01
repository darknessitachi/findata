package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.strategy.handler.ExecutionHandler;

public interface Broker extends com.numericalmethod.algoquant.execution.component.broker.Broker {
	void stop ();
	void setOrderListener (Order o, ExecutionHandler handler);
	default void printOrderListeners () {}
}