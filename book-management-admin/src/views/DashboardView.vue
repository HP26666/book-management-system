<template>
  <div class="dashboard" v-loading="loading">
    <div class="dashboard__heading">
      <h2 class="dashboard__title">仪表板</h2>
      <p class="dashboard__subtitle">{{ subtitle }}</p>
    </div>

    <section class="stat-grid" aria-label="统计概览">
      <el-card v-for="stat in statCards" :key="stat.label" class="stat-card" :body-style="{ padding: '0' }">
        <div class="stat-card__inner">
          <div class="stat-card__icon" :style="{ background: stat.iconBg }">
            <el-icon :size="24" :color="stat.iconColor"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-card__body">
            <p class="stat-card__label">{{ stat.label }}</p>
            <p class="stat-card__value">{{ stat.value }}</p>
          </div>
        </div>
      </el-card>
    </section>

    <div class="dashboard__grid">
      <el-card class="dashboard__card">
        <template #header>
          <div class="card-header">
            <h3 class="card-header__title">{{ primaryTableTitle }}</h3>
            <RouterLink :to="primaryTableLink" class="card-header__link">查看全部</RouterLink>
          </div>
        </template>

        <el-table :data="primaryTableData" stripe style="width: 100%">
          <el-table-column prop="bookTitle" label="书名" min-width="180" show-overflow-tooltip />
          <el-table-column v-if="userStore.isManager" prop="realName" label="读者" width="120" />
          <el-table-column :prop="userStore.isManager ? 'borrowDate' : 'dueDate'" :label="userStore.isManager ? '借阅日期' : '应还日期'" width="130">
            <template #default="{ row }">
              {{ formatDate(userStore.isManager ? row.borrowDate : row.dueDate) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="findOptionTag(BORROW_STATUS_OPTIONS, row.status)" round>
                {{ findOptionLabel(BORROW_STATUS_OPTIONS, row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="dashboard__card dashboard__card--narrow">
        <template #header>
          <h3 class="card-header__title">快速入口</h3>
        </template>

        <div class="quick-links">
          <RouterLink v-for="link in quickLinks" :key="link.label" :to="link.to" class="quick-link">
            <div class="quick-link__icon" :style="{ background: link.bg }">
              <el-icon :size="18" :color="link.color"><component :is="link.icon" /></el-icon>
            </div>
            <span class="quick-link__label">{{ link.label }}</span>
            <el-icon class="quick-link__arrow"><ArrowRight /></el-icon>
          </RouterLink>
        </div>

        <el-divider />

        <div v-if="userStore.isManager" class="system-status">
          <p class="system-status__heading">系统状态</p>
          <div v-for="item in systemStatus" :key="item.name" class="system-status__row">
            <span class="system-status__dot" :class="`system-status__dot--${item.status}`" />
            <span class="system-status__name">{{ item.name }}</span>
            <span class="system-status__badge" :class="`system-status__badge--${item.status}`">{{ item.label }}</span>
          </div>
        </div>

        <div v-else class="notice-list">
          <p class="system-status__heading">最新公告</p>
          <div v-for="notice in latestNotices" :key="notice.id" class="notice-item">
            <p class="notice-item__title">{{ notice.title }}</p>
            <p class="notice-item__meta">{{ formatDateTime(notice.publishTime) }}</p>
          </div>
        </div>
      </el-card>
    </div>

    <div v-if="userStore.isManager" class="dashboard__grid dashboard__grid--bottom">
      <el-card class="dashboard__card">
        <template #header>
          <h3 class="card-header__title">热门图书 Top 10</h3>
        </template>
        <div class="rank-list">
          <div v-for="(item, index) in popularBooks" :key="item.bookId || index" class="rank-item">
            <span class="rank-item__index">{{ index + 1 }}</span>
            <span class="rank-item__name">{{ item.title }}</span>
            <span class="rank-item__value">{{ item.borrowCount }} 次</span>
          </div>
        </div>
      </el-card>

      <el-card class="dashboard__card dashboard__card--narrow">
        <template #header>
          <h3 class="card-header__title">借阅状态分布</h3>
        </template>
        <div class="distribution-list">
          <div v-for="item in statusDistribution" :key="item.status" class="distribution-item">
            <div class="distribution-item__top">
              <span>{{ item.label }}</span>
              <span>{{ item.count }}</span>
            </div>
            <el-progress :percentage="calcDistribution(item.count)" :stroke-width="10" :show-text="false" />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
  ArrowRight,
  Calendar,
  Collection,
  DocumentCopy,
  Odometer,
  User,
  Bell
} from '@element-plus/icons-vue'
import { getBorrowList } from '../api/borrow'
import { getNoticeList } from '../api/notice'
import { getReservationList } from '../api/reservation'
import { getDashboardStats, getHealthStatus } from '../api/stats'
import { BORROW_STATUS_OPTIONS, findOptionLabel, findOptionTag } from '../constants/options'
import { useUserStore } from '../store/user'
import { formatDate, formatDateTime } from '../utils/format'

const userStore = useUserStore()
const loading = ref(false)
const managerStats = ref(null)
const primaryTableData = ref([])
const latestNotices = ref([])
const popularBooks = ref([])
const statusDistribution = ref([])
const systemStatus = ref([
  { name: '后端 API', status: 'ok', label: '正常' },
  { name: '健康检查', status: 'ok', label: 'UP' }
])

const subtitle = computed(() => userStore.isManager ? '实时展示馆藏、借阅和审批概况' : '查看我的借阅、预约和最新公告')
const primaryTableTitle = computed(() => userStore.isManager ? '近期借阅' : '我的借阅')
const primaryTableLink = computed(() => userStore.isManager ? '/borrow' : '/borrow')

const statCards = computed(() => {
  if (userStore.isManager) {
    const stats = managerStats.value || {}
    return [
      { label: '馆藏图书', value: stats.totalBooks || 0, icon: Collection, iconBg: '#ecfdf5', iconColor: '#15803d' },
      { label: '注册读者', value: stats.totalReaders || 0, icon: User, iconBg: '#eff6ff', iconColor: '#1d4ed8' },
      { label: '本月借阅', value: stats.monthBorrowCount || 0, icon: DocumentCopy, iconBg: '#fff7ed', iconColor: '#b45309' },
      { label: '待审批', value: stats.pendingApprovalCount || 0, icon: Odometer, iconBg: '#f0fdfa', iconColor: '#0f766e' },
      { label: '待取书', value: stats.pendingPickupCount || 0, icon: Calendar, iconBg: '#fdf4ff', iconColor: '#7e22ce' },
      { label: '逾期数', value: stats.overdueCount || 0, icon: Bell, iconBg: '#fee2e2', iconColor: '#dc2626' }
    ]
  }

  return [
    { label: '当前借阅', value: managerStats.value?.activeBorrowCount || 0, icon: DocumentCopy, iconBg: '#ecfdf5', iconColor: '#15803d' },
    { label: '有效预约', value: managerStats.value?.reservationCount || 0, icon: Calendar, iconBg: '#eff6ff', iconColor: '#1d4ed8' },
    { label: '最新公告', value: latestNotices.value.length, icon: Bell, iconBg: '#fff7ed', iconColor: '#b45309' },
    { label: '可用功能', value: 4, icon: Collection, iconBg: '#f0fdfa', iconColor: '#0f766e' }
  ]
})

const quickLinks = computed(() => {
  if (userStore.isManager) {
    return [
      { label: '图书管理', to: '/books', icon: Collection, bg: '#ecfdf5', color: '#15803d' },
      { label: '用户管理', to: '/users', icon: User, bg: '#eff6ff', color: '#1d4ed8' },
      { label: '借阅管理', to: '/borrow', icon: DocumentCopy, bg: '#fff7ed', color: '#b45309' },
      { label: '公告管理', to: '/notices', icon: Bell, bg: '#fdf4ff', color: '#7e22ce' }
    ]
  }

  return [
    { label: '图书查询', to: '/books', icon: Collection, bg: '#ecfdf5', color: '#15803d' },
    { label: '我的借阅', to: '/borrow', icon: DocumentCopy, bg: '#eff6ff', color: '#1d4ed8' },
    { label: '我的预约', to: '/reserve', icon: Calendar, bg: '#fff7ed', color: '#b45309' },
    { label: '系统公告', to: '/notices', icon: Bell, bg: '#fdf4ff', color: '#7e22ce' }
  ]
})

async function loadManagerDashboard() {
  const [stats, borrows, health] = await Promise.all([
    getDashboardStats(),
    getBorrowList({ page: 1, size: 5 }),
    getHealthStatus().catch(() => ({ status: 'DOWN' }))
  ])

  managerStats.value = stats
  primaryTableData.value = borrows.records || []
  popularBooks.value = stats.popularBooks || []
  statusDistribution.value = stats.borrowStatusDistribution || []
  systemStatus.value = [
    { name: '后端 API', status: health.status === 'UP' ? 'ok' : 'err', label: health.status || '未知' },
    { name: '健康检查', status: health.status === 'UP' ? 'ok' : 'err', label: health.status === 'UP' ? 'UP' : '异常' }
  ]
}

async function loadReaderDashboard() {
  const [displayBorrows, allBorrows, reservationData, noticeData] = await Promise.all([
    getBorrowList({ page: 1, size: 5 }),
    getBorrowList({ page: 1, size: 200 }),
    getReservationList({ page: 1, size: 20 }),
    getNoticeList({ page: 1, size: 3 })
  ])

  primaryTableData.value = displayBorrows.records || []
  latestNotices.value = noticeData.records || []
  const borrowRecords = allBorrows.records || []
  managerStats.value = {
    activeBorrowCount: borrowRecords.filter(item => [1, 2, 5].includes(item.status)).length,
    reservationCount: reservationData.total || 0
  }
}

function calcDistribution(count) {
  const total = statusDistribution.value.reduce((sum, item) => sum + Number(item.count || 0), 0)
  if (!total) {
    return 0
  }
  return Math.round((Number(count || 0) / total) * 100)
}

async function loadDashboard() {
  loading.value = true
  try {
    if (userStore.isManager) {
      await loadManagerDashboard()
    } else {
      await loadReaderDashboard()
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style lang="scss" scoped>
@use '../styles/variables' as *;

// ── Page ──────────────────────────────────────────────────────────────────────
.dashboard {
  max-width: $content-max-width;
  margin: 0 auto;
}

.dashboard__heading {
  margin-bottom: $space-8;
}

.dashboard__title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.dashboard__subtitle {
  color: $color-text-muted;
  margin-top: $space-1;
}

// ── Stat grid ─────────────────────────────────────────────────────────────────
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-6;
  margin-bottom: $space-8;

  @media (max-width: $bp-xl) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: $bp-sm) {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  border-radius: $radius-lg !important;

  &:hover {
    box-shadow: $shadow-md !important;
  }
}

.stat-card__inner {
  display: flex;
  align-items: center;
  gap: $space-4;
  padding: $space-6;
  position: relative;
}

.stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: $radius-lg;
  flex-shrink: 0;
}

.stat-card__body {
  flex: 1;
  min-width: 0;
}

.stat-card__label {
  font-size: $font-size-sm;
  color: $color-text-muted;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-card__value {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin: $space-1 0 0;
}

.stat-card__trend {
  position: absolute;
  top: $space-4;
  right: $space-4;
}

// ── Dashboard grid ────────────────────────────────────────────────────────────
.dashboard__grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: $space-6;

  @media (max-width: $bp-xl) {
    grid-template-columns: 1fr;
  }
}

.dashboard__grid--bottom {
  margin-top: $space-6;
}

.dashboard__card {
  border-radius: $radius-lg !important;

  :deep(.el-card__header) {
    padding: $space-5 $space-6;
    border-bottom-color: $color-border;
  }
}

.dashboard__card--narrow {
  height: fit-content;
}

// ── Card header ───────────────────────────────────────────────────────────────
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header__link {
  font-size: $font-size-sm;
}

.card-header__title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

// ── Quick links ───────────────────────────────────────────────────────────────
.quick-links {
  display: flex;
  flex-direction: column;
  gap: $space-1;
}

.quick-link {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 $space-2;
  border-radius: $radius-md;
  text-decoration: none;
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

.quick-link__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: $radius-md;
  flex-shrink: 0;
}

.quick-link__label {
  flex: 1;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

.quick-link__arrow {
  color: $color-text-muted;
  font-size: $font-size-xs;
}

// ── System status ─────────────────────────────────────────────────────────────
.system-status__heading {
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  color: $color-text-secondary;
  margin: 0 0 $space-3;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.system-status__row {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding-block: $space-2;
}

.system-status__dot {
  width: 8px;
  height: 8px;
  border-radius: $radius-full;
  flex-shrink: 0;

  &--ok   { background: $color-success; }
  &--warn { background: $color-warning; }
  &--err  { background: $color-error; }
}

.system-status__name {
  flex: 1;
  font-size: $font-size-sm;
  color: $color-text-primary;
}

.system-status__badge {
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  padding: 2px $space-2;
  border-radius: $radius-full;

  &--ok   { background: #dcfce7; color: $color-success; }
  &--warn { background: #fef3c7; color: $color-warning; }
  &--err  { background: #fee2e2; color: $color-error;   }
}

.rank-list,
.distribution-list,
.notice-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.rank-item,
.notice-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3;
  background: $color-bg;
  border-radius: $radius-md;
}

.rank-item__index {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-full;
  background: $color-primary-light;
  color: $color-primary-dark;
  font-weight: $font-weight-bold;
}

.rank-item__name {
  flex: 1;
}

.rank-item__value,
.notice-item__meta {
  color: $color-text-muted;
  font-size: $font-size-sm;
}

.notice-item {
  flex-direction: column;
  align-items: flex-start;
}

.notice-item__title {
  font-weight: $font-weight-medium;
}

.distribution-item {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.distribution-item__top {
  display: flex;
  justify-content: space-between;
  font-size: $font-size-sm;
}
</style>
