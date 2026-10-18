# Deployment preparation — strict $0 authorization

Status: **not published and not deployed**. No remote repository or cloud service has been created by this deployment task. No public website URL is verified. No payment, trial, subscription, credit purchase, payment-method entry, or billing activation is authorized.

## Repository and public URL

- Intended GitHub owner: `Janjira6804062617155` (confirmed through the connected GitHub account).
- Requested repository name: `numerical-differentiation-calculator`.
- GitHub repository URL: **not created/verified**. An accessible-repository search returned no matching repository; an authenticated exact-name check must precede creation.
- Public website/backend URLs: **not assigned/verified**.
- Public repository publication and the first public website deployment both require the user's explicit approval under the deployment brief. Local preparation does not constitute that approval.

## Hosting assessment

The candidate is **two explicitly Free Render web services**: a Docker-based Java 21 backend and a native Node SvelteKit frontend. Neither is a trial service. The blueprint declares `plan: free` for both and disables automatic deployment. No database, disk, private service, custom domain purchase, or paid add-on is required.

Before creating services, verify that the selected Render workspace is on a no-cost workspace plan, **has no payment method**, and the creation summary shows $0 for both services. Stop if Render requests a payment method, paid verification, upgrade, trial, credits, or any billable service. Existing payment methods must not be changed without authorization. If the billing state cannot be verified, do not create resources.

