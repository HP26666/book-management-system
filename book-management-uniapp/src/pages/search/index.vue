<template>
  <view class="page-shell search-page">
    <view class="card search-panel">
      <view class="search-bar">
        <input v-model="query.keyword" class="search-bar__input" placeholder="搜索书名、作者或 ISBN" confirm-type="search" @confirm="handleSearch" />
        <button class="search-bar__btn" @tap="handleSearch">搜索</button>
      </view>
      <view class="search-tools">
        <button class="tool-btn" @tap="scanIsbn">扫码 ISBN</button>
        <picker mode="selector" :range="categoryOptions" range-key="displayName" @change="handleCategoryChange">
          <view class="tool-btn">{{ selectedCategoryLabel }}</view>
        </picker>
      </view>
    </view>

    <view class="card result-panel">
      <view class="result-panel__header">
        <text class="section-title">搜索结果</text>
        <text class="section-subtitle">共 {{ page.total }} 本图书</text>
      </view>

      <view v-if="books.length" class="result-list">
        <view v-for="book in books" :key="book.id" class="result-item" @tap="openDetail(book.id)">
          <view class="result-item__body">
            <text class="result-item__title">{{ book.title }}</text>
            <text class="result-item__meta">{{ book.author || '未知作者' }} · {{ book.categoryName || '未分类' }}</text>
            <text class="result-item__meta">{{ book.availableStock > 0 ? `可借库存 ${book.availableStock}` : '当前仅可预约' }}</text>
          </view>
          <text class="status-pill" :class="book.availableStock > 0 ? 'status-pill--success' : 'status-pill--warning'">
            {{ book.availableStock > 0 ? '可借' : '可预约' }}
          </text>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-state__title">暂无匹配图书</text>
        <text class="empty-state__desc">可以换个关键词，或者直接扫码 ISBN 搜索。</text>
      </view>

      <button v-if="books.length < page.total" class="load-more" :loading="loadingMore" @tap="loadMore">加载更多</button>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBookList } from '../../api/book'
import { getCategoryTree } from '../../api/category'
import { flattenCategoryTree } from '../../utils/tree'

const loadingMore = ref(false)
const books = ref([])
const categoryOptions = ref([{ id: '', name: '全部分类', displayName: '全部分类' }])
const selectedCategoryIndex = ref(0)

const query = reactive({
  keyword: '',
  categoryId: ''
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const selectedCategoryLabel = computed(() => categoryOptions.value[selectedCategoryIndex.value]?.displayName || '全部分类')

async function loadCategories() {
  const tree = await getCategoryTree()
  categoryOptions.value = [{ id: '', name: '全部分类', displayName: '全部分类' }, ...flattenCategoryTree(tree)]
  if (query.categoryId) {
    const index = categoryOptions.value.findIndex(item => String(item.id) === String(query.categoryId))
    selectedCategoryIndex.value = index > -1 ? index : 0
  }
}

async function fetchBooks(reset = true) {
  if (reset) {
    page.current = 1
  }
  loadingMore.value = true
  try {
    const data = await getBookList({
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      page: page.current,
      size: page.size,
      status: 1
    })
    page.total = data.total || 0
    books.value = reset ? (data.records || []) : books.value.concat(data.records || [])
  } finally {
    loadingMore.value = false
  }
}

function handleSearch() {
  fetchBooks(true)
}

function handleCategoryChange(event) {
  selectedCategoryIndex.value = Number(event.detail.value)
  const option = categoryOptions.value[selectedCategoryIndex.value]
  query.categoryId = option?.id || ''
  fetchBooks(true)
}

function openDetail(id) {
  uni.navigateTo({ url: `/pages/book-detail/index?id=${id}` })
}

async function scanIsbn() {
  try {
    const result = await uni.scanCode({ onlyFromCamera: false, scanType: ['barCode', 'qrCode'] })
    query.keyword = result.result || ''
    fetchBooks(true)
  } catch {
    uni.showToast({ title: '已取消扫码', icon: 'none' })
  }
}

function loadMore() {
  if (books.value.length >= page.total) {
    return
  }
  page.current += 1
  fetchBooks(false)
}

onLoad(async (params) => {
  query.keyword = params.keyword ? decodeURIComponent(params.keyword) : ''
  query.categoryId = params.categoryId || ''
  await loadCategories()
  await fetchBooks(true)
})
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.search-page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.search-panel,
.result-panel {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.search-bar {
  display: flex;
  gap: $space-2;
}

.search-bar__input {
  flex: 1;
  height: 84rpx;
  padding: 0 $space-3;
  border-radius: $radius-md;
  background: $color-bg;
  border: 2rpx solid $color-border;
}

.search-bar__btn,
.tool-btn,
.load-more {
  height: 84rpx;
  border-radius: $radius-md;
  border: none;
}

.search-bar__btn,
.load-more {
  padding: 0 $space-4;
  background: $color-primary;
  color: #ffffff;
}

.search-tools {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
}

.tool-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e6fffb;
  color: $color-primary-dark;
  font-size: $font-size-sm;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-3;
  padding: $space-3;
  border-radius: $radius-md;
  background: $color-bg;
}

.result-item__body {
  flex: 1;
  min-width: 0;
}

.result-item__title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.result-item__meta {
  display: block;
  margin-top: $space-1;
  font-size: $font-size-sm;
  color: $color-text-secondary;
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
  font-size: $font-size-sm;
  color: $color-text-muted;
}
</style>