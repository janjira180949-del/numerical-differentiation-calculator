param([string]$BaseUrl = 'http://127.0.0.1:3000', [string]$JavaUrl = 'http://127.0.0.1:8080')
$ErrorActionPreference = 'Stop'
$script:passed = 0
function Assert-Check([bool]$Condition, [string]$Name) {
    if (-not $Condition) { throw "FAILED: $Name" }
    $script:passed++
    Write-Output "PASS: $Name"
}
function Post-Calculation($Body, [string]$Url = $BaseUrl) {
    Invoke-RestMethod "$Url/api/differentiation/calculate" -Method Post -ContentType 'application/json' -Headers @{ Origin = $BaseUrl } -Body ($Body | ConvertTo-Json)
}
function Request-Status([string]$Path, [string]$Body = '') {
    try {
        if ($Body) { return [int](Invoke-WebRequest "$BaseUrl$Path" -UseBasicParsing -Method Post -ContentType 'application/json' -Body $Body).StatusCode }
        return [int](Invoke-WebRequest "$BaseUrl$Path" -UseBasicParsing).StatusCode
    } catch {
        if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
        throw
    }
}
$health = Invoke-RestMethod "$BaseUrl/api/health"
Assert-Check ($health.status -eq 'UP') 'Production proxy reaches Java health endpoint'
$formulas = Invoke-RestMethod "$BaseUrl/api/differentiation/methods"
Assert-Check ($formulas.Count -eq 18) 'Production proxy exposes all 18 registered formulas'
$body = @{ expression = 'x^3 + 2*x'; x = 2; h = 0.1; derivativeOrder = 1; method = 'CENTRAL'; accuracy = 'O_H2'; referenceValue = 14 }
$result = Post-Calculation $body
$direct = Post-Calculation $body $JavaUrl
Assert-Check ([Math]::Abs($result.approximation - 14.01) -lt 1e-10) 'Required cubic result is 14.01'
Assert-Check (($result | ConvertTo-Json -Depth 30 -Compress) -eq ($direct | ConvertTo-Json -Depth 30 -Compress)) 'Full production calculation payload matches direct Java response'
Assert-Check ([Math]::Abs($result.error.absoluteError - 0.01) -lt 1e-10 -and [Math]::Abs($result.error.relativeError - (0.01 / 14 * 100)) -lt 1e-10) 'Java absolute and percentage relative errors are correct'
Assert-Check ($result.steps.Count -eq 9 -and $result.samplePoints.Count -eq 2 -and $result.graph.Count -eq 401) 'Production response includes actual steps, samples, and graph'
foreach ($formula in $formulas) {
    $order = $formula.derivativeOrder
    $expected = 1
    for ($i = 2; $i -le $order; $i++) { $expected *= $i }
    $stencilRequest = @{ expression = "x^$order"; x = 2; h = 0.1; derivativeOrder = $order; method = $formula.method; accuracy = $formula.accuracy; referenceValue = $expected }
    $stencilResult = Post-Calculation $stencilRequest
    Assert-Check ([Math]::Abs($stencilResult.approximation - $expected) -lt 1e-7) "$($formula.method) derivative $order $($formula.accuracy) through production proxy"
}
$comparison = Invoke-RestMethod "$BaseUrl/api/differentiation/compare" -Method Post -ContentType 'application/json' -Body ($body | ConvertTo-Json)
Assert-Check ($comparison.Count -eq 6 -and @($comparison | Where-Object { $null -eq $_.result }).Count -eq 0) 'All six first-derivative comparison rows succeed'
$body.expression = 'x^2'; $body.x = 0; $body.referenceValue = 0
$zero = Post-Calculation $body
Assert-Check ($null -eq $zero.error.relativeError -and $zero.error.message -eq 'Relative error is undefined.') 'Zero-reference relative error remains undefined'
$body.referenceValue = $null
$missing = Post-Calculation $body
Assert-Check ($null -eq $missing.error.absoluteError) 'Missing reference produces no fabricated error'
$body.method = 'UNKNOWN'
Assert-Check ((Request-Status '/api/differentiation/calculate' ($body | ConvertTo-Json)) -eq 400) 'Unsupported method preserves HTTP 400 through proxy'
$body.method = 'CENTRAL'; $body.expression = 'sqrt(x)'; $body.x = -2
Assert-Check ((Request-Status '/api/differentiation/calculate' ($body | ConvertTo-Json)) -eq 400) 'Invalid domain preserves HTTP 400 through proxy'
Assert-Check ((Request-Status '/api/differentiation/calculate' '{bad}') -eq 400) 'Malformed JSON preserves HTTP 400 through proxy'
foreach ($path in @('/', '/compare', '/learn', '/history')) { Assert-Check ((Request-Status $path) -eq 200) "Production route $path loads" }
Assert-Check ((Request-Status '/unknown-page') -eq 404) 'Unknown production page returns HTTP 404'
Write-Output "Production verification: $script:passed passed, 0 failed."
