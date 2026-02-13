<template>
  <div class="result">
    <header class="header">
      <button class="back" @click="goBack">← 返回</button>
      <h1>结果</h1>
    </header>

    <template v-if="response">
      <section class="block">
        <h2>✅ 问题分析</h2>
        <p class="text">{{ response.analysis }}</p>
      </section>
      <section class="block">
        <h2>💡 建议</h2>
        <p class="text">{{ response.suggestion }}</p>
      </section>
      <section v-if="response.code" class="block">
        <h2>💻 修复代码</h2>
        <pre class="code"><code>{{ response.code }}</code></pre>
      </section>
      <section v-if="response.docRefs?.length" class="block">
        <h2>📚 相关文档</h2>
        <ul class="doc-list">
          <li v-for="(doc, i) in response.docRefs" :key="i">
            《{{ doc.title }}》
            <a v-if="doc.link" :href="doc.link" target="_blank" rel="noopener">链接</a>
          </li>
        </ul>
      </section>
      <section class="footer">
        <span class="tag">请求类型: {{ response.requestType || 'public' }}</span>
        <button v-if="response.cardUrl" class="btn btn-outline">分享为卡片 → 企微</button>
      </section>
    </template>

    <p v-else class="empty">暂无结果，请从首页发起提问。</p>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { getLastResponse } from '../api';

const router = useRouter();
const response = computed(() => getLastResponse());
const goBack = () => router.push({ name: 'Home' });
</script>

<style scoped>
.result { max-width: 720px; margin: 0 auto; padding: 1.5rem; }
.header { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
.back { background: none; border: none; cursor: pointer; font-size: 1rem; color: #1a73e8; }
.header h1 { font-size: 1.25rem; margin: 0; }
.block { background: #fff; border-radius: 8px; padding: 1rem; margin-bottom: 1rem; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.block h2 { font-size: 1rem; margin: 0 0 0.5rem 0; color: #333; }
.text { margin: 0; white-space: pre-wrap; word-break: break-word; font-size: 0.95rem; line-height: 1.5; }
.code { margin: 0; padding: 1rem; background: #1e1e1e; color: #d4d4d4; border-radius: 6px; overflow-x: auto; font-size: 0.9rem; }
.doc-list { margin: 0; padding-left: 1.25rem; }
.doc-list a { margin-left: 0.5rem; color: #1a73e8; }
.footer { display: flex; align-items: center; gap: 1rem; margin-top: 1rem; }
.tag { font-size: 0.85rem; color: #666; }
.btn-outline { padding: 0.4rem 0.8rem; border: 1px solid #1a73e8; color: #1a73e8; background: transparent; border-radius: 6px; cursor: pointer; font-size: 0.9rem; }
.empty { color: #666; }
</style>
