package michael.findata.algoquant.execution.component.broker.test;

import com.numericalmethod.algoquant.execution.datatype.order.LimitOrder;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import michael.findata.algoquant.execution.component.broker.HexinBroker;
import michael.findata.algoquant.product.stock.shse.SHSEStock;
import michael.findata.algoquant.product.stock.szse.SZSEStock;
import org.slf4j.Logger;

import java.util.Collections;

import static com.numericalmethod.algoquant.execution.datatype.order.BasicOrderDescription.*;
import static com.numericalmethod.nmutils.NMUtils.getClassLogger;

/**
 * Created by nicky on 2015/8/23.
 */
public class HexinBrokerTest {

	private static final Logger LOGGER = getClassLogger();

	// Test only
	public static void main (String [] args) {
		HexinBroker hxBroker = new HexinBroker("���Ϲ�Ʊ����ϵͳ5.0", false);
//				hxBroker.sendOrder(Collections.<Order>singletonList(new LimitOrder(new SZSEStock("000568.SZ"), Side.BUY, 100, 20)));
//				hxBroker.sendOrder(Collections.<Order>singletonList(new LimitOrder(new SHSEStock("601398.SS"), Side.SELL, 100, 20)));
		hxBroker.issueBuyOrder2(null, null, null);
	}
}