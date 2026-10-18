<script lang="ts">
 import {onMount} from 'svelte';
 import MathView from '$lib/Math.svelte';
 import Plot from '$lib/Plot.svelte';
 import {defaults,fmt,methodName,accuracyName,type Request,type Result,type Formula,type Row,type History} from '$lib/types';
 export let data:{section:string};
 let form:Request={...defaults};
 let formulas:Formula[]=[];
 let result:Result|null=null;
 let rows:Row[]=[];
 let history:History[]=[];
 let dark=false,busy=false,online=false;
 let message='',storageMessage='';
 let controller:AbortController|undefined;
 let revision=0;
 let timer:ReturnType<typeof setTimeout>;
 let hydrated=false;
 $: section=data.section;
 $: available=formulas.filter(f=>f.method===form.method&&f.derivativeOrder===form.derivativeOrder);
 $: normalizeAccuracy(available);
 $: if(hydrated&&dark!==undefined){document.documentElement.dataset.theme=dark?'dark':'light';try{localStorage.setItem('nd-theme',dark?'dark':'light');}catch{}}
 $: best=rows.reduce((minimum,row)=>row.result?.error.absoluteError!=null?Math.min(minimum,row.result.error.absoluteError):minimum,Infinity);
 $: preview='f(x) = '+form.expression.replaceAll('*',' \\cdot ').replace(/\^([0-9]+)/g,'^{$1}').replace(/\b(sin|cos|tan|ln|exp)\b/g,'\\$1');
 onMount(()=>{
  try{dark=localStorage.getItem('nd-theme')==='dark';const saved=JSON.parse(localStorage.getItem('nd-history')||'[]');history=Array.isArray(saved)?saved.filter(validHistory).slice(0,30):[];}catch{storageMessage='Browser history is unavailable.';}
  hydrated=true;
  (async()=>{try{const response=await fetch('/api/differentiation/methods');if(!response.ok)throw Error('Java backend is unavailable. Start the backend, then reload this page.');formulas=await response.json();online=true;await calculate(false);}catch(e){message=(e as Error).message;}})();
  return()=>{controller?.abort();clearTimeout(timer);};
 });
 function validHistory(v:any):v is History {const r=v?.request;return typeof v?.id==='string'&&typeof v.timestamp==='string'&&Number.isFinite(v.approximation)&&r&&typeof r.expression==='string'&&r.expression.length<=200&&Number.isFinite(r.x)&&Math.abs(r.x)<=1e8&&Number.isFinite(r.h)&&r.h>0&&r.h<=1e4&&[1,2,3,4].includes(r.derivativeOrder)&&['FORWARD','BACKWARD','CENTRAL'].includes(r.method)&&['O_H','O_H2','O_H4'].includes(r.accuracy)&&(r.referenceValue===null||Number.isFinite(r.referenceValue));}
 function saveHistory(){try{localStorage.setItem('nd-history',JSON.stringify(history));}catch{storageMessage='Could not save browser history.';}}
 function normalizeAccuracy(options:Formula[]){if(options.length&&!options.some(f=>f.accuracy===form.accuracy))form.accuracy=options[0].accuracy;}
 function invalidate(){revision++;controller?.abort();busy=false;result=null;rows=[];message='';}
 function changed(){invalidate();clearTimeout(timer);}
 function stepChanged(){changed();timer=setTimeout(()=>calculate(false),400);}
 async function calculate(record=true){
  clearTimeout(timer);controller?.abort();controller=new AbortController();const current=++revision;result=null;rows=[];message='';busy=true;
  const snapshot={...form};
  try{
   if(!snapshot.expression.trim()||!Number.isFinite(snapshot.x)||!Number.isFinite(snapshot.h)||snapshot.h<=0||(snapshot.referenceValue!==null&&!Number.isFinite(snapshot.referenceValue)))throw Error('Enter a function, a finite x, and a positive finite h.');
   const response=await fetch('/api/differentiation/'+(section==='compare'?'compare':'calculate'),{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(snapshot),signal:controller.signal});
   const payload=await response.json();if(!response.ok)throw Error(payload.message||'Calculation failed.');if(current!==revision)return;
   online=true;
   if(section==='compare')rows=payload;else{result=payload;if(record){history=[{id:crypto.randomUUID(),timestamp:new Date().toISOString(),request:snapshot,approximation:payload.approximation},...history].slice(0,30);saveHistory();}}
  }catch(e){if((e as Error).name!=='AbortError'&&current===revision)message=(e as Error).message==='Failed to fetch'?'Cannot connect to the Java backend. Check that it is running.':(e as Error).message;}finally{if(current===revision)busy=false;}
 }
 function example(name:string){changed();form=name==='cubic'?{...defaults}:name==='sine'?{...defaults,expression:'sin(x)',x:0.5,h:0.01,referenceValue:0.8775825618903728}:name==='exponential'?{...defaults,expression:'exp(x)',x:1,h:0.01,referenceValue:2.718281828459045}:{...defaults,expression:'x^4',x:2,h:0.1,derivativeOrder:2,referenceValue:48};}
 function restore(item:History){changed();form={...item.request};}
 const lessons=[
  {title:'A derivative, from nearby values',tag:'01 · THE IDEA',text:'Numerical differentiation estimates how a function changes by evaluating it at nearby points. It is useful when a symbolic derivative is difficult to obtain or the function is available only through evaluations.',formula:"f'(x)=\\lim_{h\\to0}\\frac{f(x+h)-f(x)}h"},
  {title:'Forward difference',tag:'02 · LOOK AHEAD',text:'Use the current point and a point to its right. This is useful near a left boundary, where earlier values are unavailable. The basic stencil has first-order truncation error.',formula:"f'(x)\\approx\\frac{f(x+h)-f(x)}h"},
  {title:'Backward difference',tag:'03 · LOOK BEHIND',text:'Use the current point and a point to its left. It is useful at a right boundary or when only past values are available. The basic stencil also has first-order truncation error.',formula:"f'(x)\\approx\\frac{f(x)-f(x-h)}h"},
  {title:'Central difference',tag:'04 · BOTH SIDES',text:'Balance samples on both sides of x. Symmetry cancels the first-order error term, giving second-order truncation error for a sufficiently smooth function. Both sides must lie in the domain.',formula:"f'(x)\\approx\\frac{f(x+h)-f(x-h)}{2h}"},
  {title:'Choosing the step size',tag:'05 · FIND THE BALANCE',text:'h is the spacing between stencil points. A smaller h usually reduces truncation error, but subtraction of nearly equal floating-point values increases round-off error. Try several h values and look for a stable result.',formula:'E(h)\\approx C_1h^p+C_2\\varepsilon/h^m'},
  {title:'Beyond the first derivative',tag:'06 · DERIVATIVE ORDERS',text:'The first derivative describes slope; the second describes change in slope. Third and fourth derivatives describe further rates of change. Each needs its own coefficients and an h raised to the derivative order in the denominator.',formula:"f''(x)\\approx\\frac{f(x-h)-2f(x)+f(x+h)}{h^2}"},
  {title:'Reading accuracy and error',tag:'07 · INTERPRET THE RESULT',text:'O(h²) describes the asymptotic truncation rate, not a guaranteed number of correct digits. For smooth functions in the truncation-dominated regime, halving h reduces this error by about four. Verified error needs a trustworthy reference.',formula:'E_{abs}=|D-R|,\\qquad E_{rel}=\\frac{|D-R|}{|R|}\\times100\\%'},
  {title:'A worked example',tag:'08 · TRY IT YOURSELF',text:'For f(x) = x³ + 2x at x = 2 and h = 0.1, central difference gives (13.461 − 10.659) / 0.2 = 14.01. The analytic derivative is 3x² + 2 = 14, so the absolute error is 0.01. Different stencils keep different Taylor terms, which explains their different results.',formula:"f'(2)\\approx14.01,\\qquad f'(2)=14"}
 ];
