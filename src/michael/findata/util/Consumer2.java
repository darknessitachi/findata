package michael.findata.util;

@FunctionalInterface
public interface Consumer2<A, B> {
	void apply(A a, B b);
}