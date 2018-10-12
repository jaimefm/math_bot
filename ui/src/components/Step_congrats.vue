<template>
  <b-modal
    id="step-congrats-modal"
    ref="step-congrats-modal"
    @hidden="closeCongrats"
  >
    <div slot="modal-header">
      <img class="dialog-button close-congrats" @click="closeCongrats" :src="permanentImages.buttons.xButton" data-toggle="tooltip" title="Close">
    </div>
    <div class="congrats-icon">
      <img :src="permanentImages.instructionsRobot">
    </div>
    <stars :level="level" :step="step" :step-stats="stepStats" :star-group="'congrats-spread'"></stars>
    <div class="text-minor">You won!</div>
    <div class="text-minor">
      <div>Tell your friends!</div>
    </div>
    <social-sharing :size="'3rem'"></social-sharing>
    <div slot="modal-footer" class="row" style="width: 100%; display: flex; justify-content: space-between;">
      <b-btn
        size="md"
        style="width: 40%;"
        class="float-right"
        variant="primary"
        @click="quit"
      >
        Quit
      </b-btn>
      <b-btn
        size="md"
        style="width: 40%"
        class="float-right"
        variant="primary"
        @click="next"
      >
        Next level
      </b-btn>
    </div>
  </b-modal>
</template>

<script>
import Stars from './Stars'
import SocialSharing from './Social_sharing'
import utils from '../services/utils'
export default {
  computed: {
    runCompiled () {
      return this.$store.getters.getRunCompiled
    },
    level () {
      return this.$store.getters.getLevel
    },
    step () {
      return this.$store.getters.getStep
    },
    steps () {
      return this.$store.getters.getSteps
    },
    stepStats () {
      const stepName = this.step
      return this.steps.find(s => s.name === stepName)
    },
    permanentImages () {
      return this.$store.getters.getPermanentImages
    },
    socialMessage () {
      return `
        I beat planet ${this.level} step ${utils.findStepInd(this.steps, this.step)} on mathbot.com!
      `
    }
  },
  data () {
    return {
      show: true
    }
  },
  methods: {
    quit () {
      this.runCompiled.quit()
    },
    next () {
      this.runCompiled.initializeNextStep()
    },
    closeCongrats () {
      this.runCompiled.stayOnLevel()
    }
  },
  components: {
    Stars,
    SocialSharing
  }
}
</script>

<style lang="scss">
  $click-color: #B8E986;
  $background-color: rgba(0, 0, 0, 1);
  $grid-border-radius: 4px;
  $icon-size: 50%;
  $text-minor-font-size: 4vmin;
  $grid-space-size: 9vmin;
  $font-size: 2.5rem;
  $star-size: 3rem;
  $dialog-button-size: 2rem;
  $share-btn-size: 2.5rem;

  #step-congrats-modal {
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #ffffff;

    .text-minor {
      font-size: $font-size;
      *:first-child {
        font-size: calc(#{$font-size} / 2);
      }
    }

    .modal-dialog .modal-content {
      background-color: transparent;
      .modal-body {
        background-color: $background-color;
        .stars .star-spread .star {
          height: $star-size;
          width: $star-size;
          svg image {
            display: none;
          }
        }

        .congrats-icon {
          background-color: white;
          display: inline-block;
          border-radius: 50%;
          padding: 2rem;
        }

        .social-sharing {
          .social-links {
            .share-button {
              height: $share-btn-size;
              width: $share-btn-size;

              i {
                font-size: calc(#{$share-btn-size} - 1rem);
              }
            }
          }
        }
      }

      .modal-header {
        background-color: $background-color;
        border: none;
        position: relative;
        .close-congrats {
          position: absolute;
          height: $dialog-button-size;
          width: $dialog-button-size;
          top: 0;
          right: 0;
        }
      }

      .modal-footer {
        background-color: $background-color;
        border-top: 1px solid $click-color;
        .row {
          margin: 0;
          .btn {
            background-color: $click-color;
            color: #000000;
            border: none;
          }
        }
      }
    }
  }
</style>
