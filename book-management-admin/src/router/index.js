import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'books',
        name: 'Books',
        component: () => import('@/views/book/index.vue'),
        meta: { title: '图书管理', icon: 'Reading' }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理', icon: 'Menu' }
      },
      {
        path: 'borrows',
        name: 'Borrows',
        component: () => import('@/views/borrow/index.vue'),
        meta: { title: '借阅管理', icon: 'Tickets' }
      },
      {
        path: 'reservations',
        name: 'Reservations',
        component: () => import('@/views/reservation/index.vue'),
        meta: { title: '预约管理', icon: 'AlarmClock' }
      },
      {
        path: 'readers',
        name: 'Readers',
        component: () => import('@/views/reader/index.vue'),
        meta: { title: '读者管理', icon: 'User' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: ['admin'] }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/role/index.vue'),
        meta: { title: '角色管理', icon: 'Lock', roles: ['admin'] }
      },
      {
        path: 'notices',
        name: 'Notices',
        component: () => import('@/views/notice/index.vue'),
        meta: { title: '公告管理', icon: 'Bell' }
      },
      {
        path: 'logs',
        name: 'OperationLogs',
        component: () => import('@/views/log/index.vue'),
        meta: { title: '操作日志', icon: 'Document', roles: ['admin'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', hidden: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '图书管理系统'} - 图书管理系统`
  if (to.meta.public) {
    next()
    return
  }
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    next('/login')
    return
  }
  if (to.meta.roles && !to.meta.roles.some(r => auth.roles.includes(r))) {
    next('/dashboard')
    return
  }
  next()
})

export default router
