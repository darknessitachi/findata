package michael.findata.algoquant.execution.strategy;

import michael.findata.email.AsyncMailer;
import michael.findata.model.Stock;
import michael.findata.util.DBUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

import static michael.findata.util.LogUtil.getClassLogger;

public interface Strategy extends com.numericalmethod.algoquant.execution.strategy.Strategy {

	static Logger LOGGER = getClassLogger ();

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
			try {
				getRepository().save(this);
			} catch (Exception ex) {
				getLogger().warn("\t{}\t: Failed to save -- exception {} caught: {}", this, ex.getClass(), ex.getMessage());
				DBUtil.dealWithDBAccessError(ex);
			}
		} else {
			getLogger().warn("\t{}\t: Failed to save -- repository is null.", this);
		}
	}

	default void emailNotification(String titlePrefix) {
		AsyncMailer.instance.email(String.format("%s: %s", titlePrefix, this), notification());
	}

	default String notification () {
		return "";
	}

	void setRepository (CrudRepository repository);
	CrudRepository getRepository ();
	Collection<Stock> getTargetSecurities ();
	boolean isBacktestMode();
	void setBacktestMode(boolean backtestMode);
}