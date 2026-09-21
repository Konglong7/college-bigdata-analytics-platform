import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '@/layout/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据驾驶舱' }
      },
      {
        path: 'university',
        name: 'UniversityQuery',
        component: () => import('@/views/university/index.vue'),
        meta: { title: '高校查询' }
      },
      {
        path: 'university/:id',
        name: 'UniversityDetail',
        component: () => import('@/views/university/detail.vue'),
        meta: { title: '高校详情' }
      },
      {
        path: 'recommend',
        name: 'Recommend',
        component: () => import('@/views/recommend/index.vue'),
        meta: { title: '志愿推荐' }
      },
      {
        path: 'compare',
        name: 'Compare',
        component: () => import('@/views/compare/index.vue'),
        meta: { title: '高校对比' }
      },
      {
        path: 'major',
        name: 'Major',
        component: () => import('@/views/major/index.vue'),
        meta: { title: '专业分析' }
      },
      {
        path: 'enrollment',
        name: 'Enrollment',
        component: () => import('@/views/enrollment/index.vue'),
        meta: { title: '招生分析' }
      },
      {
        path: 'collect',
        name: 'Collect',
        component: () => import('@/views/collect/index.vue'),
        meta: { title: '数据采集' }
      },
      {
        path: 'clean',
        name: 'Clean',
        component: () => import('@/views/clean/index.vue'),
        meta: { title: '数据清洗' }
      },
      {
        path: 'predict',
        name: 'Predict',
        component: () => import('@/views/predict/index.vue'),
        meta: { title: '趋势预测' }
      },
      {
        path: 'warehouse',
        name: 'Warehouse',
        component: () => import('@/views/warehouse/index.vue'),
        meta: { title: '数据仓库' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/index.vue'),
        meta: { title: '系统后台', requireAdmin: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '系统登录' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.title) {
    document.title = `${to.meta.title} - 全国高校大数据分析可视化平台`
  }

  // 若已登录访问登录页，直接跳转至数据驾驶舱
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }

  // 访问后台管理鉴权
  if (to.meta.requireAdmin) {
    if (!token) {
      ElMessage.warning('访问后台管理需先登录系统')
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    if (role !== 'ROLE_ADMIN') {
      ElMessage.warning('当前账号为普通用户，无权访问系统后台')
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
