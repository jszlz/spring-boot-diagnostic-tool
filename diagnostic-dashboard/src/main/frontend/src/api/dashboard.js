import axios from 'axios'

const API_BASE = import.meta.env.DEV ? 'http://localhost:8081/api/dashboard' : '/api/dashboard'

export const dashboardApi = {
  getApps() {
    return axios.get(`${API_BASE}/apps`)
  },
  
  getOverview(appName, startDate, endDate) {
    return axios.get(`${API_BASE}/overview`, { params: { appName, startDate, endDate } })
  },
  
  getPerformance(appName, startDate, endDate) {
    return axios.get(`${API_BASE}/performance`, { params: { appName, startDate, endDate } })
  },
  
  getTopology(appName) {
    return axios.get(`${API_BASE}/topology`, { params: { appName } })
  },
  
  getRisks(appName) {
    return axios.get(`${API_BASE}/risks`, { params: { appName } })
  },
  
  getTrends(appName) {
    return axios.get(`${API_BASE}/trends`, { params: { appName } })
  },
  
  getHealth(appName) {
    return axios.get(`${API_BASE}/health`, { params: { appName } })
  },
  
  getEndpointErrors(appName, endpoint) {
    return axios.get(`${API_BASE}/endpoint-errors`, { 
      params: { appName, endpoint } 
    })
  },
  
  getEndpointIpDistribution(appName, endpoint) {
    return axios.get(`${API_BASE}/endpoint-ip-distribution`, { 
      params: { appName, endpoint } 
    })
  }
}
