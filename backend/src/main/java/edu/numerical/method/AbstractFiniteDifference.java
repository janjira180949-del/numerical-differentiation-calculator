package edu.numerical.method;

import edu.numerical.formula.FiniteDifferenceFormula;
import edu.numerical.model.CalculationResult.SamplePoint;
import java.util.*;
import java.util.function.DoubleUnaryOperator;

public abstract class AbstractFiniteDifference implements DifferentiationMethod {
    public List<SamplePoint> sample(FiniteDifferenceFormula f,DoubleUnaryOperator function,double x,double h) {
        var points=new ArrayList<SamplePoint>();
        var locations=new HashSet<Double>();
        for(int i=0;i<f.offsets().size();i++) {
            int offset=f.offsets().get(i);
            double location=x+offset*h;
            if(!Double.isFinite(location)||!locations.add(location)||(offset!=0&&location==x))
                throw new IllegalArgumentException("Step size cannot resolve distinct finite sample points at this x.");
            double value=function.applyAsDouble(location), coefficient=f.coefficients().get(i);
            points.add(new SamplePoint(offset,location,value,coefficient,coefficient*value));
        }
        return List.copyOf(points);
    }
    public double weightedSum(List<SamplePoint> points) {
        double sum=0, correction=0;
        for(var p:points) { double term=p.weightedValue()-correction; double next=sum+term; correction=(next-sum)-term; sum=next; }
        return sum;
    }
    public double approximate(FiniteDifferenceFormula f,List<SamplePoint> points,double h) {
        double sum=weightedSum(points);
        double denominator=f.denominator()*Math.pow(h,f.derivativeOrder());
        double result=sum/denominator;
        if(!Double.isFinite(result)||denominator==0||!Double.isFinite(denominator)) throw new IllegalArgumentException("Calculation exceeds floating-point range. Adjust x or h.");
        return result;
    }
}
