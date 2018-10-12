import Vue from 'vue'
import vueResource from 'vue-resource'
import urlEncode from 'urlencode'
import CompilerSocket from './CompilerSocket'
import VideoHintSocket from './VideoHintSocket'

Vue.use(vueResource)

export default {

  compilerWebSocket: null,
  videoHintSocket: null,

  getUserToken ({tokenId}, cb) {
    Vue.http.post('/api/token', JSON.stringify({token_id: tokenId}))
      .then(res => res.body)
      .then(token => {
        // console.log('GET TOKEN ~ ', JSON.parse(JSON.stringify(token)))
        this.compilerWebSocket = new CompilerSocket(token.token_id)
        this.videoHintSocket = new VideoHintSocket(token.token_id)
        cb(token)
      })
      .catch(console.error)
  },

  requestSession (successCb, errCb) {
    Vue.http.get('/api/auth/requestSession')
      .then(res => res.body)
      .then(successCb)
      .catch(errCb)
  },

  resumeSession (successCb, errCb) {
    Vue.http.post('/api/auth/resumeSession', {})
      .then(res => res.body)
      .then(successCb)
      .catch(errCb)
  },

  authorize (provider, params, successCb, errCb) {
    Vue.http.get(`/api/auth/authorize${provider}${params}`)
      .then(res => res.body)
      .then(successCb)
      .catch(errCb)
  },

  signup (form, successCb, errCb) {
    Vue.http.post('/api/auth/signup', form)
      .then(res => res.body)
      .then(successCb)
      .catch(errCb)
  },

  login (form, successCb, errCb) {
    Vue.http.post('/api/auth/authorizeMathbot', form)
      .then(res => res.body)
      .then(successCb)
      .catch(errCb)
  },

  logout (cb) {
    Vue.http.get('/api/auth/logout')
      .then(res => res.body)
      .then(cb)
      .catch(console.error)
  },

  existsCheck (email, successCb, errCb) {
    Vue.http.post('/api/auth/existsCheck', {username: email})
      .then(res => res.body)
      .then(successCb)
      .catch(errCb)
  },

  testUrl (url, cb) {
    Vue.http.get(url)
      .then(() => {
        // eslint-disable-next-line
        cb(true)
      })
      .catch(() => {
        // eslint-disable-next-line
        cb(false)
      })
  },

  recoverPassword (email, successCb, errCb) {
    Vue.http.post('/api/auth/recoverPassword', {email: email})
      .then(res => res.body)
      .then(successCb)
      .catch(errCb)
  },

  updatePassword (params, updateForm, successCb, errCb) {
    Vue.http.post('/api/auth/updatePassword' + params, updateForm)
      .then(res => res.body)
      .then(successCb)
      .catch(errCb)
  },

  /*
  * activateFunction moves function token from stagedFunction list to activeFunction list
  * @param tokenId = JWT token_id
  * @param stagedIndex = previous index of func token
  * @param activeIndex = new index of func token
  * @response.body = lambdas property of JWT
  * */
  activateFunction ({tokenId, stagedIndex, activeIndex}, cb) {
    Vue.http.get(`/api/token/activateFunction/${urlEncode(tokenId)}/${stagedIndex}/${activeIndex}`)
      .then(res => cb(res.body))
      .catch(console.error)
  },

  deactivateFunction ({tokenId, activeIndex, stagedIndex}, cb) {
    Vue.http.get(`/api/token/deactivateFunction/${urlEncode(tokenId)}/${activeIndex}/${stagedIndex}`)
      .then(res => cb(res.body))
      .catch(console.error)
  },

  /*
  * @deprecated
  * putToken replaces JWT with updated JWT
  * !!This call is deprecated but may be useful at times. Updates entire JWT so is able to max server request!!
  * @param token = entire JWT
  * @response.body = lambdas property of JWT
  * */
  putToken (token, cb) {
    Vue.http.put('/api/token', token)
      .then(res => cb(res.body))
      .catch(console.error)
  },
  /*
  * _putFunc updates the current function in the database
  * @param tokenId = JWT token_id
  * @param funcToken = current token
  * @response.body = lambdas property of JWT
  * */
  putFunc ({tokenId, funcToken, override}, cb) {
    Vue.http.put('/api/token/editLambdas', {tokenId, funcToken, override})
      .then(res => {
        cb(res.body)
      })
      .catch(console.error)
  },

  changeFunctionColor ({tokenId, funcToken}, cb) {
    Vue.http.put('/api/token/color', {tokenId, funcToken})
      .then(res => {
        cb(res.body)
      })
      .catch(console.error)
  },

  updateActives ({tokenId, oldIndex, newIndex}, cb) {
    Vue.http.get(`/api/token/updateActives/${urlEncode(tokenId)}/${oldIndex}/${newIndex}`)
      .then(res => res.body)
      .then(cb)
      .catch(console.error)
  },
  /*
  * @deprecated
  * statsWin updates JWT stats when the user wins.
  * @param tokenId = JWT token_id
  * @response.body = stats property of JWT
  * */
  statsWin ({tokenId}, cb) {
    Vue.http.put('/api/stats/win/' + urlEncode(tokenId), {})
      .then(res => cb(res))
      .catch(console.error)
  },
  /*
  * switchLevel switches current level in the JWT
  * @param tokenId = JWT token_id
  * @param level = level to switch to
  * @param equation = level step to switch to
  * @response.body = state property of JWT
  * */
  switchLevel ({tokenId, level, step}, cb) {
    Vue.http.get('/api/stats/switch/' + urlEncode(tokenId) + '/' + level + '/' + step || '')
      .then(res => cb(res))
      .catch(console.error)
  },

  getStats ({tokenId}, cb) {
    Vue.http.get('/api/stats/' + tokenId)
      .then(res => res.body)
      .then(cb)
      .catch(console.error)
  },

  /*
  * getStep gets game logic for a step
  * @param level = level
  * @param currentEquation = step
  * @response.body = grid, problem, tool tokens
  * */
  getStep ({tokenId, level, step}, cb) {
    Vue.http.get('/api/levels/getStep/' + level + '/' + step, {params: {tokenId: tokenId}})
      .then(res => res.body)
      .then(stepData => cb(stepData))
      .catch(console.error)
  }
}
