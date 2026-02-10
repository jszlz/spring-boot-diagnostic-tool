<template>
  <div class="performance">
    <div class="header-section">
      <h2>性能监控</h2>
      <AppSelector @change="handleAppChange" />
    </div>
    
    <el-card>
      <el-table :data="endpoints" stripe style="width: 100%">
        <el-table-column prop="name" label="端点" min-width="200" />
        <el-table-column prop="qps" label="QPS" width="100" sortable>
          <template #default="{ row }">
            {{ row.qps.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTime" label="平均响应时间(ms)" width="150" sortable>
          <template #default="{ row }">
            {{ row.avgResponseTime.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="p95" label="P95(ms)" width="120" sortable>
          <template #default="{ row }">
            {{ row.p95.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="p99" label="P99(ms)" width="120" sortable>
          <template #default="{ row }">
            {{ row.p99.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="errorRate" label="错误率" width="100" sortable>
          <template #default="{ row }">
            <el-tag :type="row.errorRate > 0.05 ? 'danger' : 'success'">
              {{ (row.errorRate * 100).toFixed(2) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalRequests" label="总请求数" width="120" sortable />
      </el-table>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>响应时间趋势</span>
          </template>
          <div ref="trendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { dashboardApi } from '../api/dashboard'
import { appStore } from '../store/appStore'
import AppSelector from '../components/AppSelector.vue'

export default {
  name: 'Performance',
  components: {
    AppSelector
  },
  setup() {
    const endpoints = ref([])
    const trendChart = ref(null)

    const loadData = async () => {
      try {
        const appName = appStore.selectedApp
        const response = await dashboardApi.getPerformance(appName)
        endpoints.value = response.data.endpoints
        
        if (trendChart.value && endpoints.value.length > 0) {
          const chart = echarts.init(trendChart.value)
          const topEndpoints = endpoints.value.slice(0, 5)
          
          chart.setOption({
            tooltip: { trigger: 'axis' },
            legend: {
              data: topEndpoints.map(e => e.name.split('/').pop() || e.name)
            },
            xAxis: {
              type: 'category',
              data: ['P50', 'P95', 'P99']
            },
            yAxis: {
              type: 'value',
              name: '响应时间 (ms)'
            },
            series: topEndpoints.map(ep => ({
              name: ep.name.split('/').pop() || ep.name,
              type: 'line',
              data: [ep.avgResponseTime, ep.p95, ep.p99]
            }))
          })
        }
      } catch (error) {
        console.error('加载性能数据失败:', error)
      }
    }

    onMounted(() => {
      loadData()
      setInterval(loadData, 10000) // 10秒刷新
    })

    const handleAppChange = () => {
      loadData()
    }

    return {
      endpoints,
      trendChart,
      handleAppChange
    }
  }
}
</script>

<style scoped>
.performance {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.performance h2 {
  margin-bottom: 10px;
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* 表格美化 */
.el-card :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

.el-card :deep(.el-table th) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #303133;
  font-weight: 600;
}

.el-card :deep(.el-table tr:hover) {
  background: rgba(102, 126, 234, 0.05);
}

.el-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}
</style>
