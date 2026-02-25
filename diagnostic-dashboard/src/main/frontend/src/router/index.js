import { createRouter, createWebHistory } from 'vue-router'
import Overview from '../views/Overview.vue'
import Performance from '../views/Performance.vue'
import Topology from '../views/Topology.vue'
import Risks from '../views/Risks.vue'
import JvmMonitoring from '../views/JvmMonitoring.vue'

const routes = [
  {
    path: '/',
    name: 'Overview',
    component: Overview
  },
  {
    path: '/performance',
    name: 'Performance',
    component: Performance
  },
  {
    path: '/topology',
    name: 'Topology',
    component: Topology
  },
  {
    path: '/risks',
    name: 'Risks',
    component: Risks
  },
  {
    path: '/jvm',
    name: 'JvmMonitoring',
    component: JvmMonitoring
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
