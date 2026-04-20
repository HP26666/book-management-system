<template>
  <div class="books-page">
    <!-- Page header -->
    <div class="page-header">
      <div class="page-header__left">
        <h2 class="page-header__title">图书管理</h2>
        <p class="page-header__subtitle">管理馆藏图书信息</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        新增图书
      </el-button>
    </div>

    <!-- Toolbar: search + filters -->
    <el-card class="toolbar" :body-style="{ padding: '16px 20px' }">
      <div class="toolbar__inner">
        <el-input
          v-model="search"
          placeholder="搜索书名、作者或 ISBN"
          clearable
          :prefix-icon="Search"
          class="toolbar__search"
          aria-label="搜索图书"
          @input="handleSearch"
        />
        <el-select
          v-model="categoryFilter"
          placeholder="分类"
          clearable
          style="width: 140px"
          aria-label="按分类筛选"
        >
          <el-option
            v-for="cat in categories"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>
        <el-select
          v-model="statusFilter"
          placeholder="状态"
          clearable
          style="width: 120px"
          aria-label="按状态筛选"
        >
          <el-option label="可借" value="可借" />
          <el-option label="已借出" value="已借出" />
          <el-option label="下架" value="下架" />
        </el-select>
        <el-button :icon="Refresh" circle aria-label="重置筛选" @click="resetFilters" />
      </div>
    </el-card>

    <!-- Table -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="filteredBooks"
        style="width: 100%"
        stripe
        aria-label="图书列表"
        row-key="id"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="title" label="书名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === '可借' ? 'success' : row.status === '已借出' ? 'warning' : 'info'"
              size="small"
              round
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              :aria-label="`编辑 ${row.title}`"
              @click="editBook(row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :aria-label="`删除 ${row.title}`"
              @click="deleteBook(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalBooks"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          aria-label="分页控件"
        />
      </div>
    </el-card>

    <!-- Add / Edit dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingBook ? '编辑图书' : '新增图书'"
      width="min(560px, 96vw)"
      :close-on-click-modal="false"
      draggable
    >
      <el-form
        ref="bookFormRef"
        :model="bookForm"
        :rules="bookRules"
        label-width="80px"
        @submit.prevent="submitBook"
      >
        <el-form-item label="书名" prop="title">
          <el-input v-model="bookForm.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="bookForm.isbn" placeholder="请输入 ISBN" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="bookForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="bookForm.stock" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBook">
          {{ editingBook ? '保存更改' : '确认新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const search = ref('')
const categoryFilter = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const editingBook = ref(null)
const submitting = ref(false)
const bookFormRef = ref(null)

const categories = ['计算机', '文学', '历史', '科学', '经济', '哲学', '艺术']

const totalBooks = ref(120)

// Mock data — replace with API
const books = ref([
  { id: 1, isbn: '9787111213826', title: '深入理解计算机系统', author: 'Randal E. Bryant', category: '计算机', stock: 5, status: '可借' },
  { id: 2, isbn: '9787115611163', title: 'Vue.js 3 实战', author: '辛瑞晨', category: '计算机', stock: 3, status: '可借' },
  { id: 3, isbn: '9787508380254', title: '代码整洁之道', author: 'Robert C. Martin', category: '计算机', stock: 0, status: '已借出' },
  { id: 4, isbn: '9787115249692', title: '算法导论', author: 'Thomas H. Cormen', category: '计算机', stock: 2, status: '可借' },
  { id: 5, isbn: '9787111306108', title: '人月神话', author: 'Frederick P. Brooks', category: '计算机', stock: 1, status: '可借' },
  { id: 6, isbn: '9787020024759', title: '围城', author: '钱锺书', category: '文学', stock: 4, status: '可借' },
  { id: 7, isbn: '9787806575628', title: '活着', author: '余华', category: '文学', stock: 6, status: '可借' },
  { id: 8, isbn: '9787508089966', title: '三体', author: '刘慈欣', category: '科学', stock: 0, status: '已借出' }
])

const filteredBooks = computed(() => {
  return books.value.filter(b => {
    const q = search.value.toLowerCase()
    const matchesSearch = !q || b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q) || b.isbn.includes(q)
    const matchesCat = !categoryFilter.value || b.category === categoryFilter.value
    const matchesStatus = !statusFilter.value || b.status === statusFilter.value
    return matchesSearch && matchesCat && matchesStatus
  })
})

const bookForm = reactive({ title: '', author: '', isbn: '', category: '', stock: 1 })

const bookRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入 ISBN', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function openAddDialog() {
  editingBook.value = null
  Object.assign(bookForm, { title: '', author: '', isbn: '', category: '', stock: 1 })
  dialogVisible.value = true
}

function editBook(row) {
  editingBook.value = row
  Object.assign(bookForm, { ...row })
  dialogVisible.value = true
}

async function deleteBook(row) {
  await ElMessageBox.confirm(`确认删除《${row.title}》？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })
  books.value = books.value.filter(b => b.id !== row.id)
  ElMessage.success('删除成功')
}

async function submitBook() {
  if (!bookFormRef.value) return
  const valid = await bookFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  await new Promise(r => setTimeout(r, 600))
  submitting.value = false
  dialogVisible.value = false
  ElMessage.success(editingBook.value ? '更新成功' : '新增成功')
}

function handleSearch() {
  currentPage.value = 1
}

function resetFilters() {
  search.value = ''
  categoryFilter.value = ''
  statusFilter.value = ''
  currentPage.value = 1
}
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

.toolbar__inner {
  display: flex;
  align-items: center;
  gap: $space-3;
  flex-wrap: wrap;
}

.toolbar__search {
  flex: 1;
  min-width: 200px;
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
</style>
