<script lang="ts">
 import {onMount} from 'svelte';
 import type {Result,Row} from './types';
 export let result:Result|null=null;
 export let rows:Row[]=[];
 export let dark=false;
 let element:HTMLDivElement;
 let plotly:typeof import('plotly.js')|null=null;
 let ready=false;
 let observer:ResizeObserver;
 onMount(()=>{let cancelled=false; (async()=>{const p=await import('plotly.js-dist-min');if(cancelled)return;plotly=p.default;ready=true;observer=new ResizeObserver(()=>plotly?.Plots.resize(element));observer.observe(element);})();return()=>{cancelled=true;observer?.disconnect();if(plotly)plotly.purge(element);};});
 $: if(ready&&plotly&&element) draw(result,rows,dark);
 function draw(r:Result|null,comparison:Row[],isDark:boolean){
  if(!plotly)return;
  let traces:any[]=[];
  let xTitle='x',yTitle='f(x)';
  if(comparison.length){const valid=comparison.filter(v=>v.result);const hasError=valid.some(v=>v.result?.error.absoluteError!=null);xTitle='Method · accuracy';yTitle=hasError?'Absolute error':'Approximation';traces=[{type:'bar',x:valid.map(v=>v.method+' '+v.accuracy.replace('O_H','h^')),y:valid.map(v=>hasError?v.result!.error.absoluteError:v.result!.approximation),marker:{color:['#3370ff','#739dff','#22b5bf','#8bd6d7','#7868dc','#b0a3f5']},name:yTitle}];}
  else if(r){traces=[{x:r.graph.map(p=>p.x),y:r.graph.map(p=>p.y),type:'scatter',mode:'lines',line:{color:'#3d73ff',width:3},name:'f(x)',connectgaps:false},{x:r.samplePoints.map(p=>p.x),y:r.samplePoints.map(p=>p.value),type:'scatter',mode:'markers',marker:{size:10,color:'#0bb5bd',line:{color:isDark?'#12213a':'white',width:2}},name:'Stencil samples'},{x:[r.request.x],y:[r.evaluationValue],type:'scatter',mode:'markers',marker:{size:12,color:'#f9ac4a',symbol:'diamond'},name:'Evaluation point'}];if(r.tangent.length)traces.push({x:r.tangent.map(p=>p.x),y:r.tangent.map(p=>p.y),mode:'lines',line:{color:'#a88ae5',dash:'dash',width:2},name:'Approximate tangent'});if(r.secant.length)traces.push({x:r.secant.map(p=>p.x),y:r.secant.map(p=>p.y),mode:'lines',line:{color:'#0bb5bd',width:2},name:'Secant'});}
  plotly.react(element,traces,{autosize:true,paper_bgcolor:'transparent',plot_bgcolor:'transparent',font:{family:'Segoe UI, sans-serif',color:isDark?'#b5c7e5':'#69788e',size:11},margin:{l:55,r:22,t:15,b:65},xaxis:{title:{text:xTitle},gridcolor:isDark?'#25334c':'#e9eef5',zerolinecolor:'#a7b6cc'},yaxis:{title:{text:yTitle},gridcolor:isDark?'#25334c':'#e9eef5',zerolinecolor:'#a7b6cc'},legend:{orientation:'h',y:-0.2,x:0},hovermode:'closest',showlegend:!comparison.length},{responsive:true,displaylogo:false,scrollZoom:true,modeBarButtonsToRemove:['lasso2d','select2d']});
 }
</script>
<div class="plot" bind:this={element} aria-label={rows.length?'Method comparison chart':'Function graph with stencil sample points'}></div>
<style>.plot{width:100%;height:370px;min-width:0}</style>
