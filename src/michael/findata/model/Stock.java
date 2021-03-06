package michael.findata.model;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Currency;

@Entity
@NamedQuery(name = "Stock.findAll", query = "SELECT s FROM Stock s ORDER BY s.code")
public class Stock implements com.numericalmethod.algoquant.execution.datatype.product.stock.Stock, Comparable<Product> {

	@Override
	public int compareTo(Product o) {
		if (equals(o))	return 0;
		if (o == null) return 1;
		return symbol.compareTo(o.symbol());
	}

	private int id;

	public Stock() {
	}

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name = "id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
	@Id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private String code;

	@Column(name = "code", nullable = true, insertable = true, updatable = true, length = 20, precision = 0)
	@Basic
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		// 5 digits for hk stocks
		// 6 digits for mainland stocks
		if (code.length() == 5) {
			this.code = code;
			symbol = code.substring(1, 5)+".HK";
			exchange = Exchange.HKEX;
			currency = Currencies.HKD;
			_exchange = "HK";
			return;
		}
		this.code = code.substring(0, 6);
		switch (code.charAt(0)) {
			case '5':
			case '6':
			case '9':
				symbol = code+".SS";
				exchange = Exchange.SHSE;
				_exchange = "SH";
				if (code.startsWith("900")) {
					currency = Currencies.USD;
				} else {
					currency = Currencies.CNY;
				}
				break;
			default:
				symbol = code+".SZ";
				exchange = Exchange.SZSE;
				_exchange = "SZ";
				if (code.startsWith("200")) {
					currency = Currencies.HKD;
				} else {
					currency = Currencies.CNY;
				}
		}
	}

	private String name;

	@Column(name = "name", nullable = true, insertable = true, updatable = true, length = 20, precision = 0)
	@Basic
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private float currentPrice;

	@Column(name = "current_price", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public float getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(float currentPrice) {
		this.currentPrice = currentPrice;
	}

	private Integer latestYear;

	@Column(name = "latest_year", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public Integer getLatestYear() {
		return latestYear;
	}

	public void setLatestYear(Integer latestYear) {
		this.latestYear = latestYear;
	}

	private Integer latestSeason;

	@Column(name = "latest_season", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public Integer getLatestSeason() {
		return latestSeason;
	}

	public void setLatestSeason(Integer latestSeason) {
		this.latestSeason = latestSeason;
	}

	private double numberOfShares;

	@Column(name = "number_of_shares", nullable = false, insertable = true, updatable = true, length = 20, precision = 0)
	@Basic
	public double getNumberOfShares() {
		return numberOfShares;
	}

	public void setNumberOfShares(double numberOfShares) {
		this.numberOfShares = numberOfShares;
	}

	private boolean isFinancial;

	@Column(name = "is_financial", nullable = true, insertable = true, updatable = true, length = 1, precision = 0)
	@Basic
	public boolean isFinancial() {
		return isFinancial;
	}

	public void setFinancial(boolean financial) {
		isFinancial = financial;
	}

	private boolean isIgnored;
	@Column(name = "is_ignored", nullable = true, insertable = true, updatable = true, length = 1, precision = 0)
	@Basic
	public boolean isIgnored() {
		return isIgnored;
	}
	public void setIgnored(boolean ignored) {
		isIgnored = ignored;
	}

	private boolean isFund;
	@Column(name = "is_fund", nullable = true, insertable = true, updatable = true, length = 1, precision = 0)
	@Basic
	public boolean isFund () {
		return isFund;
	}
	public void setFund (boolean fund) {
		this.isFund = fund;
	}

	private Timestamp lastUpdated;

	@Column(name = "last_updated", nullable = true, insertable = true, updatable = true, length = 19, precision = 0)
	@Basic
	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	private boolean isInteresting;

	@Column(name = "is_interesting", nullable = true, insertable = true, updatable = true, length = 1, precision = 0)
	@Basic
	public boolean isInteresting() {
		return isInteresting;
	}

	public void setInteresting(boolean interesting) {
		isInteresting = interesting;
	}

	private String industry;

	@Column(name = "industry", nullable = true, insertable = true, updatable = true, length = 255, precision = 0)
	@Basic
	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	private String subindustry;

	@Column(name = "subindustry", nullable = true, insertable = true, updatable = true, length = 255, precision = 0)
	@Basic
	public String getSubindustry() {
		return subindustry;
	}

	public void setSubindustry(String subindustry) {
		this.subindustry = subindustry;
	}

	private Double spread;

	@Basic
	@Column(name = "spread", columnDefinition = "float")
	public Double getSpread() {
		return spread;
	}

	public void setSpread(Double spread) {
		this.spread = spread;
	}

	private Integer lotSize = 100; // default value;

	@Basic
	@Column(name = "lot_size", columnDefinition = "smallint")
	public Integer getLotSize () {
		return lotSize;
	}

	public void setLotSize (Integer lotSize) {
		this.lotSize = lotSize;
	}

	private String _exchange;
	@Basic
	@Column(name = "exchange", columnDefinition = "CHAR")
	private String get_exchange () {
		return _exchange;
	}
	private void set_exchange (String _exchange) {
		this._exchange = _exchange;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof Stock)) return false;

		Stock stock = (Stock) o;

//		if (id != stock.id) return false;
		return code != null ? code.equals(stock.code) : stock.code == null;
	}

	@Override
	public int hashCode() {
		return code == null ? id : code.hashCode();
	}

	public Stock (String code) {
		setCode(code);
	}

	public Stock (String code, String name) {
		setCode(code);
		this.name = name;
	}

	private String symbol;

	private Exchange exchange;

	private Currency currency = Currencies.CNY;

	public String symbol () {
		return symbol;
	}

	@Override
	public Currency currency() {
		return currency;
	}

	@Override
	public String companyName() {
		return getName();
	}

	@Override
	public Exchange exchange() {
		return exchange;
	}

	@Override
	public String toString () {
		return code+" "+name;
	}
}