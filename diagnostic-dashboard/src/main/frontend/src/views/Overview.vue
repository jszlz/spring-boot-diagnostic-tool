<template>
  <div class="overview">
    <div class="header-section">
      <h2>系统概览</h2>
      <div class="header-controls">
        <AppSelector @change="handleAppChange" />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleDateRangeChange"
          class="date-picker"
        />
        <el-button-group class="time-preset-buttons">
          <el-button size="small" @click="setDateRange('today')">今天</el-button>
          <el-button size="small" @click="setDateRange('week')">本周</el-button>
          <el-button size="small" @click="setDateRange('month')">本月</el-button>
        </el-button-group>
      </div>
    </div>
    
    <!-- 主要指标卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
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
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon performance">�</div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(overview.totalRequests || 0) }}</div>
              <div class="stat-label">总请求数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card error">
          <div class="stat-content">
            <div class="stat-icon error">�</div>
            <div class="stat-info">
              <div class="stat-value">{{ formatPercent(overview.errorRate || 0) }}</div>
              <div class="stat-label">错误率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    

    
    <!-- 系统资源卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon throughput">📈</div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(overview.peakQps || 0) }}</div>
              <div class="stat-label">峰值QPS</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon throughput">🌡️</div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.systemLoad || '-' }}</div>
              <div class="stat-label">系统负载</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon throughput">💾</div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.memoryUsage || '-' }}</div>
              <div class="stat-label">内存使用率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要图表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header-custom">
              <el-icon class="header-icon"><TrendCharts /></el-icon>
              <span>QPS分布</span>
            </div>
          </template>
          <div ref="performanceChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header-custom">
              <el-icon class="header-icon"><DataAnalysis /></el-icon>
              <span>API调用热力图</span>
            </div>
          </template>
          <div ref="heatmapChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 错误率趋势 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header-custom">
              <el-icon class="header-icon"><CircleClose /></el-icon>
              <span>错误率趋势</span>
            </div>
          </template>
          <div ref="errorRateChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 系统资源使用 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header-custom">
              <el-icon class="header-icon"><Monitor /></el-icon>
              <span>系统资源使用</span>
            </div>
          </template>
          <div ref="resourceChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header-custom">
              <el-icon class="header-icon"><Timer /></el-icon>
              <span>响应时间分析</span>
            </div>
          </template>
          <div ref="responseTimeChart" class="chart-container"></div>
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
import { TrendCharts, Warning, Timer, CircleClose, Monitor, DataAnalysis } from '@element-plus/icons-vue'

