<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索书名/用户" clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
            <el-option label="等待中" :value="0" />
            <el-option label="已通知" :value="1" />
            <el-option label="已取消" :value="2" />
            <el-option label="已过期" :value="3" />
          </el-select>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="bookTitle" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="username" label="预约人" width="120" />
        <el-table-column prop="reserveDate" label="预约日期" width="110" />
        <el-table-column prop="expireDate" label="到期日期" width="110" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="resStatusType(row.status)" size="small">{{ resStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" v-if="row.status === 0" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getReservationList, cancelReservation } from '@/api/reservation'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: undefined, page: 1, size: 10 })

const resStatusMap = { 0: '等待中', 1: '已通知', 2: '已取消', 3: '已过期' }
const resStatusTypeMap = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
const resStatusLabel = s => resStatusMap[s] || '未知'
const resStatusType = s => resStatusTypeMap[s] || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReservationList(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleCancel = async row => {
  await ElMessageBox.confirm('确定取消该预约？', '提示', { type: 'warning' })
  await cancelReservation(row.id)
  ElMessage.success('已取消')
  loadData()
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
</style>
