<template>
  <view class="page">
    <!-- Profile header -->
    <view class="profile-header">
      <view class="profile-header__avatar" aria-label="用户头像">
        <text class="profile-header__avatar-text" aria-hidden="true">管</text>
      </view>
      <text class="profile-header__name">管理员</text>
      <text class="profile-header__role">图书管理员</text>
    </view>

    <!-- Borrow stats -->
    <view class="stats-row" aria-label="借阅统计">
      <view v-for="stat in borrowStats" :key="stat.label" class="stat-item">
        <text class="stat-item__value">{{ stat.value }}</text>
        <text class="stat-item__label">{{ stat.label }}</text>
      </view>
    </view>

    <!-- Menu items -->
    <view class="section">
      <text class="section__title">我的服务</text>
      <view class="menu-list">
        <view
          v-for="item in menuItems"
          :key="item.label"
          class="menu-item"
          hover-class="menu-item--hover"
          hover-stay-time="80"
        >
          <view class="menu-item__icon" :style="{ background: item.bg }" aria-hidden="true">
            <text class="menu-item__emoji">{{ item.icon }}</text>
          </view>
          <text class="menu-item__label">{{ item.label }}</text>
          <text class="menu-item__arrow" aria-hidden="true">›</text>
        </view>
      </view>
    </view>

    <!-- Log out -->
    <view class="logout-wrap">
      <view class="logout-btn" hover-class="logout-btn--hover" hover-stay-time="80">
        <text class="logout-btn__text">退出登录</text>
      </view>
    </view>

    <!-- Placeholder notice -->
    <view class="notice-card">
      <text class="notice-card__text">
        个人中心功能（借阅证、借阅记录、预约记录）将在后续迭代中完整实现。
      </text>
    </view>
  </view>
</template>

<script setup>
const borrowStats = [
  { label: '在借', value: '2' },
  { label: '已借', value: '18' },
  { label: '逾期', value: '0' }
]

const menuItems = [
  { label: '我的借阅', icon: '📖', bg: '#ecfdf5' },
  { label: '预约记录', icon: '📅', bg: '#eff6ff' },
  { label: '借阅证', icon: '🪪', bg: '#fff7ed' },
  { label: '消息通知', icon: '🔔', bg: '#fdf4ff' },
  { label: '设置', icon: '⚙️', bg: '#f1f5f9' }
]
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.page {
  min-height: 100vh;
  padding-bottom: $space-8;
  background: $color-bg;
}

// ── Profile header ────────────────────────────────────────────────────────────
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-8 $space-4 $space-6;
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 100%);
}

.profile-header__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 128rpx;
  height: 128rpx;
  border-radius: $radius-full;
  background: rgba(255, 255, 255, 0.25);
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  margin-bottom: $space-3;
}

.profile-header__avatar-text {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: #ffffff;
}

.profile-header__name {
  display: block;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #ffffff;
}

.profile-header__role {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.8);
}

// ── Stats row ─────────────────────────────────────────────────────────────────
.stats-row {
  display: flex;
  background: $color-surface;
  border-bottom: 2rpx solid $color-border;
  box-shadow: $shadow-sm;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-4 $space-2;
  border-right: 2rpx solid $color-border;

  &:last-child {
    border-right: none;
  }
}

.stat-item__value {
  display: block;
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-primary;
}

.stat-item__label {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-xs;
  color: $color-text-muted;
}

// ── Section ───────────────────────────────────────────────────────────────────
.section {
  padding: $space-4;
}

.section__title {
  display: block;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  color: $color-text-muted;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: $space-2;
}

// ── Menu list ─────────────────────────────────────────────────────────────────
.menu-list {
  background: $color-surface;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

.menu-item {
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

.menu-item--hover {
  background: $color-bg;
}

.menu-item__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-md;
  flex-shrink: 0;
}

.menu-item__emoji {
  font-size: $font-size-xl;
  line-height: 1;
}

.menu-item__label {
  flex: 1;
  font-size: $font-size-base;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
}

.menu-item__arrow {
  color: $color-text-muted;
  font-size: $font-size-xl;
  flex-shrink: 0;
}

// ── Logout ────────────────────────────────────────────────────────────────────
.logout-wrap {
  padding: $space-4;
  margin-bottom: $space-2;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  background: $color-surface;
  border: 2rpx solid #fca5a5;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
}

.logout-btn--hover {
  background: #fef2f2;
}

.logout-btn__text {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $color-error;
}

// ── Notice card ───────────────────────────────────────────────────────────────
.notice-card {
  margin: 0 $space-4;
  padding: $space-3 $space-4;
  background: #eff6ff;
  border-left: 8rpx solid $color-info;
  border-radius: $radius-sm;
}

.notice-card__text {
  font-size: $font-size-sm;
  color: $color-info;
  line-height: $line-height-loose;
}
</style>
