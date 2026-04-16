<template>
  <view class="page-container">
    <!-- 未登录 -->
    <view v-if="!isLoggedIn" class="login-prompt card">
      <view class="avatar-placeholder">
        <u-icon name="account" size="48" color="#d1d5db" />
      </view>
      <text class="prompt-text">登录后查看个人信息</text>
      <u-button
        type="primary"
        size="small"
        :customStyle="{ marginTop: '24rpx', background: '#0f766e', borderColor: '#0f766e' }"
        @click="goLogin"
      >去登录</u-button>
    </view>

    <!-- 已登录 - 用户信息头 -->
    <template v-else>
      <view class="user-header card">
        <view class="avatar-wrap">
          <image
            v-if="userInfo?.avatarUrl"
            class="avatar"
            :src="userInfo.avatarUrl"
            mode="aspectFill"
          />
          <view v-else class="avatar avatar-default">
            <text class="avatar-text">{{ (userInfo?.username || '?')[0].toUpperCase() }}</text>
          </view>
        </view>
        <view class="user-meta">
          <text class="user-name">{{ userInfo?.realName || userInfo?.username || '用户' }}</text>
          <text class="user-id">@{{ userInfo?.username }}</text>
        </view>
      </view>

      <!-- 读者借阅证 -->
      <view class="card reader-card" v-if="readerInfo">
        <view class="section-title">借阅证信息</view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">借阅证号</text>
            <text class="info-value">{{ readerInfo.readerCardNo || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">读者类型</text>
            <text class="info-value">{{ readerInfo.readerType || '-' }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">借阅额度</text>
            <text class="info-value">{{ readerInfo.currentBorrowCount || 0 }} / {{ readerInfo.maxBorrowCount || 5 }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">信用积分</text>
            <text class="info-value" :class="{ 'text-danger': (readerInfo.creditScore || 0) < 60 }">
              {{ readerInfo.creditScore || 0 }}
            </text>
          </view>
        </view>
        <!-- 借阅额度进度条 -->
        <view class="progress-wrap">
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: borrowProgress + '%' }"
            />
          </view>
          <text class="progress-label">已借 {{ readerInfo.currentBorrowCount || 0 }} 本，最多 {{ readerInfo.maxBorrowCount || 5 }} 本</text>
        </view>
        <view class="info-item" v-if="readerInfo.validDateEnd">
          <text class="info-label">有效期至</text>
          <text class="info-value">{{ readerInfo.validDateEnd.substring(0, 10) }}</text>
        </view>
        <view v-if="readerInfo.isBlacklist" class="blacklist-warn">
          <u-icon name="warning" size="14" color="#dc2626" />
          <text style="color: #dc2626; font-size: 24rpx; margin-left: 6rpx;">您已被列入黑名单，暂无法借阅</text>
        </view>
      </view>

      <!-- 功能菜单 -->
      <view class="card menu-card">
        <view class="menu-item" @click="goPage('/pages/borrow/index')">
          <u-icon name="order" size="20" color="#0f766e" />
          <text class="menu-text">我的借阅</text>
          <u-icon name="arrow-right" size="14" color="#c0c4cc" />
        </view>
        <view class="menu-item" @click="goPage('/pages/reservation/index')">
          <u-icon name="calendar" size="20" color="#0f766e" />
          <text class="menu-text">我的预约</text>
          <u-icon name="arrow-right" size="14" color="#c0c4cc" />
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="card" style="padding: 0;">
        <view class="menu-item logout-item" @click="handleLogout">
          <text style="color: #dc2626;">退出登录</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { getMyReader } from '../../api/reader'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const userInfo = computed(() => authStore.userInfo)

const readerInfo = ref(null)
const borrowProgress = computed(() => {
  if (!readerInfo.value) return 0
  const max = readerInfo.value.maxBorrowCount || 5
  const curr = readerInfo.value.currentBorrowCount || 0
  return Math.min((curr / max) * 100, 100)
})

const loadReaderInfo = async () => {
  try {
    const res = await getMyReader()
    if (res.code === 200) readerInfo.value = res.data
  } catch (_) { /* ignore */ }
}

watch(isLoggedIn, (val) => {
  if (val) loadReaderInfo()
}, { immediate: true })

const goLogin = () => uni.navigateTo({ url: '/pages/login/index' })
const goPage = (url) => uni.navigateTo({ url })

const handleLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        authStore.doLogout()
        readerInfo.value = null
        uni.showToast({ title: '已退出', icon: 'success' })
      }
    }
  })
}

const onShow = () => {
  if (isLoggedIn.value) loadReaderInfo()
}
defineExpose({ onShow })
</script>

<style lang="scss" scoped>
.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 32rpx;
}

.avatar-placeholder {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.prompt-text {
  color: #6b7280;
  font-size: 28rpx;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
}

.avatar-default {
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
}

.user-meta {
  flex: 1;
}

.user-name {
  display: block;
  font-size: 34rpx;
  font-weight: 600;
  color: #111827;
}

.user-id {
  display: block;
  font-size: 24rpx;
  color: #9ca3af;
  margin-top: 4rpx;
}

.reader-card .info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.info-item {
  width: calc(50% - 4rpx);
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  padding: 12rpx 0;
}

.info-label {
  font-size: 24rpx;
  color: #9ca3af;
}

.info-value {
  font-size: 28rpx;
  color: #111827;
  font-weight: 500;
}

.text-danger {
  color: #dc2626 !important;
}

.progress-wrap {
  margin: 12rpx 0 8rpx;
}

.progress-bar {
  height: 12rpx;
  background: #f3f4f6;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0f766e, #14b8a6);
  border-radius: 6rpx;
  transition: width 0.3s;
}

.progress-label {
  font-size: 22rpx;
  color: #9ca3af;
  margin-top: 6rpx;
  display: block;
}

.blacklist-warn {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
  padding: 12rpx 16rpx;
  background: #fef2f2;
  border-radius: 8rpx;
}

.menu-card {
  padding: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.menu-text {
  flex: 1;
  margin-left: 16rpx;
  font-size: 28rpx;
  color: #374151;
}

.logout-item {
  justify-content: center;
}
</style>
