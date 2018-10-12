import api from './api'
import Robot from './RobotState'
import GridAnimator from './GridAnimator'
import _ from 'underscore'

class RunCompiled extends GridAnimator {
  constructor (context) {
    super()
    if (context) {
      this.context = context
      this.robotFrames = []
      this.$store = this.context.$store
      this.$router = this.context.$router
      this.tokenId = this.$store.getters.getTokenId
      this.stats = this.$store.getters.getStats
      this.robot = this.$store.getters.getRobot
      this.stepData = this.$store.getters.getStepData
      this.toolList = this.stepData.toolList
      this.programCreate = true
      this.videoHint = this.$store.getters.getVideoHint

      this._askCompiler = this._askCompiler.bind(this)
      this._processFrames = this._processFrames.bind(this)
      this._initializeStep = this._initializeStep.bind(this)
      this.start = this.start.bind(this)
      this.pause = this.pause.bind(this)
      this.stop = this.stop.bind(this)
      this.reset = this.reset.bind(this)
      this._resetStep = this._resetStep.bind(this)
      this._waitForFrames = this._waitForFrames.bind(this)
      this.initializeNextStep = this.initializeNextStep.bind(this)
      this.resetIfFailure = this.resetIfFailure.bind(this)
    }
  }

  lastFrame = null

  _testForEmptyFunctions () {
    const mainFunction = this.$store.getters.getMainFunction.func
    const activeFuncs = this.$store.getters.getActiveFunctions

    if (!mainFunction.length) return [{name: 'Main'}]

    return _.chain(mainFunction)
      .map(f => {
        const funcToken = activeFuncs.find(af => af.created_id === f.created_id)
        if (funcToken) return funcToken
        else return null
      })
      .filter(func => func !== null)
      .filter(func => !func.func.length)
      .value()
  }

  resetIfFailure () {
    if (this.robot.state === 'failure') {
      this.reset()
    }
  }

  start () {
    // console.log('start ~ ', this.robotFrames.slice())
    const emptyFuncs = this._testForEmptyFunctions()

    if (emptyFuncs.length) {
      this._mainEmptyMessage(emptyFuncs)
    } else if (this.robot.state !== 'paused') {
      this.robotFrames = []
      this.robot.setState('running')
      this._askCompiler(this._processFrames)
    } else {
      this.robot.setState('running')
      this._processFrames()
    }
  }

  pause () {
    this._pausedMessage()
    this.robot.setState('paused')
  }

  stop () {
    this._stopMessage()
    this.robot.setState('stopped')
  }

  reset () {
    this.robot.state = 'home'
    this._stopRobot()
  }

  initializeNextStep (stepData) {
    if (stepData === undefined) {
      stepData = this.lastFrame.stepData
      this._updateStats(this.lastFrame.stats)
      this.$router.push({path: '/robot'})
    }
    this._initializeStep(stepData)
    this._hideCongrats()
    this.lastFrame = null
  }

  quit () {
    this.initializeNextStep()
    this.$router.push({path: '/profile'})
  }

  stayOnLevel () {
    this._stopRobot()
    this._hideCongrats()
  }

  _hideCongrats () {
    this._hideLevelCongrats()
    this._hideStepCongrats()
  }

  _showStepCongrats = () => this.context.$root.$emit('bv::show::modal', 'step-congrats-modal')

  _hideStepCongrats = () => this.context.$root.$emit('bv::hide::modal', 'step-congrats-modal')

  _showLevelCongrats = () => this.context.$root.$emit('bv::show::modal', 'level-congrats-modal')

  _hideLevelCongrats = () => this.context.$root.$emit('bv::hide::modal', 'level-congrats-modal')

  _showFreeHint (url) {
    this.videoHint.showFreeHint(url)
  }

  _initializeStep (stepData) {
    if (stepData.freeHint) this._showFreeHint(stepData.freeHint)
    this.$store.dispatch('updateStepData', stepData)
    this.$store.dispatch('updateLambdas', stepData.lambdas)
    stepData.initialRobotState.context = this.context
    const robot = new Robot(Object.assign(stepData.initialRobotState, {robotSpeed: this.robot.robotSpeed}))
    this.$store.dispatch('updateRobot', robot)
    this.constructor(this.context)
  }

  _stopMessage () {
    const messageBuilder = {
      type: 'warn',
      msg: 'Program stopped'
    }

    this._addMessage(messageBuilder)
  }

