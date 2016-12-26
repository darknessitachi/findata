package michael.findata.spring.data.repository;

import michael.findata.model.StockPrice;
import michael.findata.model.StockPriceMinute;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface StockPriceRepository extends PagingAndSortingRepository<StockPrice, Long> {
	StockPrice findTopByStock_CodeOrderByDateAsc(String code);
}
