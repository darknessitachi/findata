package michael.findata.algoquant.strategy.pair;

import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.order.Order;
import com.numericalmethod.algoquant.execution.datatype.product.Product;
import com.numericalmethod.algoquant.execution.datatype.product.portfolio.Portfolio;
import com.numericalmethod.algoquant.execution.strategy.handler.DepthHandler;
import michael.findata.algoquant.execution.datatype.order.HexinOrder;
import michael.findata.algoquant.execution.strategy.Strategy;
import michael.findata.algoquant.execution.strategy.handler.MarketConditionHandler;
import michael.findata.model.PairInstance;
import michael.findata.model.PairStats;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.spring.data.repository.PairInstanceRepository;
import michael.findata.spring.data.repository.PairStatsRepository;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType;
import static michael.findata.algoquant.execution.datatype.order.HexinOrder.HexinType.*;
import static michael.findata.algoquant.strategy.pair.PairStrategyUtil.calVolumes;

// This is a variation of the pair strategy.
// The difference is that it only opens and doesn't need to close.
// So in effect, it looks like the strategy is hopping from on stock/etf to another very quickly
public class HoppingStrategy implements Strategy, MarketConditionHandler, DepthHandler {

	/**
	 * 510500<->159902: 3dev->1.5dev
	 * <p>
	 * A.
	 * 300ETF 1%+ -> 0.2%- unlimited position quota
	 * 510300,510310,510330,510360,160706,512990,159919,
	 * <p>
	 * B.
	 * avg (adf_p) <= 0.01 also unlimited position quota
	 * <p>
	 * Strategy parameters
	 */
	private double amountPerSlot = 6000;
	double openThresholdCoefficient = 1.5d; // spread needs to be at least 1.6 times stdev to open the trade
	double minPctgDev = 0.015; // spread needs to be at least 1.5% to open the trade
	int maxShortsPerTickPerStock = 200;
	int maxNetPositionPerStock = 2000;
	/**
	 * Strategy parameters end
	 */

	private Map<String, Set<PairInstance>> codeToPairs;
	private Map<Product, Depth> depthMap = new HashMap<>();
	private PairInstance[] pairs;
	private Set<Stock> stocks; // involved stocks for the current day
	private DividendService.PriceAdjuster adjuster;
	private LocalDate executionDate;
	private Set<String> scope; // overall scope of stocks
	private Portfolio<Stock> portfolio;
	private Portfolio<Stock> portfolioTmr;
	private HexinType openSellOrderType = CREDIT_SELL;
	private HexinType openBuyOrderType = CREDIT_BUY;
	private PairStatsRepository pairStatsRepo;
	private DividendService dividendService;
	private PairInstanceRepository pairInstanceRepo;

	public HoppingStrategy() {
	}

	public HoppingStrategy(Set<String> scope,
						   Portfolio<Stock> portfolio,
						   LocalDate exeDate,
						   HexinType openSellOrderType,
						   HexinType openBuyOrderType,
						   PairStatsRepository pairStatsRepo,
						   PairInstanceRepository pairInstanceRepo,
						   DividendService dividendService) {
		this.openSellOrderType = openSellOrderType;
		this.openBuyOrderType = openBuyOrderType;
		this.scope = scope;
		this.portfolio = portfolio;
		this.portfolioTmr = new Portfolio<>();
		this.portfolio.forEach((product, position) -> portfolioTmr.put(product, position));
		this.pairStatsRepo = pairStatsRepo;
		this.pairInstanceRepo = pairInstanceRepo;
		this.dividendService = dividendService;
		onDateUpdate(exeDate); // in simulation exeDate must be set to the starting date of the execution
	}

	public HoppingStrategy(Set<String> scope,
						   Portfolio<Stock> portfolio,
						   HexinType openSellOrderType,
						   HexinType openBuyOrderType,
						   PairStatsRepository pairStatsRepo,
						   PairInstanceRepository pairInstanceRepo,
						   DividendService dividendService) {
		this(scope, portfolio, LocalDate.now(), openSellOrderType, openBuyOrderType, pairStatsRepo, pairInstanceRepo, dividendService);
	}

