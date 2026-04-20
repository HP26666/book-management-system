<template>
  <div class="books-page">
    <div class="page-header">
      <div>
        <h2 class="page-header__title">图书管理</h2>
        <p class="page-header__subtitle">{{ userStore.isManager ? '维护馆藏图书、库存和封面信息' : '浏览图书并直接发起借阅或预约' }}</p>
      </div>
      <el-button v-if="userStore.isManager" type="primary" :icon="Plus" @click="openCreateDialog">新增图书</el-button>
    </div>

    <el-card class="toolbar">
      <el-form :inline="true" :model="queryForm" class="toolbar__form" @submit.prevent>
        <el-form-item>
          <el-input v-model="queryForm.keyword" placeholder="搜索书名、作者或 ISBN" clearable style="width: 240px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.categoryId" placeholder="分类" clearable style="width: 180px">
            <el-option v-for="item in flatCategories" :key="item.id" :label="item.displayName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.status" placeholder="状态" clearable style="width: 140px">
            <el-option v-for="item in BOOK_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-empty v-if="!loading && books.length === 0" description="暂无图书数据" />
      <el-table v-else v-loading="loading" :data="books" row-key="id" stripe>
        <el-table-column label="封面" width="90">
          <template #default="{ row }">
            <el-image v-if="row.coverUrl" :src="row.coverUrl" fit="cover" class="book-cover" />
            <div v-else class="book-cover book-cover--placeholder">封面</div>
          </template>
        </el-table-column>
        <el-table-column prop="isbn" label="ISBN" width="160" />
        <el-table-column prop="title" label="书名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" min-width="140" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="140" />
        <el-table-column label="价格" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.price) }}</template>
        </el-table-column>
        <el-table-column label="库存" width="120" align="center">
          <template #default="{ row }">{{ row.availableStock }}/{{ row.totalStock }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="findOptionTag(BOOK_STATUS_OPTIONS, row.status)">{{ findOptionLabel(BOOK_STATUS_OPTIONS, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="馆藏位置" width="140" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <template v-if="userStore.isManager">
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link type="warning" @click="openStockDialog(row)">调库存</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" :disabled="row.status !== 1 || row.availableStock <= 0" @click="handleBorrow(row)">借阅</el-button>
              <el-button link type="warning" :disabled="row.status !== 1 || row.availableStock > 0" @click="handleReserve(row)">预约</el-button>
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
          @change="loadBooks"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增图书' : '编辑图书'" width="760px" destroy-on-close>
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="88px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="bookForm.isbn" placeholder="请输入 ISBN">
                <template #append>
                  <el-button :loading="isbnLoading" @click="handleParseIsbn">解析</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名" prop="title">
              <el-input v-model="bookForm.title" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="bookForm.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版日期" prop="publishDate">
              <el-date-picker v-model="bookForm.publishDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-tree-select v-model="bookForm.categoryId" :data="categoryTree" :props="treeProps" check-strictly node-key="id" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总库存" prop="totalStock">
              <el-input-number v-model="bookForm.totalStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可借库存" prop="availableStock">
              <el-input-number v-model="bookForm.availableStock" :min="0" :max="bookForm.totalStock || 0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="bookForm.price" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="bookForm.status" style="width: 100%">
                <el-option v-for="item in BOOK_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="馆藏位置" prop="location">
              <el-input v-model="bookForm.location" placeholder="例如 A-01-003" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面">
              <div class="cover-upload">
                <el-upload :show-file-list="false" :http-request="handleUploadCover" accept=".jpg,.jpeg,.png,.gif">
                  <el-button>上传封面</el-button>
                </el-upload>
                <el-image v-if="bookForm.coverUrl" :src="bookForm.coverUrl" class="cover-preview" fit="cover" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介" prop="summary">
              <el-input v-model="bookForm.summary" type="textarea" :rows="4" placeholder="请输入图书简介" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.submitting" @click="submitBook">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialog.visible" title="调整库存" width="420px">
      <el-form label-width="84px">
        <el-form-item label="图书">
          <span>{{ stockDialog.book?.title }}</span>
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number v-model="stockDialog.changeQty" :min="-999" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockDialog.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="stockDialog.submitting" @click="submitStockAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { applyBorrow } from '../api/borrow'
import { adjustStock, createBook, deleteBook, getBook, getBookList, parseIsbn, updateBook } from '../api/book'
import { getCategoryTree } from '../api/category'
import { uploadFile } from '../api/files'
import { createReservation } from '../api/reservation'
import { BOOK_STATUS_OPTIONS, findOptionLabel, findOptionTag } from '../constants/options'
import { useUserStore } from '../store/user'
import { formatMoney } from '../utils/format'
import { flattenCategoryTree } from '../utils/tree'

