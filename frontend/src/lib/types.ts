export interface Request { expression:string; x:number; h:number; derivativeOrder:number; method:string; accuracy:string; referenceValue:number|null }
export interface Formula { method:string; derivativeOrder:number; accuracy:string; accuracyOrder:number; offsets:number[]; coefficients:number[]; denominator:number; latex:string }
export interface Point {x:number;y:number|null}
export interface Result {request:Request; formula:Formula; approximation:number; samplePoints:{offset:number;x:number;value:number;coefficient:number;weightedValue:number}[]; steps:{title:string;explanation:string;latex:string}[];error:{referenceValue:number|null;absoluteError:number|null;relativeError:number|null;message:string}; warnings:string[]; graph:Point[];evaluationValue:number|null;tangent:Point[];secant:Point[]}
export interface Row {method:string;accuracy:string;result:Result|null;error:string|null}
export interface History {id:string; timestamp:string;request:Request;approximation:number}
export const fmt=(n:number|null|undefined)=>n==null?'Not available':new Intl.NumberFormat('en-US',{maximumSignificantDigits:12}).format(n);
export const methodName=(s:string)=>s.charAt(0)+s.slice(1).toLowerCase();
export const accuracyName=(s:string)=>s==='O_H'?'O(h)':s==='O_H2'?'O(h²)':'O(h⁴)';
export const defaults:Request={expression:'x^3 + 2*x',x:2,h:0.1,derivativeOrder:1,method:'CENTRAL',accuracy:'O_H2',referenceValue:14};
