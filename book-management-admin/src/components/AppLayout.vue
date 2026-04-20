<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': collapsed }">
    <aside class="sidebar" :class="{ 'sidebar--mobile-open': mobileMenuOpen }" role="navigation" aria-label="主导航">
      <div class="sidebar__logo" aria-label="图书管理系统">
        <el-icon class="sidebar__logo-icon" :size="28" color="#0f766e">
          <Reading />
        </el-icon>
        <Transition name="label-fade">
          <span v-if="!collapsed" class="sidebar__logo-text">图书管理</span>
        </Transition>
      </div>

      <nav class="sidebar__nav">
        <el-menu
          :default-active="activeRoute"
          :collapse="collapsed && !mobileMenuOpen"
          :collapse-transition="false"
          background-color="#0f172a"
          text-color="#cbd5e1"
          active-text-color="#ffffff"
          router
          class="sidebar__menu"
          aria-label="功能导航"
          @select="handleMenuSelect"
        >
          <el-menu-item v-for="item in menus" :key="item.name" :index="`/${item.path}`">
            <el-icon><component :is="iconMap[item.meta.icon] || Menu" /></el-icon>
            <template #title>{{ item.meta.title }}</template>
          </el-menu-item>
        </el-menu>
      </nav>

      <button
        class="sidebar__toggle"
        :aria-label="collapsed ? '展开侧边栏' : '收起侧边栏'"
        :aria-expanded="!collapsed"
        @click="collapsed = !collapsed"
      >
        <el-icon>
          <component :is="collapsed ? Expand : Fold" />
        </el-icon>
      </button>
    </aside>

    <div class="main-area">
      <header class="topbar" role="banner">
        <div class="topbar__left">
          <button class="topbar__menu-btn" aria-label="切换移动导航" @click="mobileMenuOpen = !mobileMenuOpen">
            <el-icon :size="20"><Menu /></el-icon>
          </button>

          <div>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
            <p class="topbar__subtitle">{{ welcomeText }}</p>
          </div>
        </div>

        <div class="topbar__right">
          <el-tooltip :content="notificationTooltip" placement="bottom">
            <button class="topbar__icon-btn" aria-label="通知">
              <el-badge :value="notificationCount" :hidden="notificationCount === 0" class="topbar__badge">
                <el-icon :size="20"><Bell /></el-icon>
              </el-badge>
            </button>
          </el-tooltip>

          <el-dropdown trigger="click" aria-label="用户菜单">
            <button class="topbar__avatar-btn" aria-label="打开用户菜单">
              <el-avatar :size="34" style="background-color: var(--color-primary)">{{ userInitial }}</el-avatar>
              <span class="topbar__username">{{ displayName }}</span>
              <el-icon class="topbar__caret"><CaretBottom /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ roleSummary }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="page-content" id="main-content" tabindex="-1">
        <RouterView v-slot="{ Component }">
          <Transition name="fade" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </main>
    </div>

    <Transition name="fade">
      <div
        v-if="mobileMenuOpen"
        class="mobile-overlay"
        aria-hidden="true"
        @click="mobileMenuOpen = false"
      />
    </Transition>
  </div>
</template>

<script setup>
import * as Icons from '@element-plus/icons-vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDashboardStats } from '../api/stats'
import { ROLE_OPTIONS } from '../constants/options'
import { usePermissionStore } from '../store/permission'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const collapsed = ref(false)
const mobileMenuOpen = ref(false)
const notificationCount = ref(0)

const iconMap = Icons
const menus = computed(() => permissionStore.accessibleMenus)
const activeRoute = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title ?? '')
const displayName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '未登录')
const userInitial = computed(() => displayName.value.slice(0, 1) || '图')
const roleSummary = computed(() => {
  const roles = userStore.roles
  if (!roles.length) {
    return '未分配角色'
  }
  return roles.map(role => ROLE_OPTIONS.find(item => item.code === role)?.label || role).join(' / ')
})
const notificationTooltip = computed(() => userStore.isManager ? '待审批借阅数量' : '当前提醒')
const welcomeText = computed(() => userStore.isManager ? '管理端已连接真实业务接口' : '当前账号可查看个人借阅、预约与公告')

async function loadBadge() {
  if (!userStore.isManager) {
    notificationCount.value = 0
    return
  }

  try {
    const stats = await getDashboardStats()
    notificationCount.value = stats.pendingApprovalCount || 0
  } catch {
    notificationCount.value = 0
  }
}

function handleMenuSelect() {
  mobileMenuOpen.value = false
}

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

watch(
  () => route.fullPath,
  () => {
    loadBadge()
  },
  { immediate: true }
)

onMounted(() => {
  loadBadge()
})
</script>

<style lang="scss" scoped>
@use '../styles/variables' as *;

