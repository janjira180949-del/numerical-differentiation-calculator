package edu.numerical.service;

import java.util.function.DoubleUnaryOperator;
import java.util.regex.Pattern;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Component;

@Component
public class ExpressionEvaluator {
    private static final Pattern TOKEN = Pattern.compile("[A-Za-z_][A-Za-z_0-9]*");
    private static final java.util.Set<String> ALLOWED = java.util.Set.of("x","e","pi","sin","cos","tan","exp","ln","sqrt");
    public DoubleUnaryOperator compile(String input) {
        if (input==null || input.isBlank() || input.length()>200 || !input.matches("[A-Za-z0-9_+*/^().\\s-]+"))
            throw new IllegalArgumentException("Enter an expression of at most 200 characters using the documented syntax.");
        var matcher=TOKEN.matcher(input);
        while(matcher.find()) if(!ALLOWED.contains(matcher.group())) throw new IllegalArgumentException("Unsupported name: "+matcher.group());
        if (input.chars().filter(c->c=='(').count()>25) throw new IllegalArgumentException("Expression is too complex.");
        try {
            var expression=new ExpressionBuilder(input.replaceAll("\\bln\\b","log")).variable("x").build();
            return x->{
                try {
                    double value=expression.setVariable("x",x).evaluate();
                    if(!Double.isFinite(value)) throw new ArithmeticException();
                    return value;
                } catch(Exception ex) { throw new IllegalArgumentException("Function is undefined or overflows at x = "+x+". Check its domain and denominators."); }
            };
        } catch(Exception ex) { throw new IllegalArgumentException("Invalid expression syntax. Use explicit multiplication, such as 2*x."); }
    }
}
