<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索读者卡号/姓名" clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="readerCardNo" label="读者卡号" width="140" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="readerType" label="类型" width="90">
          <template #default="{ row }">{{ row.readerType === 'general' ? '普通' : row.readerType }}</template>
        </el-table-column>
        <el-table-column label="借阅" width="100" align="center">
          <template #default="{ row }">{{ row.currentBorrowCount }}/{{ row.maxBorrowCount }}</template>
        </el-table-column>
        <el-table-column prop="creditScore" label="信用分" width="80" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.creditScore < 60, 'text-warning': row.creditScore < 80 }">{{ row.creditScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="黑名单" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isBlacklist ? 'danger' : 'success'" size="small">{{ row.isBlacklist ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validDateEnd" label="有效期至" width="110" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" title="编辑读者信息" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="最大借阅数">
          <el-input-number v-model="form.maxBorrowCount" :min="1" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input-number v-model="form.creditScore" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="黑名单">
          <el-switch v-model="form.isBlacklist" />
        </el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="form.validDateEnd" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
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
import { getReaderList, updateReader } from '@/api/reader'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const editingReader = ref(null)
const formRef = ref()
const query = reactive({ keyword: '', page: 1, size: 10 })
const form = reactive({ maxBorrowCount: 5, creditScore: 100, isBlacklist: false, validDateEnd: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReaderList(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const openDialog = (reader) => {
  editingReader.value = reader
  Object.assign(form, { maxBorrowCount: reader.maxBorrowCount, creditScore: reader.creditScore, isBlacklist: reader.isBlacklist, validDateEnd: reader.validDateEnd })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    await updateReader(editingReader.value.id, form)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
.text-danger { color: #f56c6c; font-weight: 600; }
.text-warning { color: #e6a23c; font-weight: 600; }
</style>
