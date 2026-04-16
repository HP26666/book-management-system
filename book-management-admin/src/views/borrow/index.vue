<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索借阅编号/书名" clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
            <el-option label="待审批" :value="0" />
            <el-option label="已批准" :value="1" />
            <el-option label="借阅中" :value="2" />
            <el-option label="已归还" :value="3" />
            <el-option label="已拒绝" :value="4" />
            <el-option label="已逾期" :value="5" />
          </el-select>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="borrowNo" label="借阅编号" width="190" />
        <el-table-column prop="bookTitle" label="书名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="username" label="借阅人" width="110" />
        <el-table-column prop="borrowDate" label="借阅日期" width="110" />
        <el-table-column prop="dueDate" label="应还日期" width="110" />
        <el-table-column prop="returnDate" label="归还日期" width="110" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="renewCount" label="续借" width="60" align="center" />
        <el-table-column label="罚款(元)" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.fineAmount > 0" class="text-danger">{{ row.fineAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" v-if="row.status === 0" @click="handleApprove(row)">批准</el-button>
            <el-button link type="danger" v-if="row.status === 0" @click="handleReject(row)">拒绝</el-button>
            <el-button link type="primary" v-if="row.status === 2 || row.status === 5" @click="handleReturn(row)">确认归还</el-button>
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
import { getBorrowList, approveBorrow, rejectBorrow, confirmReturn } from '@/api/borrow'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: undefined, page: 1, size: 10 })

const statusMap = { 0: '待审批', 1: '已批准', 2: '借阅中', 3: '已归还', 4: '已拒绝', 5: '已逾期' }
const statusTypeMap = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'danger', 5: 'danger' }
const statusLabel = s => statusMap[s] || '未知'
const statusType = s => statusTypeMap[s] || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBorrowList(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleApprove = async row => {
  await ElMessageBox.confirm('确定批准该借阅申请？', '提示', { type: 'info' })
  await approveBorrow(row.id)
  ElMessage.success('已批准')
  loadData()
}

const handleReject = async row => {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝借阅', { confirmButtonText: '确定', cancelButtonText: '取消', inputPlaceholder: '拒绝原因' })
  await rejectBorrow(row.id, value)
  ElMessage.success('已拒绝')
  loadData()
}

const handleReturn = async row => {
  await ElMessageBox.confirm('确认该图书已归还？', '提示', { type: 'info' })
  await confirmReturn(row.id)
  ElMessage.success('归还成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
.text-danger { color: #f56c6c; font-weight: 600; }
</style>
