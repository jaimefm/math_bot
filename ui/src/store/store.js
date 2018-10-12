import Vue from 'vue'
import Vuex from 'vuex'
import VueDefaultValue from 'vue-default-value/dist/vue-default-value'
import permanentImages from '../assets/assets'
import Message from '../services/Message'
import { AuthService } from '../services/AuthService'
import utils from '../services/utils'
// import api from '../services/api'
import VideoTimer from '../services/VideoTimer'
import RunCompiled from '../services/RunCompiled'
import VideoHint from '../services/VideoHint'

Vue.use(Vuex)
Vue.use(VueDefaultValue)

const orderEm = utils.orderEm

function addMessage (state, messageBuilder) {
  const message = new Message(state, messageBuilder)
  message.add()
}

const colors = {
  default: {
    hex: '#FFFFFF',
    next: 'grey'
  },
  grey: {
    hex: '#CCCCCC',
    next: 'blue'
  },
  blue: {
    hex: '#4A90E2',
    next: 'purple'
  },
  purple: {
    hex: '#CA7AFF',
    next: 'green'
  },
  green: {
    hex: '#50E3C2',
    next: 'pink'
  },
  pink: {
    hex: '#FF98B1',
    next: 'red'
  },
  red: {
    hex: '#F25C5C',
    next: 'default'
  }
}

