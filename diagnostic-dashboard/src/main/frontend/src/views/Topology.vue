<template>
  <div class="topology">
    <div class="header-section">
      <h2>依赖拓扑图</h2>
      <AppSelector @change="handleAppChange" />
    </div>
    
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>组件依赖关系</span>
          <el-tag v-if="topology.hasCycles" type="danger">检测到循环依赖</el-tag>
          <el-tag v-else type="success">无循环依赖</el-tag>
        </div>
      </template>
      
      <div ref="topologyChart" style="height: 600px;"></div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>节点统计</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总节点数">
              {{ topology.nodes?.length || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="应用节点">
              {{ countNodesByType('APPLICATION') }}
            </el-descriptions-item>
            <el-descriptions-item label="数据库">
              {{ countNodesByType('DATABASE') }}
            </el-descriptions-item>
            <el-descriptions-item label="缓存">
              {{ countNodesByType('CACHE') }}
            </el-descriptions-item>
            <el-descriptions-item label="消息队列">
              {{ countNodesByType('MESSAGE_QUEUE') }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>连接统计</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总连接数">
              {{ topology.edges?.length || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="HTTP调用">
              {{ countEdgesByType('HTTP') }}
            </el-descriptions-item>
            <el-descriptions-item label="数据库连接">
              {{ countEdgesByType('DATABASE') }}
            </el-descriptions-item>
            <el-descriptions-item label="缓存连接">
              {{ countEdgesByType('CACHE') }}
            </el-descriptions-item>
          </el-descriptions>
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
  name: 'Topology',
  components: {
    AppSelector
  },
  setup() {
    const topology = ref({ nodes: [], edges: [] })
    const topologyChart = ref(null)

    const countNodesByType = (type) => {
      return topology.value.nodes?.filter(n => n.type === type).length || 0
    }

    const countEdgesByType = (type) => {
      return topology.value.edges?.filter(e => e.type === type).length || 0
    }

    const loadData = async () => {
      try {
        const appName = appStore.selectedApp
        const response = await dashboardApi.getTopology(appName)
        topology.value = response.data
        
        if (topologyChart.value) {
          const chart = echarts.init(topologyChart.value)
          
          const nodes = topology.value.nodes.map(node => ({
            id: node.id,
            name: node.name || node.id || '未知节点',  // 使用name，如果没有则用id
            symbolSize: 60,
            itemStyle: {
              color: getNodeColor(node.type)
            },
            label: {
              show: true,
              position: 'inside',  // 标签显示在圆圈内部
              fontSize: 11,
              color: '#fff',
              fontWeight: 'bold'
            }
          }))
          
          const links = topology.value.edges.map(edge => ({
            source: edge.source,
            target: edge.target
          }))
          
          chart.setOption({
            tooltip: {
              formatter: (params) => {
                if (params.dataType === 'node') {
                  return `${params.data.name}<br/>类型: ${params.data.type || '未知'}`
                }
                return params.name
              }
            },
            series: [{
              type: 'graph',
              layout: 'force',
              data: nodes,
              links: links,
              roam: true,
              label: {
                show: true
              },
              force: {
                repulsion: 250,
                edgeLength: 180
              },
              emphasis: {
                focus: 'adjacency',
                label: {
                  fontSize: 14
                }
              }
            }]
          })
        }
      } catch (error) {
        console.error('加载拓扑数据失败:', error)
      }
    }

    const getNodeColor = (type) => {
      const colors = {
        'APPLICATION': '#409EFF',
        'DATABASE': '#67C23A',
        'CACHE': '#E6A23C',
        'MESSAGE_QUEUE': '#F56C6C',
        'EXTERNAL_SERVICE': '#909399'
      }
      return colors[type] || '#909399'
    }

    const handleAppChange = () => {
      loadData()
    }

    onMounted(() => {
      loadData()
    })

    return {
      topology,
      topologyChart,
      countNodesByType,
      countEdgesByType,
      handleAppChange
    }
  }
}
</script>

<style scoped>
.topology {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.topology h2 {
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

.el-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.el-card :deep(.el-descriptions__label) {
  font-weight: 600;
  color: #606266;
}

.el-card :deep(.el-descriptions__content) {
  font-weight: 500;
  color: #303133;
}
</style>
