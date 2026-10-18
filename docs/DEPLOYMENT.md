# Render deployment — verified Free services

Status on **18 October 2026 (Asia/Bangkok): both services live and publicly tested**. Deployment was explicitly authorized with a maximum budget of **$0 USD / 0 THB**. The existing Java algorithms, frontend UI, application features, Dockerfile, and `render.yaml` were preserved. This deployment changed infrastructure configuration and these deployment records only.

## Verified public URLs and resources

| Resource | Verified URL / identifier | Status |
| --- | --- | --- |
| Website | https://numerical-differentiation-web.onrender.com | HTTP 200; desktop/mobile tests passed |
| Java backend | https://numerical-differentiation-java.onrender.com | Live; API checks passed |
| Java health | https://numerical-differentiation-java.onrender.com/api/health | HTTP 200; `status: UP` |
| Frontend health proxy | https://numerical-differentiation-web.onrender.com/api/health | HTTP 200; reaches Java |
| GitHub repository | https://github.com/janjira180949-del/numerical-differentiation-calculator | Existing `main` used |
| Backend service | `srv-dba682nbtjjs73dv24rg` | Docker; `plan: free`; Singapore |
| Frontend service | `srv-dba6973bc2fs73a7gj60` | Node; `plan: free`; Singapore |

Workspace: **My Workspace**, `tea-dba5u3rbc2fs73a6butg`, explicitly confirmed by the user. Both services deploy application commit `a272890c994257e03dabef03857614c7311f16db`. Subsequent documentation-only commits do not require redeployment.

