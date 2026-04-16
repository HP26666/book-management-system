<template>
  <view class="page-container">
    <!-- Tab 切换 -->
    <view class="tabs-wrap">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'current' }"
        @click="switchTab('current')"
      >当前借阅</view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'history' }"
        @click="switchTab('history')"
      >历史记录</view>
    </view>

    <!-- 未登录提示 -->
    <view v-if="!isLoggedIn" class="empty-state">
      <u-icon name="lock" size="48" color="#d1d5db" />
      <text style="margin-top: 16rpx;">请先登录查看借阅记录</text>
      <u-button
        size="small"
        type="primary"
        :customStyle="{ marginTop: '24rpx', background: '#0f766e', borderColor: '#0f766e' }"
        @click="goLogin"
      >去登录</u-button>
    </view>

    <!-- 借阅列表 -->
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
              <text class="info-label">借阅编号</text>
              <text class="info-value">{{ item.borrowNo }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">借出日期</text>
              <text class="info-value">{{ formatDate(item.borrowDate) }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">到期日期</text>
              <text class="info-value" :class="{ overdue: isOverdue(item) }">{{ formatDate(item.dueDate) }}</text>
            </view>
            <view class="info-row" v-if="item.returnDate">
              <text class="info-label">归还日期</text>
              <text class="info-value">{{ formatDate(item.returnDate) }}</text>
            </view>
            <view class="info-row" v-if="item.fineAmount > 0">
              <text class="info-label">罚金</text>
              <text class="info-value" style="color: #dc2626;">¥{{ item.fineAmount.toFixed(2) }}</text>
            </view>
          </view>
          <view class="record-actions" v-if="currentTab === 'current'">
            <u-button
              v-if="canRenew(item)"
              size="small"
              type="primary"
              :customStyle="{ background: '#0f766e', borderColor: '#0f766e' }"
              @click="handleRenew(item)"
            >续借</u-button>
          </view>
        </view>

        <view class="load-more" v-if="hasMore">
          <text class="load-more-text" @click="loadMore">加载更多</text>
        </view>
      </view>
      <view v-else class="empty-state">
        <u-icon name="order" size="48" color="#d1d5db" />
        <text style="margin-top: 16rpx;">{{ currentTab === 'current' ? '暂无借阅记录' : '暂无历史记录' }}</text>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getBorrows, renewBorrow } from '../../api/borrow'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)

const currentTab = ref('current')
const records = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const hasMore = ref(false)

const statusMap = {
  PENDING: '待审批',
  APPROVED: '已批准',
  BORROWED: '借阅中',
  RETURNED: '已归还',
  REJECTED: '已拒绝',
  OVERDUE: '已逾期'
}

const statusClassMap = {
  PENDING: 'tag-info',
  APPROVED: 'tag-success',
  BORROWED: 'tag-success',
  RETURNED: 'tag-default',
  REJECTED: 'tag-danger',
  OVERDUE: 'tag-danger'
}

const getStatusText = (status) => statusMap[status] || status
const getStatusClass = (status) => statusClassMap[status] || 'tag-default'

const isOverdue = (item) => {
  if (item.status === 'OVERDUE') return true
  if (item.status === 'BORROWED' && item.dueDate) {
    return new Date(item.dueDate) < new Date()
  }
  return false
}

const canRenew = (item) => {
  return (item.status === 'BORROWED' || item.status === 'APPROVED') && item.renewCount < 1 && !isOverdue(item)
}

const formatDate = (d) => d ? d.substring(0, 10) : '-'

const switchTab = (tab) => {
  currentTab.value = tab
  page.value = 1
  records.value = []
  loadRecords()
}

const loadRecords = async () => {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (currentTab.value === 'current') {
      params.status = 'BORROWED'
    } else {
      params.status = 'RETURNED'
    }
    const res = await getBorrows(params)
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

const handleRenew = (item) => {
  uni.showModal({
    title: '确认续借',
    content: `确定续借此书吗？续借后借阅期限延长15天。`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        const result = await renewBorrow(item.id)
        if (result.code === 200) {
          uni.showToast({ title: '续借成功', icon: 'success' })
          switchTab('current')
        }
      } catch (e) {
        uni.showToast({ title: e.message || '续借失败', icon: 'none' })
      }
    }
  })
}

const goLogin = () => uni.navigateTo({ url: '/pages/login/index' })

// 页面显示时加载
const onShow = () => {
  if (isLoggedIn.value) loadRecords()
}

defineExpose({ onShow })
</script>

<style lang="scss" scoped>
.tabs-wrap {
  display: flex;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 22rpx 0;
  font-size: 28rpx;
  color: #6b7280;
  position: relative;
  transition: all 0.2s;

  &.active {
    color: #0f766e;
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 48rpx;
      height: 4rpx;
      background: #0f766e;
      border-radius: 2rpx;
    }
  }
}

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

  &.overdue {
    color: #dc2626;
  }
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
