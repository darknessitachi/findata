package michael.findata.spring.data.repository;

import michael.findata.model.Dividend;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface DividendRepository extends PagingAndSortingRepository<Dividend, Integer> {
	List<Dividend> findByPaymentDateBetweenAndStock_CodeInOrderByPaymentDateDesc(Date paymentDateStart, Date paymentDateEnd, Collection<String> codes);
	List<Dividend> findByPaymentDateBetweenAndStock_CodeInOrderByPaymentDate(Date paymentDateStart, Date paymentDateEnd, String ... codes);
	Set<Dividend> findByStock_Code(String code);
	List<Dividend> findByStock_CodeInOrderByPaymentDate(String ... codes);
	List<Dividend> findByStock_CodeInOrderByPaymentDate(Collection<String> codes);
}