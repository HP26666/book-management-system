<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff"><el-icon :size="28"><Reading /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalBooks ?? '-' }}</div>
            <div class="stat-label">图书总量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a"><el-icon :size="28"><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalReaders ?? '-' }}</div>
            <div class="stat-label">读者人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c"><el-icon :size="28"><Tickets /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeBorrows ?? '-' }}</div>
            <div class="stat-label">在借图书</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c"><el-icon :size="28"><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.overdueBorrows ?? '-' }}</div>
            <div class="stat-label">逾期未还</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>借阅趋势</span></template>
          <div ref="chartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>热门图书 TOP 5</span></template>
          <div v-if="stats.topBooks?.length" class="top-books">
            <div v-for="(book, i) in stats.topBooks" :key="i" class="top-book-item">
              <span class="rank" :class="{ 'top3': i < 3 }">{{ i + 1 }}</span>
              <span class="book-title">{{ book.title }}</span>
              <span class="book-count">{{ book.borrowCount }}次</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getDashboardStats } from '@/api/statistics'
import * as echarts from 'echarts'

const stats = ref({})
const chartRef = ref()
let chart = null

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data || {}
    renderChart()
  } catch {}
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const trend = stats.value.monthlyTrend || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: trend.map(t => t.month), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '借阅量',
      type: 'line',
      smooth: true,
      data: trend.map(t => t.count),
      areaStyle: { opacity: 0.15 },
      itemStyle: { color: '#409eff' }
    }]
  })
}

const handleResize = () => chart?.resize()

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style lang="scss" scoped>
.stat-cards .stat-card {
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
  }
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-info {
  .stat-value { font-size: 28px; font-weight: 700; color: #303133; line-height: 1; }
  .stat-label { font-size: 13px; color: #909399; margin-top: 6px; }
}
.top-books {
  .top-book-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
    &:last-child { border-bottom: none; }
  }
  .rank {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    background: #f0f0f0;
    color: #909399;
    margin-right: 12px;
    flex-shrink: 0;
    &.top3 { background: #409eff; color: #fff; }
  }
  .book-title { flex: 1; font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .book-count { color: #909399; font-size: 13px; margin-left: 8px; }
}
</style>
