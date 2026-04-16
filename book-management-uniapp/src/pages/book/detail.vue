<template>
  <view class="page-container">
    <view v-if="loading" class="loading-wrap">
      <u-loading-icon />
    </view>
    <template v-else-if="book">
      <!-- 封面与基本信息 -->
      <view class="book-header card">
        <image
          class="cover"
          :src="book.coverUrl || '/static/default-cover.png'"
          mode="aspectFill"
        />
        <view class="header-info">
          <text class="title">{{ book.title }}</text>
          <text class="meta">作者：{{ book.author }}</text>
          <text class="meta">出版社：{{ book.publisher }}</text>
          <text class="meta" v-if="book.publishDate">出版日期：{{ book.publishDate }}</text>
          <text class="meta" v-if="book.isbn">ISBN：{{ book.isbn }}</text>
          <view class="stock-info">
            <text class="status-tag" :class="book.availableStock > 0 ? 'tag-success' : 'tag-warning'">
              {{ book.availableStock > 0 ? `可借 ${book.availableStock} 本` : '暂无库存' }}
            </text>
            <text class="meta" v-if="book.location">📍{{ book.location }}</text>
          </view>
        </view>
      </view>

      <!-- 图书简介 -->
      <view class="card" v-if="book.summary">
        <view class="section-title">图书简介</view>
        <text class="summary-text">{{ book.summary }}</text>
      </view>

      <!-- 详细信息 -->
      <view class="card">
        <view class="section-title">详细信息</view>
        <view class="detail-row">
          <text class="detail-label">总库存</text>
          <text class="detail-value">{{ book.totalStock }} 本</text>
        </view>
        <view class="detail-row">
          <text class="detail-label">可借数量</text>
          <text class="detail-value">{{ book.availableStock }} 本</text>
        </view>
        <view class="detail-row" v-if="book.price">
          <text class="detail-label">定价</text>
          <text class="detail-value">¥{{ book.price }}</text>
        </view>
        <view class="detail-row" v-if="book.categoryName">
          <text class="detail-label">分类</text>
          <text class="detail-value">{{ book.categoryName }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-bar" v-if="isLoggedIn">
        <u-button
          v-if="book.availableStock > 0 && book.status === 1"
          type="primary"
          :customStyle="{ background: '#0f766e', borderColor: '#0f766e' }"
          @click="handleBorrow"
          :loading="actionLoading"
          :disabled="actionLoading"
        >申请借阅</u-button>
        <u-button
          v-else-if="book.availableStock === 0 && book.status === 1"
          type="warning"
          @click="handleReserve"
          :loading="actionLoading"
          :disabled="actionLoading"
        >预约此书</u-button>
        <u-button v-else type="default" disabled>暂不可借</u-button>
      </view>
      <view class="action-bar" v-else>
        <u-button type="primary" :customStyle="{ background: '#0f766e', borderColor: '#0f766e' }" @click="goLogin">登录后操作</u-button>
      </view>
    </template>
    <view v-else class="empty-state">
      <text>图书不存在</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getBookDetail } from '../../api/book'
import { applyBorrow } from '../../api/borrow'
import { createReservation } from '../../api/reservation'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)

const book = ref(null)
const loading = ref(true)
const actionLoading = ref(false)
let bookId = null

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage?.$page?.options || currentPage?.options || {}
  bookId = options.id
  if (bookId) loadBook()
  else loading.value = false
})

const loadBook = async () => {
  loading.value = true
  try {
    const res = await getBookDetail(bookId)
    if (res.code === 200) book.value = res.data
  } finally {
    loading.value = false
  }
}

const handleBorrow = async () => {
  uni.showModal({
    title: '确认借阅',
    content: `确定要借阅《${book.value.title}》吗？借阅期限30天。`,
    success: async (modalRes) => {
      if (!modalRes.confirm) return
      actionLoading.value = true
      try {
        const res = await applyBorrow({ bookId: Number(bookId), borrowDays: 30 })
        if (res.code === 200) {
          uni.showToast({ title: '借阅申请已提交', icon: 'success' })
          loadBook()
        }
      } catch (e) {
        uni.showToast({ title: e.message || '操作失败', icon: 'none' })
      } finally {
        actionLoading.value = false
      }
    }
  })
}

const handleReserve = async () => {
  uni.showModal({
    title: '确认预约',
    content: `确定要预约《${book.value.title}》吗？预约有效期3天。`,
    success: async (modalRes) => {
      if (!modalRes.confirm) return
      actionLoading.value = true
      try {
        const res = await createReservation({ bookId: Number(bookId) })
        if (res.code === 200) {
          uni.showToast({ title: '预约成功', icon: 'success' })
          loadBook()
        }
      } catch (e) {
        uni.showToast({ title: e.message || '操作失败', icon: 'none' })
      } finally {
        actionLoading.value = false
      }
    }
  })
}

const goLogin = () => uni.navigateTo({ url: '/pages/login/index' })
</script>

<style lang="scss" scoped>
.book-header {
  display: flex;
  gap: 24rpx;
}

.cover {
  width: 220rpx;
  height: 300rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  background: #e5e7eb;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.title {
  font-size: 34rpx;
  font-weight: 700;
  color: #111827;
  line-height: 1.3;
  margin-bottom: 4rpx;
}

.meta {
  font-size: 24rpx;
  color: #6b7280;
}

.stock-info {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.summary-text {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.8;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.detail-label {
  font-size: 26rpx;
  color: #6b7280;
}

.detail-value {
  font-size: 26rpx;
  color: #111827;
  font-weight: 500;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 120rpx 0;
}
</style>
