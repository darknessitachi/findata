package michael.findata.spring.data.repository;

import michael.findata.model.Stock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Set;

public interface StockRepository extends PagingAndSortingRepository<Stock, Integer> {
	Stock findOneByCode(String code);
	Set<Stock> findByCodeIn(String ... codes);
	Set<Stock> findByCodeIn(Collection<String> codes);
	@Query (value = "SELECT DISTINCT s FROM Pair p, Stock s WHERE p.stockToShort = s")
	Set<Stock> findStocksInPairs();
}