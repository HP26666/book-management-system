<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2 class="page-header__title">读者管理</h2>
        <p class="page-header__subtitle">管理读者证、有效期、借阅上限和黑名单状态</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增读者</el-button>
    </div>

    <el-card>
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item>
          <el-input v-model="queryForm.keyword" clearable placeholder="搜索姓名或卡号" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.readerType" clearable placeholder="读者类型" style="width: 160px">
            <el-option v-for="item in READER_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-empty v-if="!loading && readers.length === 0" description="暂无读者数据" />
      <el-table v-else v-loading="loading" :data="readers" stripe row-key="id">
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="readerCardNo" label="借阅证号" min-width="160" />
        <el-table-column label="读者类型" width="120">
          <template #default="{ row }">{{ findOptionLabel(READER_TYPE_OPTIONS, row.readerType) }}</template>
        </el-table-column>
        <el-table-column label="借阅上限" width="100" align="center">
          <template #default="{ row }">{{ row.currentBorrowCount }}/{{ row.maxBorrowCount }}</template>
        </el-table-column>
        <el-table-column label="有效期" min-width="200">
          <template #default="{ row }">{{ formatDate(row.validDateStart) }} 至 {{ formatDate(row.validDateEnd) }}</template>
        </el-table-column>
        <el-table-column label="黑名单" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isBlacklist ? 'danger' : 'success'">{{ row.isBlacklist ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="handleIssueCard(row)">办理借阅证</el-button>
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
          @change="loadReaders"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增读者' : '编辑读者'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="关联用户" prop="userId">
          <el-select v-model="form.userId" filterable :disabled="dialog.mode === 'edit'" style="width: 100%">
            <el-option v-for="item in userOptions" :key="item.id" :label="formatUserLabel(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="读者类型" prop="readerType">
          <el-select v-model="form.readerType" style="width: 100%">
            <el-option v-for="item in READER_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="借阅上限" prop="maxBorrowCount">
          <el-input-number v-model="form.maxBorrowCount" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效开始">
          <el-date-picker v-model="form.validDateStart" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效截止">
          <el-date-picker v-model="form.validDateEnd" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="加入黑名单">
          <el-switch v-model="form.isBlacklist" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { READER_TYPE_OPTIONS, findOptionLabel } from '../constants/options'
import { issueCard, createReader, getReader, getReaderList, updateReader } from '../api/reader'
import { getUserList } from '../api/user'
import { formatDate } from '../utils/format'

const loading = ref(false)
const readers = ref([])
const userOptions = ref([])
const formRef = ref(null)

const queryForm = reactive({
  keyword: '',
  readerType: undefined
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialog = reactive({
  visible: false,
  mode: 'create',
  editingId: null,
  submitting: false
})

const form = reactive({
  userId: null,
  readerType: 'general',
  maxBorrowCount: 5,
  validDateStart: '',
  validDateEnd: '',
  isBlacklist: false
})

const rules = {
  userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }],
  readerType: [{ required: true, message: '请选择读者类型', trigger: 'change' }],
  maxBorrowCount: [{ required: true, message: '请输入借阅上限', trigger: 'change' }]
}

function sanitizeParams(payload) {
  return Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== undefined && value !== null && value !== ''))
}

function resetForm() {
  form.userId = null
  form.readerType = 'general'
  form.maxBorrowCount = 5
  form.validDateStart = ''
  form.validDateEnd = ''
  form.isBlacklist = false
}

function formatUserLabel(item) {
  return item.realName ? `${item.username} / ${item.realName}` : item.username
}

async function loadUserOptions() {
  const data = await getUserList({ page: 1, size: 200 })
  userOptions.value = data.records || []
}

async function loadReaders() {
  loading.value = true
  try {
    const data = await getReaderList(sanitizeParams({
      ...queryForm,
      page: page.current,
      size: page.size
    }))
    readers.value = data.records || []
    page.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  page.current = 1
  loadReaders()
}

function resetQuery() {
  queryForm.keyword = ''
  queryForm.readerType = undefined
  page.current = 1
  loadReaders()
}

function openCreateDialog() {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.editingId = null
  resetForm()
}

async function openEditDialog(row) {
  const detail = await getReader(row.id)
  dialog.visible = true
  dialog.mode = 'edit'
  dialog.editingId = row.id
  form.userId = detail.userId
  form.readerType = detail.readerType
  form.maxBorrowCount = detail.maxBorrowCount
  form.validDateStart = detail.validDateStart || ''
  form.validDateEnd = detail.validDateEnd || ''
  form.isBlacklist = detail.isBlacklist
}

async function handleIssueCard(row) {
  await issueCard(row.id)
  ElMessage.success('借阅证办理成功')
  loadReaders()
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  dialog.submitting = true
  try {
    const payload = {
      userId: form.userId,
      readerType: form.readerType,
      maxBorrowCount: Number(form.maxBorrowCount),
      validDateStart: form.validDateStart || null,
      validDateEnd: form.validDateEnd || null,
      isBlacklist: form.isBlacklist
    }
    if (dialog.mode === 'create') {
      await createReader(payload)
      ElMessage.success('读者创建成功')
    } else {
      await updateReader(dialog.editingId, payload)
      ElMessage.success('读者更新成功')
    }
    dialog.visible = false
    loadReaders()
  } finally {
    dialog.submitting = false
  }
}

onMounted(async () => {
  await loadUserOptions()
  await loadReaders()
})
</script>

<style lang="scss" scoped>
@use '../styles/variables' as *;

.page-shell {
  display: flex;
  flex-direction: column;
  gap: $space-6;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: $space-4;
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