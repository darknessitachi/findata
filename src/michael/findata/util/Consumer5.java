package michael.findata.util;

/**
 * Created by nicky on 2015/12/5.
 */
@FunctionalInterface
public interface Consumer5<A, B, C, D, E> {
	void apply(A a, B b, C c, D d, E e);
}
