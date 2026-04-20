<template>
  <view class="page-shell detail-page" v-if="book">
    <view class="card detail-hero">
      <image v-if="coverUrl" :src="coverUrl" class="detail-hero__cover" mode="aspectFill" />
      <view v-else class="detail-hero__cover detail-hero__cover--placeholder">BOOK</view>
      <view class="detail-hero__body">
        <text class="detail-hero__title">{{ book.title }}</text>
        <text class="detail-hero__author">{{ book.author || '未知作者' }}</text>
        <text class="detail-hero__meta">{{ book.publisher || '未知出版社' }}</text>
        <text class="detail-hero__meta">ISBN：{{ book.isbn || '-' }}</text>
        <text class="detail-hero__meta">馆藏位置：{{ book.location || '-' }}</text>
        <text class="status-pill" :class="book.availableStock > 0 ? 'status-pill--success' : 'status-pill--warning'">
          {{ book.availableStock > 0 ? `可借 ${book.availableStock} 本` : '当前无库存，可预约' }}
        </text>
      </view>
    </view>

    <view class="card info-card">
      <text class="section-title">图书简介</text>
      <text class="info-card__summary">{{ book.summary || '暂无简介' }}</text>
      <view class="info-grid">
        <view class="info-grid__item"><text>分类</text><text>{{ book.categoryName || '-' }}</text></view>
        <view class="info-grid__item"><text>出版日期</text><text>{{ formatDate(book.publishDate) }}</text></view>
        <view class="info-grid__item"><text>价格</text><text>{{ formatMoney(book.price) }}</text></view>
        <view class="info-grid__item"><text>库存</text><text>{{ book.availableStock }}/{{ book.totalStock }}</text></view>
      </view>
    </view>

    <view class="card action-card">
      <text class="section-title">操作</text>
      <text class="section-subtitle">有库存时可提交借阅申请，无库存时可发起预约。</text>
      <view class="action-card__buttons">
        <button class="btn btn--primary" :disabled="!canBorrow" @tap="handleBorrow">借阅 30 天</button>
        <button class="btn btn--ghost" :disabled="!canReserve" @tap="handleReserve">无库存时预约</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { applyBorrow } from '../../api/borrow'
import { getBookDetail } from '../../api/book'
import { createReservation } from '../../api/reservation'
import { useUserStore } from '../../store/user'
import { getBaseUrl } from '../../utils/config'
import { formatDate, formatMoney } from '../../utils/format'

const userStore = useUserStore()
const book = ref(null)

const coverUrl = computed(() => {
  if (!book.value?.coverUrl) {
    return ''
  }
  if (/^https?:\/\//.test(book.value.coverUrl)) {
    return book.value.coverUrl
  }
  return `${getBaseUrl().replace(/\/api$/, '')}${book.value.coverUrl}`
})

const canBorrow = computed(() => Number(book.value?.status) === 1 && Number(book.value?.availableStock) > 0)
const canReserve = computed(() => Number(book.value?.status) === 1 && Number(book.value?.availableStock) <= 0)

function ensureLogin() {
  if (userStore.isAuthenticated) {
    return true
  }
  uni.navigateTo({ url: '/pages/login/index' })
  return false
}

async function loadDetail(id) {
  book.value = await getBookDetail(id)
}

async function handleBorrow() {
  if (!ensureLogin()) {
    return
  }
  await applyBorrow({ bookId: book.value.id, borrowDays: 30 })
  uni.showToast({ title: '借阅申请已提交', icon: 'success' })
  loadDetail(book.value.id)
}

async function handleReserve() {
  if (!ensureLogin()) {
    return
  }
  await createReservation({ bookId: book.value.id })
  uni.showToast({ title: '预约成功', icon: 'success' })
}

onLoad(async (params) => {
  await userStore.bootstrap()
  if (params.id) {
    await loadDetail(params.id)
  }
})
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.detail-page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.detail-hero {
  display: flex;
  gap: $space-3;
}

.detail-hero__cover {
  width: 200rpx;
  height: 280rpx;
  border-radius: $radius-md;
  background: #ccfbf1;
  flex-shrink: 0;
}

.detail-hero__cover--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-primary-dark;
  font-weight: $font-weight-bold;
}

.detail-hero__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.detail-hero__title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
}

.detail-hero__author,
.detail-hero__meta {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.info-card,
.action-card {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.info-card__summary {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  line-height: $line-height-loose;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
}

.info-grid__item {
  display: flex;
  flex-direction: column;
  gap: $space-1;
  padding: $space-3;
  border-radius: $radius-md;
  background: $color-bg;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.action-card__buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
}

.btn {
  height: 88rpx;
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
</style>