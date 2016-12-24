package michael.findata.algoquant.execution.component.broker.test;

import com.numericalmethod.algoquant.execution.datatype.order.LimitOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import michael.findata.algoquant.execution.component.broker.HexinBroker;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

import static com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription.*;
import static michael.findata.util.LogUtil.getClassLogger;

public class HexinBrokerTest {

	private static final Logger LOGGER = getClassLogger();

	// Test only
	public static void main (String [] args) {
		HexinBroker hxBroker = new HexinBroker("网上股票交易系统5.0", false);
//				hxBroker.sendOrder(Collections.<Order>singletonList(new LimitOrder(new SZSEStock("000568.SZ"), Side.BUY, 100, 20)));
//				hxBroker.sendOrder(Collections.<Order>singletonList(new LimitOrder(new SHSEStock("601398.SS"), Side.SELL, 100, 20)));
		hxBroker.issueBuyOrder2(null, null, null);
	}
}