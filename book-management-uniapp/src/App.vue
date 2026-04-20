<script>
export default {
  onLaunch() {
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
image {
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
</style>
