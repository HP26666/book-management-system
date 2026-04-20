<template>
  <view class="page-shell notices-page">
    <view class="card notice-card" v-for="item in notices" :key="item.id" @tap="toggleNotice(item.id)">
      <view class="notice-card__header">
        <view>
          <text class="notice-card__title">{{ item.title }}</text>
          <text class="notice-card__meta">{{ formatDateTime(item.publishTime) }} · {{ item.publisherName || '系统发布' }}</text>
        </view>
        <text class="status-pill" :class="`status-pill--${getNoticeType(item.type).className}`">{{ getNoticeType(item.type).label }}</text>
      </view>
      <text class="notice-card__content">{{ expandedId === item.id ? item.content : compactText(item.content, 70) }}</text>
      <text class="notice-card__toggle">{{ expandedId === item.id ? '收起' : '展开详情' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getNoticeList } from '../../api/notice'
import { compactText, formatDateTime } from '../../utils/format'
import { getNoticeType } from '../../utils/options'

const notices = ref([])
const expandedId = ref(null)

function toggleNotice(id) {
  expandedId.value = expandedId.value === id ? null : id
}

async function loadNotices() {
  const data = await getNoticeList({ page: 1, size: 50 })
  notices.value = data.records || []
}

onLoad(loadNotices)
onShow(loadNotices)
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.notices-page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.notice-card {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.notice-card__header {
  display: flex;
  justify-content: space-between;
  gap: $space-3;
}

.notice-card__title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.notice-card__meta {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-xs;
  color: $color-text-muted;
}

.notice-card__content {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  line-height: $line-height-loose;
}

.notice-card__toggle {
  font-size: $font-size-sm;
  color: $color-primary;
}
</style>