<template>
  <b-modal
    id="video-modal"
    size="lg"
    ref="video-modal"
    :hide-header="true"
    :hide-footer="true"
    :lazy="true"
  >
    <img
      class="dialog-button close-congrats"
      @click="videoHint.hideVideo()"
      :src="permanentImages.buttons.xButton"
      data-toggle="tooltip"
      title="Close"
    >
    <youtube
      class="player"
      :video-id="videoHint.currentVideo"
      :player-vars="{ autoplay: 1, rel: 0 }"
      :player-height="'100%'"
      :player-width="'100%'"
      @ended="videoHint.hideVideo()"
    ></youtube>
    <social-sharing></social-sharing>
  </b-modal>
</template>

<script>
import SplashScreen from './Splash_screen'
import SocialSharing from './Social_sharing'

export default {
  name: 'Video_hint',
  mounted () {
  },
  computed: {
    hintShowing () {
      return this.$store.getters.getHintShowing
    },
    permanentImages () {
      return this.$store.getters.getPermanentImages
    },
    videoHint () {
      return this.$store.getters.getVideoHint
    }
  },
  data () {
    return {
      show: true
    }
  },
  components: {
    SplashScreen,
    SocialSharing
  }
}
</script>

<style lang="scss">
$video-hint-close-top: 0;
$video-hint-close-left: 100%;
$dialog-button-size: 3.5vmin;
$click-color: #B8E986;
$embedded-background: #1b1e21;
$dialog-button-size: 2rem;
$background-color: rgba(0, 0, 0, 1);
$grid-space-size: 9vmin;
$share-btn-size: 2.5rem;

#video-modal {
  .modal-dialog .modal-content {
    background-color: pink;
    left: 0;
    position: relative;
    .modal-header, .modal-body, .modal-footer {
      background-color: $background-color;
      border: none;
    }
    .close-congrats {
      position: absolute;
      height: $dialog-button-size;
      width: $dialog-button-size;
      top: 0;
      right: 0;
    }
    .modal-body {
      height: 30rem;
      max-height: 100vh;
      padding: 0;
      .player {
        height: calc(100% - #{$share-btn-size});
        width: 100%;
        z-index: 1;
      }

      .hint-spinner {
        position: absolute;
        *:first-child {
          background: $embedded-background!important;
        }
      }
    }
    .social-spot {
      display: flex;
      justify-content: center;
      div {
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
    }
  }
}
</style>