export default {
  name: 'Overview',
  components: {
    AppSelector,
    TrendCharts,
    Warning,
    Timer,
    CircleClose,
    Monitor,
    DataAnalysis
  },
  setup() {
    const overview = ref({})
    const performanceChart = ref(null)
    const errorRateChart = ref(null)
    const resourceChart = ref(null)
    const heatmapChart = ref(null)
    const responseTimeChart = ref(null)
    const dateRange = ref([])
    
    // 初始化日期范围为最近7天
    const initDateRange = () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      dateRange.value = [
        start.toISOString().split('T')[0],
        end.toISOString().split('T')[0]
      ]
    }
    
    // 设置日期范围
    const setDateRange = (preset) => {
      const end = new Date()
      const start = new Date()
      
      switch (preset) {
        case 'today':
          start.setHours(0, 0, 0, 0)
          break
        case 'week':
          const day = end.getDay() || 7 // 将周日从0改为7
          start.setDate(end.getDate() - day + 1)
          start.setHours(0, 0, 0, 0)
          break
        case 'month':
          start.setDate(1)
          start.setHours(0, 0, 0, 0)
          break
      }
      
      dateRange.value = [
        start.toISOString().split('T')[0],
        end.toISOString().split('T')[0]
      ]
      
      handleDateRangeChange(dateRange.value)
    }
    
    // 处理日期范围变化
    const handleDateRangeChange = (range) => {
      if (range && range.length === 2) {
        console.log('Date range changed:', range)
        loadData()
      }
    }

    const loadData = async () => {
      try {
        const appName = appStore.selectedApp
        const [startDate, endDate] = dateRange.value
        
        const [overviewRes, perfRes] = await Promise.all([
          dashboardApi.getOverview(appName, startDate, endDate),
          dashboardApi.getPerformance(appName, startDate, endDate)
        ])
        
        const basicOverview = overviewRes.data
        const performanceData = perfRes.data
        
        // 计算性能指标
        const performanceMetrics = calculatePerformanceMetrics(performanceData.endpoints || [])
        
        // 合并概览数据
        overview.value = {
          ...basicOverview,
          ...performanceMetrics
        }
        
        // 性能图表
        if (performanceChart.value) {
          const chart = echarts.init(performanceChart.value)
          const endpoints = (performanceData.endpoints || []).slice(0, 10)
          
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
        
        // 响应时间分析图表
        if (responseTimeChart.value) {
          const chart = echarts.init(responseTimeChart.value)
          const endpoints = (performanceData.endpoints || []).slice(0, 8)
          
          chart.setOption({
            tooltip: { trigger: 'axis' },
            xAxis: {
              type: 'category',
              data: endpoints.map(e => e.name.split('/').pop() || e.name),
              axisLabel: { rotate: 45 }
            },
            yAxis: { 
              type: 'value', 
              name: '响应时间(ms)',
              axisLabel: {
                formatter: '{value}ms'
              }
            },
            series: [{
              data: endpoints.map(e => e.avgResponseTime || 0),
              type: 'bar',
              name: '平均响应时间',
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#409EFF' },
                  { offset: 1, color: '#60A5FA' }
                ])
              }
            }]
          })
        }
        
        // 错误率趋势图表
        if (errorRateChart.value) {
          const chart = echarts.init(errorRateChart.value)
          const endpoints = (performanceData.endpoints || []).slice(0, 8)
          
          chart.setOption({
            tooltip: { trigger: 'axis' },
            xAxis: {
              type: 'category',
              data: endpoints.map(e => e.name.split('/').pop() || e.name),
              axisLabel: { rotate: 45 }
            },
            yAxis: { 
              type: 'value', 
              name: '错误率',
              axisLabel: {
                formatter: '{value}%'
              }
            },
            series: [{
              data: endpoints.map(e => (e.errorRate || 0) * 100),
              type: 'bar',
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#F56C6C' },
                  { offset: 1, color: '#F7BA2A' }
                ])
              }
            }]
          })
        }
        
        // 系统资源使用图表
        if (resourceChart.value) {
          const chart = echarts.init(resourceChart.value)
          
          chart.setOption({
            tooltip: { trigger: 'item' },
            radar: {
              indicator: [
                { name: 'CPU使用率', max: 100 },
                { name: '内存使用率', max: 100 },
                { name: '磁盘使用率', max: 100 },
                { name: '网络带宽', max: 100 },
                { name: '系统负载', max: 100 }
              ]
            },
            series: [{
              type: 'radar',
              data: [{
                value: [65, 45, 68, 30, 40],
                name: '系统资源',
                itemStyle: { color: '#409EFF' },
                areaStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
                    { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
                  ])
                }
              }]
            }]
          })
        }
        
        // API调用热力图
        if (heatmapChart.value) {
          const chart = echarts.init(heatmapChart.value)
          
          // 模拟热力图数据
          const hours = ['00', '03', '06', '09', '12', '15', '18', '21']
          const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
          const heatmapData = []
          
          for (let i = 0; i < days.length; i++) {
            for (let j = 0; j < hours.length; j++) {
              heatmapData.push([
                j,
                i,
                Math.floor(Math.random() * 100)
              ])
            }
          }
          
          chart.setOption({
            tooltip: {
              position: 'top'
            },
            grid: {
              height: '60%',
              top: '10%'
            },
            xAxis: {
              type: 'category',
              data: hours,
              splitArea: {
                show: true
              }
            },
            yAxis: {
              type: 'category',
              data: days,
              splitArea: {
                show: true
              }
            },
            visualMap: {
              min: 0,
              max: 100,
              calculable: true,
              orient: 'horizontal',
              left: 'center',
              bottom: '15%',
              inRange: {
                color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695']
              }
            },
            series: [{
              name: 'API调用量',
              type: 'heatmap',
              data: heatmapData,
              label: {
                show: true
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }]
          })
        }
      } catch (error) {
        console.error('加载数据失败:', error)
      }
    }
    
    // 计算性能指标
    const calculatePerformanceMetrics = (endpoints) => {
      if (!endpoints || endpoints.length === 0) {
        return {
          totalRequests: 0,
          errorRate: 0,
          peakQps: 0,
          systemLoad: '-',
          memoryUsage: '-',
          diskUsage: '-'
        }
      }
      
      // 计算总请求数
      const totalRequests = endpoints.reduce((sum, ep) => sum + (ep.totalRequests || 0), 0)
      
      // 计算错误率
      const totalErrors = endpoints.reduce((sum, ep) => sum + (ep.errorCount || 0), 0)
      const errorRate = totalRequests > 0 ? totalErrors / totalRequests : 0
      
      // 计算峰值QPS
      const peakQps = Math.max(...endpoints.map(ep => ep.qps || 0), 0)
      
      // 模拟系统资源使用情况（实际项目中应该从API获取）
      const systemLoad = '1.2'
      const memoryUsage = '45%'
      const diskUsage = '68%'
      
      return {
        totalRequests,
        errorRate,
        peakQps,
        systemLoad,
        memoryUsage,
        diskUsage
      }
    }

    const handleAppChange = () => {
      loadData()
    }

    // 格式化数字
    const formatNumber = (num) => {
      if (typeof num !== 'number') return '0'
      if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k'
      }
      return Math.round(num)
    }

    // 格式化百分比
    const formatPercent = (num) => {
      if (typeof num !== 'number') return '0%'
      return (num * 100).toFixed(1) + '%'
    }

    onMounted(() => {
      initDateRange()
      loadData()
    })

    return {
      overview,
      performanceChart,
      errorRateChart,
      resourceChart,
      heatmapChart,
      responseTimeChart,
      dateRange,
      handleAppChange,
      handleDateRangeChange,
      setDateRange,
      formatNumber,
      formatPercent
    }
  }
}
</script>

