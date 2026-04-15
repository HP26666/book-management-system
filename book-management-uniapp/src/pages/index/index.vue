<template>
  <view class="page">
    <view class="header">
      <text class="title">图书管理系统</text>
      <text class="subtitle">Uni-App 小程序端基础骨架</text>
    </view>
    <view class="panel">
      <text>后续按 docs 文档实现图书检索、详情、借阅、预约和个人中心。</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding: 32rpx;
}

.header {
  margin: 24rpx 0 32rpx;
}

.title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
}

.subtitle {
  display: block;
  margin-top: 12rpx;
  color: #64748b;
  font-size: 28rpx;
}

.panel {
  padding: 28rpx;
  border-radius: 8rpx;
  background: #ffffff;
  line-height: 1.7;
}
</style>
