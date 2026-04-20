<template>
  <scroll-view class="page" scroll-y refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="loadHome">
    <view class="hero card">
      <view class="hero__top">
        <view>
          <text class="hero__eyebrow">图书管理系统</text>
          <text class="hero__title">发现好书，随时借阅</text>
          <text class="hero__subtitle">支持检索、借阅申请、预约和个人记录管理。</text>
        </view>
        <view class="hero__badge">{{ userStore.isAuthenticated ? '已登录' : '游客浏览' }}</view>
      </view>

      <view class="hero-stats">
        <view class="hero-stat">
          <text class="hero-stat__value">{{ userStore.readerInfo?.currentBorrowCount || 0 }}</text>
          <text class="hero-stat__label">当前借阅</text>
        </view>
        <view class="hero-stat">
          <text class="hero-stat__value">{{ reservationCount }}</text>
          <text class="hero-stat__label">有效预约</text>
        </view>
        <view class="hero-stat">
          <text class="hero-stat__value">{{ userStore.readerInfo?.creditScore || 0 }}</text>
          <text class="hero-stat__label">信用积分</text>
        </view>
      </view>
    </view>

    <view class="search-bar" @tap="goSearch()">
      <text class="search-bar__icon">🔍</text>
      <text class="search-bar__placeholder">搜索书名、作者或 ISBN</text>
      <text class="search-bar__cta">去搜索</text>
    </view>

    <view v-if="!userStore.isAuthenticated" class="login-banner card" @tap="goLogin">
      <view>
        <text class="login-banner__title">登录后可直接借阅与预约</text>
        <text class="login-banner__desc">开发环境支持 Mock 微信登录，无需等待公众号配置。</text>
      </view>
      <text class="login-banner__arrow">›</text>
    </view>

    <view class="section">
      <view class="section__header">
        <text class="section-title">快速入口</text>
      </view>
      <view class="quick-grid">
        <view v-for="action in quickActions" :key="action.label" class="quick-card" :style="{ background: action.bg }" @tap="handleQuickAction(action)">
          <text class="quick-card__icon">{{ action.icon }}</text>
          <text class="quick-card__label">{{ action.label }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__header">
        <view>
          <text class="section-title">分类入口</text>
          <text class="section-subtitle">常用图书分类快速跳转</text>
        </view>
      </view>
      <view class="category-grid">
        <view v-for="item in categoryCards" :key="item.id" class="category-card" @tap="goCategory(item)">
          <text class="category-card__name">{{ item.name }}</text>
          <text class="category-card__meta">{{ item.children?.length || 0 }} 个子类</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__header">
        <view>
          <text class="section-title">热门图书</text>
          <text class="section-subtitle">优先展示当前可借图书</text>
        </view>
        <text class="section__more" @tap="goSearch()">查看全部</text>
      </view>
      <view class="book-list">
        <view v-for="book in books" :key="book.id" class="book-card card" @tap="goBook(book.id)">
          <image v-if="resolveCover(book.coverUrl)" :src="resolveCover(book.coverUrl)" class="book-card__cover" mode="aspectFill" />
          <view v-else class="book-card__cover book-card__cover--placeholder">BOOK</view>
          <view class="book-card__body">
            <text class="book-card__title">{{ book.title }}</text>
            <text class="book-card__author">{{ book.author || '未知作者' }}</text>
            <text class="book-card__meta">{{ book.categoryName || '未分类' }} · {{ book.availableStock > 0 ? `可借 ${book.availableStock}` : '已无库存' }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__header">
        <view>
          <text class="section-title">公告通知</text>
          <text class="section-subtitle">仅展示已发布公告</text>
        </view>
        <text class="section__more" @tap="goNotices">全部公告</text>
      </view>

      <view class="notice-list card">
        <view v-for="notice in notices" :key="notice.id" class="notice-item" @tap="goNotices">
          <view class="notice-item__left">
            <text class="status-pill" :class="`status-pill--${getNoticeType(notice.type).className}`">{{ getNoticeType(notice.type).label }}</text>
          </view>
          <view class="notice-item__body">
            <text class="notice-item__title">{{ notice.title }}</text>
            <text class="notice-item__date">{{ formatDateTime(notice.publishTime) }}</text>
          </view>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getBookList } from '../../api/book'
import { getCategoryTree } from '../../api/category'
import { getNoticeList } from '../../api/notice'
import { getReservationList } from '../../api/reservation'
import { useUserStore } from '../../store/user'
import { getBaseUrl } from '../../utils/config'
import { formatDateTime } from '../../utils/format'
import { getNoticeType } from '../../utils/options'
import { flattenCategoryTree } from '../../utils/tree'

const userStore = useUserStore()
const refreshing = ref(false)
const categories = ref([])
const books = ref([])
const notices = ref([])
const reservationCount = ref(0)

const quickActions = [
  { label: '图书搜索', icon: '🔍', bg: '#ecfdf5', action: 'search' },
  { label: '我的借阅', icon: '📖', bg: '#eff6ff', action: 'borrow' },
  { label: '预约记录', icon: '📅', bg: '#fff7ed', action: 'reservation' },
  { label: '公告通知', icon: '📢', bg: '#fdf4ff', action: 'notice' }
]

const categoryCards = computed(() => categories.value.slice(0, 6))

function resolveCover(coverUrl) {
  if (!coverUrl) {
    return ''
  }
  if (/^https?:\/\//.test(coverUrl)) {
    return coverUrl
  }
  return `${getBaseUrl().replace(/\/api$/, '')}${coverUrl}`
}

function requireLogin(target) {
  if (userStore.isAuthenticated) {
    target()
    return
  }
  uni.navigateTo({ url: '/pages/login/index' })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goSearch(keyword = '', categoryId = '') {
  const query = []
  if (keyword) {
    query.push(`keyword=${encodeURIComponent(keyword)}`)
  }
  if (categoryId) {
    query.push(`categoryId=${categoryId}`)
  }
  const suffix = query.length ? `?${query.join('&')}` : ''
  uni.navigateTo({ url: `/pages/search/index${suffix}` })
}

function goCategory(item) {
  goSearch('', item.id)
}

function goBook(id) {
  uni.navigateTo({ url: `/pages/book-detail/index?id=${id}` })
}

function goNotices() {
  uni.navigateTo({ url: '/pages/notices/index' })
}

function handleQuickAction(action) {
  if (action.action === 'search') {
    goSearch()
    return
  }
  if (action.action === 'notice') {
    goNotices()
    return
  }
  if (action.action === 'borrow') {
    requireLogin(() => uni.navigateTo({ url: '/pages/borrow/index' }))
    return
  }
  if (action.action === 'reservation') {
    requireLogin(() => uni.navigateTo({ url: '/pages/reservation/index' }))
  }
}

async function loadHome() {
  refreshing.value = true
  try {
    await userStore.bootstrap()
    const tasks = [
      getCategoryTree(),
      getBookList({ page: 1, size: 6, status: 1 }),
      getNoticeList({ page: 1, size: 4 })
    ]
    if (userStore.isAuthenticated) {
      tasks.push(getReservationList({ page: 1, size: 50 }))
    }
    const [categoryTree, bookPage, noticePage, reservationPage] = await Promise.all(tasks)
    categories.value = flattenCategoryTree(categoryTree).filter(item => item.level === 0)
    books.value = bookPage.records || []
    notices.value = noticePage.records || []
    reservationCount.value = (reservationPage?.records || []).filter(item => [0, 1].includes(item.status)).length
  } finally {
    refreshing.value = false
    uni.stopPullDownRefresh()
  }
}

onLoad(loadHome)
onShow(loadHome)
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.page {
  min-height: 100vh;
  padding: $space-4 $space-4 $space-8;
  background: $color-bg;
}

.hero {
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 100%);
  color: #ffffff;
  box-shadow: $shadow-md;
}

.hero__top {
  display: flex;
  justify-content: space-between;
  gap: $space-3;
}

.hero__eyebrow {
  display: block;
  font-size: $font-size-xs;
  opacity: 0.85;
}

.hero__title {
  display: block;
  margin-top: $space-2;
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
}

.hero__subtitle {
  display: block;
  margin-top: $space-2;
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.82);
}

