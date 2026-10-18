# Numerical Differentiation Calculator Website

A Java-centered university OOP project with a Spring Boot numerical engine and a responsive SvelteKit/TypeScript interface. All expression evaluation, finite differences, graph samples, solution steps, comparisons, and error calculations run in Java. The browser renders results using KaTeX and Plotly.

Cloud deployment is being prepared under a strict $0 budget. No public repository or website URL is verified yet. See [deployment status and configuration](docs/DEPLOYMENT.md).

## Run in VS Code on Windows

Open this directory in VS Code. The current workspace includes portable Java 21, Maven 3.9.9, and Node 22 under `.tools` (ignored from source control). The scripts use them without changing system settings.

Terminal 1:

```powershell
Set-Location 'D:\Projects\Numerical Differentiation'
powershell -ExecutionPolicy Bypass -File .\scripts\start-backend.ps1
```

Terminal 2:

```powershell
Set-Location 'D:\Projects\Numerical Differentiation'
powershell -ExecutionPolicy Bypass -File .\scripts\start-frontend.ps1
```

Open **http://127.0.0.1:5173**. Java listens on port 8080. Stop each server with Ctrl+C.

On a fresh machine or checkout, install JDK 21, Maven 3.9+, and Node 22 LTS, then add their commands to PATH and set JAVA_HOME to the JDK. Run `npm.cmd ci` inside `frontend` before starting. The ignored portable tools are not needed when these tools are installed globally.

## Features

- Forward, backward, and central differences; derivatives 1–4; 18 backend-registered formulas. Unsupported combinations never appear in the accuracy menu.
- First-derivative forward/backward O(h), O(h²); central O(h²), O(h⁴). Second-derivative forward/backward O(h), O(h²); central O(h²), O(h⁴). Third/fourth-derivative forward/backward O(h), central O(h²).
- Nine solution steps generated from actual stencil coefficients and evaluated samples; compensated summation and no intermediate rounding.
- Plotly function curve, evaluation point, stencil samples, first-derivative approximate tangent, and two-point secant where applicable. Zoom, pan, hover, reset, and resize.
- Debounced h input and logarithmic slider; aborted requests and revision checks prevent stale responses. Editing inputs clears prior results.
- Optional reference derivative, absolute and percentage relative errors, and explicit handling of missing and zero references. Preset references are verified analytic values. Changing expression, x, or derivative order clears the reference.
- Compare every supported formula for an order, including per-stencil domain errors; highlight the smallest supplied-reference error only when defined.
- Eight learning sections, four examples, dark mode, mobile layout, and browser-local history capped at 30 entries with restore/delete/clear and input validation.

## Expression syntax

Use `x`, `pi`, `e`, decimal literals, `+ - * / ^`, and parentheses. Supported functions: `sin(x)`, `cos(x)`, `tan(x)`, `exp(x)`, `ln(x)`, `sqrt(x)`. Trigonometric functions use radians. Use explicit multiplication: `2*x`, not `2x`. Examples: `x^3 + 2*x`, `sin(x)`, `exp(-x)`, `ln(x)`, `sqrt(x)`. Expressions are limited to 200 characters and 25 opening parentheses. Names are allowlisted before exp4j compilation. No JavaScript eval or arbitrary execution is used.

Bounds: |x| ≤ 10⁸; 0 < h ≤ 10⁴. The UI number input has a minimum of 10⁻¹⁶; the slider explores 10⁻⁵ through 1. Java also rejects unresolvable stencil coordinates, undefined sample values, and overflowing arithmetic.

## Verify

From the repository root:

```powershell
. .\scripts\use-tools.ps1
mvn.cmd -B -ntp -f backend\pom.xml verify
Set-Location frontend
npm.cmd run check
npm.cmd run build
# Start both servers first. Browser tests use installed Google Chrome.
npm.cmd run test:e2e
```

Mathematical tests cover every registered stencil's Taylor moments, exact differentiation of a polynomial of the derivative order, and exponential convergence under halving h. Additional tests cover the required cubic example, reference rules, invalid domains/syntax/steps, unsupported formulas, graph gaps, and partial comparison failures. Spring MockMvc tests exercise all four endpoints and malformed/invalid requests. Browser tests cover calculations, h updates, domain validation, higher derivatives, comparison, learning, theme, history, and mobile overflow.

