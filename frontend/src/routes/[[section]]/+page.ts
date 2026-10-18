import { error } from '@sveltejs/kit';
export function load({params}:{params:{section?:string}}){if(params.section&&!['compare','learn','history'].includes(params.section))error(404,'Page not found');return {section:params.section||'calculator'};}
