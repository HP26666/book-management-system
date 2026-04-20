<template>
  <div class="login-page">
    <!-- Skip to content (accessibility) -->
    <a href="#login-form" class="skip-link visually-hidden">跳转到登录表单</a>

    <div class="login-card" role="main">
      <!-- Brand -->
      <header class="login-card__header">
        <el-icon class="login-card__icon" :size="40" color="#0f766e" aria-hidden="true">
          <Reading />
        </el-icon>
        <h1 class="login-card__title">图书管理系统</h1>
        <p class="login-card__subtitle">管理员登录</p>
      </header>

      <!-- Form -->
      <el-form
        id="login-form"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            autocomplete="username"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <div class="login-form__options">
          <el-checkbox v-model="rememberMe" label="记住我" />
          <a href="#" class="login-form__forgot" aria-label="忘记密码">忘记密码？</a>
        </div>

        <el-button
          type="primary"
          native-type="submit"
          class="login-form__submit"
          :loading="loading"
          block
        >
          {{ loading ? '登录中…' : '登 录' }}
        </el-button>
      </el-form>
    </div>

    <!-- Background decoration (aria-hidden) -->
    <div class="login-bg" aria-hidden="true">
      <div class="login-bg__blob login-bg__blob--1" />
      <div class="login-bg__blob login-bg__blob--2" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度为 2–30 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  // Simulate API call — replace with real auth request
  await new Promise(resolve => setTimeout(resolve, 800))
  loading.value = false

  ElMessage.success('登录成功')
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
@use '../styles/variables' as *;

// ── Page shell ────────────────────────────────────────────────────────────────
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $space-6;
  background: $color-bg;
  overflow: hidden;
}

// ── Card ──────────────────────────────────────────────────────────────────────
.login-card {
  position: relative;
  z-index: 1;
  width: min(440px, 100%);
  padding: $space-10 $space-8;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-xl;
  box-shadow: $shadow-lg;

  @media (max-width: $bp-sm) {
    padding: $space-8 $space-6;
  }
}

// ── Header ────────────────────────────────────────────────────────────────────
.login-card__header {
  text-align: center;
  margin-bottom: $space-8;
}

.login-card__icon {
  display: block;
  margin: 0 auto $space-4;
}

.login-card__title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin-bottom: $space-2;
}

.login-card__subtitle {
  color: $color-text-muted;
  font-size: $font-size-sm;
  margin: 0;
}

// ── Form ──────────────────────────────────────────────────────────────────────
.login-form {
  :deep(.el-form-item__label) {
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  :deep(.el-input__wrapper) {
    border-radius: $radius-md;
  }
}

.login-form__options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $space-6;
}

.login-form__forgot {
  font-size: $font-size-sm;
  color: $color-primary;

  &:hover {
    color: $color-primary-dark;
  }
}

.login-form__submit {
  width: 100%;
  height: 48px;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $radius-md;
  background: $color-primary;
  border-color: $color-primary;

  &:hover,
  &:focus-visible {
    background: $color-primary-dark;
    border-color: $color-primary-dark;
  }
}

// ── Background decoration ─────────────────────────────────────────────────────
.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.login-bg__blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.2;
}

.login-bg__blob--1 {
  width: 500px;
  height: 500px;
  background: $color-primary;
  top: -200px;
  right: -150px;
}

.login-bg__blob--2 {
  width: 400px;
  height: 400px;
  background: #3b82f6;
  bottom: -150px;
  left: -100px;
}
</style>
