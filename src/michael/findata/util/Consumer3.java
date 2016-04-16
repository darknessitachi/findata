package michael.findata.util;

@FunctionalInterface
public interface Consumer3<A, B, C> {
	void apply(A a, B b, C c);
}