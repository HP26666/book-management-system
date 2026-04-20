<template>
  <view class="page">
    <!-- Hero banner -->
    <view class="hero">
      <view class="hero__icon" aria-hidden="true">📚</view>
      <text class="hero__title">图书管理系统</text>
      <text class="hero__subtitle">发现好书，随时借阅</text>
    </view>

    <!-- Search bar -->
    <view class="search-bar">
      <text class="search-bar__icon" aria-hidden="true">🔍</text>
      <text class="search-bar__placeholder">搜索书名、作者或 ISBN…</text>
    </view>

    <!-- Quick actions -->
    <view class="section">
      <text class="section__title">快速入口</text>
      <view class="quick-grid">
        <view
          v-for="action in quickActions"
          :key="action.label"
          class="quick-card"
          :style="{ background: action.bg }"
          hover-class="quick-card--hover"
          hover-stay-time="100"
        >
          <text class="quick-card__icon" aria-hidden="true">{{ action.icon }}</text>
          <text class="quick-card__label">{{ action.label }}</text>
        </view>
      </view>
    </view>

    <!-- Recent notices -->
    <view class="section">
      <view class="section__header">
        <text class="section__title">公告通知</text>
        <text class="section__more">查看全部</text>
      </view>
      <view class="notice-list">
        <view
          v-for="notice in notices"
          :key="notice.id"
          class="notice-item"
          hover-class="notice-item--hover"
          hover-stay-time="100"
        >
          <view class="notice-item__dot" :class="`notice-item__dot--${notice.type}`" aria-hidden="true" />
          <view class="notice-item__body">
            <text class="notice-item__title">{{ notice.title }}</text>
            <text class="notice-item__date">{{ notice.date }}</text>
          </view>
          <text class="notice-item__arrow" aria-hidden="true">›</text>
        </view>
      </view>
    </view>

    <!-- Tech stack tags (skeleton indicator) -->
    <view class="tags-row" aria-label="技术栈">
      <view v-for="tag in tags" :key="tag.label" class="tag" :style="{ background: tag.bg, color: tag.color }">
        <text class="tag__text">{{ tag.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const quickActions = [
  { label: '图书搜索', icon: '🔍', bg: '#ecfdf5' },
  { label: '我的借阅', icon: '📖', bg: '#eff6ff' },
  { label: '图书预约', icon: '📅', bg: '#fff7ed' },
  { label: '借阅记录', icon: '📋', bg: '#fdf4ff' }
]

const notices = [
  { id: 1, title: '图书馆开放时间调整通知', date: '2024-06-01', type: 'info' },
  { id: 2, title: '新书上架：计算机与 AI 专区', date: '2024-05-28', type: 'success' },
  { id: 3, title: '逾期书籍请尽快归还', date: '2024-05-20', type: 'warn' }
]

const tags = [
  { label: 'Spring Boot 3', bg: '#dcfce7', color: '#15803d' },
  { label: 'Vue 3', bg: '#dbeafe', color: '#1d4ed8' },
  { label: 'PostgreSQL', bg: '#fef3c7', color: '#b45309' },
  { label: 'Docker', bg: '#ede9fe', color: '#6d28d9' }
]
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.page {
  min-height: 100vh;
  padding: 0 0 $space-8;
  background: $color-bg;
}

// ── Hero ──────────────────────────────────────────────────────────────────────
.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-8 $space-4 $space-6;
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 100%);
  color: #ffffff;
}

.hero__icon {
  font-size: 80rpx;
  margin-bottom: $space-2;
  line-height: 1;
}

.hero__title {
  display: block;
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  color: #ffffff;
  text-align: center;
}

.hero__subtitle {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
}

// ── Search bar ────────────────────────────────────────────────────────────────
.search-bar {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin: $space-4;
  padding: $space-3 $space-4;
  background: $color-surface;
  border: 2rpx solid $color-border;
  border-radius: $radius-full;
  box-shadow: $shadow-sm;
}

.search-bar__icon {
  font-size: $font-size-lg;
  color: $color-text-muted;
  flex-shrink: 0;
}

.search-bar__placeholder {
  color: $color-text-muted;
  font-size: $font-size-base;
  flex: 1;
}

// ── Section ───────────────────────────────────────────────────────────────────
.section {
  padding: 0 $space-4;
  margin-bottom: $space-5;
}

.section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $space-3;
}

.section__title {
  display: block;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin-bottom: $space-3;
}

.section__more {
  font-size: $font-size-sm;
  color: $color-primary;
}

// ── Quick actions grid ────────────────────────────────────────────────────────
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-3;
}

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $space-3 $space-2;
  border-radius: $radius-md;
  transition: opacity 0.1s;
}

.quick-card--hover {
  opacity: 0.75;
}

.quick-card__icon {
  font-size: 48rpx;
  margin-bottom: $space-2;
  line-height: 1;
}

.quick-card__label {
  font-size: $font-size-xs;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
  text-align: center;
}

// ── Notice list ───────────────────────────────────────────────────────────────
.notice-list {
  background: $color-surface;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 $space-4;
  border-bottom: 2rpx solid $color-border;
  transition: background 0.1s;

  &:last-child {
    border-bottom: none;
  }
}

.notice-item--hover {
  background: $color-bg;
}

.notice-item__dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: $radius-full;
  flex-shrink: 0;

  &--info    { background: $color-info; }
  &--success { background: $color-success; }
  &--warn    { background: $color-warning; }
  &--error   { background: $color-error; }
}

.notice-item__body {
  flex: 1;
  min-width: 0;
}

.notice-item__title {
  display: block;
  font-size: $font-size-base;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item__date {
  display: block;
  margin-top: 4rpx;
  font-size: $font-size-xs;
  color: $color-text-muted;
}

.notice-item__arrow {
  color: $color-text-muted;
  font-size: $font-size-xl;
  flex-shrink: 0;
}

// ── Tags row ──────────────────────────────────────────────────────────────────
.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: $space-2;
  padding: 0 $space-4;
}

.tag {
  padding: $space-1 $space-3;
  border-radius: $radius-full;
}

.tag__text {
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
}
</style>
