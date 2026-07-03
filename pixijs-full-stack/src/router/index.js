import { createRouter, createWebHistory } from 'vue-router'
import { unauthorized } from '@/net'

const routes = [
  {
    path: '/',
    name: 'login',
    component: () => import('@/views/LoginView.vue')
  },
  {
    path: '/index',
    component: () => import('@/views/IndexView.vue'),
    children: [
      { path: '', name: 'welcome', component: () => import('@/views/welcome/WelcomeView.vue') },
      { path: 'books', name: 'books', component: () => import('@/views/welcome/BookManage.vue') },
      { path: 'borrows', name: 'borrows', component: () => import('@/views/welcome/BorrowManage.vue') },
      { path: 'students', name: 'students', component: () => import('@/views/welcome/StudentManage.vue') },
      {
        path: 'swagger',
        name: 'swagger',
        component: () => import('@/views/welcome/ExternalToolView.vue'),
        props: { url: 'http://localhost:8080/swagger-ui/index.html', title: 'Swagger API 文档' }
      },
      {
        path: 'rabbitmq',
        name: 'rabbitmq',
        component: () => import('@/views/welcome/ExternalToolView.vue'),
        props: { url: 'http://localhost:15672', title: 'RabbitMQ 管理控制台' }
      },
      {
        path: 'mailpit',
        name: 'mailpit',
        component: () => import('@/views/welcome/ExternalToolView.vue'),
        props: { url: 'http://localhost:8025', title: 'Mailpit 邮件服务器' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const isLoggedIn = !unauthorized()
  if (isLoggedIn && to.name === 'login') {
    return { name: 'welcome' }
  }
  if (!isLoggedIn && to.path.startsWith('/index')) {
    return { name: 'login' }
  }
})

export default router
