package michael.findata.spring.data.repository;

import michael.findata.model.Stock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface StockRepository extends PagingAndSortingRepository<Stock, Integer> {
	Stock findOneByCode(String code);
	Set<Stock> findByCodeIn(String ... codes);
	Set<Stock> findByCodeIn(Collection<String> codes);
	List<Stock> findByFund(boolean isFund);
	List<Stock> findByFundAndInteresting(boolean isFund, boolean isInteresting);
	List<Stock> findByIgnoredAndFundAndCodeBetweenAndLatestSeasonIsNotNullOrderByCodeAsc(boolean ignored, boolean fund, String codeStart, String codeEnd);
	List<Stock> findByIgnoredAndFundAndLatestSeasonIsNotNullOrderByCodeAsc(boolean ignored, boolean fund);
	List<Stock> findByIgnoredAndFundAndInterestingOrderByCodeAsc(boolean ignored, boolean fund, boolean interesting);
	@Query (value = "SELECT DISTINCT s FROM Pair p, Stock s WHERE p.stockToShort = s")
	Set<Stock> findStocksInPairs();
}