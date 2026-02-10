import { createRouter, createWebHistory } from 'vue-router'
import Overview from '../views/Overview.vue'
import Performance from '../views/Performance.vue'
import Topology from '../views/Topology.vue'
import Risks from '../views/Risks.vue'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
