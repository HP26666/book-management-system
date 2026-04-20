<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2 class="page-header__title">预约管理</h2>
        <p class="page-header__subtitle">{{ userStore.isManager ? '查看全部预约记录与通知状态' : '查看和取消我的预约记录' }}</p>
      </div>
    </div>

    <el-card>
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item>
          <el-select v-model="queryForm.status" clearable placeholder="状态" style="width: 140px">
            <el-option v-for="item in RESERVATION_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-empty v-if="!loading && records.length === 0" description="暂无预约记录" />
      <el-table v-else v-loading="loading" :data="records" stripe row-key="id">
        <el-table-column prop="bookTitle" label="图书" min-width="200" show-overflow-tooltip />
        <el-table-column v-if="userStore.isManager" prop="realName" label="读者" min-width="120" />
        <el-table-column label="预约日期" width="120">
          <template #default="{ row }">{{ formatDate(row.reserveDate) }}</template>
        </el-table-column>
        <el-table-column label="失效日期" width="120">
          <template #default="{ row }">{{ formatDate(row.expireDate) }}</template>
        </el-table-column>
        <el-table-column label="通知时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.notifyAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="findOptionTag(RESERVATION_STATUS_OPTIONS, row.status)">{{ findOptionLabel(RESERVATION_STATUS_OPTIONS, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="[0, 1].includes(row.status)" link type="danger" @click="handleCancel(row)">取消预约</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="loadRecords"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelReservation, getReservationList } from '../api/reservation'
import { RESERVATION_STATUS_OPTIONS, findOptionLabel, findOptionTag } from '../constants/options'
import { useUserStore } from '../store/user'
import { formatDate, formatDateTime } from '../utils/format'

const userStore = useUserStore()
const loading = ref(false)
const records = ref([])

const queryForm = reactive({
  status: undefined
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

function sanitizeParams(payload) {
  return Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== undefined && value !== null && value !== ''))
}

async function loadRecords() {
  loading.value = true
  try {
    const data = await getReservationList(sanitizeParams({
      ...queryForm,
      page: page.current,
      size: page.size
    }))
    records.value = data.records || []
    page.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  page.current = 1
  loadRecords()
}

function resetQuery() {
  queryForm.status = undefined
  page.current = 1
  loadRecords()
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确认取消《${row.bookTitle}》的预约？`, '取消预约', { type: 'warning' })
  await cancelReservation(row.id)
  ElMessage.success('预约已取消')
  loadRecords()
}

onMounted(loadRecords)
</script>

<style lang="scss" scoped>
@use '../styles/variables' as *;

.page-shell {
  display: flex;
  flex-direction: column;
  gap: $space-6;
}

.page-header__title {
  font-size: $font-size-2xl;
}

.page-header__subtitle {
  color: $color-text-muted;
  margin-top: $space-1;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: $space-4;
}
</style>