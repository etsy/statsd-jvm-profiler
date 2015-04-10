package com.etsy.statsd.profiler.util;

/**
 * Represents an immutable range of integers
 */
public class Range {
    private int left;
    private int right;

    public Range(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        return left == range.left && right == range.right;

    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + right;
        return result;
    }

    @Override
    public String toString() {
        return "Range{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
