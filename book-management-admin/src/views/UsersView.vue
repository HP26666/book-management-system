<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2 class="page-header__title">用户管理</h2>
        <p class="page-header__subtitle">支持创建用户、启停状态和分配角色</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
    </div>

    <el-card>
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item>
          <el-input v-model="queryForm.keyword" clearable placeholder="搜索用户名或姓名" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.status" clearable placeholder="状态" style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-empty v-if="!loading && users.length === 0" description="暂无用户数据" />
      <el-table v-else v-loading="loading" :data="users" row-key="id" stripe>
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="realName" label="姓名" min-width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="item in row.roles || []" :key="item.id" size="small" class="mr-8">{{ item.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="openRoleDialog(row)">角色</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
          @change="loadUsers"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增用户' : '编辑用户'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="dialog.mode === 'edit'" />
        </el-form-item>
        <el-form-item v-if="dialog.mode === 'create'" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option v-for="item in ROLE_OPTIONS" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialog.visible" title="分配角色" width="420px">
      <el-form label-width="72px">
        <el-form-item label="用户">
          <span>{{ roleDialog.user?.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="roleDialog.roleIds" multiple style="width: 100%">
            <el-option v-for="item in ROLE_OPTIONS" :key="item.id" :label="item.label" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="roleDialog.submitting" @click="submitRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ROLE_OPTIONS } from '../constants/options'
import { assignRoles, createUser, deleteUser, getUser, getUserList, updateUser } from '../api/user'

const loading = ref(false)
const users = ref([])
const formRef = ref(null)

const queryForm = reactive({
  keyword: '',
  status: undefined
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

const roleDialog = reactive({
  visible: false,
  user: null,
  roleIds: [],
  submitting: false
})

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1,
  roleIds: [3]
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

function sanitizeParams(payload) {
  return Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== undefined && value !== null && value !== ''))
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.email = ''
  form.status = 1
  form.roleIds = [3]
}

async function loadUsers() {
  loading.value = true
  try {
    const data = await getUserList(sanitizeParams({
      ...queryForm,
      page: page.current,
      size: page.size
    }))
    users.value = data.records || []
    page.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  page.current = 1
  loadUsers()
}

function resetQuery() {
  queryForm.keyword = ''
  queryForm.status = undefined
  page.current = 1
  loadUsers()
}

function openCreateDialog() {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.editingId = null
  resetForm()
}

async function openEditDialog(row) {
  const detail = await getUser(row.id)
  dialog.visible = true
  dialog.mode = 'edit'
  dialog.editingId = row.id
  form.username = detail.username
  form.password = ''
  form.realName = detail.realName || ''
  form.phone = detail.phone || ''
  form.email = detail.email || ''
  form.status = detail.status
  form.roleIds = (detail.roles || []).map(item => item.id)
}

function openRoleDialog(row) {
  roleDialog.visible = true
  roleDialog.user = row
  roleDialog.roleIds = (row.roles || []).map(item => item.id)
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除用户“${row.username}”？`, '删除确认', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('用户删除成功')
  loadUsers()
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  dialog.submitting = true
  try {
    if (dialog.mode === 'create') {
      await createUser({
        username: form.username,
        password: form.password,
        realName: form.realName || null,
        phone: form.phone || null,
        email: form.email || null,
        status: form.status,
        roleIds: form.roleIds
      })
      ElMessage.success('用户创建成功')
    } else {
      await updateUser(dialog.editingId, {
        realName: form.realName || null,
        phone: form.phone || null,
        email: form.email || null,
        status: form.status
      })
      await assignRoles(dialog.editingId, { roleIds: form.roleIds })
      ElMessage.success('用户更新成功')
    }
    dialog.visible = false
    loadUsers()
  } finally {
    dialog.submitting = false
  }
}

async function submitRoles() {
  roleDialog.submitting = true
  try {
    await assignRoles(roleDialog.user.id, { roleIds: roleDialog.roleIds })
    roleDialog.visible = false
    ElMessage.success('角色分配成功')
    loadUsers()
  } finally {
    roleDialog.submitting = false
  }
}

onMounted(loadUsers)
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

.mr-8 {
  margin-right: $space-2;
}
</style>