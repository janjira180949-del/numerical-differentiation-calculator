package edu.numerical.method;

import edu.numerical.formula.FiniteDifferenceFormula;
import edu.numerical.model.CalculationResult.SamplePoint;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public interface DifferentiationMethod {
    String name();
    List<SamplePoint> sample(FiniteDifferenceFormula formula, DoubleUnaryOperator function, double x, double h);
    double weightedSum(List<SamplePoint> points);
    double approximate(FiniteDifferenceFormula formula,List<SamplePoint> points,double h);
}
