<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>个人信息</span></template>
          <div class="profile-avatar">
            <el-avatar :size="80" :src="authStore.avatarUrl || undefined">
              {{ authStore.realName?.charAt(0) || authStore.username?.charAt(0) }}
            </el-avatar>
            <h3>{{ authStore.realName || authStore.username }}</h3>
            <div class="role-tags">
              <el-tag v-for="r in authStore.roles" :key="r" size="small">{{ r }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never">
          <template #header><span>修改信息</span></template>
          <el-form ref="profileRef" :model="profileForm" label-width="80px" style="max-width: 480px">
            <el-form-item label="姓名">
              <el-input v-model="profileForm.realName" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" style="margin-top: 20px">
          <template #header><span>修改密码</span></template>
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="80px" style="max-width: 480px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { updateProfile, changePassword } from '@/api/user'
import { getCurrentUser } from '@/api/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const profileRef = ref()
const pwdRef = ref()

const profileForm = reactive({ realName: '', phone: '', email: '' })

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, val, cb) => val === pwdForm.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }
  ]
}

const loadProfile = async () => {
  try {
    const res = await getCurrentUser()
    const u = res.data
    profileForm.realName = u.realName || ''
    profileForm.phone = u.phone || ''
    profileForm.email = u.email || ''
  } catch {}
}

const handleUpdateProfile = async () => {
  await updateProfile(profileForm)
  ElMessage.success('信息已更新')
  loadProfile()
}

const handleChangePassword = async () => {
  const valid = await pwdRef.value?.validate().catch(() => false)
  if (!valid) return
  await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
  ElMessage.success('密码修改成功，请重新登录')
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}

onMounted(() => loadProfile())
</script>

<style lang="scss" scoped>
.profile-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  h3 { margin: 12px 0 8px; }
  .role-tags { display: flex; gap: 4px; }
}
</style>
