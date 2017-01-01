package michael.findata.algoquant.execution.datatype.order;

import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import michael.findata.algoquant.execution.component.broker.MetaBroker;

import java.util.Objects;

public class HexinOrder extends Order {

	public static final String ORDER_TAG_SERVER_SIDE_ID = "SSID";
	public static final String ORDER_TAG_BROKER = "BRK";

	public Integer serverSideId() {
		return (Integer) getTag(HexinOrder.ORDER_TAG_SERVER_SIDE_ID);
	}

	public void serverSideId(int id) {
		addTag(HexinOrder.ORDER_TAG_SERVER_SIDE_ID, id);
	}

	private String ack;

	private OrderExecutionType type;

	public HexinOrder(Product product, double quantity, double price, HexinType hexinType) {
		this(product, quantity, price, hexinType, OrderExecutionType.LIMIT_ORDER);
	}

	public HexinOrder(Product product, double quantity, double price, HexinType hexinType, OrderExecutionType executionType) {
		super(product,
				hexinType == HexinType.SIMPLE_BUY ||
						hexinType == HexinType.CREDIT_BUY ||
						hexinType == HexinType.PAIR_OPEN_LONG ||
						hexinType == HexinType.PAIR_CLOSE_SHORT_BUY_BACK ? Side.BUY : Side.SELL,
				quantity, price);
		this.hexinType = hexinType;
		this.type = executionType;
//		this.id(-1);
	}

	private HexinType hexinType;

	public HexinType hexinType () {
		return hexinType;
	}

	@Override
	public OrderExecutionType type() {
		return type;
	}

	@Override
	public Order opposite() {
		return new HexinOrder(product(), quantity(), price(), hexinType().opposite());
	}

	public String ack() {
		return ack;
	}

	public void ack(String ack) {
		this.ack = ack;
	}

	public boolean submitted () {
		return serverSideId() != null;
	}

	public enum HexinType {
		SIMPLE_SELL,
		SIMPLE_BUY,
		CREDIT_SELL,
		CREDIT_BUY,
		PAIR_OPEN_SHORT,
		PAIR_OPEN_LONG,
		PAIR_CLOSE_SHORT_BUY_BACK,
		PAIR_CLOSE_LONG_SELL_BACK;

		public HexinType opposite() {
			switch (this) {
				case CREDIT_BUY:
					return CREDIT_SELL;
				case CREDIT_SELL:
					return CREDIT_BUY;
				case PAIR_OPEN_SHORT:
					return PAIR_CLOSE_SHORT_BUY_BACK;
				case PAIR_OPEN_LONG:
					return PAIR_CLOSE_LONG_SELL_BACK;
				case PAIR_CLOSE_SHORT_BUY_BACK:
					return PAIR_OPEN_SHORT;
				case PAIR_CLOSE_LONG_SELL_BACK:
					return PAIR_OPEN_LONG;
				case SIMPLE_BUY:
					return SIMPLE_SELL;
				default:
					return SIMPLE_BUY;
			}
		}
	}

	@Override
	public int hashCode () {
		return (int) id();
	}

	@Override
	public boolean equals (Object another) {
		if (this == another) return true;
		if (another == null || !(another instanceof HexinOrder)) return false;
		HexinOrder anotherHexinOrder = (HexinOrder) another;
		return id() == anotherHexinOrder.id();
	}
}