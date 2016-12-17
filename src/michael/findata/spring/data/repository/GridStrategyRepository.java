package michael.findata.spring.data.repository;

import michael.findata.algoquant.strategy.grid.GridStrategy;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface GridStrategyRepository extends PagingAndSortingRepository<GridStrategy, Integer> {
	public List<GridStrategy> findByActive(boolean active);
}