export default new Vuex.Store({
  state: {
    permanentImages: permanentImages,
    currentFunction: 'main',
    programPanelShowing: false,
    editFunctionShowing: false,
    fullscreen: false,
    robotDeactivated: false,
    lock: {},
    showCongrats: false,
    tryAgainShowing: false,
    compiledDone: false,
    stepData: {},
    robot: {},
    profileView: 'Arithmetic',
    functionGroupsCalc: null,
    functionGroups: [],
    functionAreaShowing: 'editMain',
    editingIndex: null,
    trashVisible: false,
    colors: colors,
    activeFunctionGroups: null,
    swiperSlide: 0,
    messageList: [],
    auth: new AuthService(),
    showMesh: false,
    splashScreenShowing: false,
    hintShowing: {
      showing: false,
      videoURL: ''
    },
    videoTimers: {},
    editFunctionEvent: {},
    authErrors: [],
    runCompiled: {},
    videoHint: {}
  },
  mutations: {
    CLEAR_AUTH_ERRORS (state) {
      state.authErrors = []
    },
    PUSH_AUTH_ERRORS (state, msg) {
      state.authErrors.push(msg)
    },
    UPDATE_VIDEO_HINT (state, context) {
      state.videoHint = new VideoHint(context)
    },
    UPDATE_RUN_COMPILED (state, context) {
      state.runCompiled = new RunCompiled(context)
    },
    UPDATE_EDIT_FUNCTION_EVENT (state, evt) {
      state.editFunctionEvent = evt
    },
    UPDATE_ACTIVES (state, actives) {
      state.auth.userToken.lambdas.activeFuncs = actives
    },
    ADD_VIDEO_TIMER (state, remainingTime) {
      Vue.set(state.videoTimers, `${remainingTime.level}/${remainingTime.step}`, new VideoTimer({state, level: remainingTime.level, step: remainingTime.step, stars: remainingTime.stars, remainingTime: remainingTime.remainingTime}))
    },
    START_EXISTING_TIMERS (state, remainingTimes) {
      remainingTimes.forEach(rt => {
        Vue.set(state.videoTimers, `${rt.level}/${rt.step}`, new VideoTimer({state, level: rt.level, step: rt.step, stars: rt.stars, remainingTime: rt.remainingTime}))
      })
    },
    TOGGLE_HINT_SHOWING (state, {showing, videoURL}) {
      state.hintShowing = {showing, videoURL}
    },
    UPDATE_STEP_DATA (state, stepData) {
      function reverseTools (stepData) {
        stepData.gridMap = stepData.gridMap.map(row => {
          return row.map(cell => {
            cell.tools = cell.tools.reverse()
            return cell
          })
        })
        return stepData
      }
      state.auth.userToken.stats.level = stepData.level
      state.auth.userToken.stats.step = stepData.step
      state.stepData = Object.keys(stepData).length ? reverseTools(stepData) : stepData
    },
    UPDATE_SPLASH_SCREEN_SHOWING (state, bool) {
      state.splashScreenShowing = bool
    },
    TOGGLE_SHOW_MESH (state, bool) {
      state.showMesh = bool
    },
    SET_AUTH (state, auth) {
      state.auth = auth
    },
    UPDATE_TRASH_VISIBLE (state, bool) {
      state.trashVisible = bool
    },
    DEACTIVATE_ROBOT (state) {
      state.robotDeactivated = !state.robotDeactivated
    },
    TOGGLE_CONGRATS (state, bool) {
      state.showCongrats = bool
    },
    TOGGLE_TRY_AGAIN (state, bool) {
      state.tryAgainShowing = bool
    },
    UPDATE_ROBOT (state, robot) {
      state.robot = robot
    },
    UPDATE_LAMBDAS (state, lambdas) {
      state.auth.userToken.lambdas = lambdas
    },
    UPDATE_STATS (state, stats) {
      state.auth.userToken.stats = stats
    },
    CREATE_LOCK (state) {

    },
    PUSH_RANDOM_IMAGE (state, image) {
      state.auth.userToken.randomImages.push(image)
    },
    ADD_NEW_FUNCTION (state, func) {
      state.auth.userToken.oldVersion.funcs.push(func)
    },
    PUT_IMAGE_BACK (state, img, context) {
      state.randomImages.unshift(img)
    },
    CONTROL_PROGRAM_PANEL_SHOWING (state) {
      state.programPanelShowing = !state.programPanelShowing
    },
    CONTROL_EDIT_FUNCTION_SHOWING (state, bool) {
      state.editFunctionShowing = bool
    },
    ADD_CURRENT_USER (state, userData) {
      state.currentUser = userData
      state.auth.userToken.token_id = userData.token_id
      state.auth.userToken.u_id = userData._id
    },
    CHANGE_ROBOT_SPEED (state) {
      state.robot.adjustSpeed()
    },
    CHANGE_FULLSCREEN (state) {
      state.fullscreen = !state.fullscreen
    },
    UPDATE_PROFILE_VIEW (state, loc) {
      state.profileView = loc
    },
    UPDATE_COURSE (state, course) {
      state.course = course
    },
    UPDATE_COMPILED_DONE (state, bool) {
      state.compiledDone = bool
    },
    UPDATE_PAUSED (state, bool) {
      state.robot.paused = bool
    },
    UPDATE_FUNCTION_AREA_SHOWING (state, show) {
      state.functionAreaShowing = show
    },
    UPDATE_EDITING_INDEX (state, index) {
      state.editingIndex = index
    },
    UPDATE_SWIPER_SLIDE (state, slide) {
      state.swiperSlide = slide
    },
    ADD_MESSAGE: addMessage,
    REMOVE_MESSAGE (state, ind) {
      state.messageList[ind].delete()
    },
    DELETE_MESSAGES (state) {
      state.messageList.map(m => m.delete())
    }
  },
  actions: {
    clearAuthErrors ({commit}) {
      commit('CLEAR_AUTH_ERRORS')
    },
    pushAuthErrors ({commit}, msg) {
      commit('PUSH_AUTH_ERRORS', msg)
    },
    updateVideoHint ({commit}, context) {
      commit('UPDATE_VIDEO_HINT', context)
    },
    updateRunCompiled ({commit}, context) {
      commit('UPDATE_RUN_COMPILED', context)
    },
    updateEditFunctionEvent ({commit}, evt) {
      commit('UPDATE_EDIT_FUNCTION_EVENT', evt)
    },
    updateActives ({commit}, actives) {
      commit('UPDATE_ACTIVES', actives)
    },
    addVideoTimer ({commit}, context) {
      commit('ADD_VIDEO_TIMER', context)
    },
    startExistingTimers ({commit}, remainingTimes) {
      commit('START_EXISTING_TIMERS', remainingTimes)
    },
    toggleHintShowing ({commit}, {showing, videoURL}) {
      commit('TOGGLE_HINT_SHOWING', {showing, videoURL})
    },
    updateStepData ({commit}, stepData) {
      commit('UPDATE_STEP_DATA', stepData)
    },
    updateRobot ({commit}, robot) {
      commit('UPDATE_ROBOT', robot)
    },
    updateSplashScreenShowing ({commit}, bool) {
      commit('UPDATE_SPLASH_SCREEN_SHOWING', bool)
    },
    toggleShowMesh ({commit}, bool) {
      commit('TOGGLE_SHOW_MESH', bool)
    },
    setAuth ({commit}, auth) {
      commit('SET_AUTH', auth)
    },
    updateTrashVisible ({commit}, bool) {
      commit('UPDATE_TRASH_VISIBLE', bool)
    },
    updateFunctionGroups ({commit}) {
      commit('UPDATE_FUNCTION_GROUPS')
    },
    updateStats ({commit}, stats) {
      commit('UPDATE_STATS', stats)
    },
    deactivateRobot ({commit}) {
      commit('DEACTIVATE_ROBOT')
    },
    toggleCongrats ({commit}, bool) {
      commit('TOGGLE_CONGRATS', bool)
    },
    toggleTryAgain ({commit}, bool) {
      commit('TOGGLE_TRY_AGAIN', bool)
    },
    pushRandomImage ({commit}, image) {
      commit('PUSH_RANDOM_IMAGE', image)
    },
    addRandomImages ({commit}, images) {
      commit('ADD_RANDOM_IMAGES', images)
    },
    addFunctions ({commit}) {
      commit('ADD_FUNCTIONS')
    },
    addNewFunction ({commit}, func) {
      commit('ADD_NEW_FUNCTION', func)
    },
    putImageBack ({commit}, img) {
      commit('PUT_IMAGE_BACK', img)
    },
    controlProgramPanelShowing ({commit}) {
      commit('CONTROL_PROGRAM_PANEL_SHOWING')
    },
    controlEditFunctionShowing ({commit}, bool) {
      commit('CONTROL_EDIT_FUNCTION_SHOWING', bool)
    },
    addCurrentUser ({commit}, userData) {
      commit('ADD_CURRENT_USER', userData)
    },
    changeRobotSpeed ({commit}) {
      commit('CHANGE_ROBOT_SPEED')
    },
    changeFullscreen ({commit}) {
      commit('CHANGE_FULLSCREEN')
    },
    updateLambdas ({commit}, lambdas) {
      commit('UPDATE_LAMBDAS', lambdas)
    },
    createLock ({commit}) {
      commit('CREATE_LOCK')
    },
    updateProfileView ({commit}, loc) {
      commit('UPDATE_PROFILE_VIEW', loc)
    },
    updateCourse ({commit}, course) {
      commit('UPDATE_COURSE', course)
    },
    updateCompiledDone ({commit}, bool) {
      commit('UPDATE_COMPILED_DONE', bool)
    },
    updatePaused ({commit}, bool) {
      commit('UPDATE_PAUSED', bool)
    },
    updateFunctionAreaShowing ({commit}, show) {
      commit('UPDATE_FUNCTION_AREA_SHOWING', show)
    },
    updateEditingIndex ({commit}, index) {
      commit('UPDATE_EDITING_INDEX', index)
    },
    updateSwiperSlide ({commit}, slide) {
      commit('UPDATE_SWIPER_SLIDE', slide)
    },
    addMessage ({commit}, messageBuilder) {
      commit('ADD_MESSAGE', messageBuilder)
    },
    removeMessage ({commit}, ind) {
      commit('REMOVE_MESSAGE', ind)
    },
    deleteMessages ({commit}) {
      commit('DELETE_MESSAGES')
    }
  },
  getters: {
    getAuthErrors: state => state.authErrors,
    getVideoHint: state => state.videoHint,
    getRunCompiled: state => state.runCompiled,
    getEditFunctionEvent: state => state.editFunctionEvent,
    getVideoTimers: state => state.videoTimers,
    getHintShowing: state => state.hintShowing,
    getCurrentUser: state => state.auth.userToken,
    getStepData: state => state.stepData,
    getSplashScreenShowing: state => state.splashScreenShowing,
    getPointerPosition: state => state.pointerPosition,
    getShowMesh: state => state.showMesh,
    getAuth: state => state.auth,
    getTokenId: state => state.auth.userToken.token_id,
    getTrashVisible: state => state.trashVisible,
    // getFunctionGroups: state => state.functionGroups,
    getGrid: state => state.stepData.gridMap,
    getRobotDeactivated: state => state.robot.robotDeactivated,
    getCurrentEquation: state => state.auth.userToken.stats.currentEquation,
    getCongratsShowing: state => state.showCongrats,
    getTryAgainShowing: state => state.tryAgainShowing,
    getGame: state => state.game,
    getRobot: state => state.robot,
    getLevel: state => state.auth.userToken.stats.level,
    getRobotCarrying: state => state.robot.robotCarrying,
    getPermanentImages: state => state.permanentImages,
    getRandomImages: state => state.auth.userToken.randomImages,
    getCommands: state => state.auth.userToken.lambdas.cmds, // <-- Array
    getStagedFunctions: state => state.auth.userToken.lambdas.stagedFuncs, // <-- Array
    getActiveFunctions: state => state.auth.userToken.lambdas.activeFuncs, // <-- Array
    getMainFunction: state => state.auth.userToken.lambdas.main, // <-- Object
    getLambdas: state => state.auth.userToken.lambdas,
    getCurrentFunction: state => state.currentFunction,
    getProgramPanelShowing: state => state.programPanelShowing,
    getEditFunctionShowing: state => state.editFunctionShowing,
    getRememberUser: state => state.rememberUser,
    getToken: state => state.auth.userToken,
    getShowDupAlert: state => state.showDupAlert,
    getFullscreen: state => state.fullscreen,
    getSignupLoading: state => state.signupLoading,
    getLock: state => state.lock,
    getSocket: state => state.socket,
    getLevels: state => utils.assembleLevels(state.auth.userToken.stats.levels),
    getStats: state => state.auth.userToken.stats,
    getStep: state => state.auth.userToken.stats.step,
    getSteps: state => orderEm(state.auth.userToken.stats.levels[state.auth.userToken.stats.level]),
    getStars: state => state.stars,
    getProfileView: state => state.profileView,
    getCourses: state => state.courses,
    getCourse: state => state.course,
    getArithmeticLevels: state => state.arithmeticLevels,
    getCompiledDone: state => state.compiledDone,
    getPaused: state => state.robot.paused,
    getFunctionAreaShowing: state => state.functionAreaShowing,
    getEditingIndex: state => state.editingIndex,
    getColors: state => state.colors,
    getActiveFunctionGroups: state => state.activeFunctionGroups,
    getSwiperSlide: state => state.swiperSlide,
    getMessageList: state => state.messageList
  }
})