<style scoped>
.overview {
  animation: fadeIn 0.3s ease;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
  padding: 2rem 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.02); }
}

.overview h2 {
  margin-bottom: 0.5rem;
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3rem;
  padding: 0 1rem;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.date-picker {
  width: 280px;
  border-radius: 0.75rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.time-preset-buttons {
  display: flex;
  gap: 0.5rem;
}

.time-preset-buttons .el-button {
  border-radius: 0.5rem;
  transition: all 0.3s ease;
  font-weight: 500;
}

.time-preset-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.stats-row {
  margin-bottom: 2rem;
}

/* 统计卡片样式 */
.stat-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  position: relative;
  background: #ffffff;
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: none;
  height: 100%;
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  animation: pulse 2s infinite;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 2.5rem;
  position: relative;
  z-index: 1;
}

/* 图标样式 */
.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

/* 图标背景色 */
.stat-card.endpoints .stat-icon {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #2563eb;
}

.stat-card.dependencies .stat-icon {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #1e40af;
}

.stat-card.risks .stat-icon {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #d97706;
}

.stat-card.high-risk .stat-icon {
  background: linear-gradient(135deg, #fee2e2, #fca5a5);
  color: #b91c1c;
}

.stat-card.performance .stat-icon {
  background: linear-gradient(135deg, #dcfce7, #bbf7d0);
  color: #166534;
}

.stat-card.error .stat-icon {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #d97706;
}

.stat-card.throughput .stat-icon {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #1e40af;
}

/* 统计信息 */
.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 0.75rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.stat-label {
  font-size: 1rem;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.75px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 图表卡片样式 */
.chart-card {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 1rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: none;
  overflow: hidden;
  background: #ffffff;
  height: 100%;
}

.chart-card:hover {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  transform: translateY(-4px);
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #8b5cf6, #a78bfa);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.chart-card:hover::before {
  opacity: 1;
}

.card-header-custom {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 700;
  color: #1e293b;
  font-size: 1.125rem;
  padding: 1.5rem 1.5rem 1rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.header-icon {
  font-size: 20px;
  color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
  padding: 8px;
  border-radius: 0.5rem;
  background: #eff6ff;
}

/* 图表容器 */
.chart-container {
  height: 360px;
  padding: 1.5rem;
  position: relative;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stat-content {
    padding: 2rem;
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
    font-size: 28px;
  }
  
  .stat-value {
    font-size: 2rem;
  }
  
  .chart-container {
    height: 320px;
  }
}

@media (max-width: 768px) {
  .overview {
    padding: 1rem 0;
  }
  
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
    margin-bottom: 2rem;
  }
  
  .overview h2 {
    font-size: 1.5rem;
  }
  
  .stats-row {
    margin-bottom: 1.5rem;
  }
  
  .stat-content {
    padding: 1.5rem;
    gap: 1rem;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 1.75rem;
  }
  
  .stat-label {
    font-size: 0.875rem;
  }
  
  .chart-container {
    height: 280px;
    padding: 1rem;
  }
  
  .card-header-custom {
    font-size: 1rem;
    padding: 1rem;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 加载动画 */
.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-size: 1.125rem;
  color: #64748b;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #64748b;
}

.empty-state-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}
</style>
