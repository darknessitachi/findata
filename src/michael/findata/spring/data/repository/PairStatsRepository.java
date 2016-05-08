package michael.findata.spring.data.repository;

import michael.findata.model.PairStats;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface PairStatsRepository extends PagingAndSortingRepository<PairStats, Integer>{
	List<PairStats> findByTrainingEndAndCorrelcoGreaterThanAndAdfpLessThan(Date trainingEnd, double correlCoThreshold, double adf_pThreshold);
}