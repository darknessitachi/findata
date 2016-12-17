package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.datatype.order.Order;
import michael.findata.algoquant.execution.listener.DepthListener;
import michael.findata.algoquant.execution.listener.OrderListener;

public interface Broker extends com.numericalmethod.algoquant.execution.component.broker.Broker {
	void stop ();
	void setOrderListener (Order o, OrderListener listener);
}