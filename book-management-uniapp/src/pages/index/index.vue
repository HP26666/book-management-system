<template>
  <view class="page-container">
    <!-- 搜索入口 -->
    <view class="search-entry" @click="goSearch">
      <view class="search-box">
        <u-icon name="search" size="18" color="#9ca3af" />
        <text class="search-placeholder">搜索书名、作者、ISBN</text>
      </view>
      <view class="scan-btn" @click.stop="scanIsbn">
        <u-icon name="scan" size="22" color="#0f766e" />
      </view>
    </view>

    <!-- 公告栏 -->
    <view class="notice-bar card" v-if="notices.length" @click="onNoticeClick">
      <u-notice-bar
        :text="noticeTexts"
        mode="closable"
        color="#d97706"
        bgColor="#fffbeb"
        speed="60"
      />
    </view>

    <!-- 分类入口 -->
    <view class="card">
      <view class="section-title">图书分类</view>
      <view class="category-grid">
        <view
          class="category-item"
          v-for="cat in categories"
          :key="cat.id"
          @click="goCategory(cat)"
        >
          <view class="cat-icon" :style="{ background: getCatColor(cat.id) }">
            <text class="cat-emoji">{{ getCatEmoji(cat.name) }}</text>
          </view>
          <text class="cat-name">{{ cat.name }}</text>
        </view>
      </view>
    </view>

    <!-- 热门图书 -->
    <view class="card">
      <view class="section-title">热门图书</view>
      <view v-if="loading" class="loading-wrap">
        <u-loading-icon />
      </view>
      <view v-else-if="hotBooks.length" class="book-list">
        <view
          class="book-card"
          v-for="book in hotBooks"
          :key="book.id"
          @click="goBookDetail(book.id)"
        >
          <image
            class="book-cover"
            :src="book.coverUrl || '/static/default-cover.png'"
            mode="aspectFill"
          />
          <view class="book-info">
            <text class="book-title">{{ book.title }}</text>
            <text class="book-author">{{ book.author }}</text>
            <text class="book-publisher">{{ book.publisher }}</text>
            <view class="book-bottom">
              <text
                class="status-tag"
                :class="book.availableStock > 0 ? 'tag-success' : 'tag-warning'"
              >
                {{ book.availableStock > 0 ? `可借 ${book.availableStock}` : '已借完' }}
              </text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <u-icon name="book" size="48" color="#d1d5db" />
        <text style="margin-top: 16rpx;">暂无图书</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBooks, getCategoryTree } from '../../api/book'
import { getNotices } from '../../api/notice'

const notices = ref([])
const noticeTexts = ref([])
const categories = ref([])
const hotBooks = ref([])
const loading = ref(false)

const catColors = ['#ecfdf5', '#eff6ff', '#fef3c7', '#fce7f3', '#ede9fe', '#fed7d7', '#e0f2fe', '#f0fdf4']
const catEmojis = { '文学': '📖', '历史': '🏛️', '科技': '🔬', '教育': '📚', '经济': '💰', '艺术': '🎨', '哲学': '🤔', '社科': '🌍', '计算机': '💻', '法律': '⚖️', '医学': '🏥', '自然': '🌿' }

const getCatColor = (id) => catColors[(id - 1) % catColors.length]
const getCatEmoji = (name) => {
  for (const [key, emoji] of Object.entries(catEmojis)) {
    if (name.includes(key)) return emoji
  }
  return '📁'
}

const loadData = async () => {
  loading.value = true
  try {
    const [noticeRes, catRes, bookRes] = await Promise.all([
      getNotices({ page: 1, size: 5 }).catch(() => null),
      getCategoryTree().catch(() => null),
      getBooks({ page: 1, size: 10, sort: 'createdAt,desc' }).catch(() => null)
    ])
    if (noticeRes?.code === 200 && noticeRes.data?.records) {
      notices.value = noticeRes.data.records
      noticeTexts.value = notices.value.map(n => n.title)
    }
    if (catRes?.code === 200) {
      categories.value = (catRes.data || []).slice(0, 8)
    }
    if (bookRes?.code === 200 && bookRes.data?.records) {
      hotBooks.value = bookRes.data.records
    }
  } finally {
    loading.value = false
  }
}

const goSearch = () => uni.navigateTo({ url: '/pages/search/index' })
const goCategory = (cat) => uni.navigateTo({ url: `/pages/search/index?categoryId=${cat.id}&categoryName=${cat.name}` })
const goBookDetail = (id) => uni.navigateTo({ url: `/pages/book/detail?id=${id}` })

const scanIsbn = () => {
  uni.scanCode({
    scanType: ['barCode'],
    success: (res) => {
      if (res.result) {
        uni.navigateTo({ url: `/pages/search/index?keyword=${res.result}` })
      }
    }
  })
}

const onNoticeClick = () => {
  if (notices.value.length) {
    // 可以导航到公告详情
  }
}

onMounted(() => loadData())

// 下拉刷新
defineExpose({
  onPullDownRefresh() {
    loadData().finally(() => uni.stopPullDownRefresh())
  }
})
</script>

<style lang="scss" scoped>
.search-entry {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: #fff;
  border-radius: 40rpx;
  padding: 18rpx 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.search-placeholder {
  color: #9ca3af;
  font-size: 28rpx;
}

.scan-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.notice-bar {
  padding: 0;
  overflow: hidden;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.category-item {
  width: calc(25% - 12rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 0;
}

.cat-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cat-emoji {
  font-size: 36rpx;
}

.cat-name {
  font-size: 24rpx;
  color: #4b5563;
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.book-card {
  display: flex;
  gap: 20rpx;
  padding: 16rpx;
  border-radius: 12rpx;
  background: #fafbfc;
}

.book-cover {
  width: 160rpx;
  height: 210rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  background: #e5e7eb;
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.book-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 26rpx;
  color: #6b7280;
}

.book-publisher {
  font-size: 24rpx;
  color: #9ca3af;
}

.book-bottom {
  margin-top: auto;
  display: flex;
  align-items: center;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 60rpx 0;
}
</style>