## Architecture and Java OOP

`backend/src/main/java/edu/numerical`:

- `model`: immutable request/result records and structured sample, step, graph, and error data.
- `formula`: immutable stencil metadata and a registry of 18 explicit, verified formulas.
- `method`: `DifferentiationMethod` abstracts sampling and approximation. `AbstractFiniteDifference` implements shared stencil evaluation and compensated summation. `ForwardDifference`, `BackwardDifference`, and `CentralDifference` inherit that behavior and identify the strategy.
- `service`: `DifferentiationService` composes the registry, evaluator, error calculator, step generator, and injected strategy list. It selects strategies through the interface (polymorphism). `ComparisonService` reuses this engine. The restricted evaluator is isolated from stencil algorithms.
- `controller`: thin REST controller and consistent JSON exception responses.

Private final fields encapsulate service state; immutable records and copied lists protect formula/result data. Constructors establish valid state and support Spring constructor injection. Shared inheritance avoids three duplicated numerical algorithms; composition keeps expression, error, solution, and HTTP responsibilities separate.

`frontend/src/lib`: typed API models, KaTeX renderer, lazy-loaded Plotly renderer. `routes/[[section]]` serves Calculator (`/`), Compare (`/compare`), Learn (`/learn`), and History (`/history`). Tailwind is integrated through Vite alongside custom CSS. `routes/api/[...path]` forwards production API requests to Java. No numerical differentiation runs in TypeScript.

## REST API

| Endpoint | Purpose |
| --- | --- |
| GET `/api/health` | Java engine health |
| GET `/api/differentiation/methods` | All supported formula metadata |
| POST `/api/differentiation/calculate` | Derivative, samples, steps, errors, warnings, graph |
| POST `/api/differentiation/compare` | All supported formulas for an order; per-row results/errors |

```json
{"expression":"x^3 + 2*x","x":2,"h":0.1,"derivativeOrder":1,"method":"CENTRAL","accuracy":"O_H2","referenceValue":14}
```

`referenceValue` can be null or omitted. Methods are `FORWARD`, `BACKWARD`, `CENTRAL`; accuracy identifiers are `O_H`, `O_H2`, `O_H4`. GET methods is authoritative for supported combinations. Comparison accepts the same envelope but enumerates every formula for the given order. Input errors return HTTP 400 with `code`, `message`, and optional field errors. Unexpected failures return a generic HTTP 500 without stack traces.

## Production build

```powershell
. .\scripts\use-tools.ps1
mvn.cmd -B -ntp -f backend\pom.xml package
java -jar backend\target\differentiation-1.0.0.jar
```

In another terminal:

```powershell
. .\scripts\use-tools.ps1
Set-Location frontend
npm.cmd run build
$env:JAVA_API_URL = 'http://127.0.0.1:8080'
$env:ORIGIN = 'http://localhost:3000'
node build
```

Open http://localhost:3000. The Node server proxies `/api` to Java; the frontend never needs direct cross-origin backend access. For deployment, run both processes behind HTTPS, set the public ORIGIN and internal JAVA_API_URL, add platform request limits/monitoring, and review current dependency advisories. This project is prepared for a local demonstration and has not been deployed to public hosting.

## Numerical limitations

- IEEE-754 double precision, especially cancellation in high-order derivatives and at tiny h. O(hᵖ) is a truncation rate for sufficiently smooth functions, not an accuracy guarantee.
- References are supplied by the user or selected example; there is no general symbolic differentiation engine and the app cannot verify an arbitrary user reference.
- The graph uses 401 bounded Java sample locations and inserts null breaks for invalid values and detected steep sign-changing poles. Sampling cannot detect every singularity, narrow domain hole, or oscillation. It is an illustration rather than proof of continuity. Plot range radius is capped at 1000; very wide stencils can lie outside that local view.
- Graph/formula previews are display aids; Java's parser is authoritative. The live preview formats common expressions but is not a full symbolic typesetter.
- Full Plotly is lazy-loaded but has a large bundle. History and theme belong to the current browser; no authentication, database, or cross-device synchronization.

See [implementation report](docs/IMPLEMENTATION.md) for the verified test results and submission notes.
