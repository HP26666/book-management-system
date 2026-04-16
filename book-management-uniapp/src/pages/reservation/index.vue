<template>
  <view class="page-container">
    <!-- 未登录 -->
    <view v-if="!isLoggedIn" class="empty-state">
      <u-icon name="lock" size="48" color="#d1d5db" />
      <text style="margin-top: 16rpx;">请先登录查看预约记录</text>
      <u-button
        size="small"
        type="primary"
        :customStyle="{ marginTop: '24rpx', background: '#0f766e', borderColor: '#0f766e' }"
        @click="goLogin"
      >去登录</u-button>
    </view>

    <template v-else>
      <view v-if="loading" class="loading-wrap">
        <u-loading-icon />
      </view>
      <view v-else-if="records.length" class="record-list">
        <view class="record-card card" v-for="item in records" :key="item.id">
          <view class="record-header">
            <text class="record-title">{{ item.bookTitle || `图书#${item.bookId}` }}</text>
            <text class="status-tag" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</text>
          </view>
          <view class="record-body">
            <view class="info-row">
              <text class="info-label">预约日期</text>
              <text class="info-value">{{ formatDate(item.reserveDate) }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">到期日期</text>
              <text class="info-value">{{ formatDate(item.expireDate) }}</text>
            </view>
            <view class="info-row" v-if="item.notifyAt">
              <text class="info-label">通知时间</text>
              <text class="info-value">{{ formatDate(item.notifyAt) }}</text>
            </view>
          </view>
          <view class="record-actions" v-if="item.status === 'WAITING'">
            <u-button
              size="small"
              type="error"
              plain
              @click="handleCancel(item)"
            >取消预约</u-button>
          </view>
        </view>

        <view class="load-more" v-if="hasMore">
          <text class="load-more-text" @click="loadMore">加载更多</text>
        </view>
        <view class="load-more" v-else-if="records.length">
          <text style="color:#c0c4cc;font-size:24rpx;">没有更多了</text>
        </view>
      </view>
      <view v-else class="empty-state">
        <u-icon name="calendar" size="48" color="#d1d5db" />
        <text style="margin-top: 16rpx;">暂无预约记录</text>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getReservations, cancelReservation } from '../../api/reservation'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)

const records = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const hasMore = ref(false)

const statusMap = {
  WAITING: '预约中',
  NOTIFIED: '待取书',
  CANCELLED: '已取消',
  EXPIRED: '已过期'
}
const statusClassMap = {
  WAITING: 'tag-info',
  NOTIFIED: 'tag-success',
  CANCELLED: 'tag-default',
  EXPIRED: 'tag-warning'
}

const getStatusText = (s) => statusMap[s] || s
const getStatusClass = (s) => statusClassMap[s] || 'tag-default'
const formatDate = (d) => d ? d.substring(0, 10) : '-'

const loadRecords = async () => {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    const res = await getReservations({ page: page.value, size: 10 })
    if (res.code === 200 && res.data) {
      if (page.value === 1) {
        records.value = res.data.records || []
      } else {
        records.value.push(...(res.data.records || []))
      }
      total.value = res.data.total || 0
      hasMore.value = records.value.length < total.value
    }
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  loadRecords()
}

const handleCancel = (item) => {
  uni.showModal({
    title: '取消预约',
    content: '确定要取消这条预约吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        const result = await cancelReservation(item.id)
        if (result.code === 200) {
          uni.showToast({ title: '已取消', icon: 'success' })
          page.value = 1
          records.value = []
          loadRecords()
        }
      } catch (e) {
        uni.showToast({ title: e.message || '操作失败', icon: 'none' })
      }
    }
  })
}

const goLogin = () => uni.navigateTo({ url: '/pages/login/index' })

const onShow = () => {
  if (isLoggedIn.value) {
    page.value = 1
    records.value = []
    loadRecords()
  }
}

defineExpose({ onShow })
</script>

<style lang="scss" scoped>
.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.record-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16rpx;
}

.record-body {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
}

.info-label {
  font-size: 26rpx;
  color: #9ca3af;
}

.info-value {
  font-size: 26rpx;
  color: #374151;
}

.record-actions {
  margin-top: 20rpx;
  display: flex;
  justify-content: flex-end;
}

.load-more {
  text-align: center;
  padding: 24rpx;
}

.load-more-text {
  color: #0f766e;
  font-size: 26rpx;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 80rpx 0;
}
</style>