Render explicitly states that even Free instances can incur bandwidth/build overages when a payment method is attached. Without one, usage-limit exhaustion suspends services or disables builds instead. Therefore `plan: free` alone is not a guarantee of zero possible charges. Current official terms must be rechecked at execution time. [Free instances](https://render.com/docs/free), [billing FAQ](https://render.com/docs/faq), [pricing](https://render.com/pricing).

Render provides 750 shared Free instance hours per workspace per calendar month. Two running services consume that pool together; continuous operation of both for a whole month is not covered. Idle services sleep after 15 minutes and cold starts can take around a minute. This is appropriate for a university demonstration with interruptions, not guaranteed always-on hosting. Free backends cannot receive private-network requests, so the frontend must call the backend's assigned public HTTPS URL. [Free instance limitations](https://render.com/docs/free).

Railway was evaluated but not selected: its onboarding includes trial credits and its Free resource allowance is limited. No trial or account/resource creation was initiated. [Railway pricing](https://railway.com/pricing).

Sites hosting uses a Workers runtime and does not run this Spring Boot Java process. Replacing the backend or migrating the frontend framework is outside this task, so the existing Java/Node architecture is preserved.

## Prepared configuration

| Service | Configuration |
| --- | --- |
| Java backend | Dockerfile `backend/Dockerfile`; build context `backend`; Maven `verify`; Java 21 runtime; non-root process; Free plan |
| SvelteKit frontend | Root `frontend`; Node 22; `npm ci --include=dev && npm run check && npm run build`; start `node build`; Free plan |
| Backend binding | `server.port=${PORT:8080}`; `server.address=${SERVER_ADDRESS:0.0.0.0}` |
| Frontend binding | `HOST=0.0.0.0`; Render supplies `PORT` |
| Frontend → Java | `JAVA_API_URL`: actual assigned backend HTTPS origin, without a trailing slash |
| Frontend origin | `ORIGIN`: actual assigned frontend HTTPS origin |
| Java heap | `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=50.0`; memory suitability still needs cloud verification |

The Docker build runs the existing Java test suite instead of skipping tests. No numerical formulas, OOP classes, UI, or calculations were changed. Docker was not installed locally when this preparation started; the Docker image must not be reported as tested unless built successfully later. [Docker deployment](https://render.com/docs/docker), [SvelteKit Node deployment](https://render.com/docs/deploy-sveltekit), [port binding](https://render.com/docs/web-services), [blueprint reference](https://render.com/docs/blueprint-spec).

## Publication/deployment procedure

1. Review the local Git file list and secret scan. Keep `.tools`, dependencies, compiled output, logs, local environment files, credentials, and the duplicate academic handoff archive out of Git.
2. Authenticate GitHub CLI through official `gh auth login --web` if necessary. Never paste credentials into chat. A connected GitHub integration does not automatically authenticate a local CLI.
3. Check the exact owner/name for conflicts. After the user approves visibility/publication, create the requested repository with the supplied description, connect `origin`, and push `main` normally. Never overwrite another repository, force-push, or replace remote history.
4. Connect Render and verify account/workspace billing before resource creation. Obtain the user's first-public-deployment approval for the concrete two-Free-service configuration.
5. Create the backend using the exact Docker configuration and Free compute plan. Wait for the build and `/api/health` success; record its actual HTTPS URL.
6. Create/configure the frontend on Free with actual `JAVA_API_URL` and `ORIGIN`. Render-assigned URLs may need to be set after service creation and followed by a manual deploy. Do not assume a planned service name determines the final URL.
7. Verify public HTTP and browser behavior before updating this document and README with real repository/site URLs. Do not label guessed URLs as live.

If using the blueprint, remember that `sync: false` prompts for environment values during initial creation. Actual service URLs must be obtained and supplied correctly; manual creation of the two services is an alternative. No blueprint has been applied by preparing the file.

## Regression and public verification

From the workspace root:

```powershell
. .\scripts\use-tools.ps1
mvn.cmd -B -ntp -f backend\pom.xml verify
Set-Location frontend
npm.cmd run check
npm.cmd run build
```

After actual deployment, run the existing HTTP checks against the assigned URLs, allowing services to finish cold starting first:

```powershell
# Replace these variables with the actual verified service URLs.
.\scripts\verify-production.ps1 -BaseUrl $publicFrontendUrl -JavaUrl $publicJavaUrl
Set-Location frontend
$env:PLAYWRIGHT_BASE_URL = $publicFrontendUrl
npm.cmd run test:e2e
```

The public cubic example must return approximately 14.01 with nine solution steps and graph samples. Confirm all three methods, orders 1–4, comparisons, references/errors, local history, theme, and mobile layout. Backend logs/direct API comparison must confirm that calculations reach the deployed Java engine. Prior successful localhost tests are not proof of cloud deployment.

### Local checks completed during deployment preparation

On 18 October 2026, after changing only the backend port/binding configuration:

- Maven `verify` and executable JAR packaging succeeded: **67 tests passed, zero failures/errors/skips**.
- Frontend diagnostics succeeded: **zero errors and zero warnings**. Frontend source was unchanged, so its previously verified production build/browser results are not represented as newly rerun cloud tests.
- The packaged JAR started with `PORT=18080` and `SERVER_ADDRESS=127.0.0.1`; its health endpoint returned `UP` and its calculation endpoint returned **14.010000000000016**, with nine solution steps, for the cubic example. The temporary process was stopped afterward.
- The reviewed Git candidate list contained 54 files under 1 MB, with no credential-pattern matches. Exclusions were checked for runtimes, dependencies, compiled outputs, local environment files, and the original report handoff archive.
- GitHub CLI 2.102.0 was downloaded from its official release into ignored `.tools/gh-2.102.0` and verified against the official SHA-256 digest before use. `gh auth status` reported no authenticated GitHub host. No credential values were requested or printed.

Docker is not installed locally, and the container build and Render blueprint have not been executed. No public integration test has been performed because no cloud service has been created.

## Redeployment

Commit and push reviewed source changes to `main`, then manually deploy only the affected Render service. Automatic deployment is disabled to conserve build usage. Recheck environment URLs and health after each deploy, run the HTTP/browser checks, and record actual results. Recheck $0 billing safeguards before any service/plan change. Never upgrade or attach payment information to keep a service running when free limits are reached.

## Current blockers and limitations

- GitHub CLI is now installed at `.tools/gh-2.102.0/bin/gh.exe` but is not authenticated; the connected GitHub tools can inspect the account but expose no repository-creation operation in this session. Complete the official login with `& '.\.tools\gh-2.102.0\bin\gh.exe' auth login --hostname github.com --git-protocol https --web`. Do not paste tokens/passwords into chat.
- Render has been found as an available integration but is not confirmed installed/connected.
- Account-specific zero-cost eligibility, lack of payment method, service capacity, actual public URLs, and public integration results remain unverified.
- Public publication/deployment approvals remain outstanding.
- Free-tier sleep, shared runtime quota, cold-start errors during proxy requests, and 512 MB instance memory can affect the demonstration. No always-on availability guarantee is made.

Prepared files are not evidence of successful deployment. Total spending authorized: **$0**. No cloud resources or payments have been initiated.
