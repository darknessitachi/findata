package michael.findata.data.local;

import com.numericalmethod.algoquant.data.cache.SequentialCache;
import com.numericalmethod.algoquant.data.cache.SequentialCacheFactory;
import com.numericalmethod.algoquant.data.cache.TimedEntry;
import michael.findata.model.Dividend;
import michael.findata.model.Stock;
import michael.findata.service.DividendService;
import michael.findata.spring.data.repository.DividendRepository;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dividend data from local DB
 */
@Service
public class LocalDividendCacheFactory implements SequentialCacheFactory<Stock, Dividend> {

	@Autowired
	private DividendRepository dividendRepo;

	@Override
	public SequentialCache<Dividend> newInstance(Stock product, Interval interval) {
		return new LocalDividendCache(product, interval);
	}

	private class LocalDividendCache implements SequentialCache<Dividend> {

		private List<TimedEntry<Dividend>> list;

		private LocalDividendCache(List<TimedEntry<Dividend>> list) {
			this.list = list;
		}

		private LocalDividendCache(Stock stock, Interval interval) {
			list = dividendRepo.findByPaymentDateBetweenAndStock_CodeInOrderByPaymentDate(
					interval.getStart().toDate(),
					interval.getEnd().toDate(),
					stock.getCode())
					.stream().map(d -> new TimedEntry<>(new DateTime(d.getPaymentDate().getTime()), d))
					.collect(Collectors.toList());
		}

		@Override
		public Iterator<TimedEntry<Dividend>> iterator() {
			return list.iterator();
		}
	}
}