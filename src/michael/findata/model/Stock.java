package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç5:01
 * To change this template use File | Settings | File Templates.
 */
@Entity
@NamedQuery(name = "Stock.findAll", query = "SELECT s FROM Stock s ORDER BY s.code")
public class Stock {
	private int id;

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
		this.code = code;
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

	private int latestYear;

	@Column(name = "latest_year", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getLatestYear() {
		return latestYear;
	}

	public void setLatestYear(int latestYear) {
		this.latestYear = latestYear;
	}

	private int latestSeason;

	@Column(name = "latest_season", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getLatestSeason() {
		return latestSeason;
	}

	public void setLatestSeason(int latestSeason) {
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

//	private Collection<StockPrice> stockPrices;
//
//	@OneToMany(mappedBy = "stock")
//	@OrderBy("date desc")
//	public Collection<StockPrice> getStockPrices () {
//		return stockPrices;
//	}
//
//	public void setStockPrices (Collection<StockPrice> stockPrices) {
//		this.stockPrices = stockPrices;
//	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Stock stock = (Stock) o;

//		if (id != stock.id) return false;
		if (code != null ? !code.equals(stock.code) : stock.code != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result= (code != null ? code.hashCode() : 0);
		return result;
	}
}
