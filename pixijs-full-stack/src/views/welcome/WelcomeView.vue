<script setup>
import auth from '@/stores/auth'
</script>

<template>
  <div class="welcome-page">
    <div class="welcome-card">
      <div class="avatar">
        {{ (auth.username || '?')[0].toUpperCase() }}
      </div>
      <h1 class="welcome-title">欢迎回来，{{ auth.username || '用户' }}</h1>
      <p class="welcome-role">角色: {{ auth.role || 'user' }}</p>
      <p class="welcome-desc">请从左侧菜单选择功能模块</p>
    </div>
  </div>
</template>

<style scoped>
.welcome-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.welcome-card {
  text-align: center;
}
.avatar {
  width: 80px; height: 80px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; font-size: 36px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 20px;
}
.welcome-title { font-size: 24px; font-weight: 700; color: #333; margin-bottom: 8px; }
.welcome-role { font-size: 14px; color: #999; margin-bottom: 8px; }
.welcome-desc { font-size: 15px; color: #666; }
</style>