	public void onDateUpdate() {
		onDateUpdate(LocalDate.now());
	}

	public void onDateUpdate(LocalDate exeDate) {
		System.out.println("Preparing for date: " + exeDate);
		trySave();
		refreshShortablePortfolio();
		this.executionDate = exeDate;

		Set<String> toShorts = portfolio.products().stream().map(Stock::getCode).collect(Collectors.toSet());
		List<PairStats> stats =
				pairStatsRepo.findByTrainingEndAndAdfpLessThanAndAdfpmaLessThanAndCodeToShortInAndCodeToLongIn(
						exeDate.minusDays(1).toDate(), 0.011d, 0.070d, toShorts, scope);

		stocks = new TreeSet<>();
		LocalDate earliest = exeDate;
		LocalDate trainingStart;

		pairs = new PairInstance[stats.size()];
		codeToPairs = new HashMap<>();
		Set<PairInstance> pT;
		PairStats stat;
		PairInstance pi;
		for (int i = stats.size() - 1; i > -1; i--) {
			stat = stats.get(i);
			pi = new PairInstance();
			pi.setStats(stat);
			pi.setOpenableDate(exeDate.toDate());
			pi.setForceClosureDate(exeDate.plusDays(20).toDate()); // todo parameter, only useful in open/close pair trading, not used in hopping
			pi.setStatus(PairInstance.PairStatus.NEW);
			pi.setThresholdOpen(
					Math.min(1.8 * stat.getStdev(),
							Math.min(0.018,
									Math.max(stat.getStdev() * openThresholdCoefficient, minPctgDev)))
			);
			pairs[i] = pi;
			pairInstanceRepo.save(pi);

			if (codeToPairs.containsKey(stat.getCodeToShort())) {
				codeToPairs.get(stat.getCodeToShort()).add(pi);
			} else {
				pT = new HashSet<>();
				pT.add(pi);
				codeToPairs.put(stat.getCodeToShort(), pT);
			}
			if (codeToPairs.containsKey(stat.getCodeToLong())) {
				codeToPairs.get(stat.getCodeToLong()).add(pi);
			} else {
				pT = new HashSet<>();
				pT.add(pi);
				codeToPairs.put(stat.getCodeToLong(), pT);
			}
			stocks.add(stat.getPair().getStockToShort());
			stocks.add(stat.getPair().getStockToLong());
			trainingStart = LocalDate.fromDateFields(stat.getTrainingStart());
			if (trainingStart.isBefore(earliest)) {
				earliest = trainingStart;
			}
		}
		adjuster = dividendService.newPriceAdjuster(earliest, exeDate, codeToPairs.keySet().toArray(new String[codeToPairs.size()]));
	}

	// when a trading day passes, the stocks you bought on the first day can finally be sold on the second day
	private void refreshShortablePortfolio() {
		this.portfolio = portfolioTmr;
		this.portfolioTmr = new Portfolio<>();
		this.portfolio.forEach((product, position) -> {
			if (position != 0) {
				portfolioTmr.put(product, position);
				if (!scope.contains(product.getCode())) {
					throw new RuntimeException(product.getCode() + " is not in scope. Please verify!");
				}
			}
		});
	}

	public Portfolio<Stock> getPortfolio() {
		Portfolio<Stock> result = new Portfolio<>();
		this.portfolioTmr.forEach((product, position) -> {
			if (position != 0) {
				result.put(product, position);
				if (!scope.contains(product.getCode())) {
					throw new RuntimeException(product.getCode() + " is not in scope. Please verify!");
				}
			}
		});
		return result;
	}

	private final Set<PairInstance> emptySet = Collections.emptySet();

	@Override
	public void onDepthUpdate(DateTime now, Depth depth, MarketCondition mc, TradeBlotter blotter, Broker broker) {
		onMarketConditionUpdate(now, mc, blotter, broker);
	}

