<script lang="ts">
 import katex from 'katex';
 export let formula='';
 $: rendered=katex.renderToString(formula,{throwOnError:false,strict:false,trust:false});
</script>
<span class="math">{@html rendered}</span>
