package edu.numerical.service;

import java.util.*;
import edu.numerical.model.CalculationRequest;
import edu.numerical.model.CalculationResult.*;
import edu.numerical.formula.FiniteDifferenceFormula;
import org.springframework.stereotype.Component;

@Component
public class StepByStepGenerator {
    public List<CalculationStep> generate(CalculationRequest r,FiniteDifferenceFormula f,List<SamplePoint> points,double weightedSum,double answer,ErrorInfo error) {
        var steps=new ArrayList<CalculationStep>();
        steps.add(new CalculationStep("Choose the stencil",r.method()+" difference; derivative order "+r.derivativeOrder()+"; truncation order O(h^"+f.accuracyOrder()+").",""));
        steps.add(new CalculationStep("Write the formula","Use the registered finite difference coefficients.",f.latex()));
        steps.add(new CalculationStep("Insert the parameters","Use x = "+r.x()+" and h = "+r.h()+".",""));
        steps.add(new CalculationStep("Locate the samples",String.join("; ",points.stream().map(p->r.x()+" + ("+p.offset()+") × "+r.h()+" = "+p.x()).toList()),""));
        steps.add(new CalculationStep("Evaluate the function",String.join("; ",points.stream().map(p->"f("+p.x()+") = "+p.value()).toList()),""));
        steps.add(new CalculationStep("Apply the weights",String.join(" + ",points.stream().map(p->"("+p.coefficient()+" × "+p.value()+")").toList()),""));
        double denominator=f.denominator()*Math.pow(r.h(),r.derivativeOrder());
        steps.add(new CalculationStep("Sum and divide","Compensated weighted sum = "+weightedSum+"; denominator = "+f.denominator()+" × "+r.h()+"^"+r.derivativeOrder()+" = "+denominator+".",""));
        steps.add(new CalculationStep("Read the approximation","Full precision result: "+answer,"f^{("+r.derivativeOrder()+")}("+r.x()+") \\approx "+answer));
        steps.add(new CalculationStep("Check the error",error.referenceValue()==null?error.message():"Reference = "+error.referenceValue()+"; |approximation − reference| = "+error.absoluteError()+". "+(error.relativeError()==null?error.message():"Relative error = "+error.relativeError()+"%."),""));
        return List.copyOf(steps);
    }
}
