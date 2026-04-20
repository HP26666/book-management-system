<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2 class="page-header__title">公告管理</h2>
        <p class="page-header__subtitle">管理员可发布公告，普通用户可浏览公告内容</p>
      </div>
      <el-button v-if="isAdmin" type="primary" @click="openCreateDialog">发布公告</el-button>
    </div>

    <el-card>
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item>
          <el-select v-model="queryForm.type" clearable placeholder="公告类型" style="width: 160px">
            <el-option v-for="item in NOTICE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isAdmin">
          <el-select v-model="queryForm.status" clearable placeholder="发布状态" style="width: 160px">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-empty v-if="!loading && notices.length === 0" description="暂无公告" />
      <el-table v-else v-loading="loading" :data="notices" stripe row-key="id">
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="findOptionTag(NOTICE_TYPE_OPTIONS, row.type)">{{ findOptionLabel(NOTICE_TYPE_OPTIONS, row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="内容摘要" min-width="260">
          <template #default="{ row }">{{ compactText(row.content, 80) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openViewDialog(row)">查看</el-button>
            <el-button v-if="isAdmin" link type="warning" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="isAdmin" link type="danger" @click="handleDelete(row)">删除</el-button>
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
          @change="loadNotices"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'view' ? '公告详情' : dialog.mode === 'create' ? '发布公告' : '编辑公告'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px" :disabled="dialog.mode === 'view'">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="item in NOTICE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isAdmin && dialog.mode !== 'view'" label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">已发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker v-model="form.publishTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">{{ dialog.mode === 'view' ? '关闭' : '取消' }}</el-button>
        <el-button v-if="dialog.mode !== 'view'" type="primary" :loading="dialog.submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createNotice, deleteNotice, getNotice, getNoticeList, updateNotice } from '../api/notice'
import { NOTICE_TYPE_OPTIONS, findOptionLabel, findOptionTag } from '../constants/options'
import { useUserStore } from '../store/user'
import { compactText, formatDateTime } from '../utils/format'

const userStore = useUserStore()
const loading = ref(false)
const notices = ref([])
const formRef = ref(null)

const queryForm = reactive({
  type: undefined,
  status: undefined
})

const page = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialog = reactive({
  visible: false,
  mode: 'view',
  editingId: null,
  submitting: false
})

const form = reactive({
  title: '',
  type: 1,
  status: 1,
  publishTime: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const isAdmin = computed(() => userStore.roles.includes('admin'))

function sanitizeParams(payload) {
  return Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== undefined && value !== null && value !== ''))
}

function resetForm() {
  form.title = ''
  form.type = 1
  form.status = 1
  form.publishTime = ''
  form.content = ''
}

async function loadNotices() {
  loading.value = true
  try {
    const data = await getNoticeList(sanitizeParams({
      ...queryForm,
      page: page.current,
      size: page.size
    }))
    notices.value = data.records || []
    page.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  page.current = 1
  loadNotices()
}

function resetQuery() {
  queryForm.type = undefined
  queryForm.status = undefined
  page.current = 1
  loadNotices()
}

function openCreateDialog() {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.editingId = null
  resetForm()
}

async function openEditDialog(row) {
  const detail = await getNotice(row.id)
  dialog.visible = true
  dialog.mode = 'edit'
  dialog.editingId = row.id
  form.title = detail.title
  form.type = detail.type
  form.status = detail.status
  form.publishTime = detail.publishTime || ''
  form.content = detail.content || ''
}

async function openViewDialog(row) {
  const detail = await getNotice(row.id)
  dialog.visible = true
  dialog.mode = 'view'
  dialog.editingId = row.id
  form.title = detail.title
  form.type = detail.type
  form.status = detail.status
  form.publishTime = detail.publishTime || ''
  form.content = detail.content || ''
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除公告“${row.title}”？`, '删除确认', { type: 'warning' })
  await deleteNotice(row.id)
  ElMessage.success('公告删除成功')
  loadNotices()
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  dialog.submitting = true
  try {
    const payload = {
      title: form.title,
      type: Number(form.type),
      status: Number(form.status),
      publishTime: form.publishTime || null,
      content: form.content
    }
    if (dialog.mode === 'create') {
      await createNotice(payload)
      ElMessage.success('公告发布成功')
    } else {
      await updateNotice(dialog.editingId, payload)
      ElMessage.success('公告更新成功')
    }
    dialog.visible = false
    loadNotices()
  } finally {
    dialog.submitting = false
  }
}

onMounted(loadNotices)
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