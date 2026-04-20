<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h2 class="page-header__title">分类管理</h2>
        <p class="page-header__subtitle">维护树形分类结构，删除前自动校验子分类和关联图书</p>
      </div>
      <el-button type="primary" @click="openCreateRoot">新增根分类</el-button>
    </div>

    <el-card>
      <el-table v-loading="loading" :data="treeData" row-key="id" border default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="分类名称" min-width="240" />
        <el-table-column prop="level" label="层级" width="100" align="center" />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCreateChild(row)">新增子类</el-button>
            <el-button link type="warning" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增分类' : '编辑分类'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" placeholder="根分类" clearable style="width: 100%">
            <el-option v-for="item in flatCategories" :key="item.id" :label="item.displayName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCategory, deleteCategory, getCategoryTree, updateCategory } from '../api/category'
import { flattenCategoryTree } from '../utils/tree'

const loading = ref(false)
const treeData = ref([])
const flatCategories = ref([])
const formRef = ref(null)

const dialog = reactive({
  visible: false,
  mode: 'create',
  submitting: false,
  editingId: null
})

const form = reactive({
  parentId: null,
  name: '',
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序值', trigger: 'change' }]
}

function resetForm() {
  form.parentId = null
  form.name = ''
  form.sortOrder = 0
}

async function loadTree() {
  loading.value = true
  try {
    const data = await getCategoryTree()
    treeData.value = data
    flatCategories.value = flattenCategoryTree(data)
  } finally {
    loading.value = false
  }
}

function openCreateRoot() {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.editingId = null
  resetForm()
}

function openCreateChild(row) {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.editingId = null
  resetForm()
  form.parentId = row.id
}

function openEdit(row) {
  dialog.visible = true
  dialog.mode = 'edit'
  dialog.editingId = row.id
  form.parentId = row.parentId || null
  form.name = row.name
  form.sortOrder = row.sortOrder
  // 编辑时排除自身及其子节点，防止循环引用
  const excludeIds = new Set([row.id])
  function collectChildIds(nodes) {
    for (const node of nodes) {
      if (excludeIds.has(node.parentId)) {
        excludeIds.add(node.id)
      }
      if (node.children) collectChildIds(node.children)
    }
  }
  collectChildIds(flatCategories.value)
  flatCategories.value = flattenCategoryTree(treeData.value).filter(item => !excludeIds.has(item.id))
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除分类“${row.name}”？`, '删除确认', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('分类删除成功')
  loadTree()
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  dialog.submitting = true
  try {
    const payload = {
      parentId: form.parentId,
      name: form.name,
      sortOrder: Number(form.sortOrder || 0)
    }
    if (dialog.mode === 'create') {
      await createCategory(payload)
      ElMessage.success('分类创建成功')
    } else {
      await updateCategory(dialog.editingId, payload)
      ElMessage.success('分类更新成功')
    }
    dialog.visible = false
    loadTree()
  } finally {
    dialog.submitting = false
  }
}

onMounted(loadTree)
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
</style>