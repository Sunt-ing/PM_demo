import Vue from 'vue'
import Router from 'vue-router'
import mainpage from '@/App'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Test',
      component: mainpage
    }
  ]
})
