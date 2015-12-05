package michael.findata.util;

/**
 * Created by nicky on 2015/11/29.
 */
@FunctionalInterface
public interface Consumer3<A, B, C> {
	void apply(A a, B b, C c);
}