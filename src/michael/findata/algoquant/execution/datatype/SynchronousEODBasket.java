package michael.findata.algoquant.execution.datatype;

import com.numericalmethod.algoquant.execution.datatype.StockEOD;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import org.joda.time.DateTime;

import java.util.*;

/**
 * Modelled after SynchronousPriceBasket
 */
public class SynchronousEODBasket {

	private final List<Product> products;
	private final Set<Product> counter;
	private final LinkedHashMap<Product, StockEOD> eods; // keep the insertion order
	private DateTime now = new DateTime(0); // the current time stamp

	public SynchronousEODBasket(Collection<? extends Product> products) {
		this.products = new ArrayList<>(products);
		this.counter = new HashSet<>(products.size(), 1f);
		this.eods = new LinkedHashMap<>(products.size(), 1f);

		for (Product product : products) {
			eods.put(product, null);// fix the product positions in the map
		}
	}

	public void updateEOD(DateTime time, StockEOD eod, Product product) {
        /*
         * This "if" is why this class can only be used in simulation. In simulation, there are
         * multiple updates that have the same timestamp, hence synchronous. In real time, however,
         * the clock 'time' keeps advancing and therefore the "counter" will never be ready, hence
         * asynchronous
         */
		if (time.compareTo(now) > 0) {
			now = time;
			counter.clear();
		}

		if (!eods.keySet().contains(product)) {
			throw new RuntimeException(String.format("unregistered product %s", product));
		}

		eods.put(product, eod);
		counter.add(product);
	}

	public boolean isReady() {
		return counter.size() == products.size();
	}

	public DateTime time() {
		return now;
	}

	public int count() {
		return counter.size();
	}

	public Map<Product, StockEOD> eods() {
		return Collections.unmodifiableMap(eods);
	}

	public double[] midPrices() {
		double[] prices = new double[eods.size()];
		int count = 0;
		for (StockEOD eod : eods.values()) {
			prices[count++] = eod.adjClose();
		}
		return prices;
	}

	@Override
	public String toString() {
		return String.format("@%s: %s", now, eods.toString());
	}
}