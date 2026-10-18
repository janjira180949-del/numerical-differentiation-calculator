# Continuation verification report

Verified on 18 October 2026. The existing UI, numerical algorithms, and original implementation report were preserved. The workspace has no Git repository, so there was no Git status/diff to recover; the existing source, README, implementation report, generated test reports, and prior session context were inspected instead.

## Overall status

**Ready for a complete local demonstration.** The previously unfinished production forwarding check passed with a real production SvelteKit Node server and packaged Spring Boot Java backend. No application correctness or integration defect was found requiring a source change.

The completed feature set remains: forward/backward/central differentiation, orders 1–4, all 18 supported stencils, expression validation, Java-generated solutions and graph data, KaTeX formulas, interactive Plotly graph, h recalculation, supplied-reference error analysis, method comparison, learning content, examples, local history, and responsive light/dark themes. Java OOP architecture is explained in the original [implementation report](IMPLEMENTATION.md).

## Verification results

| Check | Passed | Failed | Details |
| --- | ---: | ---: | --- |
| Java mathematics tests | 63 | 0 | All stencils: moments, polynomial differentiation, exponential convergence; validation, reference errors, steps, graph gaps |
| Spring API integration tests | 4 | 0 | Health/registry, calculation, comparison, malformed and invalid requests |
| Production HTTP smoke assertions | 35 | 0 | All 18 stencils through Node, payload equality with Java, errors, comparison, pages and status codes |
| Production browser scenarios | 4 | 0 | Desktop Chrome and mobile Chromium emulation |
| Development browser scenarios | 4 | 0 | Same end-to-end scenarios against the Vite development server |

Java compilation, Maven `verify`, executable JAR packaging, and the SvelteKit Node production build succeeded. Frontend diagnostics reported **0 errors and 0 warnings**. All 67 Java tests ran, with zero errors or skipped tests. Vite's large Plotly chunk notice and a generated adapter unused-import notice remain non-blocking build advisories.

Browser verification covers actual calculations, Java-backed graph creation, all nine solution steps, h changes, reference clearing, domain errors, higher derivatives, supported accuracy selection, comparison, theme persistence, learning navigation, history save/restore/clear, runtime errors, and mobile horizontal overflow.

## Actual production request

The test used the packaged JAR on `127.0.0.1:8080` and `node build` on `127.0.0.1:3000`, with `JAVA_API_URL=http://127.0.0.1:8080` and `ORIGIN=http://127.0.0.1:3000`.

```http
POST http://127.0.0.1:3000/api/differentiation/calculate
Content-Type: application/json
Origin: http://127.0.0.1:3000

{"expression":"x^3 + 2*x","x":2,"h":0.1,"derivativeOrder":1,"method":"CENTRAL","accuracy":"O_H2","referenceValue":14}
```

Observed result: **14.010000000000016**; absolute error **0.010000000000015774**; nine solution steps; two stencil points; 401 function graph samples. Java's HTTP log confirmed the request reached its calculation controller and returned HTTP 200. A separate direct request to Java produced the same complete JSON calculation payload. All 18 formula combinations were then checked against the derivative of x^m through the production proxy.

Production comparison returned all six first-derivative rows. Zero-reference relative error remained undefined and absent references produced no fabricated errors. Unsupported methods, invalid domains, and malformed JSON retained HTTP 400 through the proxy. All four application routes returned HTTP 200, and an unknown page returned HTTP 404. Browser tests succeeded through the same-origin API path without blocking CORS, proxy, or routing errors.

Two verification-command issues were corrected during this continuation: PowerShell's JSON array wrapping in the new smoke script, and terminating native stderr handling when redirecting Vite's bundle advisory. These were test-harness/command issues; no application algorithm, UI, or proxy change was needed.

## Files changed during this continuation

- **Modified:** `frontend/playwright.config.ts` — accepts `PLAYWRIGHT_BASE_URL` so the existing browser tests can target production or development without duplication.
- **Added:** `scripts/verify-production.ps1` — repeatable real HTTP production verification with 35 assertions.
- **Added:** `docs/CONTINUATION-VERIFICATION.md` — this report; the original implementation report was not overwritten.
- Generated build outputs and ignored runtime/test logs were refreshed by verification.

## Run the development website

Open two PowerShell terminals in VS Code. Do not run a second backend if port 8080 is already occupied by this project.

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

Open **http://127.0.0.1:5173**. The launchers use the existing portable tools under `.tools`; README also explains fresh-machine prerequisites.

## Run the production website

Terminal 1:

```powershell
Set-Location 'D:\Projects\Numerical Differentiation'
. .\scripts\use-tools.ps1
mvn.cmd -B -ntp -f backend\pom.xml verify
java -jar backend\target\differentiation-1.0.0.jar
```

Terminal 2:

```powershell
Set-Location 'D:\Projects\Numerical Differentiation'
. .\scripts\use-tools.ps1
Set-Location frontend
npm.cmd run check
npm.cmd run build
$env:JAVA_API_URL = 'http://127.0.0.1:8080'
$env:ORIGIN = 'http://127.0.0.1:3000'
$env:HOST = '127.0.0.1'
$env:PORT = '3000'
node build
```

Open **http://127.0.0.1:3000**. To reproduce production checks in a third terminal while both servers run:

```powershell
Set-Location 'D:\Projects\Numerical Differentiation'
. .\scripts\use-tools.ps1
.\scripts\verify-production.ps1
Set-Location frontend
$env:PLAYWRIGHT_BASE_URL = 'http://127.0.0.1:3000'
npm.cmd run test:e2e
```

For development browser tests, set `PLAYWRIGHT_BASE_URL` to `http://127.0.0.1:5173` while the development server runs. Browser tests require installed Google Chrome, as configured in the existing project. Stop servers with Ctrl+C. Verification servers were shut down after the checks to release their ports.

## Remaining issues

No blocking local-demonstration issues remain. Existing numerical limits still apply: double precision and cancellation, finite graph sampling, and no independent verification of arbitrary user-provided reference derivatives. Full Plotly has a large lazy-loaded bundle. Mobile tests used browser emulation rather than a physical device. Public deployment was outside this continuation and has not been performed.
