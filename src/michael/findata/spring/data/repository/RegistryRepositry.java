package michael.findata.spring.data.repository;

import michael.findata.model.Registry;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RegistryRepositry extends PagingAndSortingRepository <Registry, Integer>{
	Registry findOneByName (String name);
}