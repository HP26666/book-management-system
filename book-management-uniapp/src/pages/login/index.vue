<template>
  <view class="page-container">
    <view class="login-header">
      <view class="logo">📚</view>
      <text class="app-name">图书管理系统</text>
      <text class="app-desc">借阅 · 预约 · 管理</text>
    </view>

    <!-- 微信登录 -->
    <!-- #ifdef MP-WEIXIN -->
    <view class="card" style="text-align: center;">
      <u-button
        type="primary"
        :customStyle="{ background: '#07c160', borderColor: '#07c160' }"
        @click="handleWechatLogin"
        :loading="wxLoading"
      >
        <u-icon name="weixin-fill" size="18" color="#fff" style="margin-right: 8rpx;" />
        微信一键登录
      </u-button>
    </view>
    <view class="divider">
      <view class="divider-line" />
      <text class="divider-text">或使用账号登录</text>
      <view class="divider-line" />
    </view>
    <!-- #endif -->

    <!-- 账号密码登录 -->
    <view class="card">
      <view class="form-group">
        <text class="form-label">用户名</text>
        <input
          class="form-input"
          v-model="username"
          placeholder="请输入用户名"
          maxlength="50"
        />
      </view>
      <view class="form-group">
        <text class="form-label">密码</text>
        <input
          class="form-input"
          v-model="password"
          type="password"
          placeholder="请输入密码"
          maxlength="100"
          @confirm="handleLogin"
        />
      </view>
      <u-button
        type="primary"
        :customStyle="{ marginTop: '32rpx', background: '#0f766e', borderColor: '#0f766e' }"
        @click="handleLogin"
        :loading="loginLoading"
        :disabled="!username || !password"
      >登录</u-button>
    </view>

    <view class="footer-tip">
      <text class="tip-text">登录即表示同意服务条款与隐私政策</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const loginLoading = ref(false)
const wxLoading = ref(false)

const handleLogin = async () => {
  if (!username.value || !password.value) return
  loginLoading.value = true
  try {
    const res = await authStore.doLogin(username.value, password.value)
    if (res.code === 200) {
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 800)
    } else {
      uni.showToast({ title: res.message || '登录失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  } finally {
    loginLoading.value = false
  }
}

const handleWechatLogin = async () => {
  wxLoading.value = true
  try {
    const res = await authStore.doWechatLogin()
    if (res.code === 200) {
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 800)
    } else {
      uni.showToast({ title: res.message || '登录失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '微信登录失败', icon: 'none' })
  } finally {
    wxLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0 48rpx;
}

.logo {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.app-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #111827;
}

.app-desc {
  font-size: 26rpx;
  color: #9ca3af;
  margin-top: 8rpx;
}

.divider {
  display: flex;
  align-items: center;
  padding: 32rpx 0;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background: #e5e7eb;
}

.divider-text {
  padding: 0 24rpx;
  font-size: 24rpx;
  color: #9ca3af;
}

.form-group {
  margin-bottom: 28rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #374151;
  margin-bottom: 12rpx;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 12rpx;
  font-size: 28rpx;
  background: #fafbfc;
  box-sizing: border-box;

  &:focus {
    border-color: #0f766e;
  }
}

.footer-tip {
  text-align: center;
  padding: 40rpx 0;
}

.tip-text {
  font-size: 22rpx;
  color: #c0c4cc;
}
</style>