const userStore = useUserStore()
const loading = ref(false)
const isbnLoading = ref(false)
const books = ref([])
const categoryTree = ref([])
const bookFormRef = ref(null)

const treeProps = {
  label: 'name',
  children: 'children'
}

const queryForm = reactive({
  keyword: '',
  categoryId: undefined,
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
  submitting: false,
  editingId: null
})

const stockDialog = reactive({
  visible: false,
  book: null,
  changeQty: 1,
  remark: '',
  submitting: false
})

const bookForm = reactive({
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishDate: '',
  categoryId: null,
  coverUrl: '',
  summary: '',
  price: 0,
  totalStock: 1,
  availableStock: 1,
  status: 1,
  location: ''
})

const bookRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  totalStock: [{ required: true, message: '请输入总库存', trigger: 'change' }],
  availableStock: [{ required: true, message: '请输入可借库存', trigger: 'change' }]
}

const flatCategories = computed(() => flattenCategoryTree(categoryTree.value))

function sanitizeParams(payload) {
  return Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== undefined && value !== null && value !== ''))
}

function resetBookForm() {
  Object.assign(bookForm, {
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishDate: '',
    categoryId: null,
    coverUrl: '',
    summary: '',
    price: 0,
    totalStock: 1,
    availableStock: 1,
    status: 1,
    location: ''
  })
}

async function loadCategories() {
  categoryTree.value = await getCategoryTree()
}

