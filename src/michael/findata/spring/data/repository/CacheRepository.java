package michael.findata.spring.data.repository;

import michael.findata.model.Cache;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CacheRepository extends PagingAndSortingRepository<Cache, Integer> {
	Cache findOneByName (String name);
}