// ── Layout shell ──────────────────────────────────────────────────────────────
.app-layout {
  display: flex;
  min-height: 100vh;
  background: $color-bg;
}

// ── Sidebar ───────────────────────────────────────────────────────────────────
.sidebar {
  display: flex;
  flex-direction: column;
  width: $sidebar-width;
  min-height: 100vh;
  background: $sidebar-bg;
  color: $sidebar-text;
  transition: width $transition-normal;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: hidden;

  @media (max-width: $bp-lg) {
    position: fixed;
    left: 0;
    transform: translateX(-100%);
    z-index: 120;
    display: flex;
  }
}

.sidebar--mobile-open {
  @media (max-width: $bp-lg) {
    transform: translateX(0);
  }
}

.app-layout.sidebar-collapsed .sidebar {
  width: $sidebar-width-collapsed;
}

// Logo
.sidebar__logo {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-5 $space-4;
  border-bottom: 1px solid rgb(255 255 255 / 0.08);
  min-height: $topbar-height;
  overflow: hidden;
}

.sidebar__logo-icon {
  flex-shrink: 0;
}

.sidebar__logo-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: #ffffff;
  white-space: nowrap;
}

// Menu
.sidebar__menu {
  flex: 1;
  border-right: none !important;
  padding-block: $space-2;

  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    margin-block: 2px;
    border-radius: $radius-md;
    margin-inline: $space-2;
    width: calc(100% - #{$space-4});

    &:hover {
      background-color: rgb(255 255 255 / 0.08) !important;
    }

    &.is-active {
      background-color: $sidebar-active !important;
      color: #ffffff !important;
    }

    &.is-disabled {
      opacity: 0.4;
    }
  }
}

// Collapse toggle
.sidebar__toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 48px;
  background: transparent;
  border: none;
  border-top: 1px solid rgb(255 255 255 / 0.08);
  color: $sidebar-text;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: rgb(255 255 255 / 0.08);
  }

  &:focus-visible {
    outline: 2px solid $color-primary;
    outline-offset: -2px;
  }
}

// ── Main area ─────────────────────────────────────────────────────────────────
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0; // prevent overflow
  overflow: hidden;
}

// ── Top bar ───────────────────────────────────────────────────────────────────
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: $topbar-height;
  padding-inline: $space-6;
  background: $color-surface;
  border-bottom: 1px solid $color-border;
  box-shadow: $shadow-sm;
  position: sticky;
  top: 0;
  z-index: 100;

  @media (max-width: $bp-md) {
    padding-inline: $space-4;
  }
}

.topbar__left {
  display: flex;
  align-items: center;
  gap: $space-4;
}

.topbar__subtitle {
  margin-top: $space-1;
  font-size: $font-size-sm;
  color: $color-text-muted;
}

.topbar__right {
  display: flex;
  align-items: center;
  gap: $space-3;
}

.topbar__menu-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: transparent;
  border: none;
  border-radius: $radius-md;
  color: $color-text-secondary;
  cursor: pointer;

  &:hover {
    background: $color-bg;
  }

  @media (max-width: $bp-lg) {
    display: flex;
  }
}

.topbar__icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: transparent;
  border: none;
  border-radius: $radius-md;
  color: $color-text-secondary;
  cursor: pointer;
  transition: background $transition-fast, color $transition-fast;

  &:hover {
    background: $color-bg;
    color: $color-text-primary;
  }

  &:focus-visible {
    outline: 2px solid $color-primary;
    outline-offset: 2px;
  }
}

.topbar__badge {
  :deep(.el-badge__content) {
    font-size: 10px;
  }
}

.topbar__avatar-btn {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-1 $space-2;
  background: transparent;
  border: none;
  border-radius: $radius-md;
  cursor: pointer;
  color: $color-text-primary;
  transition: background $transition-fast;

  &:hover {
    background: $color-bg;
  }

  &:focus-visible {
    outline: 2px solid $color-primary;
    outline-offset: 2px;
  }
}

.topbar__username {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;

  @media (max-width: $bp-md) {
    display: none;
  }
}

.topbar__caret {
  color: $color-text-muted;
  font-size: $font-size-xs;
}

// ── Page content ──────────────────────────────────────────────────────────────
.page-content {
  flex: 1;
  padding: $space-8;
  overflow-x: hidden;
  overflow-y: auto;

  @media (max-width: $bp-md) {
    padding: $space-4;
  }
}

// ── Mobile overlay ────────────────────────────────────────────────────────────
.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgb(0 0 0 / 0.5);
  z-index: 200;
}

// ── Transitions ───────────────────────────────────────────────────────────────
.label-fade-enter-active,
.label-fade-leave-active {
  transition: opacity $transition-fast;
}

.label-fade-enter-from,
.label-fade-leave-to {
  opacity: 0;
}
</style>
