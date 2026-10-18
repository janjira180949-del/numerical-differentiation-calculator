package edu.numerical.controller;

import edu.numerical.formula.*;
import edu.numerical.model.*;
import edu.numerical.service.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApiController {
    private final FormulaRegistry registry;
    private final DifferentiationService service;
    private final ComparisonService comparison;
    public ApiController(FormulaRegistry registry,DifferentiationService service,ComparisonService comparison){this.registry=registry;this.service=service;this.comparison=comparison;}
    @GetMapping("/api/health") public Map<String,String> health(){return Map.of("status","UP","engine","Java 21 / Spring Boot");}
    @GetMapping("/api/differentiation/methods") public List<FiniteDifferenceFormula> methods(){return registry.all();}
    @PostMapping("/api/differentiation/calculate") public CalculationResult calculate(@Valid @RequestBody CalculationRequest r){return service.calculate(r);}
    @PostMapping("/api/differentiation/compare") public List<ComparisonService.ComparisonRow> compare(@Valid @RequestBody CalculationRequest r){return comparison.compare(r);}
}
