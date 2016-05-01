package michael.findata.spring.data.repository;

import michael.findata.model.Stock;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface StockRepository extends PagingAndSortingRepository<Stock, Integer> {
	Stock findOneByCode(String code);
	List<Stock> findByCodeIn(String ... codes);
}