- [Backend dashboard](https://dashboard.render.com/web/srv-dba682nbtjjs73dv24rg)
- [Frontend dashboard](https://dashboard.render.com/web/srv-dba6973bc2fs73a7gj60)
- Successful backend deployment: `dep-dba683fbtjjs73dv2750`.
- Successful frontend deployment with production environment: `dep-dba699bbc5jc73c7pkr0`.

The backend was deployed and its public health verified **before** the frontend was created. The frontend's initial deploy was followed by the environment-update deploy that applied its actual assigned `ORIGIN`. Both final deploys reached Render's `live` state. The final resource listing contained exactly these two web services.

## Billing verification and confirmed costs

Before service creation, the user supplied actual Render Billing dashboard screenshots showing:

- Current workspace plan: **Hobby**.
- Payment Method: **No card on file**.
- Free Instance Hours: **0 / 750**; Included Bandwidth: **0 MB / 5 GB**; Included Pipeline Minutes: **0 / 500**.
- Existing Services: **0**; Unbilled Charges: **No pending charges**; Invoice History: **No past invoices**; Credit Balance: **$0.00**.

These usage figures describe the pre-deployment screenshots, not post-deployment usage. The connector does not expose live billing/payment-method details.

Before each creation, the requested compute plan was explicitly set to **`free`**, verified against current [Render compute-plan documentation](https://render.com/docs/compute-plans) and [Free instance documentation](https://render.com/docs/free). Each creation response and subsequent service inspection confirmed `serviceDetails.plan: free`.

**Recurring compute cost: backend $0/month + frontend $0/month = $0/month (0 THB).** No payment information was entered, billing was not activated, and no paid plan, trial, database, disk, worker, custom-domain purchase, or other resource was created. Automatic deployment is off on both services to conserve included build usage.

Without a payment method, exhausting included bandwidth suspends Free services; exhausting included pipeline minutes disables new builds rather than purchasing extra usage. Never attach a card, upgrade, activate a trial, or purchase usage to keep this deployment running. [Render Free usage policy](https://render.com/docs/free).

## Existing configuration and applied settings

The services were created individually through the Render connector, using the existing `render.yaml` settings, so the backend could be verified first. No Blueprint was applied and the services are not currently Blueprint-managed. Do not apply a new Blueprint blindly over these existing services.

| Setting | Backend | Frontend |
| --- | --- | --- |
| Compute plan | `free` | `free` |
| Region | Singapore | Singapore |
| Runtime | Docker; Java 21 | Node 22; SvelteKit adapter-node |
| Branch | `main` | `main` |
| Dockerfile / context | `./backend/Dockerfile` / `./backend` | Not applicable |
| Build | Existing Dockerfile runs `mvn -B -ntp verify` | `cd frontend && npm ci --include=dev && npm run check && npm run build` |
| Start | Existing Dockerfile entrypoint: `java -jar /app/app.jar` | `cd frontend && node build` |
| Auto-deploy | Off | Off |
| Bind address | `SERVER_ADDRESS=0.0.0.0`; `server.port=${PORT:8080}` | `HOST=0.0.0.0`; Render supplies `PORT` |
| Additional environment | `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=50.0` | `NODE_VERSION=22`; URL variables below |

Production frontend environment values:

```text
JAVA_API_URL=https://numerical-differentiation-java.onrender.com
ORIGIN=https://numerical-differentiation-web.onrender.com
```

These are the actual HTTPS origins returned by Render, without trailing slashes. The existing SvelteKit server proxy forwards `/api` requests to Java; calculation payloads were verified to match the direct Java API.

Connector limitations: its creation operation does not expose `rootDir`, `healthCheckPath`, or build filters. Frontend commands therefore enter `frontend` explicitly, preserving the existing build/start behavior. Render's live services currently use default **TCP health checks**; the public HTTP `/api/health` endpoints were independently tested successfully. The unchanged Blueprint retains `/api/health` and `/` as its intended HTTP health paths and the backend build filter. Setting platform HTTP health paths later requires the Dashboard or another supported configuration interface; this limitation does not prevent the verified public endpoints from working. [Health-check behavior](https://render.com/docs/health-checks).

The backend image uses the existing multi-stage Maven/Temurin Dockerfile, a non-root Java process, and the existing test suite. No numerical formulas, OOP classes, UI components, or API behavior were rewritten.

## Public verification results

All checks below ran against the assigned public HTTPS URLs on 18 October 2026:

| Check | Result |
| --- | --- |
| Cloud Docker build and Maven verification | **67 tests passed**; zero failures, errors, or skips |
| Cloud frontend diagnostics and build | **0 Svelte errors/warnings**; Vite production build succeeded |
| Existing `scripts/verify-production.ps1` | **35 passed, 0 failed** |
| Existing Playwright suite, public base URL | **4 passed**: two tests each on desktop Chrome and mobile emulation |
| Additional public UI verification | **21 passed, 0 failed** |
| Runtime error-level logs queried after final deployment | No entries returned during the verification window |
| Desktop and 390px mobile screenshots | Captured and visually inspected; existing layout preserved |

Coverage included:

- Calculator; forward, backward, and central differences; all **18 supported formulas** and derivative orders **1–4** through the public proxy.
- All **12 method/order combinations** through browser controls using polynomials with known derivatives.
- Nine step-by-step solution entries, actual stencil samples, and **401 graph points**.
- Interactive graph sample hover, zoom, pan, and reset; graph rendering after resizing.
- Six first-derivative method-comparison rows and smallest-error highlighting.
- Absolute/percentage relative errors; zero-reference undefined relative error; absent-reference behavior.
- Step-size updates, domain validation, unsupported methods, malformed JSON, HTTP 400/404 handling.
- Calculator, comparison, learning, and history routes; theme persistence and local-history restore/clear.
- Mobile emulation and layout checks at **320px, 390px, and 768px**, with no horizontal page overflow or uncaught browser errors.

Required example:

```json
{"expression":"x^3 + 2*x","x":2,"h":0.1,"derivativeOrder":1,"method":"CENTRAL","accuracy":"O_H2","referenceValue":14}
```

The direct Java API and frontend proxy returned **14.010000000000016**, displayed as **14.01**. Absolute error was **0.010000000000015774**, and percentage relative error was **0.0714285714286841%**. The response contained nine solution steps, two stencil samples, and 401 graph points.

Repeat the checked-in public verification from the workspace root:

```powershell
.\scripts\verify-production.ps1 `
  -BaseUrl 'https://numerical-differentiation-web.onrender.com' `
  -JavaUrl 'https://numerical-differentiation-java.onrender.com'

. .\scripts\use-tools.ps1
Set-Location frontend
$env:PLAYWRIGHT_BASE_URL = 'https://numerical-differentiation-web.onrender.com'
npm.cmd run test:e2e
```

The additional one-time UI script, its JSON results, HTTP output, and public screenshots are in ignored `.tools/public-verification/`. The checked-in browser suite remains unchanged.

## Local preparation record

Before cloud creation on the same date, the existing deployment preparation had already verified:

- Maven `verify` and executable JAR packaging: **67 tests passed**.
- Frontend diagnostics: **zero errors and zero warnings**.
- The packaged Java JAR honored `PORT=18080` and `SERVER_ADDRESS=127.0.0.1`, returned `UP`, and returned the expected cubic result with nine solution steps. The temporary process was stopped afterward.
- A 54-file Git candidate list under 1 MB was reviewed without credential-pattern matches. Runtimes, dependencies, compiled output, local environment files, credentials, and the academic handoff archive were excluded.
- Portable Java 21, Maven 3.9.9, Node 22, and GitHub CLI were prepared under ignored `.tools`; GitHub CLI's download digest was checked. No credential values were requested or printed.

Docker was unavailable locally during preparation. The existing Dockerfile has now been successfully built and tested by Render. This cloud verification supersedes the earlier preparation-only deployment status.

For local checks:

```powershell
. .\scripts\use-tools.ps1
mvn.cmd -B -ntp -f backend\pom.xml verify
Set-Location frontend
npm.cmd run check
npm.cmd run build
```

Alternative-hosting assessment remains unchanged: Render preserves the Java/Node architecture. Railway trials were not activated; Sites' Workers runtime cannot run this Spring Boot process, and no framework migration was performed.

## Redeployment and Free-tier limitations

Commit and push reviewed changes to `main`, then manually deploy only the affected existing Render service. Auto-deploy remains disabled. Recheck the Hobby/no-card billing safeguard and `free` compute plan before configuration changes. Verify health and public behavior after each deployment. Documentation-only changes need no application rebuild.

The two services share **750 Free instance hours per calendar month**; running both continuously for a whole month exceeds that shared allowance. Free services sleep after 15 minutes of inactivity and may take around a minute to wake. Allow both services to wake before testing; cold backend startup can temporarily interrupt a frontend proxy request. Free services cannot receive private-network traffic, so `JAVA_API_URL` uses the public HTTPS backend. [Free instance limits](https://render.com/docs/free).

Memory is limited to 512 MB per Free instance. The initial Docker/Node builds and public tests passed within the selected configuration, but this is a hobby demonstration without an always-on or load-capacity guarantee. No paid resource is authorized to resolve future usage or memory limits.
