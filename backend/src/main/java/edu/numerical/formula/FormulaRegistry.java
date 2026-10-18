package edu.numerical.formula;

import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class FormulaRegistry {
    private final List<FiniteDifferenceFormula> formulas;
    public FormulaRegistry() {
        var f = new ArrayList<FiniteDifferenceFormula>();
        add(f,"FORWARD",1,1,new int[]{0,1},new double[]{-1,1},1);
        add(f,"FORWARD",1,2,new int[]{0,1,2},new double[]{-3,4,-1},2);
        add(f,"FORWARD",2,1,new int[]{0,1,2},new double[]{1,-2,1},1);
        add(f,"FORWARD",2,2,new int[]{0,1,2,3},new double[]{2,-5,4,-1},1);
        add(f,"FORWARD",3,1,new int[]{0,1,2,3},new double[]{-1,3,-3,1},1);
        add(f,"FORWARD",4,1,new int[]{0,1,2,3,4},new double[]{1,-4,6,-4,1},1);
        for (var a : List.copyOf(f)) {
            add(f,"BACKWARD",a.derivativeOrder(),a.accuracyOrder(),a.offsets().stream().mapToInt(i->-i).toArray(),
                a.coefficients().stream().mapToDouble(c->c*(a.derivativeOrder()%2==0?1:-1)).toArray(),a.denominator());
        }
        add(f,"CENTRAL",1,2,new int[]{-1,1},new double[]{-1,1},2);
        add(f,"CENTRAL",1,4,new int[]{-2,-1,1,2},new double[]{1,-8,8,-1},12);
        add(f,"CENTRAL",2,2,new int[]{-1,0,1},new double[]{1,-2,1},1);
        add(f,"CENTRAL",2,4,new int[]{-2,-1,0,1,2},new double[]{-1,16,-30,16,-1},12);
        add(f,"CENTRAL",3,2,new int[]{-2,-1,1,2},new double[]{-1,2,-2,1},2);
        add(f,"CENTRAL",4,2,new int[]{-2,-1,0,1,2},new double[]{1,-4,6,-4,1},1);
        formulas = List.copyOf(f);
    }
    private void add(List<FiniteDifferenceFormula> list,String method,int order,int accuracy,int[] offsets,double[] coefficients,double denominator) {
        StringJoiner numerator = new StringJoiner(" + ");
        for (int i=0;i<offsets.length;i++) numerator.add("("+coefficients[i]+")f(x"+(offsets[i]>=0?"+":"")+offsets[i]+"h)");
        list.add(new FiniteDifferenceFormula(method,order,"O_H"+(accuracy==1?"":accuracy),accuracy,
            Arrays.stream(offsets).boxed().toList(),Arrays.stream(coefficients).boxed().toList(),denominator,
            "f^{("+order+")}(x) \\approx \\frac{"+numerator+"}{"+denominator+"h^{"+order+"}}"));
    }
    public List<FiniteDifferenceFormula> all() { return formulas; }
    public FiniteDifferenceFormula find(String method,int order,String accuracy) {
        return formulas.stream().filter(f->f.method().equals(method)&&f.derivativeOrder()==order&&f.accuracy().equals(accuracy))
            .findFirst().orElseThrow(()->new IllegalArgumentException("Unsupported method, derivative order, or accuracy combination."));
    }
}
