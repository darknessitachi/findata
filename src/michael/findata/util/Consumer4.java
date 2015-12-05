package michael.findata.util;

/**
 * Created by nicky on 2015/11/29.
 */
@FunctionalInterface
public interface Consumer4 <A, B, C, D> {
	void apply(A a, B b, C c, D d);
}