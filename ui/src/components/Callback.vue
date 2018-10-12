<template>
  <splash-screen></splash-screen>
</template>

<script>
import SplashScreen from './Splash_screen'
export default {
  name: 'Callback',
  mounted () {
    this.auth.handleAuthentication()
  },
  computed: {
    auth () {
      return this.$store.getters.getAuth
    }
  },
  data () {
    return {
    }
  },
  components: {
    SplashScreen
  }
}
</script>

<style scoped>

</style>
