<script>
import { useAuthStore } from './stores/auth'

export default {
  onLaunch() {
    const auth = useAuthStore()
    if (auth.isLoggedIn) {
      auth.fetchUserInfo()
    }
  }
}
</script>

<style lang="scss">
@import 'uview-plus/index.scss';

page {
  background: #f5f7fa;
  color: #1f2937;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: 28rpx;
  line-height: 1.6;
}

.page-container {
  min-height: 100vh;
  padding: 24rpx;
  box-sizing: border-box;
}

/* 通用卡片 */
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

/* 通用标签 */
.status-tag {
  display: inline-block;
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 6rpx;
  line-height: 1.5;
}

.tag-success { background: #ecfdf5; color: #059669; }
.tag-warning { background: #fffbeb; color: #d97706; }
.tag-danger  { background: #fef2f2; color: #dc2626; }
.tag-info    { background: #eff6ff; color: #2563eb; }
.tag-default { background: #f3f4f6; color: #6b7280; }

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
  color: #9ca3af;
  font-size: 28rpx;
}

/* section title */
.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 20rpx;
}
</style>

