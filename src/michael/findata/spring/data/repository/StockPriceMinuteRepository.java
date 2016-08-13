package michael.findata.spring.data.repository;

import michael.findata.model.StockPriceMinute;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface StockPriceMinuteRepository extends PagingAndSortingRepository<StockPriceMinute, Long> {
	StockPriceMinute findOneByStock_CodeAndDate(String code, Timestamp date);
	List<StockPriceMinute> findByDateBetweenAndStock_CodeInOrderByDate(Timestamp start, Timestamp end, Collection<String> codes);
	List<StockPriceMinute> findByDateBetweenAndStock_CodeInOrderByDate(Timestamp start, Timestamp end, String ... codes);
}