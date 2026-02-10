<template>
  <div class="risks">
    <div class="header-section">
      <h2>风险告警</h2>
      <AppSelector @change="handleAppChange" />
    </div>
    
    <el-row :gutter="20" class="risk-summary">
      <el-col :span="8">
        <el-card class="risk-card high">
          <div class="risk-stat">
            <div class="risk-icon">🔴</div>
            <div class="risk-info">
              <div class="risk-count">{{ riskData.high || 0 }}</div>
              <div class="risk-label">高危风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="risk-card medium">
          <div class="risk-stat">
            <div class="risk-icon">🟡</div>
            <div class="risk-info">
              <div class="risk-count">{{ riskData.medium || 0 }}</div>
              <div class="risk-label">中危风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="risk-card low">
          <div class="risk-stat">
            <div class="risk-icon">🟢</div>
            <div class="risk-info">
              <div class="risk-count">{{ riskData.low || 0 }}</div>
              <div class="risk-label">低危风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>风险详情</span>
      </template>
      
      <el-timeline>
        <el-timeline-item
          v-for="(risk, index) in risks"
          :key="index"
          :type="getSeverityType(risk.severity)"
          :icon="getSeverityIcon(risk.severity)"
        >
          <el-card>
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span><strong>{{ getRiskTypeLabel(risk.type) }}</strong></span>
                <el-tag :type="getSeverityType(risk.severity)">
                  {{ risk.severity }}
                </el-tag>
              </div>
            </template>
            
            <el-descriptions :column="1" border>
              <el-descriptions-item label="描述">
                {{ risk.description }}
              </el-descriptions-item>
              <el-descriptions-item label="影响组件">
                <el-tag
                  v-for="(component, idx) in risk.affectedComponents"
                  :key="idx"
                  style="margin-right: 5px;"
                >
                  {{ component }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="建议">
                {{ risk.recommendation }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      
      <el-empty v-if="risks.length === 0" description="暂无风险告警" />
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { dashboardApi } from '../api/dashboard'
import { appStore } from '../store/appStore'
import AppSelector from '../components/AppSelector.vue'

export default {
  name: 'Risks',
  components: {
    AppSelector
  },
  setup() {
    const riskData = ref({})
    const risks = ref([])

    const loadData = async () => {
      try {
        const appName = appStore.selectedApp
        const response = await dashboardApi.getRisks(appName)
        riskData.value = response.data
        risks.value = response.data.risks || []
      } catch (error) {
        console.error('加载风险数据失败:', error)
      }
    }

    const getSeverityType = (severity) => {
      const types = {
        'HIGH': 'danger',
        'MEDIUM': 'warning',
        'LOW': 'success'
      }
      return types[severity] || 'info'
    }

    const getSeverityIcon = (severity) => {
      const icons = {
        'HIGH': 'CircleCloseFilled',
        'MEDIUM': 'WarningFilled',
        'LOW': 'InfoFilled'
      }
      return icons[severity] || 'InfoFilled'
    }

    const getRiskTypeLabel = (type) => {
      const labels = {
        'SINGLE_POINT_OF_FAILURE': '单点故障',
        'CIRCULAR_DEPENDENCY': '循环依赖',
        'SLOW_ENDPOINT': '慢端点',
        'HIGH_ERROR_RATE': '高错误率',
        'PERFORMANCE_DEGRADATION': '性能下降'
      }
      return labels[type] || type
    }

    const handleAppChange = () => {
      loadData()
    }

    onMounted(() => {
      loadData()
    })

    return {
      riskData,
      risks,
      getSeverityType,
      getSeverityIcon,
      getRiskTypeLabel,
      handleAppChange
    }
  }
}
</script>

<style scoped>
.risks {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.risks h2 {
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

.risk-summary {
  margin-bottom: 30px;
}

.risk-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 20px;
  overflow: hidden;
  position: relative;
}

.risk-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.risk-card.high::before {
  background: linear-gradient(90deg, #f56c6c 0%, #ff8a8a 100%);
}

.risk-card.medium::before {
  background: linear-gradient(90deg, #e6a23c 0%, #f5b95f 100%);
}

.risk-card.low::before {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.risk-card:hover::before {
  transform: scaleX(1);
}

.risk-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.risk-card.high {
  border-left: none;
}

.risk-card.medium {
  border-left: none;
}

.risk-card.low {
  border-left: none;
}

.risk-stat {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.risk-icon {
  font-size: 48px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.risk-count {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.risk-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.el-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.el-timeline :deep(.el-timeline-item__wrapper) {
  padding-left: 30px;
}

.el-timeline :deep(.el-timeline-item__node) {
  width: 16px;
  height: 16px;
}
</style>
