package michael.findata.algoquant.execution.component.broker;

import com.numericalmethod.algoquant.execution.datatype.order.Order;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.model.Stock;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import static michael.findata.util.LogUtil.getClassLogger;

abstract class LocalBrokerProxy implements Broker {
	private static Logger LOGGER = getClassLogger();
	private final DecimalFormat chinaStockPriceFormat = new DecimalFormat("#.###");
	private final DecimalFormat chinaStockQuantityFormat = new DecimalFormat("#");

	void cancelOrderToDotNetComponent(Collection<? extends Order> ordrs, int port) {
		if (ordrs.size() == 0) {
			return;
		}
		ArrayList<Order> orders = new ArrayList<>();
		for (Order o : ordrs) {
			if (o.id() == -1) {
				LOGGER.warn("Order {} is very likely not submitted yet. So we shouldn't cancel it.", o);
			} else {
				orders.add(o);
			}
		}
		if (orders.size() == 0) {
			return;
		}
		Socket s;
		try {
			s = new Socket(InetAddress.getByName("127.0.0.1"), port);
			OutputStream ops = s.getOutputStream();
			for (Order o : orders) {
				ops.write(("Cancel|"+o.id()+'\n').getBytes());
			}
			ops.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String line;
			StringBuilder sb1 = new StringBuilder();
			while (null != (line = br.readLine())) {
				System.out.println(line);
				sb1.append(line);
			}
			ops.close();
			s.close();
			String [] results = sb1.toString().split("\\|");
			HexinOrder hxOrder;
			for (int i = orders.size() - 1; i > -1; i--) {
				if (orders.get(i) instanceof HexinOrder) {
					hxOrder = ((HexinOrder) orders.get(i));
					hxOrder.ack(results[i]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void orderOutToDotNetComponent(Collection<? extends Order> ordrs, int port) {
		ArrayList<Order> orders;
		if (ordrs instanceof ArrayList) {
			orders = (ArrayList<Order>) ordrs;
		} else {
			orders = new ArrayList<>();
		}
		Socket s;
		try {
			s = new Socket(InetAddress.getByName("127.0.0.1"), port);
			OutputStream ops = s.getOutputStream();

			StringBuilder sb = new StringBuilder();
			for (Order o : ordrs) {
				switch (o.side()) {
					case SELL:
						sb.append("Sell");
						break;
					case BUY:
						sb.append("Buy");
				}
				sb.append('|').append(o.product() instanceof Stock ? ((Stock) o.product()).getCode() : o.product().symbol());
				sb.append('|').append(chinaStockPriceFormat.format(o.price()));
				sb.append('|').append(chinaStockQuantityFormat.format(o.quantity())).append('\n');
				if (ordrs != orders) {
					orders.add(o);
				}
			}
			ops.write(sb.toString().getBytes());
			ops.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String line;
			StringBuilder sb1 = new StringBuilder();
			while (null != (line = br.readLine())) {
				LOGGER.info(line);
				sb1.append(line);
			}
			ops.close();
			s.close();
			String [] results = sb1.toString().split("\\|");
			HexinOrder hxOrder;

			for (int i = orders.size() - 1; i > -1; i--) {
				if (orders.get(i) instanceof HexinOrder) {
					hxOrder = ((HexinOrder) orders.get(i));
					hxOrder.ack(results[i]);
					hxOrder.id(Integer.parseInt(results[i].substring(results[i].indexOf("{=") + 2, results[i].length() - 1)));
				}
			}
//			if (line.length() > 0) {
////					results.put(oArray[count].id(), line);
//				if (oArray[count] instanceof HexinOrder) {
//					((HexinOrder) oArray[count]).ack(line);
//				}
//				count++;
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}