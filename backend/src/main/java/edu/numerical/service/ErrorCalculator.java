package edu.numerical.service;

import edu.numerical.model.CalculationResult.ErrorInfo;
import org.springframework.stereotype.Component;

@Component
public class ErrorCalculator {
    public ErrorInfo calculate(double approximation,Double reference) {
        if(reference==null) return new ErrorInfo(null,null,null,"Error analysis requires a reference value.");
        if(!Double.isFinite(reference)) throw new IllegalArgumentException("Reference value must be finite.");
        double absolute=Math.abs(approximation-reference);
        Double relative=reference==0?null:absolute/Math.abs(reference)*100;
        if(!Double.isFinite(absolute)||(relative!=null&&!Double.isFinite(relative)))
            return new ErrorInfo(reference,null,null,"Error exceeds floating-point range.");
        return new ErrorInfo(reference,absolute,relative,reference==0?"Relative error is undefined.":"Errors use the supplied reference value.");
    }
}
