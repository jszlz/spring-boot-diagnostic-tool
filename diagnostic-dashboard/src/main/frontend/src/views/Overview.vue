<template>
  <div class="overview">
    <div class="header-section">
      <h2>系统概览</h2>
      <AppSelector @change="handleAppChange" />
    </div>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon endpoints">📊</div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalEndpoints || 0 }}</div>
              <div class="stat-label">监控端点</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon dependencies">🔗</div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.dependencies || 0 }}</div>
              <div class="stat-label">依赖组件</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon risks">⚠️</div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalRisks || 0 }}</div>
              <div class="stat-label">总风险数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card high-risk">
          <div class="stat-content">
            <div class="stat-icon high">🔴</div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.highRisks || 0 }}</div>
              <div class="stat-label">高危风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>性能概览</span>
          </template>
          <div ref="performanceChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>风险分布</span>
          </template>
          <div ref="riskChart" style="height: 300px;"></div>
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
  name: 'Overview',
  components: {
    AppSelector
  },
  setup() {
    const overview = ref({})
    const performanceChart = ref(null)
    const riskChart = ref(null)

    const loadData = async () => {
      try {
        const appName = appStore.selectedApp
        
        const [overviewRes, perfRes, riskRes] = await Promise.all([
          dashboardApi.getOverview(appName),
          dashboardApi.getPerformance(appName),
          dashboardApi.getRisks(appName)
        ])
        
        overview.value = overviewRes.data
        
        // 性能图表
        if (performanceChart.value) {
          const chart = echarts.init(performanceChart.value)
          const endpoints = perfRes.data.endpoints.slice(0, 10)
          
          chart.setOption({
            tooltip: { trigger: 'axis' },
            xAxis: {
              type: 'category',
              data: endpoints.map(e => e.name.split('/').pop() || e.name),
              axisLabel: { rotate: 45 }
            },
            yAxis: { type: 'value', name: 'QPS' },
            series: [{
              data: endpoints.map(e => e.qps),
              type: 'bar',
              itemStyle: { color: '#409EFF' }
            }]
          })
        }
        
        // 风险图表
        if (riskChart.value) {
          const chart = echarts.init(riskChart.value)
          const risks = riskRes.data
          
          chart.setOption({
            tooltip: { trigger: 'item' },
            series: [{
              type: 'pie',
              radius: '60%',
              data: [
                { value: risks.high, name: '高危', itemStyle: { color: '#F56C6C' } },
                { value: risks.medium, name: '中危', itemStyle: { color: '#E6A23C' } },
                { value: risks.low, name: '低危', itemStyle: { color: '#67C23A' } }
              ]
            }]
          })
        }
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    }

    const handleAppChange = () => {
      loadData()
    }

    onMounted(() => {
      loadData()
    })

    return {
      overview,
      performanceChart,
      riskChart,
      handleAppChange
    }
  }
}
</script>

<style scoped>
.overview {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.overview h2 {
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

.stats-row {
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 20px;
  overflow: hidden;
  position: relative;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.3);
}

.stat-card.high-risk {
  border-left: none;
}

.stat-card.high-risk::before {
  background: linear-gradient(90deg, #f56c6c 0%, #ff8a8a 100%);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.stat-icon {
  font-size: 48px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 图表卡片美化 */
.el-card {
  border-radius: 20px;
  overflow: hidden;
}

.el-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}
</style>
