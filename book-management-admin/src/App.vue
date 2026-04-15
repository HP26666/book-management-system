<template>
  <main class="shell">
    <section class="hero">
      <p class="eyebrow">Book Management System</p>
      <h1>图书管理系统管理端</h1>
      <p class="summary">
        当前为 Vue 3 + Vite + Element Plus 的可构建基础骨架，后续开发应按 docs 文档拆分页面、路由、状态和接口模块。
      </p>
      <el-space wrap>
        <el-tag type="success">Spring Boot 3</el-tag>
        <el-tag>Vue 3</el-tag>
        <el-tag type="warning">PostgreSQL</el-tag>
        <el-tag type="info">Docker</el-tag>
      </el-space>
    </section>
  </main>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  background: #f6f8fb;
  color: #1f2937;
}

.hero {
  width: min(720px, 100%);
  padding: 32px;
  border: 1px solid #d8dee9;
  border-radius: 8px;
  background: #ffffff;
}

.eyebrow {
  margin: 0 0 12px;
  color: #0f766e;
  font-weight: 700;
}

h1 {
  margin: 0;
  font-size: 36px;
  line-height: 1.2;
}

.summary {
  margin: 16px 0 24px;
  line-height: 1.8;
}
</style>
