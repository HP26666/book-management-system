<template>
  <view class="page-shell borrow-page">
    <view class="tab-bar card">
      <view class="tab-bar__item" :class="{ 'tab-bar__item--active': activeTab === 'current' }" @tap="activeTab = 'current'">当前借阅</view>
      <view class="tab-bar__item" :class="{ 'tab-bar__item--active': activeTab === 'history' }" @tap="activeTab = 'history'">历史借阅</view>
    </view>

    <view class="card list-card">
      <view v-if="filteredRecords.length" class="borrow-list">
        <view v-for="item in filteredRecords" :key="item.id" class="borrow-item">
          <view class="borrow-item__header">
            <text class="borrow-item__title">{{ item.bookTitle }}</text>
            <text class="status-pill" :class="`status-pill--${getBorrowStatus(item.status).className}`">{{ getBorrowStatus(item.status).label }}</text>
          </view>
          <text class="borrow-item__meta">借阅单号：{{ item.borrowNo }}</text>
          <text class="borrow-item__meta">借阅日期：{{ formatDate(item.borrowDate) }}</text>
          <text class="borrow-item__meta">应还日期：{{ formatDate(item.dueDate) }}</text>
          <text class="borrow-item__meta">罚金：{{ formatMoney(item.fineAmount) }}</text>
          <view class="borrow-item__footer">
            <text class="borrow-item__meta">续借次数：{{ item.renewCount || 0 }}</text>
            <button v-if="[1, 2].includes(item.status)" class="renew-btn" @tap="handleRenew(item)">续借</button>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-state__title">暂无记录</text>
        <text class="empty-state__desc">去首页搜索图书并提交借阅申请。</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getBorrowList, renewBorrow } from '../../api/borrow'
import { useUserStore } from '../../store/user'
import { formatDate, formatMoney } from '../../utils/format'
import { getBorrowStatus } from '../../utils/options'

const userStore = useUserStore()
const activeTab = ref('current')
const records = ref([])

const filteredRecords = computed(() => {
  if (activeTab.value === 'current') {
    return records.value.filter(item => [0, 1, 2, 5].includes(item.status))
  }
  return records.value.filter(item => [3, 4].includes(item.status))
})

async function loadRecords() {
  await userStore.bootstrap()
  if (!userStore.isAuthenticated) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  const data = await getBorrowList({ page: 1, size: 100 })
  records.value = data.records || []
}

async function handleRenew(item) {
  await renewBorrow(item.id)
  uni.showToast({ title: '续借成功', icon: 'success' })
  loadRecords()
}

onLoad(loadRecords)
onShow(loadRecords)
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.borrow-page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.tab-bar {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  padding: $space-1;
  gap: $space-1;
}

.tab-bar__item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 76rpx;
  border-radius: $radius-md;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.tab-bar__item--active {
  background: #e6fffb;
  color: $color-primary-dark;
  font-weight: $font-weight-bold;
}

.list-card,
.borrow-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.borrow-item {
  padding: $space-3;
  border-radius: $radius-md;
  background: $color-bg;
}

.borrow-item__header,
.borrow-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-3;
}

.borrow-item__title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.borrow-item__meta {
  display: block;
  margin-top: $space-2;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.renew-btn {
  height: 64rpx;
  padding: 0 $space-3;
  border: none;
  border-radius: $radius-md;
  background: $color-primary;
  color: #ffffff;
  font-size: $font-size-sm;
}

.empty-state {
  padding: $space-6 0;
  text-align: center;
}

.empty-state__title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.empty-state__desc {
  display: block;
  margin-top: $space-2;
  color: $color-text-muted;
  font-size: $font-size-sm;
}
</style>