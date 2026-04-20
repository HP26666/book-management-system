<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2 class="page-header__title">借阅管理</h2>
        <p class="page-header__subtitle">{{ userStore.isManager ? '处理审批、归还与借阅流转' : '查看个人借阅记录并发起续借' }}</p>
      </div>
    </div>

    <el-card>
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item>
          <el-input v-model="queryForm.keyword" clearable placeholder="搜索单号、书名或读者" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.status" clearable placeholder="状态" style="width: 140px">
            <el-option v-for="item in BORROW_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-empty v-if="!loading && records.length === 0" description="暂无借阅记录" />
      <el-table v-else v-loading="loading" :data="records" stripe row-key="id">        <el-table-column prop="borrowNo" label="借阅单号" min-width="160" />
        <el-table-column prop="bookTitle" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="userStore.isManager" prop="realName" label="读者" min-width="120" />
        <el-table-column label="借阅日期" width="120">
          <template #default="{ row }">{{ formatDate(row.borrowDate) }}</template>
        </el-table-column>
        <el-table-column label="应还日期" width="120">
          <template #default="{ row }">{{ formatDate(row.dueDate) }}</template>
        </el-table-column>
        <el-table-column label="罚金" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.fineAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="findOptionTag(BORROW_STATUS_OPTIONS, row.status)">{{ findOptionLabel(BORROW_STATUS_OPTIONS, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <template v-if="userStore.isManager">
              <el-button v-if="row.status === 0" link type="primary" @click="handleApprove(row)">审批通过</el-button>
              <el-button v-if="row.status === 0" link type="danger" @click="handleReject(row)">拒绝</el-button>
              <el-button v-if="[1, 2, 5].includes(row.status)" link type="warning" @click="handleReturn(row)">归还</el-button>
            </template>
            <template v-else>
              <el-button v-if="[1, 2].includes(row.status)" link type="primary" @click="handleRenew(row)">续借</el-button>
            </template>
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
import { approveBorrow, getBorrowList, rejectBorrow, renewBorrow, returnBorrow } from '../api/borrow'
import { BORROW_STATUS_OPTIONS, findOptionLabel, findOptionTag } from '../constants/options'
import { useUserStore } from '../store/user'
import { formatDate, formatMoney } from '../utils/format'

const userStore = useUserStore()
const loading = ref(false)
const records = ref([])

const queryForm = reactive({
  keyword: '',
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
    const data = await getBorrowList(sanitizeParams({
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
  queryForm.keyword = ''
  queryForm.status = undefined
  page.current = 1
  loadRecords()
}

async function handleApprove(row) {
  await ElMessageBox.confirm(`确认通过《${row.bookTitle}》的借阅申请？`, '审批确认', { type: 'info' })
  await approveBorrow(row.id)
  ElMessage.success('审批通过')
  loadRecords()
}

async function handleReject(row) {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝借阅', {
    inputType: 'textarea',
    inputValidator: text => Boolean(text && text.trim()),
    inputErrorMessage: '请输入拒绝原因'
  })
  await rejectBorrow(row.id, { rejectReason: value })
  ElMessage.success('已拒绝借阅申请')
  loadRecords()
}

async function handleReturn(row) {
  await ElMessageBox.confirm(`确认登记归还《${row.bookTitle}》？`, '归还确认', { type: 'warning' })
  await returnBorrow(row.id)
  ElMessage.success('归还登记成功')
  loadRecords()
}

async function handleRenew(row) {
  await ElMessageBox.confirm(`确认续借《${row.bookTitle}》？到期日将延长15天`, '续借确认', { type: 'info' })
  await renewBorrow(row.id)
  ElMessage.success('续借成功')
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