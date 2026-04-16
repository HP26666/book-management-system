<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索书名/ISBN/作者" clearable style="width: 260px" @clear="loadData" @keyup.enter="loadData" />
          <el-tree-select v-model="query.categoryId" :data="categoryTree" :props="{ label: 'name', value: 'id', children: 'children' }" placeholder="分类" clearable check-strictly style="width: 160px" @change="loadData" />
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 110px" @change="loadData">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
        <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 新增图书</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="封面" width="70">
          <template #default="{ row }">
            <el-image :src="row.coverUrl" fit="cover" style="width: 40px; height: 54px; border-radius: 4px" v-if="row.coverUrl">
              <template #error><div class="img-placeholder"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <div v-else class="img-placeholder"><el-icon><Picture /></el-icon></div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
        <el-table-column label="库存" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.availableStock === 0 }">{{ row.availableStock }}/{{ row.totalStock }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="openStockDialog(row)">调库存</el-button>
            <el-button link :type="row.status === 1 ? 'info' : 'success'" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- Book Form Dialog -->
    <el-dialog v-model="dialogVisible" :title="editingBook ? '编辑图书' : '新增图书'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="书名" prop="title"><el-input v-model="form.title" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn"><el-input v-model="form.isbn" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社"><el-input v-model="form.publisher" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-tree-select v-model="form.categoryId" :data="categoryTree" :props="{ label: 'name', value: 'id', children: 'children' }" check-strictly clearable style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版日期"><el-date-picker v-model="form.publishDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="馆藏位置"><el-input v-model="form.location" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="总库存" v-if="!editingBook">
          <el-input-number v-model="form.totalStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.summary" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Stock Adjust Dialog -->
    <el-dialog v-model="stockDialogVisible" title="调整库存" width="400px" destroy-on-close>
      <el-form ref="stockFormRef" :model="stockForm" label-width="80px">
        <el-form-item label="当前库存">
          <span>总库存: {{ stockForm.currentTotal }}, 可用: {{ stockForm.currentAvailable }}</span>
        </el-form-item>
        <el-form-item label="新总库存" prop="totalStock">
          <el-input-number v-model="stockForm.totalStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBookList, createBook, updateBook, deleteBook, updateBookStatus, adjustStock } from '@/api/book'
import { getCategoryTree } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const stockDialogVisible = ref(false)
const editingBook = ref(null)
const formRef = ref()
const stockFormRef = ref()
const categoryTree = ref([])

const query = reactive({ keyword: '', categoryId: undefined, status: undefined, page: 1, size: 10 })

const form = reactive({
  title: '', isbn: '', author: '', publisher: '', publishDate: '',
  categoryId: null, coverUrl: '', summary: '', price: 0, totalStock: 0, location: ''
})

const stockForm = reactive({ bookId: null, totalStock: 0, remark: '', currentTotal: 0, currentAvailable: 0 })

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBookList(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data || []
  } catch {}
}

const openDialog = (book) => {
  editingBook.value = book || null
  if (book) {
    Object.assign(form, { title: book.title, isbn: book.isbn, author: book.author, publisher: book.publisher, publishDate: book.publishDate, categoryId: book.categoryId, coverUrl: book.coverUrl, summary: book.summary, price: book.price, location: book.location })
  } else {
    Object.assign(form, { title: '', isbn: '', author: '', publisher: '', publishDate: '', categoryId: null, coverUrl: '', summary: '', price: 0, totalStock: 0, location: '' })
  }
  dialogVisible.value = true
}

const openStockDialog = (book) => {
  Object.assign(stockForm, { bookId: book.id, totalStock: book.totalStock, remark: '', currentTotal: book.totalStock, currentAvailable: book.availableStock })
  stockDialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editingBook.value) {
      await updateBook(editingBook.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await createBook(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleStockSubmit = async () => {
  await adjustStock(stockForm.bookId, { totalStock: stockForm.totalStock, remark: stockForm.remark })
  ElMessage.success('库存调整成功')
  stockDialogVisible.value = false
  loadData()
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await updateBookStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除图书 "${row.title}"？`, '提示', { type: 'warning' })
  await deleteBook(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
.img-placeholder { width: 40px; height: 54px; border-radius: 4px; background: #f5f7fa; display: flex; align-items: center; justify-content: center; color: #c0c4cc; }
.text-danger { color: #f56c6c; font-weight: 600; }
</style>
