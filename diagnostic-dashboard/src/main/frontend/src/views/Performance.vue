<template>
  <div class="performance">
    <div class="header-section">
      <h2>性能监控</h2>
      <AppSelector @change="handleAppChange" />
    </div>
    
    <!-- 业务端点 -->
    <el-card class="endpoint-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Document /></el-icon>
            业务端点
          </span>
          <el-tag type="info">{{ businessEndpoints.length }} 个端点</el-tag>
        </div>
      </template>
      <el-table :data="businessEndpoints" stripe style="width: 100%">
        <el-table-column prop="name" label="端点" min-width="200" />
        <el-table-column prop="qps" label="QPS" width="140" sortable>
          <template #default="{ row }">
            {{ row.qps.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTime" label="平均响应时间(ms)" width="190" sortable>
          <template #default="{ row }">
            {{ row.avgResponseTime.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="p95" label="P95(ms)" width="160" sortable>
          <template #default="{ row }">
            {{ row.p95.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="p99" label="P99(ms)" width="160" sortable>
          <template #default="{ row }">
            {{ row.p99.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="错误数" width="140" sortable>
          <template #default="{ row }">
            <el-tag 
              :type="row.errorCount > 0 ? 'danger' : 'success'"
              style="cursor: pointer"
              @click="row.errorCount > 0 && showErrorDetails(row.name)">
              {{ row.errorCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorRate" label="错误率" width="140" sortable>
          <template #default="{ row }">
            <el-tag :type="row.errorRate > 0.05 ? 'danger' : 'success'">
              {{ (row.errorRate * 100).toFixed(2) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalRequests" label="总请求数" width="130" sortable />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary"
              @click="showIpDistribution(row.name)">
              IP分布
            </el-button>
            <el-button 
              v-if="row.errorCount > 0"
              size="small" 
              type="danger"
              @click="showErrorDetails(row.name)">
              错误
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 诊断端点 -->
    <el-card class="endpoint-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Monitor /></el-icon>
            诊断端点
          </span>
          <el-tag type="warning">{{ diagnosticEndpoints.length }} 个端点</el-tag>
        </div>
      </template>
      <el-table :data="diagnosticEndpoints" stripe style="width: 100%">
        <el-table-column prop="name" label="端点" min-width="200" />
        <el-table-column prop="qps" label="QPS" width="140" sortable>
          <template #default="{ row }">
            {{ row.qps.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTime" label="平均响应时间(ms)" width="190" sortable>
          <template #default="{ row }">
            {{ row.avgResponseTime.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="p95" label="P95(ms)" width="160" sortable>
          <template #default="{ row }">
            {{ row.p95.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="p99" label="P99(ms)" width="160" sortable>
          <template #default="{ row }">
            {{ row.p99.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="错误数" width="140" sortable>
          <template #default="{ row }">
            <el-tag 
              :type="row.errorCount > 0 ? 'danger' : 'success'"
              style="cursor: pointer"
              @click="row.errorCount > 0 && showErrorDetails(row.name)">
              {{ row.errorCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorRate" label="错误率" width="140" sortable>
          <template #default="{ row }">
            <el-tag :type="row.errorRate > 0.05 ? 'danger' : 'success'">
              {{ (row.errorRate * 100).toFixed(2) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalRequests" label="总请求数" width="130" sortable />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary"
              @click="showIpDistribution(row.name)">
              IP分布
            </el-button>
            <el-button 
              v-if="row.errorCount > 0"
              size="small" 
              type="danger"
              @click="showErrorDetails(row.name)">
              错误
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>响应时间趋势（业务端点 Top 5）</span>
          </template>
          <div ref="trendChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 错误详情弹窗 - 精美版 -->
    <el-dialog 
      v-model="errorDialogVisible" 
      width="85%"
      top="5vh"
      :show-close="true"
      class="error-details-dialog">
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <el-icon class="header-icon" :size="24"><Warning /></el-icon>
            <div class="header-text">
              <h3>错误详情分析</h3>
              <p class="endpoint-path">{{ selectedEndpoint }}</p>
            </div>
          </div>
        </div>
      </template>
      
      <div v-if="errorDetails" class="error-details-content">
        <!-- 统计卡片 - 精美版 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="8">
            <div class="stat-card stat-card-danger">
              <div class="stat-icon">
                <el-icon :size="32"><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">总错误数</div>
                <div class="stat-value">{{ errorDetails.errorCount }}</div>
                <div class="stat-trend">累计错误请求</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card stat-card-warning">
              <div class="stat-icon">
                <el-icon :size="32"><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">错误率</div>
                <div class="stat-value">{{ (errorDetails.errorRate * 100).toFixed(2) }}%</div>
                <div class="stat-trend">占总请求比例</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card stat-card-info">
              <div class="stat-icon">
                <el-icon :size="32"><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">最近错误</div>
                <div class="stat-value">{{ errorDetails.recentErrors?.length || 0 }}</div>
                <div class="stat-trend">最多显示100条</div>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 状态码分布图表 -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header-custom">
              <el-icon class="header-icon-small"><PieChart /></el-icon>
              <span>错误状态码分布</span>
            </div>
          </template>
          <div ref="statusCodeChart" class="chart-container"></div>
        </el-card>

        <!-- 错误记录表格 -->
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="card-header-custom">
              <el-icon class="header-icon-small"><List /></el-icon>
              <span>最近错误记录</span>
              <el-tag type="info" size="small" style="margin-left: auto;">
                共 {{ errorDetails.recentErrors?.length || 0 }} 条
              </el-tag>
            </div>
          </template>
          <el-table 
            :data="errorDetails.recentErrors" 
            stripe 
            max-height="450"
            class="error-table">
            <el-table-column prop="timestamp" label="发生时间" width="190" fixed>
              <template #default="{ row }">
                <div class="time-cell">
                  <el-icon class="time-icon"><Clock /></el-icon>
                  {{ formatTime(row.timestamp) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="statusCode" label="状态码" width="120" align="center">
              <template #default="{ row }">
                <el-tag 
                  :type="getStatusCodeType(row.statusCode)"
                  effect="dark"
                  size="large"
                  class="status-tag">
                  {{ row.statusCode }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="method" label="请求方法" width="120" align="center">
              <template #default="{ row }">
                <el-tag 
                  :type="getMethodType(row.method)"
                  size="small"
                  class="method-tag">
                  {{ row.method }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="endpoint" label="端点路径" min-width="250">
              <template #default="{ row }">
                <div class="endpoint-cell">
                  <el-icon class="endpoint-icon"><Link /></el-icon>
                  <span class="endpoint-text">{{ row.endpoint }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="响应时间" width="130" align="center" sortable>
              <template #default="{ row }">
                <div class="duration-cell">
                  <el-tag 
                    :type="getDurationType(row.duration)"
                    effect="plain"
                    size="small">
                    {{ row.duration.toFixed(2) }} ms
                  </el-tag>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      
      <div v-else class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载错误详情中...</p>
      </div>
    </el-dialog>

    <!-- IP分布分析弹窗 -->
    <el-dialog 
      v-model="ipDistributionVisible" 
      width="85%"
      top="5vh"
      :show-close="true"
      class="ip-distribution-dialog">
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <el-icon class="header-icon" :size="24"><User /></el-icon>
            <div class="header-text">
              <h3>IP访问分布分析</h3>
              <p class="endpoint-path">{{ selectedEndpoint }}</p>
            </div>
          </div>
        </div>
      </template>
      
      <div v-if="ipDistribution" class="ip-distribution-content">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <div class="stat-card stat-card-primary">
              <div class="stat-icon">
                <el-icon :size="32"><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">总请求数</div>
                <div class="stat-value">{{ ipDistribution.totalRequests }}</div>
                <div class="stat-trend">累计请求</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-card-info">
              <div class="stat-icon">
                <el-icon :size="32"><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">唯一IP数</div>
                <div class="stat-value">{{ ipDistribution.uniqueIpCount }}</div>
                <div class="stat-trend">不同来源</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card stat-card-warning">
              <div class="stat-icon">
                <el-icon :size="32"><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">Top1集中度</div>
                <div class="stat-value">{{ (ipDistribution.concentrationRate * 100).toFixed(1) }}%</div>
                <div class="stat-trend">最高占比</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card" :class="ipDistribution.isAnomalous ? 'stat-card-danger' : 'stat-card-success'">
              <div class="stat-icon">
                <el-icon :size="32"><Warning /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">状态</div>
                <div class="stat-value">
                  <el-tag 
                    :type="ipDistribution.isAnomalous ? 'danger' : 'success'"
                    effect="dark"
                    size="large">
                    {{ ipDistribution.isAnomalous ? '异常' : '正常' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 异常提示 -->
        <el-alert
          v-if="ipDistribution.isAnomalous"
          type="warning"
          :title="ipDistribution.anomalyDescription"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;">
        </el-alert>

        <!-- 图表区域 -->
        <!-- 请求分布图 -->
        <el-row :gutter="20" class="charts-row">
          <el-col :span="24">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header-custom">
                  <el-icon class="header-icon-small"><PieChart /></el-icon>
                  <span>请求分布</span>
                </div>
              </template>
              <div ref="ipDistributionChart" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 地理位置分布图 -->
        <el-row :gutter="20" class="charts-row">
          <el-col :span="24">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header-custom">
                  <el-icon class="header-icon-small"><TrendCharts /></el-icon>
                  <span>地理位置分布 (城市级别)</span>
                </div>
              </template>
              <div ref="geoDistributionChart" id="geoDistributionChart" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- IP卡片列表 -->
        <el-row :gutter="16" class="ip-cards-row">
          <el-col 
            v-for="(ip, index) in ipDistribution.topIps" 
            :key="ip.ip"
            :xs="24" :sm="12" :md="8" :lg="6">
            <el-card class="ip-card" shadow="hover" :body-style="{ padding: '16px' }">
              <div class="ip-card-header">
                <div class="ip-address">
                  <el-tag type="primary" effect="dark" size="large">{{ ip.ip }}</el-tag>
                </div>
                <el-tag 
                  :type="getAnomalyLevelType(ip.anomalyLevel)"
                  size="small"
                  effect="dark">
                  {{ getAnomalyLevelText(ip.anomalyLevel) }}
                </el-tag>
              </div>
              
              <div class="ip-card-body">
                <div class="ip-stat-row">
                  <span class="stat-label">请求次数</span>
                  <span class="stat-value">{{ ip.requestCount }}</span>
                </div>
                <div class="ip-stat-row">
                  <span class="stat-label">占比</span>
                  <span class="stat-value">{{ ((ip.requestCount / ipDistribution.totalRequests) * 100).toFixed(1) }}%</span>
                </div>
                <div class="ip-stat-row">
                  <span class="stat-label">错误率</span>
                  <el-tag 
                    :type="ip.errorRate > 0.5 ? 'danger' : ip.errorRate > 0.1 ? 'warning' : 'success'"
                    size="small">
                    {{ (ip.errorRate * 100).toFixed(1) }}%
                  </el-tag>
                </div>
                <div class="ip-stat-row">
                  <span class="stat-label">平均耗时</span>
                  <span class="stat-value">{{ ip.avgResponseTime.toFixed(2) }} ms</span>
                </div>
                
                <div v-if="ip.city || ip.province || ip.country" class="ip-location">
                  <el-icon><Location /></el-icon>
                  <div class="location-details">
                    <div v-if="ip.city" class="location-row">
                      <span class="location-label">城市:</span>
                      <span class="location-value">{{ ip.city }}</span>
                    </div>
                    <div v-if="ip.province" class="location-row">
                      <span class="location-label">省份:</span>
                      <span class="location-value">{{ ip.province }}</span>
                    </div>
                    <div v-if="ip.country" class="location-row">
                      <span class="location-label">国家:</span>
                      <span class="location-value">{{ ip.country }}</span>
                    </div>
                    <div v-if="ip.isp" class="location-row">
                      <span class="location-label">运营商:</span>
                      <span class="location-value">{{ ip.isp }}</span>
                    </div>
                  </div>
                </div>
                
                <div v-if="ip.anomalyReason" class="ip-anomaly">
                  <el-icon><Warning /></el-icon>
                  <span>{{ ip.anomalyReason }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <div v-else class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载IP分布数据中...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { dashboardApi } from '../api/dashboard'
import { appStore } from '../store/appStore'
import AppSelector from '../components/AppSelector.vue'
import { Document, Monitor, Warning, TrendCharts, PieChart, List, Clock, Link, Loading, User, Location } from '@element-plus/icons-vue'

// 注册中国地图数据
async function registerMapData() {
  try {
    console.log('开始检查地图数据...')
    // 检查是否已经注册了地图数据
    let hasMap = false
    try {
      hasMap = !!echarts.getMap('china')
      console.log('地图数据检查结果:', hasMap ? '已注册' : '未注册')
    } catch (error) {
      console.error('获取地图数据失败:', error)
    }
    
    if (!hasMap) {
      console.warn('中国地图数据未注册，尝试加载并注册...')
      try {
        // 从CDN加载中国地图数据
        console.log('尝试从CDN加载中国地图数据...')
        // 尝试多个数据源
        const mapDataSources = [
          'https://cdn.jsdelivr.net/npm/echarts/map/json/china.json',
          'https://api.map.baidu.com/api?v=3.0&ak=您的密钥',
          'https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json'
        ]
        
        let chinaGeoData = null
        let success = false
        
        for (const source of mapDataSources) {
          try {
            console.log(`尝试从数据源加载: ${source}`)
            const response = await fetch(source)
            if (response.ok) {
              chinaGeoData = await response.json()
              console.log('地图数据加载成功，开始注册...')
              
              // 注册地图数据
              echarts.registerMap('china', chinaGeoData)
              console.log('地图数据注册成功!')
              success = true
              break
            } else {
              console.warn(`数据源 ${source} 返回错误: ${response.status}`)
            }
          } catch (error) {
            console.warn(`从数据源 ${source} 加载失败:`, error)
          }
        }
        
        if (!success) {
          throw new Error('所有地图数据源加载失败')
        }
        
        // 再次检查
        const hasMapAfter = !!echarts.getMap('china')
        console.log('注册后地图数据检查结果:', hasMapAfter ? '已注册' : '未注册')
      } catch (regError) {
        console.error('加载并注册地图数据失败:', regError)
        console.warn('将回退到柱状图显示')
      }
    }
  } catch (error) {
    console.error('检查地图数据失败:', error)
  }
}

// 初始化时注册地图数据
registerMapData().catch(error => {
  console.error('地图数据初始化失败:', error)
})

export default {
  name: 'Performance',
  components: {
    AppSelector,
    Document,
    Monitor,
    Warning,
    TrendCharts,
    PieChart,
    List,
    Clock,
    Link,
    Loading,
    User,
    Location
  },
  setup() {
    const endpoints = ref([])
    const trendChart = ref(null)
    const errorDialogVisible = ref(false)
    const selectedEndpoint = ref('')
    const errorDetails = ref(null)
    const statusCodeChart = ref(null)
    const ipDistributionVisible = ref(false)
    const ipDistribution = ref(null)
    const ipDistributionChart = ref(null)
    const geoDistributionChart = ref(null)

    // 计算业务端点和诊断端点
    const businessEndpoints = computed(() => {
      return endpoints.value.filter(ep => ep.endpointType === 'BUSINESS' || !ep.endpointType)
    })

    const diagnosticEndpoints = computed(() => {
      return endpoints.value.filter(ep => ep.endpointType === 'DIAGNOSTIC')
    })

    const loadData = async () => {
      try {
        const appName = appStore.selectedApp
        const response = await dashboardApi.getPerformance(appName)
        endpoints.value = response.data.endpoints
        
        if (trendChart.value && businessEndpoints.value.length > 0) {
          const chart = echarts.init(trendChart.value)
          const topEndpoints = businessEndpoints.value.slice(0, 5)
          
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

    const showErrorDetails = async (endpointName) => {
      try {
        selectedEndpoint.value = endpointName
        errorDialogVisible.value = true
        
        const appName = appStore.selectedApp
        const response = await dashboardApi.getEndpointErrors(appName, endpointName)
        errorDetails.value = response.data
        
        // 等待 DOM 更新后渲染图表
        await nextTick()
        renderStatusCodeChart()
      } catch (error) {
        console.error('加载错误详情失败:', error)
      }
    }

    const renderStatusCodeChart = () => {
      if (!statusCodeChart.value || !errorDetails.value) return
      
      const chart = echarts.init(statusCodeChart.value)
      const distribution = errorDetails.value.statusCodeDistribution || {}
      
      const data = Object.entries(distribution).map(([code, count]) => ({
        name: `${code} (${getStatusCodeName(code)})`,
        value: count
      }))
      
      // 定义状态码颜色
      const colorMap = {
        '400': '#f59e0b',
        '401': '#f59e0b',
        '403': '#f59e0b',
        '404': '#f59e0b',
        '405': '#f59e0b',
        '500': '#ef4444',
        '502': '#ef4444',
        '503': '#ef4444',
        '504': '#ef4444'
      }
      
      chart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e2e8f0',
          borderWidth: 1,
          textStyle: {
            color: '#1e293b',
            fontSize: 14
          },
          padding: [12, 16],
          extraCssText: 'border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'
        },
        legend: {
          orient: 'vertical',
          left: '5%',
          top: 'center',
          itemGap: 16,
          itemWidth: 14,
          itemHeight: 14,
          textStyle: {
            fontSize: 13,
            color: '#475569',
            padding: [0, 0, 0, 8]
          },
          formatter: (name) => {
            const item = data.find(d => d.name === name)
            return `${name}  ${item ? item.value : ''}`
          }
        },
        series: [
          {
            name: '状态码',
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['65%', '50%'],
            avoidLabelOverlap: true,
            itemStyle: {
              borderRadius: 8,
              borderColor: '#fff',
              borderWidth: 3
            },
            label: {
              show: true,
              position: 'outside',
              formatter: '{d}%',
              fontSize: 14,
              fontWeight: 'bold',
              color: '#1e293b'
            },
            labelLine: {
              show: true,
              length: 15,
              length2: 10,
              smooth: true
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              },
              itemStyle: {
                shadowBlur: 20,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.3)'
              }
            },
            data: data.map(item => {
              const code = item.name.split(' ')[0]
              return {
                ...item,
                itemStyle: {
                  color: colorMap[code] || '#94a3b8'
                }
              }
            })
          }
        ],
        graphic: {
          type: 'text',
          left: 'center',
          top: 'center',
          style: {
            text: `总计\n${data.reduce((sum, item) => sum + item.value, 0)}`,
            textAlign: 'center',
            fill: '#1e293b',
            fontSize: 20,
            fontWeight: 'bold',
            lineHeight: 28
          }
        }
      })
      
      // 响应式
      window.addEventListener('resize', () => {
        chart.resize()
      })
    }

    const formatTime = (timestamp) => {
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }

    const getStatusCodeType = (code) => {
      if (code >= 500) return 'danger'
      if (code >= 400) return 'warning'
      return 'success'
    }

    const getStatusCodeName = (code) => {
      const names = {
        400: 'Bad Request',
        401: 'Unauthorized',
        403: 'Forbidden',
        404: 'Not Found',
        405: 'Method Not Allowed',
        500: 'Internal Server Error',
        502: 'Bad Gateway',
        503: 'Service Unavailable',
        504: 'Gateway Timeout'
      }
      return names[code] || 'Error'
    }

    const getMethodType = (method) => {
      const types = {
        'GET': 'success',
        'POST': 'primary',
        'PUT': 'warning',
        'DELETE': 'danger',
        'PATCH': 'info'
      }
      return types[method] || 'info'
    }

    const getDurationType = (duration) => {
      if (duration < 100) return 'success'
      if (duration < 500) return 'warning'
      return 'danger'
    }

    const showIpDistribution = async (endpointName) => {
      try {
        selectedEndpoint.value = endpointName
        ipDistributionVisible.value = true
        ipDistribution.value = null
        
        const appName = appStore.selectedApp
        const response = await dashboardApi.getEndpointIpDistribution(appName, endpointName)
        ipDistribution.value = response.data
        
        await nextTick()
        renderIpDistributionCharts()
        
        console.log('IP distribution loaded:', ipDistribution.value)
      } catch (error) {
        console.error('加载IP分布失败:', error)
      }
    }

    const renderIpDistributionCharts = () => {
      if (!ipDistribution.value || !ipDistributionChart.value || !geoDistributionChart.value) return
      
      const topIps = ipDistribution.value.topIps
      
      const chart1 = echarts.init(ipDistributionChart.value)
      chart1.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e2e8f0',
          borderWidth: 1,
          textStyle: { color: '#1e293b', fontSize: 14 },
          padding: [12, 16],
          extraCssText: 'border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'
        },
        legend: {
          orient: 'vertical',
          left: '5%',
          top: 'center',
          itemGap: 12,
          itemWidth: 12,
          itemHeight: 12,
          textStyle: { fontSize: 12, color: '#475569' }
        },
        series: [{
          name: '请求分布',
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['65%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{d}%',
            fontSize: 12,
            fontWeight: 'bold',
            color: '#1e293b'
          },
          labelLine: {
            show: true,
            length: 12,
            length2: 8,
            smooth: true
          },
          emphasis: {
            label: { show: true, fontSize: 14, fontWeight: 'bold' },
            itemStyle: {
              shadowBlur: 15,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.3)'
            }
          },
          data: topIps.map((ip, index) => ({
            name: ip.ip,
            value: ip.requestCount,
            itemStyle: {
              color: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'][index % 5]
            }
          }))
        }]
      })
      
      const geoData = []
      console.log('Top IPs for geo distribution:', topIps)
      // 直辖市映射表，确保地图显示正确
      const municipalityMap = {
        '北京': '北京市',
        '上海': '上海市',
        '天津': '天津市',
        '重庆': '重庆市'
      }
      
      // 省份名称标准化映射，确保与地图数据匹配
      const provinceNormalizationMap = {
        '北京市': '北京',
        '上海市': '上海',
        '天津市': '天津',
        '重庆市': '重庆',
        '河北省': '河北',
        '山西省': '山西',
        '辽宁省': '辽宁',
        '吉林省': '吉林',
        '黑龙江省': '黑龙江',
        '江苏省': '江苏',
        '浙江省': '浙江',
        '安徽省': '安徽',
        '福建省': '福建',
        '江西省': '江西',
        '山东省': '山东',
        '河南省': '河南',
        '湖北省': '湖北',
        '湖南省': '湖南',
        '广东省': '广东',
        '海南省': '海南',
        '四川省': '四川',
        '贵州省': '贵州',
        '云南省': '云南',
        '陕西省': '陕西',
        '甘肃省': '甘肃',
        '青海省': '青海',
        '台湾省': '台湾',
        '内蒙古自治区': '内蒙古',
        '广西壮族自治区': '广西',
        '西藏自治区': '西藏',
        '宁夏回族自治区': '宁夏',
        '新疆维吾尔自治区': '新疆',
        '香港特别行政区': '香港',
        '澳门特别行政区': '澳门'
      }
      
      // 按省份分组城市数据
      const provinceCityMap = {}
      
      topIps.forEach(ip => {
        console.log('Processing IP:', ip.ip, 'City:', ip.city, 'Province:', ip.province, 'Country:', ip.country, 'RequestCount:', ip.requestCount)
        if ((ip.city || ip.province) && ip.requestCount > 0) {
          // 确保省份名称有效
          let provinceName = ip.province
          if (!provinceName || provinceName === '0') {
            console.log('过滤无效省份:', provinceName)
            return
          }
          
          // 处理直辖市
          if (municipalityMap[provinceName]) {
            provinceName = municipalityMap[provinceName]
          }
          
          // 标准化省份名称
          if (provinceNormalizationMap[provinceName]) {
            provinceName = provinceNormalizationMap[provinceName]
          }
          
          // 确保省份城市映射存在
          if (!provinceCityMap[provinceName]) {
            provinceCityMap[provinceName] = {
              totalRequests: 0,
              cities: {}
            }
          }
          
          // 如果有城市信息且城市有效
          if (ip.city && ip.city !== '0') {
            // 确保城市名称有效
            let cityName = ip.city
            if (cityName === '0') {
              console.log('过滤无效城市:', cityName)
              // 即使城市无效，也要累加省份总请求数
              provinceCityMap[provinceName].totalRequests += ip.requestCount
              return
            }
            
            // 累加省份总请求数
            provinceCityMap[provinceName].totalRequests += ip.requestCount
            
            // 添加或更新城市请求数
            if (!provinceCityMap[provinceName].cities[cityName]) {
              provinceCityMap[provinceName].cities[cityName] = 0
            }
            provinceCityMap[provinceName].cities[cityName] += ip.requestCount
            
            console.log(`添加城市到省份 ${provinceName}: ${cityName} 请求数: ${ip.requestCount}`)
            console.log(`当前 ${provinceName} 总请求数: ${provinceCityMap[provinceName].totalRequests}`)
            console.log(`当前 ${cityName} 总请求数: ${provinceCityMap[provinceName].cities[cityName]}`)
            
          } else {
            // 没有城市信息，只累加省份总请求数
            provinceCityMap[provinceName].totalRequests += ip.requestCount
            console.log(`添加无城市信息到省份 ${provinceName}: 请求数: ${ip.requestCount}`)
            console.log(`当前 ${provinceName} 总请求数: ${provinceCityMap[provinceName].totalRequests}`)
          }
        }
      })
      
      // 构建省份级别的geoData
      Object.entries(provinceCityMap).forEach(([provinceName, data]) => {
        geoData.push({
          name: provinceName,
          value: data.totalRequests,
          cities: data.cities  // 保存城市数据，用于tooltip显示
        })
        console.log('添加省份到geoData:', { name: provinceName, value: data.totalRequests, cities: data.cities })
      })
      console.log('最终geoData:', geoData)
      console.log('Geo data for map:', geoData)
      
      const chart2 = echarts.init(geoDistributionChart.value)
      
      // 尝试加载地图数据并渲染
      async function renderMapWithData() {
        console.log('开始准备渲染地图...')
        
        // 先尝试注册地图数据
        await registerMapData()
        
        // 再次检查是否注册了中国地图
        let hasChinaMap = false
        try {
          hasChinaMap = !!echarts.getMap('china')
          console.log('地图渲染前检查结果:', hasChinaMap ? '已注册' : '未注册')
        } catch (error) {
          console.warn('检查地图数据失败:', error)
        }
        
        if (hasChinaMap) {
          try {
            console.log('尝试渲染地图...')
            chart2.setOption({
              tooltip: {
          trigger: 'item',
          formatter: function(params) {
            let html = `<div style="font-weight: bold; margin-bottom: 8px;">${params.name}</div>`
            let totalRequests = params.value[1] || params.value
            html += `<div>总请求次数: ${totalRequests}</div>`
            
            // 计算有城市信息的请求数总和
            let cityRequestsTotal = 0
            if (params.data.cities && Object.keys(params.data.cities).length > 0) {
              cityRequestsTotal = Object.values(params.data.cities).reduce((sum, count) => sum + count, 0)
            }
            
            // 计算无城市信息的请求数
            let noCityRequests = totalRequests - cityRequestsTotal
            
            // 显示请求数明细
            html += `<div>有城市信息: ${cityRequestsTotal}</div>`
            if (noCityRequests > 0) {
              html += `<div>无城市信息: ${noCityRequests}</div>`
            }
            
            // 显示该省份内的城市请求数量
            if (params.data.cities && Object.keys(params.data.cities).length > 0) {
              html += `<div style="margin-top: 8px; font-weight: bold;">城市分布:</div>`
              
              // 将城市按请求数排序，显示前5个城市
              const sortedCities = Object.entries(params.data.cities)
                .sort((a, b) => b[1] - a[1])
                .slice(0, 5)
              
              sortedCities.forEach(([cityName, requestCount]) => {
                html += `<div>${cityName}: ${requestCount}</div>`
              })
              
              // 如果城市数量超过5个，显示省略信息
              if (Object.keys(params.data.cities).length > 5) {
                html += `<div style="color: #64748b; font-size: 12px;">...</div>`
              }
            }
            
            return html
          },
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e2e8f0',
          borderWidth: 1,
          textStyle: { color: '#1e293b', fontSize: 14 },
          padding: [12, 16],
          extraCssText: 'border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'
        },
              visualMap: {
          min: 0,
          max: Math.max(...geoData.map(item => {
            // 确保value是数字
            return typeof item.value === 'number' ? item.value : 0
          }).filter(val => !isNaN(val)), 10),
          calculable: true,
          inRange: {
            color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695']
          },
          textStyle: {
            color: '#64748b'
          }
        },
              geo: {
          map: 'china',
          roam: true,
          zoom: 1.5,
          center: [104.195, 35.861],
          label: {
            show: true,
            fontSize: 12,
            color: '#475569'
          },
          itemStyle: {
            areaColor: '#f8fafc',
            borderColor: '#e2e8f0',
            borderWidth: 1
          },
          emphasis: {
            itemStyle: {
              areaColor: '#dbeafe'
            },
            label: {
              show: true,
              fontSize: 14,
              color: '#1e293b'
            }
          }
        },
              series: [
          {
            name: '省份请求数',
            type: 'map',
            geoIndex: 0,
            data: geoData,
            label: {
              show: true,
              fontSize: 10,
              color: '#64748b'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 12,
                color: '#1e293b'
              },
              itemStyle: {
                areaColor: '#dbeafe'
              }
            }
          }
        ]
            })
            console.log('地图渲染成功!')
          } catch (error) {
            console.error('地图渲染失败，回退到柱状图:', error)
            // 回退到柱状图
            renderBarChart(chart2, geoData)
          }
        } else {
          console.warn('中国地图数据未注册，使用柱状图显示')
          // 回退到柱状图
          renderBarChart(chart2, geoData)
        }
      }
      
      // 执行渲染
      renderMapWithData().catch(error => {
        console.error('渲染地图过程中发生错误:', error)
        // 出错时回退到柱状图
        renderBarChart(chart2, geoData)
      })
      
      // 柱状图渲染函数
      function renderBarChart(chart, data) {
        // 聚合数据，按省份分组
        const provinceData = {}
        data.forEach(item => {
          if (provinceData[item.name]) {
            provinceData[item.name] += item.value
          } else {
            provinceData[item.name] = item.value
          }
        })
        
        chart.setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: '#e2e8f0',
            borderWidth: 1,
            textStyle: { color: '#1e293b', fontSize: 14 },
            padding: [12, 16],
            extraCssText: 'border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);'
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: Object.keys(provinceData),
            axisLabel: {
              interval: 0,
              rotate: 45,
              fontSize: 11,
              color: '#64748b'
            },
            axisLine: {
              lineStyle: {
                color: '#e2e8f0'
              }
            }
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              fontSize: 11,
              color: '#64748b'
            },
            axisLine: {
              lineStyle: {
                color: '#e2e8f0'
              }
            },
            splitLine: {
              lineStyle: {
                color: '#f1f5f9'
              }
            }
          },
          series: [{
            name: '请求数',
            type: 'bar',
            data: Object.values(provinceData),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#3b82f6' },
                { offset: 1, color: '#60a5fa' }
              ]),
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#2563eb' },
                  { offset: 1, color: '#3b82f6' }
                ])
              }
            }
          }]
        })
      }
    }

    const getAnomalyLevelType = (level) => {
      const types = {
        'NORMAL': 'success',
        'SUSPICIOUS': 'warning',
        'DANGEROUS': 'danger'
      }
      return types[level] || 'info'
    }

    const getAnomalyLevelText = (level) => {
      const texts = {
        'NORMAL': '正常',
        'SUSPICIOUS': '可疑',
        'DANGEROUS': '危险'
      }
      return texts[level] || level
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
      businessEndpoints,
      diagnosticEndpoints,
      trendChart,
      handleAppChange,
      errorDialogVisible,
      selectedEndpoint,
      errorDetails,
      statusCodeChart,
      showErrorDetails,
      formatTime,
      getStatusCodeType,
      getMethodType,
      getDurationType,
      ipDistributionVisible,
      ipDistribution,
      showIpDistribution,
      getAnomalyLevelType,
      getAnomalyLevelText,
      ipDistributionChart,
      geoDistributionChart
    }
  }
}
</script>

<style scoped>
.performance {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.performance h2 {
  margin-bottom: 0.5rem;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-dark);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

/* 端点卡片样式 */
.endpoint-card {
  animation: slideIn 0.3s ease;
  margin-bottom: 1.5rem;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 600;
  font-size: 1.15rem;
}

/* 错误详情弹窗样式 */
.error-details-dialog :deep(.el-dialog) {
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
}

.error-details-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, var(--danger) 0%, #E63757 100%);
  border-bottom: none;
  padding: 0;
  margin: 0;
}

.dialog-header {
  padding: 2rem 2.5rem;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.header-text h3 {
  margin: 0;
  color: white;
  font-size: 1.35rem;
  font-weight: 600;
}

.endpoint-path {
  margin: 0.35rem 0 0 0;
  color: rgba(255, 255, 255, 0.95);
  font-size: 0.875rem;
  font-family: 'Courier New', monospace;
}

.error-details-content {
  padding: 0.5rem;
}

.ip-distribution-dialog :deep(.el-dialog) {
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
}

.ip-distribution-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, var(--primary) 0%, #3b82f6 100%);
  border-bottom: none;
  padding: 0;
  margin: 0;
}

.ip-distribution-content {
  padding: 0.5rem;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 2rem;
}

.stat-card {
  background: var(--white);
  border-radius: var(--border-radius-lg);
  padding: 2rem;
  display: flex;
  align-items: center;
  gap: 1.25rem;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  border: none;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 24px;
}

.stat-card-danger .stat-icon {
  background: var(--danger-light);
  color: var(--danger);
}

.stat-card-warning .stat-icon {
  background: var(--warning-light);
  color: var(--warning);
}

.stat-card-info .stat-icon {
  background: var(--primary-light);
  color: var(--primary);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.925rem;
  color: var(--text-muted);
  font-weight: 500;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 2rem;
  font-weight: 600;
  color: var(--text-dark);
  line-height: 1;
  margin-bottom: 0.35rem;
}

.stat-trend {
  font-size: 0.875rem;
  color: var(--text-gray);
}

/* 图表卡片 */
.chart-card {
  margin-bottom: 2rem;
}

.charts-row {
  margin-bottom: 2rem;
}

.card-header-custom {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.15rem;
  font-weight: 600;
}

.header-icon-small {
  font-size: 20px;
  color: var(--info);
}

.chart-container {
  height: 450px;
  padding: 1.5rem;
}

/* 地理位置分布图容器 */
#geoDistributionChart {
  height: 600px !important;
  width: 100% !important;
}

/* IP卡片列表 */
.ip-cards-row {
  margin-bottom: 1rem;
}

.ip-card {
  transition: all 0.3s ease;
  border: none;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  height: 100%;
}

.ip-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow);
}

.ip-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #f1f5f9;
}

.ip-address {
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

.ip-card-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.ip-stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ip-stat-row .stat-label {
  font-size: 0.875rem;
  color: var(--text-muted);
  font-weight: 500;
}

.ip-stat-row .stat-value {
  font-size: 1rem;
  color: var(--text-dark);
  font-weight: 600;
}

.ip-location {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  font-size: 0.875rem;
  color: var(--primary);
  font-weight: 500;
  padding: 1rem;
  background: var(--primary-light);
  border-radius: var(--border-radius);
  margin-top: 0.75rem;
}

.ip-location .el-icon {
  font-size: 16px;
  margin-top: 2px;
}

.location-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.location-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.location-label {
  font-weight: 600;
  color: var(--text-muted);
  min-width: 50px;
  font-size: 0.825rem;
}

.location-value {
  color: var(--text-dark);
  font-weight: 500;
  font-size: 0.875rem;
}

.ip-anomaly {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--danger);
  font-weight: 500;
  padding: 0.5rem;
  background: var(--danger-light);
  border-radius: var(--border-radius);
}

.ip-anomaly .el-icon {
  font-size: 14px;
}

/* 表格卡片 */
.table-card {
  overflow: hidden;
}

.error-table {
  overflow: hidden;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 13px;
  color: var(--text-dark);
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.time-icon {
  color: var(--primary);
  font-size: 14px;
}

.status-tag {
  font-weight: 600;
  font-size: 0.825rem;
  padding: 0.35rem 0.75rem;
}

.method-tag {
  font-weight: 600;
}

.endpoint-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.endpoint-icon {
  color: var(--info);
  font-size: 14px;
  flex-shrink: 0;
}

.endpoint-text {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: var(--text-dark);
  font-weight: 500;
  word-break: break-all;
}

.duration-cell {
  display: flex;
  justify-content: center;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 1.5rem;
}

.loading-container .el-icon {
  margin-bottom: 1.25rem;
  color: var(--primary);
}

.loading-container p {
  font-size: 0.925rem;
  color: var(--text-muted);
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 1.5rem;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
  }
  
  .stat-value {
    font-size: 1.75rem;
  }
  
  .chart-container {
    height: 280px;
  }
}
</style>
