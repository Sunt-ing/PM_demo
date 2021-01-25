// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.

import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'
Vue.use(ElementUI)

Vue.prototype.$axios = axios;
axios.defaults.baseURL = 'http://10.20.123.55:23332/'
// axios.defaults.timeout = 5000

import Vue from 'vue'
import App from './App'
import router from './router'
import {
  Button,
  Select,
  Row,
  Col,
  Pagination,
  Table,
  TableColumn,
  Form,
  FormItem,
  Input,
  Dialog,
  Option,
  Card
} from 'element-ui'

Vue.config.productionTip = false
Vue.use(Button);
Vue.use(Select);
Vue.use(Row);
Vue.use(Col);
Vue.use(Pagination);
Vue.use(Table);
Vue.use(TableColumn);
Vue.use(Form);
Vue.use(FormItem);
Vue.use(Input);
Vue.use(Dialog);
Vue.use(Option);
Vue.use(Card);


/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})


