<template>
  <div class="dashboard">
    <!-- Page title -->
    <div class="dashboard__heading">
      <h2 class="dashboard__title">仪表板</h2>
      <p class="dashboard__subtitle">欢迎回来，管理员 👋</p>
    </div>

    <!-- Stat cards -->
    <section aria-label="统计数据" class="stat-grid">
      <el-card
        v-for="stat in stats"
        :key="stat.label"
        class="stat-card"
        :body-style="{ padding: '0' }"
      >
        <div class="stat-card__inner">
          <div class="stat-card__icon" :style="{ background: stat.iconBg }">
            <el-icon :size="24" :color="stat.iconColor" aria-hidden="true">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-card__body">
            <p class="stat-card__label">{{ stat.label }}</p>
            <p class="stat-card__value">{{ stat.value }}</p>
          </div>
          <el-tag
            :type="stat.trend > 0 ? 'success' : 'danger'"
            size="small"
            round
            class="stat-card__trend"
            :aria-label="`较上月${stat.trend > 0 ? '增加' : '减少'} ${Math.abs(stat.trend)}%`"
          >
            {{ stat.trend > 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}%
          </el-tag>
        </div>
      </el-card>
    </section>

    <!-- Recent activity + quick links -->
    <div class="dashboard__grid">
      <!-- Recent borrows table -->
      <el-card class="dashboard__card" aria-label="近期借阅">
        <template #header>
          <div class="card-header">
            <h3 class="card-header__title">近期借阅</h3>
            <el-button link type="primary" size="small">查看全部</el-button>
          </div>
        </template>

        <el-table
          :data="recentBorrows"
          style="width: 100%"
          aria-label="近期借阅记录"
          stripe
        >
          <el-table-column prop="title" label="书名" min-width="140" show-overflow-tooltip />
          <el-table-column prop="reader" label="读者" width="100" />
          <el-table-column prop="date" label="借阅日期" width="110" />
          <el-table-column prop="due" label="应还日期" width="110" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.status === '借阅中' ? 'primary' : row.status === '已逾期' ? 'danger' : 'success'"
                size="small"
                round
              >
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- Quick links -->
      <el-card class="dashboard__card dashboard__card--narrow" aria-label="快速入口">
        <template #header>
          <h3 class="card-header__title">快速入口</h3>
        </template>

        <div class="quick-links">
          <RouterLink
            v-for="link in quickLinks"
            :key="link.label"
            :to="link.to"
            class="quick-link"
          >
            <div class="quick-link__icon" :style="{ background: link.bg }">
              <el-icon :size="20" :color="link.color" aria-hidden="true">
                <component :is="link.icon" />
              </el-icon>
            </div>
            <span class="quick-link__label">{{ link.label }}</span>
            <el-icon class="quick-link__arrow" aria-hidden="true"><ArrowRight /></el-icon>
          </RouterLink>
        </div>

        <el-divider />

        <!-- System status -->
        <div class="system-status" role="status" aria-label="系统状态">
          <p class="system-status__heading">系统状态</p>
          <div
            v-for="svc in services"
            :key="svc.name"
            class="system-status__row"
          >
            <span class="system-status__dot" :class="`system-status__dot--${svc.status}`" aria-hidden="true" />
            <span class="system-status__name">{{ svc.name }}</span>
            <span class="system-status__badge" :class="`system-status__badge--${svc.status}`">
              {{ svc.statusText }}
            </span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import {
  Collection,
  User,
  DocumentCopy,
  Calendar,
  ArrowRight
} from '@element-plus/icons-vue'

const stats = [
  {
    label: '馆藏图书',
    value: '12,480',
    icon: Collection,
    iconBg: '#ecfdf5',
    iconColor: '#15803d',
    trend: 3.2
  },
  {
    label: '注册读者',
    value: '3,641',
    icon: User,
    iconBg: '#eff6ff',
    iconColor: '#1d4ed8',
    trend: 5.7
  },
  {
    label: '本月借阅',
    value: '864',
    icon: DocumentCopy,
    iconBg: '#fff7ed',
    iconColor: '#b45309',
    trend: -1.4
  },
  {
    label: '预约待取',
    value: '52',
    icon: Calendar,
    iconBg: '#fdf4ff',
    iconColor: '#7e22ce',
    trend: 12.0
  }
]

const recentBorrows = [
  { title: '深入理解计算机系统', reader: '张三', date: '2024-06-01', due: '2024-06-22', status: '借阅中' },
  { title: 'Vue.js 3 实战', reader: '李四', date: '2024-05-28', due: '2024-06-18', status: '借阅中' },
  { title: '代码整洁之道', reader: '王五', date: '2024-05-15', due: '2024-06-05', status: '已逾期' },
  { title: '算法导论', reader: '赵六', date: '2024-05-10', due: '2024-05-31', status: '已归还' },
  { title: '人月神话', reader: '孙七', date: '2024-05-05', due: '2024-05-26', status: '已归还' }
]

const quickLinks = [
  { label: '图书管理', to: '/books', icon: Collection, bg: '#ecfdf5', color: '#15803d' },
  { label: '读者管理', to: '/users', icon: User, bg: '#eff6ff', color: '#1d4ed8' },
  { label: '借阅管理', to: '/borrow', icon: DocumentCopy, bg: '#fff7ed', color: '#b45309' },
  { label: '预约管理', to: '/reserve', icon: Calendar, bg: '#fdf4ff', color: '#7e22ce' }
]

const services = [
  { name: '后端 API', status: 'ok', statusText: '正常' },
  { name: '数据库', status: 'ok', statusText: '正常' },
  { name: '存储服务', status: 'warn', statusText: '降级' }
]
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
  grid-template-columns: repeat(4, 1fr);
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
</style>
