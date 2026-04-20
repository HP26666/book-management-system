import { defineStore } from 'pinia'
import { menuRoutes } from '../router/routes'
import { useUserStore } from './user'

export const usePermissionStore = defineStore('permission', {
  getters: {
    accessibleMenus() {
      const userStore = useUserStore()
      return menuRoutes.filter((route) => {
        const requiredRoles = route.meta?.requiredRoles
        if (!requiredRoles || requiredRoles.length === 0) {
          return true
        }
        return requiredRoles.some(role => userStore.roles.includes(role))
      })
    }
  },
  actions: {
    hasAnyRole(requiredRoles = []) {
      if (!requiredRoles || requiredRoles.length === 0) {
        return true
      }

      const userStore = useUserStore()
      return requiredRoles.some(role => userStore.roles.includes(role))
    },
    resolveHomePath() {
      const firstRoute = this.accessibleMenus[0]
      return firstRoute ? `/${firstRoute.path}` : '/dashboard'
    }
  }
})