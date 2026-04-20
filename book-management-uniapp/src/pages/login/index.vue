<template>
  <view class="page-shell login-page">
    <view class="hero card">
      <text class="hero__eyebrow">BookMS Mini Program</text>
      <text class="hero__title">微信登录</text>
      <text class="hero__desc">开发环境支持 Mock code 登录，正式环境可直接切换为微信 code 登录链路。</text>
    </view>

    <view class="card form-card">
      <text class="section-title">登录配置</text>
      <text class="section-subtitle">默认连接本机 8080，可按需切换到局域网或测试环境。</text>

      <view class="field">
        <text class="field__label">接口地址</text>
        <input v-model="baseUrl" class="field__input" placeholder="http://127.0.0.1:8080/api" />
      </view>

      <view class="field">
        <text class="field__label">Mock code</text>
        <input v-model="loginCode" class="field__input" placeholder="留空则自动生成" />
      </view>

      <view class="actions">
        <button class="btn btn--ghost" @tap="resetBaseUrl">恢复默认地址</button>
        <button class="btn btn--primary" :loading="submitting" @tap="handleLogin">微信快捷登录</button>
      </view>
    </view>

    <view class="card hint-card">
      <text class="hint-card__title">当前说明</text>
      <text class="hint-card__text">1. 未配置微信开放平台时，可直接使用 Mock code 登录。</text>
      <text class="hint-card__text">2. 同一个 code 会映射到同一个读者账号。</text>
      <text class="hint-card__text">3. 首次登录会自动生成读者账号和基础读者信息。</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user'
import { getBaseUrl, getDefaultBaseUrl, setBaseUrl } from '../../utils/config'

const userStore = useUserStore()
const submitting = ref(false)
const baseUrl = ref(getBaseUrl())
const loginCode = ref('reader-demo')

function resetBaseUrl() {
  baseUrl.value = getDefaultBaseUrl()
}

function buildFallbackCode() {
  return `reader-${Date.now()}`
}

async function resolveWechatCode() {
  const customCode = loginCode.value.trim()
  if (customCode) {
    return customCode
  }

  // #ifdef MP-WEIXIN
  try {
    const response = await uni.login({ provider: 'weixin' })
    if (response.code) {
      return response.code
    }
  } catch {
    return buildFallbackCode()
  }
  // #endif

  return buildFallbackCode()
}

async function handleLogin() {
  submitting.value = true
  try {
    setBaseUrl(baseUrl.value.trim() || getDefaultBaseUrl())
    const code = await resolveWechatCode()
    await userStore.loginWithCode(code)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 300)
  } finally {
    submitting.value = false
  }
}

onLoad(() => {
  userStore.hydrate()
  if (userStore.isAuthenticated) {
    uni.switchTab({ url: '/pages/index/index' })
  }
})
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.login-page {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.hero {
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 100%);
  color: #ffffff;
}

.hero__eyebrow {
  display: block;
  font-size: $font-size-xs;
  opacity: 0.8;
}

.hero__title {
  display: block;
  margin-top: $space-2;
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
}

.hero__desc {
  display: block;
  margin-top: $space-2;
  font-size: $font-size-sm;
  line-height: $line-height-loose;
  opacity: 0.9;
}

.form-card,
.hint-card {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.field {
  display: flex;
  flex-direction: column;
  gap: $space-2;
}

.field__label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.field__input {
  height: 88rpx;
  padding: 0 $space-3;
  border-radius: $radius-md;
  background: $color-bg;
  border: 2rpx solid $color-border;
}

.actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
  margin-top: $space-2;
}

.btn {
  height: 88rpx;
  border-radius: $radius-md;
  font-size: $font-size-base;
  border: none;
}

.btn--primary {
  background: $color-primary;
  color: #ffffff;
}

.btn--ghost {
  background: #e6fffb;
  color: $color-primary-dark;
}

.hint-card__title {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
}

.hint-card__text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  line-height: $line-height-loose;
}
</style>