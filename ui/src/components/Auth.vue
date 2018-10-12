<template>
<div class="container-fluid auth" v-if="auth.requestSession !== null" data-aos="zoom-in">
  <div class="row">
    <div class="card" style="width: 18rem;">
      <img class="dialog-button control-btn close-auth" @click="auth.logout" :src="permanentImages.buttons.xButtonTransparent">
      <img v-if="updateParams === null && recoverShowing" class="dialog-button control-btn back-button" @click="recoverShowing = false" :src="permanentImages.buttons.back"/>
      <img class="card-img-top" :src="permanentImages.instructionsRobot" alt="Card image cap">
      <div class="card-title">MATH_BOT</div>
      <div class="auth-errors">
        <div v-for="(err, ind) in authErrors" :key="'auth-error/' + ind" class="auth-error-msg">
          {{err}}
        </div>
      </div>
      <div class="card-body" v-if="!recoverShowing">
        <div class="card-header">
          <ul class="nav nav-tabs">
            <li class="nav-item" @click="signupShowing = true">
              <a class="nav-link" :class="signupShowing ? 'active' : ''" href="#">Sign Up</a>
            </li>
            <li class="nav-item" @click="signupShowing = false">
              <a class="nav-link" :class="!signupShowing ? 'active' : ''" href="#">Log In</a>
            </li>
          </ul>
        </div>
        <social-auth :title-prefix="signupShowing ? 'SIGN UP' : 'LOG IN'"></social-auth>
        <div class="or-divider">or</div>
        <signup v-if="signupShowing"></signup>
        <login v-else :show-recover="showRecover"></login>
      </div>
      <div class="card-body" v-else>
        <recover-password v-if="updateParams === null"></recover-password>
        <update-password v-else :update-params="updateParams"></update-password>
      </div>
    </div>
  </div>
</div>
</template>

<script>
import Signup from './Signup'
import Login from './Login'
import SocialAuth from './Social_auth'
import RecoverPassword from './Recover_password'
import UpdatePassword from './Update_password'

export default {
  name: 'Auth',
  mounted () {
    this.auth.clearErrors()
    this.showUpdate()
    if (this.auth.authenticated) this.$router.push('/profile')
  },
  computed: {
    permanentImages () {
      return this.$store.getters.getPermanentImages
    },
    auth () {
      return this.$store.getters.getAuth
    },
    authErrors () {
      return this.$store.getters.getAuthErrors
    }
  },
  data () {
    return {
      recoverShowing: false,
      signupShowing: true,
      updateParams: null // ?recoveryId=OP_IUxHp7zM%3D
    }
  },
  methods: {
    showRecover () {
      this.recoverShowing = true
    },
    showUpdate () {
      const params = window.location.search
      if (params.includes('?recoveryId=')) {
        this.recoverShowing = true
        this.updateParams = params
      }
    }
  },
  components: {
    Signup,
    Login,
    SocialAuth,
    RecoverPassword,
    UpdatePassword
  }
}
</script>

<style scoped lang="scss">
$title-background-color: rgba(241,241,241,0.8);
$btn-height: 3rem;
$font-size: 0.75rem;

.auth {
  height: 100%;
  overflow: auto;
  display: flex;
  justify-content: center;
  .row {
    display: flex;
    justify-content: center;
    align-items: center;
    height: min-content;
    min-height: 100%;
    width: min-content;
  }
}

.card {
  font-size: 1em;
  transition: all .5s linear;

  .card-img-top, .card-title {
    background-color: $title-background-color;
    padding-top: 0.5em;
  }
  .card-img-top {
    height: 5em;
  }
  .card-title {
    font-size: 1.5em;
    margin: 0;
  }
  .card-body {
    padding: 0;
    .card-header {
      background-color: $title-background-color;
      width: 100%;
      padding: 0;
      border-bottom: 1px solid rgba(92, 102, 111, 0.1);
      .nav {
        .nav-item {
          width: 50%;
          .nav-link {
            border: none;
            background-color: #ffffff;
            color: rgba(92, 102, 111, 0.6);
            border-radius: 0;
            font-size: 0.75em;
          }
          .active {
            color: #000000;
            box-shadow: 0 1px 0 0 #5c666f;
            cursor: default;
          }
        }
      }
    }
    .or-divider {
      display: flex;
      justify-content: center;
      align-items: center;
      color: #5c666f;
      font-size: 1em;
    }
  }
  .auth-errors {
    background-color: rgba(255, 69, 0, 0.8);
    color: #ffffff;
  }
  .control-btn {
    position: absolute;
    top: 0;
    border-radius: 50%;
    height: 1.5em;
    width: 1.5em;
  }
  .close-auth {
    right: 0;
  }
  .back-button {
    left: 0;
    height: 1.9em;
    width: 1.9em;
  }
}

@media screen and (max-width: 480px) {
  .card {
    width: 100%;
  }
}
</style>
