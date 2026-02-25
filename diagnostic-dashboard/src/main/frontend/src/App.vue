<template>
  <div id="app">
    <el-container>
      <el-header class="keen-header">
        <div class="header-content">
          <div class="logo-section">
            <div class="logo-icon">📊</div>
            <h1>Spring Boot 诊断监控</h1>
          </div>
          <el-menu mode="horizontal" :default-active="activeMenu" router class="keen-nav-menu">
            <el-menu-item index="/">
              <span>概览</span>
            </el-menu-item>
            <el-menu-item index="/performance">
              <span>性能监控</span>
            </el-menu-item>
            <el-menu-item index="/jvm">
              <span>JVM监控</span>
            </el-menu-item>
            <el-menu-item index="/topology">
              <span>拓扑图</span>
            </el-menu-item>
            <el-menu-item index="/risks">
              <span>风险告警</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main class="keen-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  computed: {
    activeMenu() {
      return this.$route.path
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  height: 100vh;
  background: var(--body-bg);
}

.el-container {
  height: 100%;
}

/* Keen 风格头部 */
.keen-header {
  background: var(--white);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-xs);
  padding: 0;
  height: 70px !important;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 2rem;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
}

.logo-section h1 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-dark);
}

/* Keen 风格导航菜单 */
.keen-nav-menu {
  border-bottom: none !important;
  background-color: transparent !important;
}

.keen-nav-menu .el-menu-item {
  color: var(--text-gray);
  border-bottom: 3px solid transparent;
  font-weight: 600;
  font-size: 0.925rem;
  transition: all 0.2s ease;
  height: 70px;
  line-height: 70px;
  padding: 0 1.5rem;
}

.keen-nav-menu .el-menu-item:hover {
  background: transparent;
  color: var(--primary);
}

.keen-nav-menu .el-menu-item.is-active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  background: transparent;
}

/* 主内容区 */
.keen-main {
  background: var(--body-bg);
  padding: 2rem;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
