<template>
  <div class="home">
    <header class="header">
      <h1>CodePilot Mini</h1>
      <button v-if="!loggedIn" class="btn btn-outline" @click="doWecomLogin">企微登录</button>
      <span v-else class="user">已登录</span>
    </header>

    <section class="entry">
      <div class="entry-row">
        <span class="entry-item">📸 拍照识码</span>
        <span class="entry-item">🎤 语音输入</span>
        <span class="entry-item">📋 粘贴错误</span>
      </div>
    </section>

    <section class="input-section">
      <textarea
        v-model="content"
        class="input"
        placeholder="请描述你的问题或粘贴代码/错误..."
        rows="4"
      />
      <div class="actions">
        <button class="btn btn-primary" :disabled="loading || !content.trim()" @click="submit">
          {{ loading ? '处理中...' : '提问' }}
        </button>
      </div>
    </section>

    <section class="examples">
      <p class="examples-title">示例</p>
      <button
        v-for="(ex, i) in examples"
        :key="i"
        class="example-btn"
        @click="content = ex"
      >
        {{ ex }}
      </button>
    </section>

    <section v-if="needDemoAuth" class="demo-auth">
      <p>本地演示需 Basic 认证，请输入后端配置的账号（如 demo / demo）：</p>
      <input v-model="demoUser" placeholder="用户名" />
      <input v-model="demoPass" type="password" placeholder="密码" />
      <button class="btn btn-outline" @click="setDemoAuth">确认</button>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ask, wecomLoginUrl, setDemoAuth as saveDemoAuth } from '../api';

const router = useRouter();
const content = ref('');
const loading = ref(false);
const loggedIn = ref(!!sessionStorage.getItem('codepilot_user') || !!sessionStorage.getItem('codepilot_token'));
const needDemoAuth = ref(false);
const demoUser = ref('demo');
const demoPass = ref('demo');

const examples = [
  '帮我写一个 Redis 缓存装饰器',
  '这个 NullPointerException 怎么修？',
  '解释这段 React Hook 代码',
];

function setDemoAuth() {
  saveDemoAuth(demoUser.value, demoPass.value);
  loggedIn.value = true;
  needDemoAuth.value = false;
}

async function doWecomLogin() {
  try {
    const url = await wecomLoginUrl();
    if (url) window.location.href = url;
  } catch (e) {
    needDemoAuth.value = true;
  }
}

async function submit() {
  if (!content.value.trim()) return;
  loading.value = true;
  try {
    await ask(content.value.trim(), null, 'paste');
    router.push({ name: 'Result' });
  } catch (e) {
    if (e.response?.status === 401) {
      needDemoAuth.value = true;
      loggedIn.value = false;
    } else {
      alert('请求失败: ' + (e.response?.data?.message || e.message));
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (!loggedIn.value) needDemoAuth.value = true;
});
</script>

<style scoped>
.home { max-width: 640px; margin: 0 auto; padding: 1.5rem; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.header h1 { font-size: 1.25rem; margin: 0; }
.entry { margin-bottom: 1rem; }
.entry-row { display: flex; gap: 1rem; flex-wrap: wrap; }
.entry-item { padding: 0.5rem 0.75rem; background: #fff; border-radius: 8px; font-size: 0.9rem; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.input-section { margin-bottom: 1.5rem; }
.input { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-size: 1rem; resize: vertical; }
.actions { margin-top: 0.75rem; }
.btn { padding: 0.5rem 1rem; border-radius: 8px; cursor: pointer; font-size: 0.95rem; border: none; }
.btn-primary { background: #1a73e8; color: #fff; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-outline { background: transparent; border: 1px solid #1a73e8; color: #1a73e8; }
.examples-title { font-size: 0.85rem; color: #666; margin-bottom: 0.5rem; }
.example-btn { display: block; width: 100%; text-align: left; padding: 0.6rem 0.75rem; margin-bottom: 0.5rem; background: #fff; border: 1px solid #eee; border-radius: 8px; cursor: pointer; font-size: 0.9rem; }
.example-btn:hover { background: #f5f5f5; }
.demo-auth { margin-top: 1.5rem; padding: 1rem; background: #fff3cd; border-radius: 8px; font-size: 0.9rem; }
.demo-auth input { margin-right: 0.5rem; margin-bottom: 0.5rem; padding: 0.35rem 0.5rem; }
</style>
