<template>
  <div id="Marketing" class="marketing" data-aos="zoom-in">
    <top-section :gotosite="goToSite"></top-section>
    <middle-section :gotosite="goToSite"></middle-section>
    <bottom-section></bottom-section>
  </div>
</template>

<script>
import TopSection from './TopSection'
import MiddleSection from './MiddleSection'
import BottomSection from './BottomSection'

export default {
  name: 'Marketing',
  computed: {
  },
  components: {
    TopSection,
    MiddleSection,
    BottomSection
  },
  methods: {
    goToSite () {
      this.$router.push({path: '/auth'})
    }
  }
}
</script>

<style lang="scss">
  @import url('https://fonts.googleapis.com/css?family=Roboto');
  @import url('https://fonts.googleapis.com/css?family=Material+Icons');

  body {
    margin: 0 auto;
  }

  * {
    font-family: 'probapro-regular', 'Roboto', Helvetica, Arial, sans-serif;
  }

  #app {
  }

  #Marketing {
    width: 100%;
    height: 100vh;
    margin: 0 auto;
    font-family: "Proba Pro Regular", serif;
    overflow: scroll;
    -webkit-overflow-scrolling: touch;
  }

  #Marketing::-webkit-scrollbar {
    display: none;
  }

  .play-now {
    height: 65px;
    width: 327px;
    color: #000000;
    font-size: 22px;
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
    border-radius: 1px;
    background-color: #B8E986;
    box-shadow: 0 2px 10px 0 rgba(0,0,0,0.5);
    cursor: pointer;
  }

  /* ipad pro Portrait */
  @media only screen
  and (min-device-width: 1024px) and (max-device-width: 1024px)
  and (min-device-height: 1366px) and (max-device-height: 1366px)
  and (min-width: 1024px) and (max-width: 1024px)
  and (orientation: portrait)
  and (-webkit-min-device-pixel-ratio: 1.5) {
    .play-now {
      height: 65px;
      width: 327px;
      font-size: 22px;
    }
  }

  // ipad landscape
  @media only screen
  and (min-device-width : 768px)
  and (max-device-height : 1024px)
  and (orientation : landscape) {
    .play-now {
      height: 65px;
      width: 327px;
      font-size: 22px;
    }
  }

  // ipad portrait
  @media only screen
  and (min-device-width : 768px)
  and (max-device-height : 1024px)
  and (orientation : portrait)  {
    .play-now {
      height: 65px;
      width: 275px;
      font-size: 22px;
    }
  }

  @media only screen and (max-width: 823px) and (orientation: landscape) {
    .play-now {
      height: 50px;
      width: 220px;
      font-size: 12px;
    }
  }

  @media only screen and (max-width: 736px) and (orientation: landscape) {
    .play-now {
      height: 50px;
      width: 250px;
      font-size: 12px;
    }
  }

  @media only screen and (max-width: 667px) and (orientation: landscape) {
    .play-now {
      height: 42px;
      width: 200px;
      font-size: 12px;
    }
  }

  @media only screen and (max-width: 568px) and (orientation: landscape) {
    .play-now {
      height: 42px;
      width: 153px;
      font-size: 12px;
    }
  }

  @media only screen and (max-width: 414px) {
    .play-now {
      height: 32px;
      width: 93px;
      font-size: 12px;
    }
  }

  @media only screen and (max-width: 375px) {
    .play-now {
      height: 32px;
      width: 93px;
      font-size: 12px;
    }
  }

  @media only screen and (max-width: 320px){
    .play-now {
      height: 32px;
      width: 93px;
      font-size: 12px;
    }
  }
</style>
