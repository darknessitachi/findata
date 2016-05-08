package michael.findata.spring.data.repository;

import michael.findata.model.PairInstance;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

public interface PairInstanceRepository extends PagingAndSortingRepository<PairInstance, Integer>{
	List<PairInstance> findByOpenableDateLessThanEqualAndForceClosureDateGreaterThanEqual(Date sameDate1, Date sameDate2);
	List<PairInstance> findByOpenableDateBetween(Date start, Date end);
	List<PairInstance> findByOpenableDateBetweenAndStats_CorrelcoGreaterThanAndStats_AdfpLessThanAndCodeToShortInAndCodeToLongIn
			(Date start, Date end, double correlcoThreshold, double adfpThreshold, String[] codesToShort, String[] codesToLong);
}