async function loadBooks() {
  loading.value = true
  try {
    const data = await getBookList(sanitizeParams({
      ...queryForm,
      page: page.current,
      size: page.size
    }))
    books.value = data.records || []
    page.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  page.current = 1
  loadBooks()
}

function resetQuery() {
  queryForm.keyword = ''
  queryForm.categoryId = undefined
  queryForm.status = undefined
  page.current = 1
  loadBooks()
}

function openCreateDialog() {
  dialog.visible = true
  dialog.mode = 'create'
  dialog.editingId = null
  resetBookForm()
}

async function openEditDialog(row) {
  const detail = await getBook(row.id)
  dialog.visible = true
  dialog.mode = 'edit'
  dialog.editingId = row.id
  Object.assign(bookForm, {
    isbn: detail.isbn || '',
    title: detail.title || '',
    author: detail.author || '',
    publisher: detail.publisher || '',
    publishDate: detail.publishDate || '',
    categoryId: detail.categoryId || null,
    coverUrl: detail.coverUrl || '',
    summary: detail.summary || '',
    price: detail.price || 0,
    totalStock: detail.totalStock || 0,
    availableStock: detail.availableStock || 0,
    status: detail.status ?? 1,
    location: detail.location || ''
  })
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除《${row.title}》？`, '删除确认', { type: 'warning' })
  await deleteBook(row.id)
  ElMessage.success('删除成功')
  loadBooks()
}

async function handleParseIsbn() {
  if (!bookForm.isbn) {
    ElMessage.warning('请先输入 ISBN')
    return
  }
  isbnLoading.value = true
  try {
    const data = await parseIsbn(bookForm.isbn)
    bookForm.title = data.title || bookForm.title
    bookForm.author = data.author || bookForm.author
    bookForm.publisher = data.publisher || bookForm.publisher
    bookForm.summary = data.summary || bookForm.summary
    bookForm.price = data.price || bookForm.price
  } finally {
    isbnLoading.value = false
  }
}

async function handleUploadCover(options) {
  try {
    const result = await uploadFile(options.file)
    bookForm.coverUrl = result.fileUrl
    options.onSuccess(result)
  } catch (error) {
    options.onError(error)
  }
}

function buildBookPayload() {
  return {
    isbn: bookForm.isbn || null,
    title: bookForm.title,
    author: bookForm.author || null,
    publisher: bookForm.publisher || null,
    publishDate: bookForm.publishDate || null,
    categoryId: bookForm.categoryId,
    coverUrl: bookForm.coverUrl || null,
    summary: bookForm.summary || null,
    price: Number(bookForm.price || 0),
    totalStock: Number(bookForm.totalStock || 0),
    availableStock: Number(bookForm.availableStock || 0),
    status: Number(bookForm.status),
    location: bookForm.location || null
  }
}

async function submitBook() {
  const valid = await bookFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  dialog.submitting = true
  try {
    const payload = buildBookPayload()
    if (dialog.mode === 'create') {
      await createBook(payload)
      ElMessage.success('图书创建成功')
    } else {
      await updateBook(dialog.editingId, payload)
      ElMessage.success('图书更新成功')
    }
    dialog.visible = false
    loadBooks()
  } finally {
    dialog.submitting = false
  }
}

function openStockDialog(row) {
  stockDialog.visible = true
  stockDialog.book = row
  stockDialog.changeQty = 1
  stockDialog.remark = ''
}

async function submitStockAdjust() {
  if (!stockDialog.book || !stockDialog.changeQty) {
    ElMessage.warning('请输入调整数量')
    return
  }

  stockDialog.submitting = true
  try {
    await adjustStock(stockDialog.book.id, {
      changeQty: Number(stockDialog.changeQty),
      remark: stockDialog.remark
    })
    ElMessage.success('库存调整成功')
    stockDialog.visible = false
    loadBooks()
  } finally {
    stockDialog.submitting = false
  }
}

async function handleBorrow(row) {
  await applyBorrow({ bookId: row.id, borrowDays: 30 })
  ElMessage.success('借阅申请已提交')
  loadBooks()
}

async function handleReserve(row) {
  await createReservation({ bookId: row.id })
  ElMessage.success('预约成功')
  loadBooks()
}

onMounted(async () => {
  await loadCategories()
  await loadBooks()
})
</script>

<style lang="scss" scoped>
@use '../styles/variables' as *;

.books-page {
  max-width: $content-max-width;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: $space-6;
}

// ── Page header ───────────────────────────────────────────────────────────────
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: $space-4;

  @media (max-width: $bp-sm) {
    flex-direction: column;
    align-items: flex-start;
  }
}

.page-header__title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.page-header__subtitle {
  color: $color-text-muted;
  margin-top: $space-1;
}

// ── Toolbar ───────────────────────────────────────────────────────────────────
.toolbar {
  border-radius: $radius-lg !important;
}

.toolbar__form {
  display: flex;
  flex-wrap: wrap;
}

// ── Table card ────────────────────────────────────────────────────────────────
.table-card {
  border-radius: $radius-lg !important;

  :deep(.el-card__body) {
    padding: 0;
  }

  :deep(.el-table) {
    border-radius: $radius-lg;
  }
}

// ── Pagination ────────────────────────────────────────────────────────────────
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: $space-4 $space-6;
  border-top: 1px solid $color-border;
}

.book-cover {
  width: 48px;
  height: 64px;
  border-radius: $radius-sm;
  background: $color-bg;
}

.book-cover--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-text-muted;
  font-size: $font-size-xs;
}

.cover-upload {
  display: flex;
  align-items: center;
  gap: $space-4;
}

.cover-preview {
  width: 64px;
  height: 84px;
  border-radius: $radius-sm;
  border: 1px solid $color-border;
}
</style>
