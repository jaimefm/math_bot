import api from './api'
import $router from './../router'
import $store from '../store/store'

export class AuthService {
  authenticated = false
  userToken = {}
  userProfile = {}
  requestSession = {}
  cookiesEnabled = !navigator.cookieEnabled

  constructor () {
    this._setProfile = this._setProfile.bind(this)
    this.logout = this.logout.bind(this)
    this._handleErr = this._handleErr.bind(this)
    this._resumeSession = this._resumeSession.bind(this)
    this._requestSession = this._requestSession.bind(this)
    this._resumeSession()
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

  _setProfile (profile) {
    this.userProfile = profile
    this._getUserToken()
  }

  _resumeSession () {
    api.resumeSession((profileOrRequestSession) => {
      if (profileOrRequestSession.action && profileOrRequestSession.action === 'needsAuthorization') {
        this.requestSession = profileOrRequestSession
      } else {
        this.userProfile = profileOrRequestSession
        this.authenticated = true
        this._getUserToken()
      }
    }, this._requestSession)
  }

  _requestSession (cb) {
    api.requestSession((requestSession) => {
      this.requestSession = requestSession
      if (cb) cb()
    }, this._handleErr)
  }

  _handleErr (err) {
    $store.dispatch('pushAuthErrors', err.body)
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
      this._setProfile(profile)
    }, this._handleErr)
  }

  logout () {
    api.logout(() => {
      this.clearErrors()
      this.authenticated = false
      $router.push({path: '/about'})
    })
  }

  signup (form) {
    const prep = {}
    prep.username = form.email
    prep.password = form.password
    prep.picture = form.picture
    prep.name = form.email
    this.clearErrors()
    api.signup(prep, (profile) => {
      this.userProfile = profile
      this._setProfile(profile)
    }, this._handleErr)
  }

  authorizeMathbot (form) {
    const prep = {}
    prep.username = form.email
    prep.password = form.password
    this.clearErrors()
    api.login(prep, (profile) => {
      this.userProfile = profile
      this._setProfile(profile)
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
      this._setProfile(profile)
    }, this._handleErr)
  }
}
