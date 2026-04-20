import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router/index.js'
import './styles/base.scss'

const app = createApp(App)

// Register all Element Plus icons globally
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

app
  .use(createPinia())
  .use(ElementPlus)
  .use(router)
  .mount('#app')