	/**
	 * Executed when some product quotes change.
	 * Only products whose quotes have change are included in the MarketCondition
	 *
	 * @param now     the current time
	 * @param mc      the current market condition
	 * @param blotter the current trade blotter
	 * @param broker  a broker service
	 */
	@Override
	public void onMarketConditionUpdate(DateTime now, MarketCondition mc, TradeBlotter blotter, Broker broker) {
//		System.out.println("Hopping strategy executing @ "+now);
//		mc.depths().values().forEach(System.out::println);
		if (!executionDate.equals(now.toLocalDate())) {
			onDateUpdate(now.toLocalDate());
		}
		depthMap.putAll(mc.depths());
		Set<Product> dirtyPrd = mc.depths().keySet();
		michael.findata.algoquant.execution.datatype.depth.Depth depthShort, depthLong;
		String codeShort, codeLong;
		double actualPriceShort, adjustedPriceShort, actualPriceLong, adjustedPriceLong;
		double residual;
		double existingPosition;
		int volumeShort, volumeLong;
		Set<PairInstance> affectedPairs = new TreeSet<>();
		for (Product prod: dirtyPrd) {
			affectedPairs.addAll(codeToPairs.getOrDefault(prod.symbol().substring(0, 6), emptySet));
		}
//		if (now.toString().equals("2016-02-05T10:31:00.000+08:00") || now.toString().equals("2016-02-05T10:35:00.000+08:00")) {
//			System.out.println();
//		}

		for (PairInstance pair : affectedPairs) {
//			System.out.println("Affected pair: "+pair.getStats().getAdfpma());

			if (!depthMap.containsKey(pair.toShort())) continue;
			depthShort = (michael.findata.algoquant.execution.datatype.depth.Depth) depthMap.get(pair.toShort()).otherInfo();
			if (depthShort == null || !depthShort.isTraded()) {
				continue;
			}
			if (!depthMap.containsKey(pair.toLong())) continue;
			depthLong = (michael.findata.algoquant.execution.datatype.depth.Depth) depthMap.get(pair.toLong()).otherInfo();
			if (depthLong == null || !depthLong.isTraded()) {
				continue;
			}
			codeShort = ((Stock) pair.toShort()).getCode();
			codeLong = ((Stock) pair.toLong()).getCode();

//			if (now.toString().equals("2016-02-05T10:31:00.000+08:00") && codeShort.equals("159903") && codeLong.equals("512230")) {
//				System.out.println();
//			}
//			if (now.toString().equals("2016-02-05T10:35:00.000+08:00") && codeShort.equals("159903") && codeLong.equals("512230")) {
//				System.out.println();
//			}
			actualPriceShort = depthShort.bestBid(amountPerSlot);
			actualPriceLong = depthLong.bestAsk(amountPerSlot);
			if (actualPriceLong < 0 || actualPriceShort < 0) continue;
			existingPosition = portfolio.position((Stock) pair.toShort());
			volumeShort = Math.min(depthShort.totalBidAtOrAbove(actualPriceShort), (int) existingPosition);
			volumeLong = depthLong.totalAskAtOrBelow(actualPriceLong);
//			double possibleAmount = Math.min(actualPriceShort*volumeShort, actualPriceLong*volumeLong);

			// calculate residual, this is the only place where adjusted prices are used
			adjustedPriceShort = adjuster.adjust(codeShort, pair.trainingStart(), executionDate, actualPriceShort);
			adjustedPriceLong = adjuster.adjust(codeLong, pair.trainingStart(), executionDate, actualPriceLong);
			residual = adjustedPriceShort * pair.slope() / adjustedPriceLong - 1d;

			// for stats only
			if (pair.getMaxResidual() == null || residual > pair.getMaxResidual()) {
				pair.setMaxResidual(residual);
			}
			// end stats

			// check whether there is still position left to be shorted
			// skip if there is no position left.
			// Why perform this check so late? I still need to have max residual calculated.
			if (existingPosition <= 0) {
				continue;
			}

			/**
			 *  交易算法：
			 1. 确认价格进入交易区间
			 2. 如下 A - G 中选出最小值 H
			 一、融券卖出：
			 1. 确认可融券数量 A
			 2. 确认可买方数量 B
			 3. 确认融券上限足够 C
			 二、买入：
			 1. 确认资金 D
			 2. 确认卖方数量 E
			 三、资产杠杆上限 F
			 四、单只票对买入上限 G
			 3. 以H为基准进行融券卖出
			 4. 以H为基准进行融资买入
			 **/
//			if (true) {
			if (residual >= pair.getThresholdOpen() && residual > 0.0083) {
				// Calculate the most suitable amount:
				// It should minimize the amount difference between buy/sell
				double[] sellBuyVol = calVolumes(actualPriceShort,
						volumeShort, 100, actualPriceLong, volumeLong, 100, 50000, 20000, PairStrategyUtil.BalanceOption.CLOSEST_MATCH,
						0.005, 0.01, 0.02, 0.05);
				if (sellBuyVol == null) {
					// unable to find suitable sell/buy volumes
					continue;
				}

				// found suitable sell/buy volumes
				double shortVol = sellBuyVol[0];
				double longVol = sellBuyVol[1];

				// determine buy/sell order, take the whichever side that has the higher depth distance
				ArrayList<Order> orders = new ArrayList<>(3);
				Order sellOrder = new HexinOrder(pair.toShort(), shortVol, actualPriceShort, openSellOrderType);
				Order buyOrder = new HexinOrder(pair.toLong(), longVol, actualPriceLong, openBuyOrderType);
				if (depthLong.askDepthDistance(actualPriceLong) > depthShort.bidDepthDistance(actualPriceShort)) {
					// buy first
					orders.add(buyOrder);
					System.out.println(now + ": buy order " + buyOrder);
					orders.add(sellOrder);
					System.out.println(now + ": sell order " + sellOrder);
				} else {
					// sell first
					orders.add(sellOrder);
					System.out.println(now + ": sell order " + sellOrder);
					orders.add(buyOrder);
					System.out.println(now + ": buy order " + buyOrder);
				}

				broker.sendOrder(orders);
				System.out.println(depthShort);
				System.out.println(depthLong);
				// TODO: 2016/5/22 order management
				// now we assume that all orders are executed

				Double newPosShort = portfolio.position((Stock) pair.toShort()) - shortVol;
				portfolio.put((Stock) pair.toShort(), newPosShort);
//				Double newPosLong = portfolio.position((Stock)pair.toLong());
//				newPosLong = (newPosLong == null? 0d : newPosLong) + longVol;
//				portfolio.put((Stock)pair.toLong(), newPosLong); // this will only be allowed if this stock is T+0

				newPosShort = portfolioTmr.position((Stock) pair.toShort()) - shortVol;
				Double newPosLong = portfolioTmr.position((Stock) pair.toLong());
				newPosLong = (newPosLong == null ? 0d : newPosLong) + longVol;
				portfolioTmr.put((Stock) pair.toShort(), newPosShort);
				portfolioTmr.put((Stock) pair.toLong(), newPosLong);
				System.out.println(now + ": shortables :\t" + portfolio);
				System.out.println(now + ": all stocks :\t" + portfolioTmr);
				break; // max one trade per depth update, because it takes 1+ seconds to execute two orders (one trade)
			}
		}
//		System.out.println("Strategy executed @\t"+System.currentTimeMillis());
	}

	@Override
	public Collection<Stock> getTargetSecurities() {
		return stocks;
	}

	public void onStop() {
		trySave();
		refreshShortablePortfolio();
	}

	@Override
	public void setRepository(CrudRepository repository) {
	}

	@Override
	public CrudRepository getRepository () {
		return null;
	}

	public void trySave() {
		if (pairs == null) return;
		System.out.println("Saving pair instances.");
		for (PairInstance p : pairs) {
			if (p.getMaxResidual() != null) {
				pairInstanceRepo.save(p);
			} else {
				pairInstanceRepo.delete(p);
			}
		}
	}
}