<template>
  <scroll-view class="page-shell profile-page" scroll-y refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="loadProfile">
    <view class="profile-hero card">
      <view class="profile-hero__avatar">{{ displayInitial }}</view>
      <text class="profile-hero__name">{{ userStore.displayName }}</text>
      <text class="profile-hero__meta">{{ userStore.readerInfo?.readerCardNo || '未办理借阅证' }}</text>
      <text class="profile-hero__meta">{{ readerTypeLabel }}</text>
    </view>

    <view v-if="!userStore.isAuthenticated" class="card auth-card" @tap="goLogin">
      <text class="auth-card__title">尚未登录</text>
      <text class="auth-card__desc">登录后可查看借阅记录、预约记录和读者证信息。</text>
    </view>

    <view class="stats-grid">
      <view class="stat-card card">
        <text class="stat-card__value">{{ currentBorrowCount }}</text>
        <text class="stat-card__label">当前借阅</text>
      </view>
      <view class="stat-card card">
        <text class="stat-card__value">{{ historyBorrowCount }}</text>
        <text class="stat-card__label">历史借阅</text>
      </view>
      <view class="stat-card card">
        <text class="stat-card__value">{{ activeReservationCount }}</text>
        <text class="stat-card__label">有效预约</text>
      </view>
      <view class="stat-card card">
        <text class="stat-card__value">{{ userStore.readerInfo?.creditScore || 0 }}</text>
        <text class="stat-card__label">信用积分</text>
      </view>
    </view>

    <view class="card reader-card">
      <text class="section-title">读者信息</text>
      <view class="info-row"><text>借阅证号</text><text>{{ userStore.readerInfo?.readerCardNo || '未办理' }}</text></view>
      <view class="info-row"><text>借阅上限</text><text>{{ userStore.readerInfo?.maxBorrowCount || 0 }}</text></view>
      <view class="info-row"><text>当前在借</text><text>{{ userStore.readerInfo?.currentBorrowCount || 0 }}</text></view>
      <view class="info-row"><text>有效期限</text><text>{{ validRange }}</text></view>
      <view class="info-row"><text>黑名单状态</text><text>{{ userStore.readerInfo?.isBlacklist ? '已拉黑' : '正常' }}</text></view>
    </view>

    <view class="card service-card">
      <text class="section-title">我的服务</text>
      <view class="menu-item" @tap="openBorrowPage"><text>我的借阅</text><text>›</text></view>
      <view class="menu-item" @tap="openReservationPage"><text>预约记录</text><text>›</text></view>
      <view class="menu-item" @tap="openNoticePage"><text>系统公告</text><text>›</text></view>
      <view class="menu-item" @tap="openSearchPage"><text>图书搜索</text><text>›</text></view>
    </view>

    <view class="card config-card">
      <text class="section-title">开发配置</text>
      <text class="section-subtitle">当前请求地址可在开发环境中直接修改。</text>
      <input v-model="baseUrl" class="config-card__input" placeholder="http://127.0.0.1:8080/api" />
      <view class="config-card__actions">
        <button class="btn btn--ghost" @tap="resetBaseUrl">默认地址</button>
        <button class="btn btn--primary" @tap="saveBaseUrl">保存地址</button>
      </view>
    </view>

    <button v-if="userStore.isAuthenticated" class="logout-btn" @tap="handleLogout">退出登录</button>
  </scroll-view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getBorrowList } from '../../api/borrow'
import { getReservationList } from '../../api/reservation'
import { useUserStore } from '../../store/user'
import { getBaseUrl, getDefaultBaseUrl, setBaseUrl } from '../../utils/config'
import { formatDate } from '../../utils/format'
import { getReaderTypeLabel } from '../../utils/options'

const userStore = useUserStore()
const refreshing = ref(false)
const baseUrl = ref(getBaseUrl())
const borrowRecords = ref([])
const reservationRecords = ref([])

const displayInitial = computed(() => (userStore.displayName || '图').slice(0, 1))
const readerTypeLabel = computed(() => getReaderTypeLabel(userStore.readerInfo?.readerType))
const currentBorrowCount = computed(() => borrowRecords.value.filter(item => [0, 1, 2, 5].includes(item.status)).length)
const historyBorrowCount = computed(() => borrowRecords.value.filter(item => [3, 4].includes(item.status)).length)
const activeReservationCount = computed(() => reservationRecords.value.filter(item => [0, 1].includes(item.status)).length)
const validRange = computed(() => {
  if (!userStore.readerInfo) {
    return '未设置'
  }
  return `${formatDate(userStore.readerInfo.validDateStart)} 至 ${formatDate(userStore.readerInfo.validDateEnd)}`
})

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function ensureLogin(callback) {
  if (userStore.isAuthenticated) {
    callback()
    return
  }
  goLogin()
}

function openBorrowPage() {
  ensureLogin(() => uni.navigateTo({ url: '/pages/borrow/index' }))
}

function openReservationPage() {
  ensureLogin(() => uni.navigateTo({ url: '/pages/reservation/index' }))
}

function openNoticePage() {
  uni.navigateTo({ url: '/pages/notices/index' })
}

function openSearchPage() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function resetBaseUrl() {
  baseUrl.value = getDefaultBaseUrl()
}

function saveBaseUrl() {
  setBaseUrl(baseUrl.value.trim() || getDefaultBaseUrl())
  uni.showToast({ title: '保存成功', icon: 'success' })
}

async function handleLogout() {
  await userStore.logout()
  uni.showToast({ title: '已退出', icon: 'success' })
  setTimeout(() => {
    uni.switchTab({ url: '/pages/index/index' })
  }, 300)
}

async function loadProfile() {
  refreshing.value = true
  try {
    await userStore.bootstrap()
    if (!userStore.isAuthenticated) {
      borrowRecords.value = []
      reservationRecords.value = []
      return
    }
    const [borrowPage, reservationPage] = await Promise.all([
      getBorrowList({ page: 1, size: 100 }),
      getReservationList({ page: 1, size: 100 })
    ])
    borrowRecords.value = borrowPage.records || []
    reservationRecords.value = reservationPage.records || []
  } finally {
    refreshing.value = false
    uni.stopPullDownRefresh()
  }
}

onLoad(loadProfile)
onShow(loadProfile)
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.profile-page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.profile-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 100%);
  color: #ffffff;
}

.profile-hero__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 120rpx;
  border-radius: $radius-full;
  background: rgba(255, 255, 255, 0.18);
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
}

.profile-hero__name {
  display: block;
  margin-top: $space-3;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
}

.profile-hero__meta {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-sm;
  opacity: 0.85;
}

.auth-card__title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.auth-card__desc {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
}

.stat-card__value {
  display: block;
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-primary;
}

.stat-card__label {
  display: block;
  margin-top: $space-1;
  color: $color-text-muted;
  font-size: $font-size-sm;
}

.reader-card,
.service-card,
.config-card {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.info-row,
.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-2 0;
  border-bottom: 2rpx solid $color-border;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.info-row:last-child,
.menu-item:last-child {
  border-bottom: none;
}

.config-card__input {
  height: 84rpx;
  padding: 0 $space-3;
  border-radius: $radius-md;
  background: $color-bg;
  border: 2rpx solid $color-border;
}

.config-card__actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
}

.btn,
.logout-btn {
  height: 84rpx;
  border: none;
  border-radius: $radius-md;
  font-size: $font-size-base;
}

.btn--primary {
  background: $color-primary;
  color: #ffffff;
}

.btn--ghost {
  background: #e6fffb;
  color: $color-primary-dark;
}

.logout-btn {
  background: #fee2e2;
  color: $color-error;
}
</style>