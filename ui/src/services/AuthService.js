import api from './api'
import $router from './../router'
import $store from '../store/store'

export class AuthService {
  authenticated = false
  userToken = {}
  userProfile = {}
  session = {}

  constructor () {
    this.login = this.login.bind(this)
    this._setSession = this._setSession.bind(this)
    this.logout = this.logout.bind(this)
    this._requestSession()
  }

  _getUserProfile () {
    return JSON.parse(localStorage.getItem('profile'))
  }

  _storeLastRoute () {
    localStorage.setItem('last_location', $router.history.current.fullPath)
  }

  _getUserToken () {
    const tokenId = this.userProfile.sub || this.userProfile.user_id
    api.getUserToken({tokenId: tokenId}, token => {
      this.userToken = token
      this.authenticated = true
      window.onbeforeunload = this._storeLastRoute
      const lastPath = localStorage.getItem('last_location')
      this._getVideoHints()
      if (lastPath && lastPath !== '/about' && lastPath !== '/auth') {
        $router.push({path: lastPath})
      } else {
        $router.push({path: '/profile'})
      }
    }, this._handleErr)
  }

  _getVideoHints () {
    api.videoHintSocket.requestHintsTaken((hints) => {
      $store.dispatch('startExistingTimers', hints.remainingTimes)
    }, this._handleErr)
  }

  _setSession (profile) {
    this.userProfile = profile
    localStorage.setItem('profile', JSON.stringify(profile))
    this._getUserToken()
  }

  _requestSession () {
    api.requestSession((session) => {
      this.session = session
    }, this._handleErr)
  }

  _handleErr (err) {
    $store.dispatch('pushAuthErrors', err.body)
  }

  _testLegacy (sub) {
    return sub.includes('auth0|')
  }

  clearErrors () {
    $store.dispatch('clearAuthErrors')
  }

  handleAuthentication () {
    const params = window.location.search
    const provider = localStorage.getItem('authProvider')
      .split('')
      .map((v, i) => {
        if (i === 0) return v.toUpperCase()
        else return v
      })
      .join('')
    api.authorize(provider, params, (profile) => {
      localStorage.removeItem('authProvider')
      this._setSession(profile)
    }, this._handleErr)
  }

  login () {
    const profile = this._getUserProfile()
    if (profile && !this._testLegacy(profile.sub)) {
      this.userProfile = this._getUserProfile()
      this._getUserToken()
    } else {
      localStorage.clear()
      $router.push({path: '/auth'})
    }
  }

  logout () {
    localStorage.clear()
    this.clearErrors()
    this.authenticated = false
    $router.push({path: '/about'})
  }

  signup (form) {
    const prep = {}
    prep.username = form.email
    prep.password = form.password
    prep.picture = form.picture
    prep.name = form.name
    this.clearErrors()
    api.signup(prep, (profile) => {
      this.userProfile = profile
      this._setSession(profile)
    }, this._handleErr)
  }

  authorizeMathbot (form) {
    const prep = {}
    prep.username = form.email
    prep.password = form.password
    this.clearErrors()
    api.login(prep, (profile) => {
      this.userProfile = profile
      this._setSession(profile)
    }, this._handleErr)
  }

  recoverPassword (email) {
    this.clearErrors()
    api.recoverPassword(email, (res) => {
      this._handleErr({body: res})
    }, this._handleErr)
  }

  updatePassword (updateForm, updateParams) {
    this.clearErrors()
    api.updatePassword(updateParams, updateForm, (profile) => {
      this.userProfile = profile
      this._setSession(profile)
    }, this._handleErr)
  }
}
