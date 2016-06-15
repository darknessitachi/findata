package michael.findata.spring.data.repository;

import michael.findata.model.Pair;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PairRepository extends PagingAndSortingRepository<Pair, Integer> {
	List<Pair> findByEnabled(boolean enabled);
	List<Pair> findByEnabledAndIdGreaterThan(boolean enabled, int id);
	List<Pair> findByEnabledAndIdGreaterThanAndIdLessThan(boolean enabled, int idGt, int idLt);
}