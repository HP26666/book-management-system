<script>
import { useUserStore } from './store/user'

export default {
  onLaunch() {
    const userStore = useUserStore()
    userStore.bootstrap().catch(() => null)
    console.log('Book Management Uni-App launched')
  }
}
</script>

<style lang="scss">
@import './styles/variables.scss';

// ── Global reset ──────────────────────────────────────────────────────────────
page {
  background: $color-bg;
  color: $color-text-primary;
  font-family: "PingFang SC", "Helvetica Neue", "Microsoft YaHei", sans-serif;
  font-size: $font-size-base;
  line-height: $line-height-normal;
  -webkit-font-smoothing: antialiased;
}

view,
text,
image,
button,
input,
scroll-view {
  box-sizing: border-box;
}

// ── Utility ───────────────────────────────────────────────────────────────────
.text-primary   { color: $color-primary; }
.text-muted     { color: $color-text-muted; }
.text-secondary { color: $color-text-secondary; }
.text-error     { color: $color-error; }
.text-success   { color: $color-success; }

.font-bold   { font-weight: $font-weight-bold; }
.font-medium { font-weight: $font-weight-medium; }

.mt-2 { margin-top: $space-2; }
.mt-3 { margin-top: $space-3; }
.mt-4 { margin-top: $space-4; }

.mb-2 { margin-bottom: $space-2; }
.mb-3 { margin-bottom: $space-3; }
.mb-4 { margin-bottom: $space-4; }

// ── Card surface ──────────────────────────────────────────────────────────────
.card {
  background: $color-surface;
  border-radius: $radius-md;
  padding: $space-4;
}

.page-shell {
  min-height: 100vh;
  padding: $space-4;
  background: $color-bg;
}

.section-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.section-subtitle {
  display: block;
  margin-top: $space-1;
  color: $color-text-muted;
  font-size: $font-size-sm;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 104rpx;
  padding: 8rpx 18rpx;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;

  &--success {
    background: #dcfce7;
    color: $color-success;
  }

  &--danger {
    background: #fee2e2;
    color: $color-error;
  }

  &--warning,
  &--pending {
    background: #fef3c7;
    color: $color-warning;
  }

  &--info,
  &--primary {
    background: #dbeafe;
    color: $color-info;
  }

  &--muted {
    background: #e2e8f0;
    color: $color-text-muted;
  }
}
</style>