</script>

<svelte:head><title>{section==='compare'?'Compare Methods':section==='learn'?'Learn':section==='history'?'History':'Calculator'} · Numerical Differentiation</title><meta name="description" content="Explore numerical derivatives with a Java-powered finite difference calculator, interactive graphs, and transparent worked solutions."/></svelte:head>

<div class="app-shell">
 <aside class="sidebar">
  <a class="brand" href="/" aria-label="Numerical Differentiation home"><span class="brand-symbol">∂</span><span>Numerical<span class="brand-sub">DIFFERENTIATION</span></span></a>
  <div class="nav-caption">WORKSPACE</div>
  <nav aria-label="Main navigation">
   {#each [{url:'/',id:'calculator',icon:'▦',label:'Calculator'},{url:'/compare',id:'compare',icon:'⇄',label:'Compare methods'},{url:'/learn',id:'learn',icon:'▤',label:'Learn'},{url:'/history',id:'history',icon:'◷',label:'History'}] as link}
    <a href={link.url} class:active={section===link.id} aria-current={section===link.id?'page':undefined} on:click={changed}><span class="nav-icon">{link.icon}</span>{link.label}{#if section===link.id}<span class="active-dot"></span>{/if}</a>
   {/each}
  </nav>
  <div class="sidebar-note"><span class="tiny-label">SMALL STEPS. BIG INSIGHTS.</span><div class="side-equation">f′(x) ≈ <span>Δf / Δx</span></div><p>Explore the mathematics<br/>behind the change.</p><a href="/learn">Explore the guide <span>↗</span></a></div>
  <div class="sidebar-bottom"><span class:connected={online} class="status-dot"></span>{online?'Java engine connected':'Connecting to Java…'}<small>LOCAL WORKSPACE · v1.0</small></div>
 </aside>
 <div class="main-shell">
  <header class="topbar"><div><span class="breadcrumb">Workspace</span><span class="slash">/</span>{section==='calculator'?'Calculator':section==='compare'?'Compare methods':section==='learn'?'Learn':'History'}</div><div class="topbar-actions"><span class="engine-badge">JAVA POWERED</span><button class="theme-button" on:click={()=>dark=!dark} aria-label="Toggle dark mode">{dark?'☀':'☾'}</button></div></header>
  <main>
   <div class="page-heading"><div><div class="eyebrow">THE MATHEMATICS OF CHANGE</div><h1>{section==='calculator'?'Numerical differentiation':section==='compare'?'One function. Every method.':section==='learn'?'Understand every step.':'Your calculation notebook.'}</h1><p>{section==='calculator'?'From a function to an insight. Calculate, visualize, and understand derivatives.':section==='compare'?'Compare supported stencils with a single calculation from the Java engine.':section==='learn'?'A practical guide to finite differences, accuracy, and the space between points.':'Recent calculations, saved in this browser. Pick up where you left off.'}</p></div>{#if section==='calculator'}<a class="text-link" href="/learn">How it works ↗</a>{/if}</div>
   {#if storageMessage}<div class="notice">{storageMessage}</div>{/if}
   {#if section==='calculator'||section==='compare'}
    <div class="workspace-grid">
     <section class="card input-card">
      <div class="card-heading"><div><span class="section-number">01</span><h2>Define your calculation</h2></div><button class="quiet" on:click={()=>{changed();form={...defaults};}}>↺ Reset</button></div>
      <form on:submit|preventDefault={()=>calculate()}>
       <label for="expression">Function <span class="serif">f(x)</span></label><div class="expression-input"><span>f(x) =</span><input id="expression" bind:value={form.expression} on:input={()=>{changed();form.referenceValue=null;}} placeholder="Example: x^3 + 2*x" maxlength="200" required autocomplete="off"/></div>
       <div class="formula-preview"><MathView formula={preview}/></div>
       <details class="syntax"><summary>Expression syntax</summary><p>Use + − * / ^ and parentheses, with explicit multiplication (2*x). Functions: sin, cos, tan, exp, ln, sqrt. Constants: pi, e. Trigonometric inputs use radians. Maximum 200 characters; graph sampling is bounded.</p></details>
       <div class="field-pair"><div><label for="order">Derivative order</label><select id="order" bind:value={form.derivativeOrder} on:change={()=>{changed();form.referenceValue=null;}}>{#each [1,2,3,4] as n}<option value={n}>{n}{n===1?'st':n===2?'nd':n===3?'rd':'th'} derivative</option>{/each}</select></div><div><label for="accuracy">Accuracy</label><select id="accuracy" bind:value={form.accuracy} on:change={changed} disabled={section==='compare'}>{#if section==='compare'}<option>All supported</option>{:else}{#each available as f}<option value={f.accuracy}>{accuracyName(f.accuracy)}</option>{/each}{/if}</select></div></div>
       {#if section==='calculator'}<label for="method-forward">Difference method</label><div class="method-picker">{#each ['FORWARD','BACKWARD','CENTRAL'] as method}<button id={'method-'+method.toLowerCase()} type="button" class:selected={form.method===method} on:click={()=>{changed();form.method=method;}}><span>{method==='FORWARD'?'→':method==='BACKWARD'?'←':'↔'}</span>{methodName(method)}</button>{/each}</div>{:else}<div class="info-strip">All available methods and accuracy levels will be compared.</div>{/if}
       <div class="field-pair parameters"><div><label for="point">Evaluation point <i>x</i></label><input id="point" type="number" step="any" bind:value={form.x} on:input={()=>{changed();form.referenceValue=null;}} required min="-100000000" max="100000000"/></div><div><label for="step">Step size <i>h</i></label><input id="step" type="number" step="any" bind:value={form.h} on:input={stepChanged} required min="0.0000000000000001" max="10000"/></div></div>
       <div class="slider-row"><span>Explore h</span><input aria-label="Adjust step size" type="range" min="-5" max="0" step="0.1" value={Math.log10(form.h||0.1)} on:input={(e)=>{form.h=Number(Math.pow(10,Number(e.currentTarget.value)).toPrecision(6));stepChanged();}}/><span>{fmt(form.h)}</span></div>
       <label for="reference">Reference derivative value <span class="optional">optional</span></label><input id="reference" type="number" step="any" value={form.referenceValue??''} on:input={(e)=>{changed();form.referenceValue=e.currentTarget.value===''?null:Number(e.currentTarget.value);}} placeholder="Add a trustworthy reference for error analysis"/><p class="field-hint">Examples include a verified analytic reference. Clear it when changing the function.</p>
       <button class="primary" type="submit" disabled={busy||!formulas.length}>{busy?'Calculating…':section==='compare'?'Compare methods':'Calculate derivative'}<span>→</span></button>
      </form>
      <div class="example-section"><span class="tiny-label">TRY AN EXAMPLE</span><div class="example-chips"><button on:click={()=>example('cubic')}>x³ + 2x</button><button on:click={()=>example('sine')}>sin(x)</button><button on:click={()=>example('exponential')}>eˣ</button><button on:click={()=>example('quartic')}>x⁴ · 2nd derivative</button></div></div>
     </section>
     <div class="results-column" aria-live="polite" aria-busy={busy}>
      {#if message}<div class="error" role="alert"><strong>Check your calculation</strong><p>{message}</p></div>{/if}
      {#if section==='calculator'}
       <section class="result-card"><div class="result-top"><span class="tiny-label">DERIVATIVE APPROXIMATION</span><span class="result-tag">{result?accuracyName(result.request.accuracy):'READY WHEN YOU ARE'}</span></div><div class="answer">{busy?'Computing…':result?fmt(result.approximation):'—'}</div><div class="answer-caption">{#if result}<MathView formula={'f^{('+result.request.derivativeOrder+')}('+result.request.x+')'}/><span>·</span>{methodName(result.request.method)} difference<span>·</span>h = {fmt(result.request.h)}{:else}Define a function and calculate its derivative.{/if}</div><div class="error-metrics"><div><span>Reference value</span><strong>{fmt(result?.error.referenceValue)}</strong></div><div><span>Absolute error</span><strong>{fmt(result?.error.absoluteError)}</strong></div><div><span>Relative error</span><strong>{result?.error.referenceValue===0?'Undefined':result?.error.relativeError!=null?fmt(result.error.relativeError)+'%':'Not available'}</strong></div></div></section>
       <section class="card graph-card"><div class="card-heading"><div><span class="section-number">02</span><h2>See the difference</h2></div><span class="subtle-badge">INTERACTIVE GRAPH</span></div>{#if result}<Plot {result} {dark}/><div class="graph-footnote">{result.request.derivativeOrder===1?'A local view of the function, samples, and approximate tangent.':'Sample locations show the higher-order stencil; the result is not a slope.'} Scroll to zoom · drag to pan · double-click to reset</div>{:else}<div class="empty-graph"><span>∂</span><p>{busy?'Sampling the function in Java…':'Your function graph will appear here.'}</p></div>{/if}</section>
      {:else}
       <section class="card comparison-card"><div class="card-heading"><h2>Method comparison</h2><span class="subtle-badge">JAVA RESULTS</span></div>{#if rows.length}<div class="table-scroll"><table><thead><tr><th>Method</th><th>Accuracy</th><th>Approximation</th><th>Abs. error</th><th>Rel. error</th></tr></thead><tbody>{#each rows as row}<tr class:best={row.result?.error.absoluteError!=null&&row.result.error.absoluteError===best}><td>{methodName(row.method)}{#if row.result?.error.absoluteError!=null&&row.result.error.absoluteError===best}<span class="best-label"> Smallest error</span>{/if}</td><td>{accuracyName(row.accuracy)}</td><td>{row.error||fmt(row.result?.approximation)}</td><td>{fmt(row.result?.error.absoluteError)}</td><td>{row.result?.error.referenceValue===0?'Undefined':row.result?.error.relativeError!=null?fmt(row.result.error.relativeError)+'%':'—'}</td></tr>{/each}</tbody></table></div><Plot {rows} {dark}/><p class="graph-footnote">{Number.isFinite(best)?'Highlighted rows have the smallest error against the supplied reference.':'No reference supplied: values are compared without accuracy rankings.'}</p>{:else}<div class="empty-graph"><span>⇄</span><p>{busy?'Comparing the supported stencils…':'Run a comparison to inspect each supported formula.'}</p></div>{/if}</section>
      {/if}
     </div>
    </div>
    {#if result&&section==='calculator'}
     {#each result.warnings as warning}<div class="notice">{warning}</div>{/each}
     <div class="detail-grid"><section class="card steps-card"><div class="card-heading"><div><span class="section-number">03</span><h2>Every step, explained</h2></div><span class="subtle-badge">FROM THE JAVA ENGINE</span></div><div class="steps">{#each result.steps as step,i}<div class="step"><span class="step-index">{String(i+1).padStart(2,'0')}</span><div><h3>{step.title}</h3><p>{step.explanation}</p>{#if step.latex}<div class="step-math"><MathView formula={step.latex}/></div>{/if}</div></div>{/each}</div></section><section class="card samples-card"><div class="card-heading"><h2>The finite difference stencil</h2></div><p class="muted">These are the exact samples used in the calculation.</p><div class="table-scroll"><table><thead><tr><th>Offset</th><th>x</th><th>f(x)</th><th>Weight</th></tr></thead><tbody>{#each result.samplePoints as point}<tr><td>{point.offset}h</td><td>{fmt(point.x)}</td><td>{fmt(point.value)}</td><td>{point.coefficient}</td></tr>{/each}</tbody></table></div><div class="insight"><span>◎</span><h3>Precision has a balance.</h3><p>A smaller step reduces truncation error, but very small steps can amplify round-off. Try adjusting h and watch the result.</p><a href="/learn">Understand the trade-off →</a></div><p class="field-hint">{result.error.message}</p></section></div>
    {/if}
   {:else if section==='learn'}
    <div class="learning-intro"><div><span class="tiny-label">A CLOSER LOOK AT CHANGE</span><h2>Small distances.<br/>Meaningful approximations.</h2><p>A finite difference turns nearby function values into an estimate of a derivative. The stencil you choose determines which points to sample.</p><a href="/" class="light-button">Open the calculator →</a></div><div class="learning-art" aria-hidden="true"><span>f(x + h)</span><div class="curve">⌁</div><span class="art-formula">Δy / Δx → f′(x)</span></div></div><div class="lesson-grid">{#each lessons as lesson}<article class="card lesson"><span class="tiny-label">{lesson.tag}</span><h2>{lesson.title}</h2><p>{lesson.text}</p><div class="lesson-formula"><MathView formula={lesson.formula}/></div></article>{/each}</div>
   {:else if section==='history'}
    <section class="card history-card"><div class="card-heading"><h2>Recent calculations <span class="count">{history.length} / 30</span></h2><button class="quiet" on:click={()=>{history=[];saveHistory();}}>Clear all history</button></div>{#if history.length}<div class="history-list">{#each history as item}<article><div><strong>{item.request.expression}</strong><p>{methodName(item.request.method)} · derivative {item.request.derivativeOrder} · {accuracyName(item.request.accuracy)} · x = {item.request.x} · h = {item.request.h}</p><time>{new Date(item.timestamp).toLocaleString()}</time></div><div class="history-value">{fmt(item.approximation)}</div><a href="/" class="secondary" on:click={()=>restore(item)}>Restore</a><button class="quiet" aria-label={'Delete calculation '+item.request.expression} on:click={()=>{history=history.filter(h=>h.id!==item.id);saveHistory();}}>Delete</button></article>{/each}</div>{:else}<div class="empty-graph"><span>◷</span><p>Your calculations will be saved here, in this browser.</p><a href="/">Make your first calculation →</a></div>{/if}</section>
   {/if}
   <footer><span>Numerical Differentiation Calculator Website</span><span>Java 21 engine <span class="footer-dot">·</span> Built for understanding</span></footer>
  </main>
 </div>
</div>