.hero__badge {
  align-self: flex-start;
  padding: 10rpx 18rpx;
  border-radius: $radius-full;
  background: rgba(255, 255, 255, 0.18);
  font-size: $font-size-xs;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-3;
  margin-top: $space-4;
}

.hero-stat {
  padding: $space-3;
  border-radius: $radius-md;
  background: rgba(255, 255, 255, 0.12);
}

.hero-stat__value {
  display: block;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
}

.hero-stat__label {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-xs;
  opacity: 0.8;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin: $space-4 0;
  padding: $space-3 $space-4;
  background: $color-surface;
  border: 2rpx solid $color-border;
  border-radius: $radius-full;
  box-shadow: $shadow-sm;
}

.search-bar__icon {
  font-size: $font-size-lg;
  color: $color-text-muted;
}

.search-bar__placeholder {
  color: $color-text-muted;
  font-size: $font-size-base;
  flex: 1;
}

.search-bar__cta {
  color: $color-primary;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

.login-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-3;
  margin-bottom: $space-4;
}

.login-banner__title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.login-banner__desc {
  display: block;
  margin-top: $space-1;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.login-banner__arrow {
  font-size: $font-size-2xl;
  color: $color-primary;
}

.section {
  margin-bottom: $space-5;
}

.section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $space-3;
}

.section__more {
  font-size: $font-size-sm;
  color: $color-primary;
}

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
  min-height: 152rpx;
}

.quick-card__icon {
  font-size: 48rpx;
  margin-bottom: $space-2;
}

.quick-card__label {
  font-size: $font-size-xs;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
  text-align: center;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
  margin-top: $space-3;
}

.category-card {
  padding: $space-3;
  border-radius: $radius-md;
  background: $color-surface;
  box-shadow: $shadow-sm;
}

.category-card__name {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.category-card__meta {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-xs;
  color: $color-text-muted;
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
  margin-top: $space-3;
}

.book-card {
  display: flex;
  gap: $space-3;
  align-items: stretch;
}

.book-card__cover {
  width: 132rpx;
  height: 176rpx;
  border-radius: $radius-sm;
  background: #d1fae5;
  flex-shrink: 0;
}

.book-card__cover--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-primary-dark;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
}

.book-card__body {
  flex: 1;
  min-width: 0;
}

.book-card__title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.book-card__author,
.book-card__meta {
  display: block;
  margin-top: $space-2;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3;
  background: $color-surface;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;
}

.notice-item__body {
  flex: 1;
  min-width: 0;
}

.notice-item__title {
  display: block;
  font-size: $font-size-base;
  color: $color-text-primary;
  font-weight: $font-weight-medium;
}

.notice-item__date {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-xs;
  color: $color-text-muted;
}

@media screen and (max-width: 360px) {
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>