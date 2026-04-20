<template>
  <view class="page-shell reservation-page">
    <view class="card reservation-card">
      <view v-if="records.length" class="reservation-list">
        <view v-for="item in records" :key="item.id" class="reservation-item">
          <view class="reservation-item__header">
            <text class="reservation-item__title">{{ item.bookTitle }}</text>
            <text class="status-pill" :class="`status-pill--${getReservationStatus(item.status).className}`">{{ getReservationStatus(item.status).label }}</text>
          </view>
          <text class="reservation-item__meta">预约日期：{{ formatDate(item.reserveDate) }}</text>
          <text class="reservation-item__meta">失效日期：{{ formatDate(item.expireDate) }}</text>
          <text class="reservation-item__meta">通知时间：{{ formatDateTime(item.notifyAt) }}</text>
          <view class="reservation-item__footer">
            <text class="reservation-item__meta">当前仅展示本人预约记录</text>
            <button v-if="[0, 1].includes(item.status)" class="cancel-btn" @tap="handleCancel(item)">取消预约</button>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-state__title">暂无预约记录</text>
        <text class="empty-state__desc">当图书无库存时，可在详情页发起预约。</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { cancelReservation, getReservationList } from '../../api/reservation'
import { useUserStore } from '../../store/user'
import { formatDate, formatDateTime } from '../../utils/format'
import { getReservationStatus } from '../../utils/options'

const userStore = useUserStore()
const records = ref([])

async function loadRecords() {
  await userStore.bootstrap()
  if (!userStore.isAuthenticated) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  const data = await getReservationList({ page: 1, size: 100 })
  records.value = data.records || []
}

async function handleCancel(item) {
  await cancelReservation(item.id)
  uni.showToast({ title: '预约已取消', icon: 'success' })
  loadRecords()
}

onLoad(loadRecords)
onShow(loadRecords)
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.reservation-page,
.reservation-card,
.reservation-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.reservation-item {
  padding: $space-3;
  border-radius: $radius-md;
  background: $color-bg;
}

.reservation-item__header,
.reservation-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-3;
}

.reservation-item__title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.reservation-item__meta {
  display: block;
  margin-top: $space-2;
  color: $color-text-secondary;
  font-size: $font-size-sm;
}

.cancel-btn {
  height: 64rpx;
  padding: 0 $space-3;
  border: none;
  border-radius: $radius-md;
  background: #fee2e2;
  color: $color-error;
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