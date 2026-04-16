<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="page-title">角色管理</span>
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 新增角色</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roleCode" label="角色编码" width="140" />
        <el-table-column prop="roleName" label="角色名称" width="140" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="权限数" width="100" align="center">
          <template #default="{ row }">{{ row.permissions?.length || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :disabled="['admin', 'librarian', 'reader'].includes(row.roleCode)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingRole ? '编辑角色' : '新增角色'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!editingRole" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="权限">
          <el-tree
            ref="treeRef"
            :data="permTree"
            :props="{ label: 'permName', children: 'children' }"
            show-checkbox
            node-key="id"
            :default-checked-keys="form.permissionIds"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRoleList, createRole, updateRole, deleteRole } from '@/api/role'
import { getPermissionTree } from '@/api/permission'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const editingRole = ref(null)
const formRef = ref()
const treeRef = ref()
const permTree = ref([])

const form = reactive({ roleCode: '', roleName: '', description: '', permissionIds: [] })
const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const loadPerms = async () => {
  try {
    const res = await getPermissionTree()
    permTree.value = res.data || []
  } catch {}
}

const openDialog = (role) => {
  editingRole.value = role || null
  if (role) {
    Object.assign(form, { roleCode: role.roleCode, roleName: role.roleName, description: role.description || '', permissionIds: role.permissions?.map(p => p.id) || [] })
  } else {
    Object.assign(form, { roleCode: '', roleName: '', description: '', permissionIds: [] })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const checkedIds = treeRef.value?.getCheckedKeys(false) || []
    const halfIds = treeRef.value?.getHalfCheckedKeys() || []
    const data = { ...form, permissionIds: [...checkedIds, ...halfIds] }
    if (editingRole.value) {
      await updateRole(editingRole.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await createRole(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除角色 "${row.roleName}"？`, '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadPerms()
})
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 16px; font-weight: 600; }
</style>
