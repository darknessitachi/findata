package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.datatype.order.Order;

import java.util.Collection;

public class DummyBroker implements Broker {
	@Override
	public void sendOrder(Collection<? extends Order> orders) {
		orders.forEach(order -> System.out.println("Dummy broker: "+order));
	}

	@Override
	public void cancelOrder(Collection<? extends Order> orders) {
	// TODO: 2016/5/22 Cancel order
	}
}