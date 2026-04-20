import { createRouter, createWebHistory } from 'vue-router'
import { pinia } from '../store'
import { routes } from './routes'
import { usePermissionStore } from '../store/permission'
import { useUserStore } from '../store/user'

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore(pinia)
  const permissionStore = usePermissionStore(pinia)

  if (!userStore.initialized) {
    userStore.hydrate()
  }

  document.title = to.meta?.title
    ? `${to.meta.title} — 图书管理系统`
    : '图书管理系统'

  if (to.meta?.public) {
    if (to.path === '/login' && userStore.isAuthenticated) {
      try {
        if (!userStore.userInfo) {
          await userStore.fetchMe()
        }
        return permissionStore.resolveHomePath()
      } catch {
        userStore.clearSession()
      }
    }
    return true
  }

  if (!userStore.isAuthenticated) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  try {
    if (!userStore.userInfo) {
      await userStore.fetchMe()
    }
  } catch {
    userStore.clearSession()
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  const requiredRoles = to.meta?.requiredRoles
  if (!permissionStore.hasAnyRole(requiredRoles)) {
    return permissionStore.resolveHomePath()
  }

  return true
})

export default router
