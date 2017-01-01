package michael.findata.algoquant.execution.strategy;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.numericalmethod.algoquant.execution.component.broker.Broker;
import com.numericalmethod.algoquant.execution.component.simulator.event.timer.TimerEvent;
import com.numericalmethod.algoquant.execution.component.tradeblotter.TradeBlotter;
import com.numericalmethod.algoquant.execution.datatype.StockEOD;
import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.algoquant.execution.datatype.depth.marketcondition.MarketCondition;
import com.numericalmethod.algoquant.execution.datatype.execution.Execution;
import com.numericalmethod.algoquant.execution.datatype.product.stock.market.MarketSnapshot;
import com.numericalmethod.algoquant.execution.strategy.handler.*;
import michael.findata.algoquant.execution.strategy.handler.DividendHandler;
import michael.findata.algoquant.execution.strategy.handler.MarketConditionHandler;
import michael.findata.email.AsyncMailer;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import michael.findata.util.DBUtil;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

import static michael.findata.algoquant.execution.strategy.Strategy.DBPersister.instance;
import static michael.findata.util.LogUtil.getClassLogger;

public interface Strategy extends com.numericalmethod.algoquant.execution.strategy.Strategy,
		DepthHandler, DividendHandler, MarketConditionHandler, ExecutionHandler, TimerHandler, StockEODHandler, MarketSnapshotHandler {

	@Override
	default void onExecution(DateTime now, Execution execution, MarketCondition mc, TradeBlotter blotter, Broker broker) {
	}

	@Override
	default void onEODUpdate(DateTime now, StockEOD eod, MarketCondition mc, TradeBlotter blotter, Broker broker) {
	}

	@Override
	default void onTimerUpdate(DateTime now, TimerEvent event, MarketCondition mc, TradeBlotter blotter, Broker broker) {
	}

	Logger LOGGER = getClassLogger ();

	default Logger getLogger() {
		return LOGGER;
	}

	default void onStop() {
		// Save to DB
		getLogger().info("\t{}\t: Saving to DB and stop.", this);
		trySave();
		emailNotification("Trading Ended");
	}

	default void trySave() {
		if (getRepository() != null) {
			instance().save(getRepository(), this);
		} else {
			getLogger().warn("\t{}\t: Failed to save -- repository is null.", this);
		}
	}

	default void emailNotification(String titlePrefix) {
		AsyncMailer.instance().email(String.format("%s: %s", titlePrefix, this), notification());
	}

	default String notification () {
		return "";
	}

	default void onDepthUpdate (DateTime now,
						  Depth depth,
						  MarketCondition mc,
						  TradeBlotter blotter,
						  Broker broker) {

	}

	void setRepository (CrudRepository repository);
	CrudRepository getRepository ();
	Collection<Stock> getTargetSecurities ();

	@Override
	default void onMarketSnapshotUpdate(DateTime now, MarketSnapshot snapshot, MarketCondition mc, TradeBlotter blotter, Broker broker) {
	}

	static class DBPersister {
		private static DBPersister instance = null;
		private Disruptor<DBSaveEvent> disruptor;
		private DBPersister () {
			// Specify the size of the ring buffer, must be power of 2.
			int bufferSize = 128;
			// Construct the Disruptor
			disruptor = new Disruptor<>(DBSaveEvent::new, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new BlockingWaitStrategy());
			// Connect the handler
			disruptor.handleEventsWith((event, sequence, endOfBatch) -> event.save());
			// Start the Disruptor, starts all threads running
			disruptor.start();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					LOGGER.info("DBPersister shutting down...");
					disruptor.shutdown();
					LOGGER.info("DBPersister shutdown completed.");
				}
			});
		}
		private void save (CrudRepository repo, Object toBeSaved) {
			disruptor.getRingBuffer().publishEvent((event, sequence, rep, obj) -> event.set(rep, obj), repo, toBeSaved);
			LOGGER.info("\t{}\t: DB save queued.", toBeSaved);
		}
		static DBPersister instance () {
			if (instance == null) {
				instance = new DBPersister();
			}
			return instance;
		}
	}

	static class DBSaveEvent {
		private CrudRepository repo;
		private Object toBeSaved;
		private void set (CrudRepository repo, Object toBeSaved) {
			this.repo = repo;
			this.toBeSaved = toBeSaved;
		}

		private void save () {
			try {
				repo.save(toBeSaved);
				LOGGER.info("\t{}\t: Saved to DB.", toBeSaved);
			} catch (Exception ex) {
				LOGGER.warn("\t{}\t: Failed to save -- exception {} caught: {}", toBeSaved, ex.getClass(), ex.getMessage());
				DBUtil.dealWithDBAccessError(ex);
			}
		}
	}
}