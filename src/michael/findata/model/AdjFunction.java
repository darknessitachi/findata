package michael.findata.model;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.function.Function;

public class AdjFunction<T, R> implements Function<T, R> {
	public DateTime paymentDate;
	public Function<T, R> function;

	public AdjFunction(DateTime paymentDate, Function<T, R> function) {
		this.paymentDate = paymentDate;
		this.function = function;
	}

	public AdjFunction(Date paymentDate, Function<T, R> function) {
		this.paymentDate = new DateTime(paymentDate);
		this.function = function;
	}

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	@Override
	public R apply(T t) {
		return function.apply(t);
	}
}