import { reactive } from 'vue'

export const appStore = reactive({
  apps: [],
  selectedApp: null,
  
  setApps(apps) {
    this.apps = apps
    if (apps.length > 0 && !this.selectedApp) {
      this.selectedApp = apps[0].name
    }
  },
  
  setSelectedApp(appName) {
    this.selectedApp = appName
  }
})
