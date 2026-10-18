import {env} from '$env/dynamic/private';
import type {RequestHandler} from './$types';
const proxy:RequestHandler=async({request,params})=>{
 const target=(env.JAVA_API_URL||'http://127.0.0.1:8080')+'/api/'+params.path;
 const response=await fetch(target,{method:request.method,headers:{'Content-Type':'application/json'},body:request.method==='POST'?await request.text():undefined});
 return new Response(response.body,{status:response.status,headers:{'Content-Type':response.headers.get('Content-Type')||'application/json'}});
};
export const GET=proxy;
export const POST=proxy;
