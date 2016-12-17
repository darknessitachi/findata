package michael.findata.spring.data.repository;

import michael.findata.model.ExchangeRateDaily;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Timestamp;
import java.util.List;

public interface ExchangeRateDailyRepository extends PagingAndSortingRepository<ExchangeRateDaily, Integer> {
	List<ExchangeRateDaily> findByDateBetweenAndCurrency(Timestamp start, Timestamp end, String currency);
}