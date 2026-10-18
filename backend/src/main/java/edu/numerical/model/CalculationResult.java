package edu.numerical.model;

import edu.numerical.formula.FiniteDifferenceFormula;
import java.util.List;

public record CalculationResult(CalculationRequest request, FiniteDifferenceFormula formula,
    double approximation, List<SamplePoint> samplePoints, List<CalculationStep> steps,
    ErrorInfo error, List<String> warnings, List<GraphPoint> graph, Double evaluationValue,
    List<GraphPoint> tangent, List<GraphPoint> secant) {
    public record SamplePoint(int offset, double x, double value, double coefficient, double weightedValue) {}
    public record CalculationStep(String title, String explanation, String latex) {}
    public record GraphPoint(double x, Double y) {}
    public record ErrorInfo(Double referenceValue, Double absoluteError, Double relativeError, String message) {}
}
