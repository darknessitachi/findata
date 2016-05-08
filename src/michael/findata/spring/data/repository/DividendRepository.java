package michael.findata.spring.data.repository;

import michael.findata.model.Dividend;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface DividendRepository extends PagingAndSortingRepository<Dividend, Integer> {
	List<Dividend> findByPaymentDateBetweenAndStock_CodeInOrderByPaymentDateDesc(Date paymentDateStart, Date paymentDateEnd, Collection<String> codes);
	Set<Dividend> findByStock_Code(String code);
}