  _pausedMessage () {
    const messageBuilder = {
      type: 'success',
      msg: 'Program paused'
    }

    this._addMessage(messageBuilder)
  }

  _closeMessageRobotHome () {
    return () => {
      return this.robot.state === 'home'
    }
  }

  _failedMessage () {
    const dis = this
    const failedMessage = {
      type: 'success',
      msg: 'Not quite, a hint might help',
      handlers () {
        const $helpButton = $('.help-button')

        return {
          runBeforeAppend () {
            $helpButton.addClass('background-alert')
          },
          runOnDelete () {
            $helpButton.removeClass('background-alert')
          },
          closeControl: dis._closeMessageRobotHome()
        }
      }
    }
    this._addMessage(failedMessage)
  }

  _mainEmptyMessage (emptyFuncs) {
    const emptyCount = emptyFuncs.length

    const messageBuilder = {
      type: 'warn',
      msg: emptyFuncs.find(f => f.name === 'Main') ? 'Main cannot be empty' : `${emptyFuncs.length} of your functions ${emptyCount > 1 ? 'are' : 'is'} empty`,
      handlers () {
        const $bar = $('.bar')

        return {
          runBeforeAppend () {
            $bar.addClass('red-bar')
          },
          runOnDelete () {
            $bar.removeClass('red-bar')
          }
        }
      }
    }

    this._addMessage(messageBuilder)
  }

  _addMessage (messageBuilder) {
    this.$store.dispatch('addMessage', messageBuilder)
  }

  _updateStats (stats) {
    this.$store.dispatch('updateStats', stats)
  }

  _resetStep () {
    api.getStep({tokenId: this.tokenId, level: this.stats.level, step: this.stats.step}, stepData => {
      this._initializeStep(stepData)
    })
  }

  _stopRobot () {
    api.compilerWebSocket.haltProgram(() => {})
    this._resetStep()
  }

  _toggleBridge = (which, bool) => this.$store.dispatch(`toggle${which}`, bool)

  _success (frame) {
    return this.initializeAnimation(this.$store, frame, async () => {
      // console.log('[last frame grid]', JSON.parse(JSON.stringify(frame.robotState.grid)))
      // console.log('[grid]', JSON.parse(JSON.stringify(this.grid)))
      const isLastStep = this.stats.levels[this.stats.level][this.stats.step].nextStep === 'None'
      if (isLastStep) {
        this.$router.push({path: '/profile', query: {showCongrats: 'true'}})
        // this._showLevelCongrats()
      } else {
        this._showStepCongrats()
      }
      this.lastFrame = frame
    })
  }

  _failure (frame) {
    // console.log(JSON.parse(JSON.stringify(frame)))
    return this.initializeAnimation(this.$store, frame, async () => {
      this._updateStats(frame.stats)
      this.robot.setState('failure')
      this._failedMessage()
    })
  }

  /*
  * In case program state is still running but server has not responded yet
  * */
  _waitForFrames (waitTime) {
    if (this.robotFrames.length) return this._processFrames()
    else if (waitTime === 0) return this._stopRobot()
    setTimeout(() => this._waitForFrames(waitTime - 1), 50)
  }

  _running (frame) {
    return this.initializeAnimation(this.$store, frame, () => {
      if (this.robot.state === 'running') {
        if (this.robotFrames.length) {
          this._processFrames()
        } else {
          this._waitForFrames(50)
        }
      } else if (this.robot.state === 'stopped') {
        this._stopRobot()
      }
    })
  }

  async _processFrames (_) {
    // console.log('frames ~ ', this.robotFrames.slice())
    const current = this.robotFrames.shift()
    this._controlAsk()
    const run = await this[`_${current.programState}`](current)
    run(current)
  }

  _controlAsk () {
    if (this.robotFrames.length > 0 && this.robotFrames.length < 20) {
      const last = this.robotFrames[this.robotFrames.length - 1]
      if (this.robotFrames.length < 20 && last.programState === 'running') {
        this._askCompiler()
      }
    }
  }

  _askCompiler (startRunning) {
    api.compilerWebSocket.compileWs({problem: this.stepData.problem.encryptedProblem}, (compiled) => {
      this.robotFrames = this.robotFrames.concat(compiled.frames)
      if (startRunning) startRunning()
    })
  }
}

export default RunCompiled
