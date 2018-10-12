<template>
  <div class="col-8 user-profile-controls" style="display: flex;">
    <div class="profile-image">
      <img class="profile-logo" :src="permanentImages.instructionsRobot">
    </div>
    <div class="profile-text">
      <div class="user-profile-user-name">{{currentUser.name}}</div>
      <mathbot-header :font-size="headerFont" :line-height="headerFont + 2"></mathbot-header>
      <div class="profile-text-footer">
        <span class="nav-links about" @click="gotoMarketing">About</span>
        <span class="nav-links sign-in" v-if="!auth.authenticated" @click="auth.login">Sign In</span>
        <span class="nav-links sign-out" v-else @click="auth.logout">Sign Out</span>
      </div>
    </div>
  </div>
</template>

<script>
import MathbotHeader from './Mathbot_header'
export default {
  name: 'user_profile_controls',
  computed: {
    currentUser () {
      return this.auth.userProfile
    },
    auth () {
      return this.$store.getters.getAuth
    },
    windowWidth () {
      return window.innerWidth
    },
    headerFont () {
      if (this.windowWidth < 1024) {
        return 30
      }
    }
  },
  methods: {
    gotoMarketing () {
      this.$router.push({path: '/about'})
    }
  },
  components: {
    MathbotHeader
  },
  props: ['permanentImages']
}
</script>

<style scoped lang="scss">
  $profile-font-size: 3vmin;
  $text-color: rgba(255, 255, 255, 0.3);
  $font-family: Roboto, Georgia, serif;
  $logo-width: 20vmin;
  $line-height: 1.2em;

  .profile-text .social-sharing .social-links .share-button {
    height: $line-height!important;
    width: $line-height!important;
    i {
      font-size: $line-height!important;
    }
  }

  .user-profile-controls {
    font-size: $profile-font-size;
    color: $text-color;
    font-weight: 600;
    font-family: $font-family;

    .profile-image {
      display: flex;
      align-items: flex-end;
      padding: 0 1vmin 0 0;

      .profile-logo {
        width: $logo-width;
      }
    }

    .profile-text {
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      font-size: 1em;
      line-height: $line-height;
      padding: 0 0 1vmin 0;

      .mathbot-header {
        font-size: 1.5em;
      }

      .profile-text-footer {
        display: flex;
        justify-content: space-between;

        span:hover {
          cursor: pointer;
          color: #ffffff;
        }
      }
    }
  }
</style>
