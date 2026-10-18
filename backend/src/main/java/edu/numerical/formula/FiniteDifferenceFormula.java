package edu.numerical.formula;

import java.util.List;

public record FiniteDifferenceFormula(String method, int derivativeOrder, String accuracy,
    int accuracyOrder, List<Integer> offsets, List<Double> coefficients, double denominator, String latex) {
    public FiniteDifferenceFormula {
        offsets = List.copyOf(offsets);
        coefficients = List.copyOf(coefficients);
        if (offsets.size() != coefficients.size()) throw new IllegalArgumentException("Stencil lengths differ");
    }
}
