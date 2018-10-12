import Vue from 'vue'
import Router from 'vue-router'
import VueResource from 'vue-resource'
import Sortable from 'vue-sortable'
import Robot from '@/components/Robot'
import Profile from '@/components/Profile'
import Marketing from '@/components/marketing/Marketing'
import Callback from '@/components/Callback'
import Auth from '@/components/Auth'
import BootstrapVue from 'bootstrap-vue'
import VueYouTubeEmbed from 'vue-youtube-embed'
import VueForm from 'vue-form'
import VueSocialSharing from 'vue-social-sharing'
import VueShortKey from 'vue-shortkey'

import $store from '../store/store'

// require styles
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.use(VueShortKey)
Vue.use(VueSocialSharing)
Vue.use(VueYouTubeEmbed)
Vue.use(BootstrapVue)
Vue.use(Router)
Vue.use(VueResource)
Vue.use(Sortable)
Vue.use(VueForm, {
  inputClasses: {
    valid: 'form-control-success',
    invalid: 'form-control-danger'
  },
  validators: {
    matches (value, attrValue) {
      if (!attrValue) {
        return true
      }
      return value === attrValue
    },
    'password-strength' (value) {
      return value.length > 4
    }
  }
})

const router = new Router({
  mode: 'history',
  routes: [
    {
      path: '/auth',
      name: 'Auth',
      component: Auth
    },
    {
      path: '/robot',
      name: 'Robot',
      secure: true,
      component: Robot
    },
    {
      path: '/signup',
      name: 'Signup',
      component: Robot
    },
    {
      path: '/profile',
      name: 'Profile',
      secure: true,
      component: Profile
    },
    {
      path: '/about',
      name: 'Marketing',
      component: Marketing
    },
    {
      path: '/callback',
      name: 'Callback',
      component: Callback
    },
    {
      path: '*',
      redirect: '/about'
    }
  ]
})

router.beforeEach((to, from, next) => {
  // Look at all routes
  router.options.routes.forEach((route) => {
    // If this is the current route and it's secure
    if (to.matched[0].path === route.path && route.secure && !$store.state.auth.authenticated) {
      return next('/auth')
    }
  })
  // Proceed as normal
  return next()
})

export default router
