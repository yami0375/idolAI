import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
// 导入全局样式文件
import './assets/styles/global.scss';

// 引入 Vant 组件库
import 'vant/lib/index.css'
import { Toast, Dialog, Notify, ImagePreview } from 'vant'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Toast)
app.use(Dialog)
app.use(Notify)
app.use(ImagePreview)

// 将Toast注册为全局属性
app.config.globalProperties.$toast = Toast

app.mount('#app')