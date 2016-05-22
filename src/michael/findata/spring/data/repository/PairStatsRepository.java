package michael.findata.spring.data.repository;

import michael.findata.model.PairStats;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface PairStatsRepository extends PagingAndSortingRepository<PairStats, Integer>{

	List<PairStats> findByTrainingEndAndCorrelcoGreaterThanAndAdfpLessThan(Date trainingEnd, double correlCoThreshold, double adf_pThreshold);

	List<PairStats> findByTrainingEndAndAdfpLessThanAndAdfpmaLessThan(Date trainingEnd, double adf_pThreshold, double adf_p_maThreshold);

	List<PairStats> findByTrainingEndAndAdfpLessThanAndAdfpmaLessThanAndCodeToShortIn(Date trainingEnd, double adf_pThreshold, double adf_p_maThreshold, Collection<String> codesToShort);

	@Modifying
	@Query(value="update \n" +
			"pair_stats s,\n" +
			"(select avg(adf_p) adf_p, code_to_short, code_to_long from pair_stats " +
			"where TIMESTAMPDIFF(DAY,training_end,?1) < ?2 and TIMESTAMPDIFF(DAY,training_end,?1) >= 0 group by code_to_short, code_to_long) avga\n" +
			"set s.adf_p_ma = avga.adf_p\n" +
			"where avga.code_to_short = s.code_to_short and avga.code_to_long = s.code_to_long and s.training_end = ?1",nativeQuery=true)
	int updateAdfpMovingAverage(Date date, int window);
}