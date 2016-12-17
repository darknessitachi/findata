package michael.findata.spring.data.repository;

import michael.findata.algoquant.strategy.pair.stocks.ShortInHKPairStrategy;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface ShortInHkPairStrategyRepository extends PagingAndSortingRepository<ShortInHKPairStrategy, Integer> {
	List<ShortInHKPairStrategy> findByOpenableDate (Date openableOn);
}