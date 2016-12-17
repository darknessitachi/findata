package michael.findata.algoquant.execution.listener;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.order.Order;

public interface OrderListener {
	void orderUpdated (Order order, Broker broker);
}