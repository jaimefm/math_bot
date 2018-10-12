<template>
  <div class="col-4 steps">
    <div class="row">
      <h2 class="header"><span v-if="level !== 'Sandbox'">{{`${planetName}`}}.</span> {{ parseCamelCase(level) }}</h2>
    </div>
    <div class="row steps-container">
      <div class="btn-group-vertical">
        <button
          v-for="(step, value) in steps"
          type="button"
          class="btn btn-dark btn-lg btn-block"
          :id="actionClass(step.name)"
          @click="step.active ? goToRobot(level, step.name) : ''"
          :key="step + ':' + value"
          :disabled="!step.active"
        >
          <div class="col-6">
            <div class="step-info-text">Level {{ value + 1 }}</div>
          </div>
          <div class="col-6">
            <stars :level="level" :step="step.name" :step-stats="step" :star-group="'star-cluster'"></stars>
          </div>
        </button>
        <button
          v-if="steps[steps.length - 1].wins > 0 && nextLevel !== 'None'"
          type="button"
          class="btn btn-dark btn-lg btn-block"
          @click="goToRobot(nextLevel.name, nextLevel.firstStep)">
          <div class="col-6">
            <div class="step-info-text"><div>Next planet!</div> {{parseCamelCase(nextLevel.name)}}</div>
          </div>
          <div class="col-6">
            <img
              class="step-next-planet"
              :class="`step-${nextLevel.planetClass}`"
              :src="permanentImages.planets[nextLevel.planet]"
              alt="Image of a planet"
            />
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import utils from '../services/utils'
import api from '../services/api'
import Stars from './Stars'

export default {
  name: 'steps',
  computed: {
    planetName () {
      const planets = {
        BasicProgramming: '1',
        Counting: '2',
        Numbers: '3',
        Recursion: '4',
        Conditionals: '5'
      }
      return planets[this.level]
    },
    step () {
      return this.$store.getters.getStep
    },
    level () {
      return this.$store.getters.getLevel
    },
    levels () {
      return this.$store.getters.getLevels
    },
    steps () {
      return this.$store.getters.getSteps
    },
    nextLevel () {
      const name = this.steps[this.steps.length - 1].nextLevel
      if (name === 'None') {
        return 'None'
      } else {
        let planetNumber = 0
        const nextLevelData = {}
        nextLevelData.name = name
        nextLevelData.firstStep = this.findFirstStep(this.levels.find((level, index) => {
          planetNumber = index + 1
          return level.name === name
        }))
        nextLevelData.planet = `planet${planetNumber}`
        nextLevelData.planetClass = `planet-${planetNumber}`
        return nextLevelData
      }
    },
    tokenId () {
      return this.$store.getters.getTokenId
    }
  },
  methods: {
    parseCamelCase: utils.parseCamelCase,
    findFirstStep (level) {
      const levelSteps = utils.orderEm(level.level)[0].name
      return levelSteps
    },
    goToRobot (level, step) {
      api.switchLevel({tokenId: this.tokenId, level: level, step: step}, (res) => {
        this.$store.dispatch('updateStats', res.body)
        this.$router.push({path: '/robot'})
      })
    },
    actionClass (step) {
      if (this.step === step) {
        const id = 'selected-step' + '-planet-' + this.planetName + '-selected'
        setTimeout(() => {
          const $stepsContainer = $('.steps-container')
          const stepsHeight = $stepsContainer.innerHeight()
          $stepsContainer.animate({
            scrollTop: $(`#${id}`).offset().top - stepsHeight / 2
          })
        }, 50)
        return id
      }
    }
  },
  components: {
    Stars
  },
  props: ['permanentImages']
}
</script>

<style scoped lang="scss">
  $steps-font-size: 2.3vmin; // using vh for this font media queries all sizes
  $gradient-size: 100px;
  $outer-shadow-blur: 50px;
  $outer-shadow-size: 1px;
  $planet-1-color: rgba(202, 122, 255, 1);
  $planet-2-color: rgba(242, 92, 92, 1);
  $planet-3-color: rgba(74, 144, 226, 1);
  $planet-4-color: rgba(255, 152, 177, 1);
  $planet-5-color: rgba(80, 227, 194, 1);
  $planet-6-color: rgba(184, 233, 134, 1);
  $gradient-size: 100px;
  $font-color: #ffffff;

  .steps {
    color: $font-color;
    margin: 8% 0 0 0;
    font-size: $steps-font-size;
    height: 100%;

    .header {
      font-size: 1.5em;
      padding: 2% 0;
    }

    .btn {
      color: $font-color;
      display: flex;
      margin: 0.15em 0 0.15em 0;
      border-radius: 0.25rem!important;
      background-color: #000000;
      font-size: 1.1em;
      padding: 8% 0;
    }

    .steps-container {
      overflow-y: auto;
      -webkit-overflow-scrolling: touch;
      height: 90%;
      .btn-group-vertical {
        min-height: min-content;
        width: 100%;

        #selected-step {
          border: 2px solid pink;
        }
      }

      .step-info-text {
        width: 100%;
        word-wrap: break-word;
      }
    }

    .step-next-planet {
      height: 2.5em;
      border-radius: 50%;
    }
  }

  .step-planet-1 {
    background: radial-gradient(circle at $gradient-size $gradient-size, $planet-1-color, #000);
    box-shadow: 0 0 $outer-shadow-blur $outer-shadow-size $planet-1-color;
  }

  .step-planet-2 {
    background: radial-gradient(circle at $gradient-size $gradient-size, $planet-2-color, #000);
    box-shadow: 0 0 $outer-shadow-blur $outer-shadow-size $planet-2-color;
  }

  .step-planet-3 {
    background: radial-gradient(circle at $gradient-size $gradient-size, $planet-3-color, #000);
    box-shadow: 0 0 $outer-shadow-blur $outer-shadow-size $planet-3-color;
  }

  .step-planet-4 {
    background: radial-gradient(circle at $gradient-size $gradient-size, $planet-4-color, #000);
    box-shadow: 0 0 $outer-shadow-blur $outer-shadow-size $planet-4-color;
  }

  .step-planet-5 {
    background: radial-gradient(circle at $gradient-size $gradient-size, $planet-5-color, #000);
    box-shadow: 0 0 $outer-shadow-blur $outer-shadow-size $planet-5-color;
  }

  #selected-step-planet-1-selected {
    border: 2px solid $planet-1-color;
  }

  #selected-step-planet-2-selected {
    border: 2px solid $planet-2-color;
  }

  #selected-step-planet-3-selected {
    border: 2px solid $planet-3-color;
  }

  #selected-step-planet-4-selected {
    border: 2px solid $planet-4-color;
  }

  #selected-step-planet-5-selected {
    border: 2px solid $planet-5-color;
  }

  #selected-step-planet-6-selected {
    border: 2px solid $planet-6-color;
  }

  .step-image-stars {
    height: 100%;
  }

  .step-image-lock {
    height: 51px;
    width: 42px;
  }

  .step-active {
    opacity: 1;
  }

  .step-disables {
    opacity: 0.8;
  }
</style>
