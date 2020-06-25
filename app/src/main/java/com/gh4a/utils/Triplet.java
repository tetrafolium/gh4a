package com.gh4a.utils;

public class Triplet<F, S, T> {
    public final F first;
    public final S second;
    public final T third;

    public Triplet(final F first, final S second, final T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Triplet)) {
            return false;
        }
        Triplet<?, ?, ?> p = (Triplet<?, ?, ?>) o;
        return objectEquals(first, p.first)
               && objectEquals(second, p.second)
               && objectEquals(third, p.third);
    }

    private static boolean objectEquals(final Object x, final Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode())
               ^ (second == null ? 0 : second.hashCode())
               ^ (third == null ? 0 : third.hashCode());
    }

    public static <F, S, T> Triplet<F, S, T> create(final F f, final S s, final T t) {
        return new Triplet<>(f, s, t);
    }
}
