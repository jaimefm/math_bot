<template>
  <div class="container profile" v-shortkey.once="['ctrl', 'space']" @shortkey="goToSandbox()">
    <level-congrats key="level-congrats"></level-congrats>
    <div class="row" style="height: 80%;">
      <space :permanent-images="permanentImages"></space>
      <steps :permanent-images="permanentImages"></steps>
    </div>
    <div class="col-8 controls" style="height: 20%;">
      <social-sharing :size="'1vmin'"></social-sharing>
      <user-profile-controls :permanent-images="permanentImages"></user-profile-controls>
    </div>
  </div>
</template>

<script>
import SplashScreen from './Splash_screen'
import UserProfileControls from './User_profile_controls'
import Steps from './Steps'
import Space from './Space'
import SocialSharing from './Social_sharing'
import LevelCongrats from './Level_congrats'
import api from '../services/api'

export default {
  mounted () {
    this.handleCongrats()
  },
  computed: {
    auth () {
      return this.$store.getters.getAuth
    },
    splashScreenShowing () {
      return this.$store.getters.getSplashScreenShowing
    },
    profileView () {
      return this.$store.getters.getProfileView
    },
    permanentImages () {
      return this.$store.getters.getPermanentImages
    },
    stats () {
      return this.$store.getters.getStats
    },
    currentUserName () {
      let currentUser = this.$store.getters.getCurrentUser
      if (currentUser === null) {
        return 'Profile'
      } else {
        return currentUser.given_name || currentUser.nickname
      }
    },
    step () {
      return this.$store.getters.getStep
    },
    tokenId () {
      return this.$store.getters.getTokenId
    }
  },
  methods: {
    handleCongrats () {
      if (this.$route.query.showCongrats === 'true') {
        this.$root.$emit('bv::show::modal', 'level-congrats-modal')
        this.$router.push({query: {}})
      }
    },
    goToSandbox () {
      this.$store.dispatch('updateRunCompiled', this)
      api.switchLevel({tokenId: this.tokenId, level: 'Sandbox', step: '1'}, (res) => {
        this.$store.dispatch('updateStats', res.body)
        this.$router.push({path: '/robot'})
      })
    }
  },
  components: {
    SplashScreen,
    UserProfileControls,
    Steps,
    Space,
    SocialSharing,
    LevelCongrats
  }
}
</script>

<style lang="scss">
  .profile {
    height: 100%;
    position: relative;
    .controls {
      padding: 0;
      display: flex;
      .social-sharing {
        .social-links {
          display: flex;
          justify-content: space-evenly;
          flex-direction: column;
          height: 100%;
        }
      }
    }
  }
</style>
