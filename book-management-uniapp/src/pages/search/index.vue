<template>
  <view class="page-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <u-icon name="search" size="16" color="#9ca3af" />
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索书名、作者、ISBN"
          confirm-type="search"
          @confirm="doSearch"
          focus
        />
        <u-icon v-if="keyword" name="close-circle-fill" size="16" color="#c0c4cc" @click="keyword = ''; doSearch()" />
      </view>
      <text class="search-cancel" @click="goBack">取消</text>
    </view>

    <!-- 分类筛选 -->
    <scroll-view scroll-x class="filter-bar" v-if="categoryList.length">
      <view
        class="filter-tag"
        :class="{ active: !selectedCategoryId }"
        @click="selectCategory(null)"
      >全部</view>
      <view
        v-for="cat in categoryList"
        :key="cat.id"
        class="filter-tag"
        :class="{ active: selectedCategoryId === cat.id }"
        @click="selectCategory(cat.id)"
      >{{ cat.name }}</view>
    </scroll-view>

    <!-- 搜索结果 -->
    <view v-if="loading && page === 1" class="loading-wrap">
      <u-loading-icon />
    </view>
    <view v-else-if="books.length" class="book-list">
      <view class="book-card" v-for="book in books" :key="book.id" @click="goDetail(book.id)">
        <image
          class="book-cover"
          :src="book.coverUrl || '/static/default-cover.png'"
          mode="aspectFill"
        />
        <view class="book-info">
          <text class="book-title">{{ book.title }}</text>
          <text class="book-author">{{ book.author }} · {{ book.publisher }}</text>
          <text class="book-isbn" v-if="book.isbn">ISBN: {{ book.isbn }}</text>
          <view class="book-bottom">
            <text class="status-tag" :class="book.availableStock > 0 ? 'tag-success' : 'tag-warning'">
              {{ book.availableStock > 0 ? `可借 ${book.availableStock}` : '已借完' }}
            </text>
            <text class="book-location" v-if="book.location">📍{{ book.location }}</text>
          </view>
        </view>
      </view>

      <view class="load-more" v-if="hasMore">
        <u-loading-icon v-if="loading" size="20" />
        <text v-else class="load-more-text" @click="loadMore">加载更多</text>
      </view>
      <view class="load-more" v-else>
        <text class="no-more-text">没有更多了</text>
      </view>
    </view>
    <view v-else-if="searched" class="empty-state">
      <u-icon name="search" size="48" color="#d1d5db" />
      <text style="margin-top: 16rpx;">未找到相关图书</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBooks, getCategoryTree } from '../../api/book'

const keyword = ref('')
const selectedCategoryId = ref(null)
const books = ref([])
const categoryList = ref([])
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const searched = ref(false)
const hasMore = ref(false)

// 接收页面参数
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage?.$page?.options || currentPage?.options || {}
  if (options.keyword) keyword.value = options.keyword
  if (options.categoryId) selectedCategoryId.value = Number(options.categoryId)
  loadCategories()
  doSearch()
})

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    if (res.code === 200) categoryList.value = res.data || []
  } catch (_) { /* ignore */ }
}

const doSearch = async () => {
  page.value = 1
  books.value = []
  searched.value = true
  await fetchBooks()
}

const fetchBooks = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (keyword.value) params.keyword = keyword.value
    if (selectedCategoryId.value) params.categoryId = selectedCategoryId.value
    const res = await getBooks(params)
    if (res.code === 200 && res.data) {
      if (page.value === 1) {
        books.value = res.data.records || []
      } else {
        books.value.push(...(res.data.records || []))
      }
      total.value = res.data.total || 0
      hasMore.value = books.value.length < total.value
    }
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  fetchBooks()
}

const selectCategory = (id) => {
  selectedCategoryId.value = id
  doSearch()
}

const goDetail = (id) => uni.navigateTo({ url: `/pages/book/detail?id=${id}` })
const goBack = () => uni.navigateBack()
</script>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: #fff;
  border-radius: 40rpx;
  padding: 14rpx 20rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
}

.search-cancel {
  color: #0f766e;
  font-size: 28rpx;
  white-space: nowrap;
}

.filter-bar {
  white-space: nowrap;
  margin-bottom: 20rpx;
  padding: 4rpx 0;
}

.filter-tag {
  display: inline-block;
  padding: 10rpx 24rpx;
  margin-right: 12rpx;
  border-radius: 28rpx;
  font-size: 24rpx;
  background: #fff;
  color: #6b7280;

  &.active {
    background: #0f766e;
    color: #fff;
  }
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.book-card {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
}

.book-cover {
  width: 150rpx;
  height: 200rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  background: #e5e7eb;
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
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
  font-size: 24rpx;
  color: #6b7280;
}

.book-isbn {
  font-size: 22rpx;
  color: #9ca3af;
}

.book-bottom {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.book-location {
  font-size: 22rpx;
  color: #9ca3af;
}

.load-more {
  text-align: center;
  padding: 24rpx;
}

.load-more-text {
  color: #0f766e;
  font-size: 26rpx;
}

.no-more-text {
  color: #c0c4cc;
  font-size: 24rpx;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 80rpx 0;
}
</style>
