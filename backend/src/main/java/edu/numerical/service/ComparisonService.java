package edu.numerical.service;

import edu.numerical.formula.FormulaRegistry;
import edu.numerical.model.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ComparisonService {
    public record ComparisonRow(String method,String accuracy,CalculationResult result,String error) {}
    private final FormulaRegistry registry;
    private final DifferentiationService service;
    public ComparisonService(FormulaRegistry registry,DifferentiationService service){this.registry=registry;this.service=service;}
    public List<ComparisonRow> compare(CalculationRequest r) {
        service.validate(r);
        var rows=new ArrayList<ComparisonRow>();
        for(var f:registry.all()) if(f.derivativeOrder()==r.derivativeOrder()) {
            try { rows.add(new ComparisonRow(f.method(),f.accuracy(),service.calculate(new CalculationRequest(r.expression(),r.x(),r.h(),r.derivativeOrder(),f.method(),f.accuracy(),r.referenceValue())),null)); }
            catch(IllegalArgumentException ex){rows.add(new ComparisonRow(f.method(),f.accuracy(),null,ex.getMessage()));}
        }
        return List.copyOf(rows);
    }
}
