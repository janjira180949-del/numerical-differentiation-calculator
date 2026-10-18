package edu.numerical.service;

import edu.numerical.formula.FormulaRegistry;
import edu.numerical.method.DifferentiationMethod;
import edu.numerical.model.*;
import edu.numerical.model.CalculationResult.*;
import java.util.*;
import java.util.function.DoubleUnaryOperator;
import org.springframework.stereotype.Service;

@Service
public class DifferentiationService {
    private final FormulaRegistry registry;
    private final ExpressionEvaluator evaluator;
    private final ErrorCalculator errors;
    private final StepByStepGenerator steps;
    private final List<DifferentiationMethod> methods;
    public DifferentiationService(FormulaRegistry registry,ExpressionEvaluator evaluator,ErrorCalculator errors,StepByStepGenerator steps,List<DifferentiationMethod> methods) {
        this.registry=registry; this.evaluator=evaluator; this.errors=errors; this.steps=steps; this.methods=List.copyOf(methods);
    }
    public CalculationResult calculate(CalculationRequest r) {
        validate(r);
        var formula=registry.find(r.method(),r.derivativeOrder(),r.accuracy());
        var function=evaluator.compile(r.expression());
        var method=methods.stream().filter(m->m.name().equals(r.method())).findFirst().orElseThrow();
        var points=method.sample(formula,function,r.x(),r.h());
        double answer=method.approximate(formula,points,r.h());
        var error=errors.calculate(answer,r.referenceValue());
        var warnings=new ArrayList<String>();
        if(r.h()<1e-5||r.derivativeOrder()>2) warnings.add("Small h and higher derivatives amplify floating-point round-off. Check stability with several step sizes.");
        double radius=Math.min(1000,Math.max(2,4*r.h()));
        var graph=new ArrayList<GraphPoint>();
        Double previous=null;
        for(int i=0;i<=400;i++) {
            double x=r.x()-radius+2*radius*i/400;
            Double y=safe(function,x);
            // Break sampled sign-changing poles and invalid domains rather than joining across them.
            if(previous!=null&&y!=null&&Math.signum(previous)!=Math.signum(y)&&Math.abs(previous-y)>20) graph.add(new GraphPoint(x,null));
            graph.add(new GraphPoint(x,y)); previous=y;
        }
        Double center=safe(function,r.x());
        var tangent=new ArrayList<GraphPoint>(); var secant=new ArrayList<GraphPoint>();
        if(r.derivativeOrder()==1&&center!=null) {
            for(double shift:new double[]{-radius/2,radius/2}) {
                double y=center+answer*shift;
                if(Double.isFinite(y)) tangent.add(new GraphPoint(r.x()+shift,y));
            }
            if(points.size()==2) for(var p:points) secant.add(new GraphPoint(p.x(),p.value()));
        }
        return new CalculationResult(r,formula,answer,points,steps.generate(r,formula,points,method.weightedSum(points),answer,error),error,warnings,List.copyOf(graph),center,List.copyOf(tangent),List.copyOf(secant));
    }
    private Double safe(DoubleUnaryOperator f,double x) { try{return f.applyAsDouble(x);}catch(IllegalArgumentException ex){return null;} }
    public void validate(CalculationRequest r) {
        if(r.x()==null||!Double.isFinite(r.x())||Math.abs(r.x())>1e8) throw new IllegalArgumentException("Point x must be finite and between −100000000 and 100000000.");
        if(r.h()==null||!Double.isFinite(r.h())||r.h()<=0||r.h()>1e4) throw new IllegalArgumentException("Step size h must be finite, positive, and at most 10000.");
        if(r.derivativeOrder()<1||r.derivativeOrder()>4) throw new IllegalArgumentException("Derivative order must be from 1 to 4.");
    }
}
