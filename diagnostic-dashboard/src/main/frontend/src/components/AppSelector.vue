<template>
  <div class="app-selector">
    <span class="label">监控应用:</span>
    <el-select 
      v-model="selectedApp" 
      placeholder="选择应用" 
      @change="handleChange"
      size="default"
    >
      <el-option
        v-for="app in apps"
        :key="app.name"
        :label="app.name"
        :value="app.name"
      >
        <span style="float: left">{{ app.name }}</span>
        <span style="float: right; color: #8492a6; font-size: 13px">{{ app.url }}</span>
      </el-option>
    </el-select>
  </div>
</template>

<script>
import { ref, watch, onMounted } from 'vue'
import { appStore } from '../store/appStore'
import { dashboardApi } from '../api/dashboard'

export default {
  name: 'AppSelector',
  emits: ['change'],
  setup(props, { emit }) {
    const apps = ref([])
    const selectedApp = ref(null)

    const loadApps = async () => {
      try {
        const response = await dashboardApi.getApps()
        apps.value = response.data
        appStore.setApps(response.data)
        
        if (response.data.length > 0) {
          selectedApp.value = appStore.selectedApp || response.data[0].name
          appStore.setSelectedApp(selectedApp.value)
        }
      } catch (error) {
        console.error('加载应用列表失败:', error)
      }
    }

    const handleChange = (value) => {
      appStore.setSelectedApp(value)
      emit('change', value)
    }

    // 监听全局状态变化
    watch(() => appStore.selectedApp, (newVal) => {
      if (newVal && newVal !== selectedApp.value) {
        selectedApp.value = newVal
      }
    })

    onMounted(() => {
      loadApps()
    })

    return {
      apps,
      selectedApp,
      handleChange
    }
  }
}
</script>

<style scoped>
.app-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.app-selector:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.label {
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
  font-size: 15px;
}

.el-select {
  min-width: 320px;
}

.el-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.el-select :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.el-select :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}
</style>
