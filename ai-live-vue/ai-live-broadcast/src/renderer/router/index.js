import Router from 'vue-router'
import Layout from '@/layout'
// 引入路由表
import asyncRouterMap from './constantRouterMap'

export const constantRouterMap = [{
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    name: '首页',
    hidden: true,
    children: [{
        path: 'dashboard',
        name: '首页',
        component: () => import('@/components/LandingPage')
    }]
}, {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
}, {
    path: '/grabPublicScreen',
    component: () => import('@/views/live-pane/components/grabPublicScreen'),
    hidden: true
}, {
    path: '*',
    component: () => import('@/views/404'),
    hidden: true
}]
export const asyncRoutes = asyncRouterMap

const createRouter = () => new Router({
    scrollBehavior: () => ({y: 0}),
    routes: constantRouterMap
})

export function resetRouter() {
    const newRouter = createRouter()
    router.matcher = newRouter.matcher
}

const router = createRouter()

export default router
