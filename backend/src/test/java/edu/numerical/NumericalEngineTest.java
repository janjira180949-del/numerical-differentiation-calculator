package edu.numerical;

import edu.numerical.formula.*;
import edu.numerical.method.*;
import edu.numerical.model.*;
import edu.numerical.service.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class NumericalEngineTest {
    final FormulaRegistry registry=new FormulaRegistry();
    final DifferentiationService service=new DifferentiationService(registry,new ExpressionEvaluator(),new ErrorCalculator(),new StepByStepGenerator(),List.of(new ForwardDifference(),new BackwardDifference(),new CentralDifference()));
    static Stream<FiniteDifferenceFormula> formulas(){return new FormulaRegistry().all().stream();}
    @ParameterizedTest @MethodSource("formulas")
    void everyStencilHasCorrectMoments(FiniteDifferenceFormula f) {
        for(int power=0;power<f.derivativeOrder()+f.accuracyOrder();power++) {
            double moment=0;for(int i=0;i<f.offsets().size();i++) moment+=f.coefficients().get(i)*Math.pow(f.offsets().get(i),power)/f.denominator();
            double expected=power==f.derivativeOrder()?factorial(power):0;
            assertEquals(expected,moment,1e-9,f.toString()+" power="+power);
        }
    }
    @ParameterizedTest @MethodSource("formulas")
    void everyFormulaDifferentiatesPolynomial(FiniteDifferenceFormula f) {
        var r=service.calculate(new CalculationRequest("x^"+f.derivativeOrder(),2.0,0.1,f.derivativeOrder(),f.method(),f.accuracy(),null));
        assertEquals(factorial(f.derivativeOrder()),r.approximation(),1e-7);
        assertEquals(9,r.steps().size()); assertEquals(f.offsets().size(),r.samplePoints().size());
    }
    @ParameterizedTest @MethodSource("formulas")
    void everyFormulaConvergesOnExponential(FiniteDifferenceFormula f) {
        double coarse=service.calculate(new CalculationRequest("exp(x)",0.2,0.2,f.derivativeOrder(),f.method(),f.accuracy(),null)).approximation();
        double fine=service.calculate(new CalculationRequest("exp(x)",0.2,0.1,f.derivativeOrder(),f.method(),f.accuracy(),null)).approximation();
        assertTrue(Math.abs(fine-Math.exp(0.2))<Math.abs(coarse-Math.exp(0.2)));
    }
    @Test void cubicExample(){var r=service.calculate(request("x^3+2*x",2,0.1,14.0));assertEquals(14.01,r.approximation(),1e-10);assertEquals(0.01,r.error().absoluteError(),1e-10);}
    @Test void zeroReference(){assertNull(service.calculate(request("x^2",0,0.1,0.0)).error().relativeError());}
    @Test void missingReference(){assertNull(service.calculate(request("sin(x)",1,0.1,null)).error().absoluteError());}
    @Test void invalidInputs(){for(String expression:List.of("", "alert(x)","x+","1/0","ln(x)","sqrt(x)","exp(10000)"))assertThrows(IllegalArgumentException.class,()->service.calculate(request(expression,-1,0.1,null)),expression);}
    @Test void invalidSteps(){for(double h:new double[]{0,-1,Double.NaN,Double.POSITIVE_INFINITY,1e-300})assertThrows(IllegalArgumentException.class,()->service.calculate(request("x^2",2,h,null)));}
    @Test void unsupported(){assertThrows(IllegalArgumentException.class,()->registry.find("FORWARD",4,"O_H4"));}
    @Test void graphDomainGaps(){var r=service.calculate(request("sqrt(x)",1,0.1,null));assertTrue(r.graph().stream().anyMatch(p->p.y()==null));}
    @Test void comparisonHandlesIndividualDomainFailure(){var rows=new ComparisonService(registry,service).compare(request("ln(x)",0.1,0.2,null));assertTrue(rows.stream().anyMatch(r->r.error()!=null));assertTrue(rows.stream().anyMatch(r->r.result()!=null));}
    @Test void finiteErrorOverflow(){assertNull(new ErrorCalculator().calculate(Double.MAX_VALUE,-Double.MAX_VALUE).absoluteError());}
    private CalculationRequest request(String expression,double x,double h,Double reference){return new CalculationRequest(expression,x,h,1,"CENTRAL","O_H2",reference);}
    private static double factorial(int n){double value=1;for(int i=2;i<=n;i++)value*=i;return value;}
}
