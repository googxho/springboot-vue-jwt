<script setup>
import { onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { logout } from '@/net'
import auth, { loadAuthFromToken, clearAuth } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const loggingOut = ref(false)

onMounted(() => {
  loadAuthFromToken()
})

function handleLogout() {
  loggingOut.value = true
  logout(() => {
    clearAuth()
    router.push('/')
  }, () => {
    loggingOut.value = false
  })
}

const menuItems = [
  { path: '/index', icon: '🏠', label: '首页' },
  { path: '/index/books', icon: '📚', label: '图书管理' },
  { path: '/index/borrows', icon: '📋', label: '借阅管理' },
  { path: '/index/students', icon: '👨‍🎓', label: '学生管理' },
  { type: 'divider' },
  { path: '/index/swagger', icon: '📖', label: 'Swagger 文档' },
  { path: '/index/rabbitmq', icon: '🐰', label: 'RabbitMQ' },
  { path: '/index/mailpit', icon: '📧', label: 'Mailpit' }
]

function isActive(path) {
  if (path === '/index') return route.path === '/index'
  return route.path.startsWith(path)
}

function navigate(path) {
  router.push(path)
}
</script>

<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">📖</div>
        <span class="logo-text">图书管理系统</span>
      </div>
      <nav class="sidebar-nav">
        <template v-for="item in menuItems" :key="item.path || item.type">
          <div v-if="item.type === 'divider'" class="nav-divider" />
          <div
            v-else
            class="nav-item"
            :class="{ active: isActive(item.path) }"
            @click="navigate(item.path)"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </template>
      </nav>
      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">{{ (auth.username || '?')[0].toUpperCase() }}</div>
          <div class="user-detail">
            <div class="user-name">{{ auth.username || '用户' }}</div>
            <div class="user-role">{{ auth.role || 'user' }}</div>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout" :disabled="loggingOut">
          {{ loggingOut ? '退出中...' : '退出' }}
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  width: 100%;
  height: 100%;
  display: flex;
  background: #f5f6fa;
}

.sidebar {
  width: 220px;
  height: 100%;
  background: #fff;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px;
  border-bottom: 1px solid #f0f0f0;
}

.logo {
  font-size: 28px;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;
  font-size: 14px;
}

.nav-item:hover {
  background: #f5f6fa;
  color: #333;
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.nav-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.nav-label {
  font-weight: 500;
}

.nav-divider {
  height: 1px;
  background: #eee;
  margin: 6px 8px;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-detail {
  overflow: hidden;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-size: 11px;
  color: #999;
}

.logout-btn {
  width: 100%;
  padding: 8px;
  background: #fff0f0;
  color: #e74c3c;
  border: 1px solid #ffd4d4;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #ffe8e8;
}

.logout-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.main-content {
  flex: 1;
  height: 100%;
  overflow: hidden;
  background: #f5f6fa;
}
</style>
