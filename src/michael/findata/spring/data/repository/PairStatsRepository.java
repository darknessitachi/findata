package michael.findata.spring.data.repository;

import michael.findata.model.PairStats;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PairStatsRepository extends PagingAndSortingRepository<PairStats, Integer>{
}