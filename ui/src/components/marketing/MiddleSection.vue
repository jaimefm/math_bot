<template>
 <div id="Marketing-middle-section">
   <div class="middle-container">
     <div class="message top-message">
       <div class="header">For kids and adults.</div>
       <p>To get started you don't need to know how to read or add, but before you're finished you'll know how to calculate derivatives.</p>
     </div>
     <div class="message middle-message">
       <div class="header">Super easy peasey.</div>
       <p>Programming is simple. In fact, mathbot only has four commands (step forward, turn right, pickup, and put down) so you'll be crushing problems in under 3 minutes.</p>
     </div>
     <div class="message bottom-message">
       <div class="header">Math is Fun</div>
       <p>By using a robot to do the work you get all the joy of problem solving without any annoying or repetitive tasks. And since you are able to focus on the big picture you'll learn faster too.</p>
     </div>
     <img class="left-arch-line" :src="imgResources['leftArchLine']">
     <img class="ship-and-planet" :src="imgResources['shipAndPlanet']">
     <img class="middle-arch-line" :src="imgResources['middleArchLine']">
     <img class="planet-cluster" :src="imgResources['planetCluster']">
     <div class="play-now" @click="gotosite()">SIGN UP</div>
   </div>
 </div>
</template>

<script>
import api from '../../services/api'
export default {
  name: 'MiddleSection',
  computed: {
    runCompiled () {
      return this.$store.getters.getRunCompiled
    },
    tokenId () {
      return this.$store.getters.getTokenId
    }
  },
  data () {
    return {
      imgResources: {
        'leftArchLine': 'https://res.cloudinary.com/doohickey/image/upload/v1522023333/Path_3_i2cwxd.svg',
        'shipAndPlanet': 'https://res.cloudinary.com/doohickey/image/upload/v1522023583/Group_3_xhgk5t.png',
        'middleArchLine': 'https://res.cloudinary.com/doohickey/image/upload/v1522023645/Path_4_vwxssf.svg',
        'planetCluster': 'https://res.cloudinary.com/doohickey/image/upload/v1522023702/noun_922273_cc_atgmbu.svg'
      }
    }
  },
  methods: {
    goToSandBox () {
      this.$store.dispatch('updateRunCompiled', this)
      api.switchLevel({tokenId: this.tokenId, level: 'Sandbox', step: '1'}, (res) => {
        this.$store.dispatch('updateStats', res.body)
        this.$router.push({path: '/robot'})
      })
    }
  },
  props: ['gotosite']
}
</script>

<style scoped lang="scss">
  $planet-cluster-size: 234px;
  $ship-and-planet-size: 385px;
  $left-arch-line-size: 805px;
  $middle-arch-line-size: 340px;
  $header-font-size: 48px;
  $header-line-height: 58px;
  $message-font-size: 23px;
  $message-line-height: 23px;

  #Marketing-middle-section {
    background-color: #FFFFFF;
    grid-area: middlesection;
    color: #000000;
  }

  .middle-container {
    max-width: 1366px;
    margin: 0 auto;
    padding: 100px 0 100px 0;
    display: grid;
    grid-template-columns: [c-one]12.5% [c-two]12.5% [c-three]12.5% [c-four]12.5% [c-five]12.5% [c-six]12.5% [c-seven]12.5% [c-eight]12.5%;
    grid-template-rows: [r-one]auto[r-two]auto [r-three]auto [r-four]auto [r-five]auto [r-six]auto [r-seven]auto [r-eight]auto [r-nine]auto [r-ten]auto [r-eleven]auto [r-twelve]auto;
  }

  .message > p {
    font-size: $message-font-size;
    line-height: $message-line-height;
    text-align: left;
  }

  .message .header {
    font-size: $header-font-size;
    line-height: $header-line-height;
    text-align: left;
  }

  .top-message {
    grid-column: c-two / span 3;
    grid-row: r-two / span 2;
  }

  .middle-message {
    grid-column: c-six / span 3;
    grid-row: r-seven / span 2;
  }

  .bottom-message {
    grid-column: c-two / span 3;
    grid-row: r-nine / span 2;
  }

  .left-arch-line {
    grid-column: c-one / span 6;
    grid-row: r-three / span 2;
    width: $left-arch-line-size;
  }

  .ship-and-planet {
    grid-column: c-six / span 3;
    grid-row: r-one / span 4;
    align-self: end;
    margin-bottom: 80px;
    width: $ship-and-planet-size;
  }

  .middle-arch-line {
    grid-column: c-four / span 3;
    grid-row: r-seven / span 2;
    align-self: end;
    width: $middle-arch-line-size;
  }

  .planet-cluster {
    grid-column: c-two / span 3;
    grid-row: r-five / span 3;
    margin-left: 60px;
    align-self: end;
    width: $planet-cluster-size;
  }

  .play-now {
    grid-column: c-six / span 2;
    grid-row: r-nine / span 1;
  }

  /* ipad pro Portrait */
  @media only screen
  and (min-device-width: 1024px) and (max-device-width: 1024px)
  and (min-device-height: 1366px) and (max-device-height: 1366px)
  and (min-width: 1024px) and (max-width: 1024px)
  and (orientation: portrait)
  and (-webkit-min-device-pixel-ratio: 1.5) {
    $planet-cluster-size: 234px;
    $ship-and-planet-size: 385px;
    $left-arch-line-size: 490px;
    $middle-arch-line-size: 400px;
    $header-font-size: 48px;
    $header-line-height: 58px;
    $message-font-size: 23px;
    $message-line-height: 23px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-five / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: -10px;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-three / span 3;
      grid-row: r-nine / span 2;
      margin-bottom: 40px;
      margin-left: 30px;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-six / span 3;
      margin-left: 10px;
      align-self: end;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  // ipad landscape
  @media only screen
  and (min-device-height : 768px)
  and (max-device-width : 1024px)
  and (orientation : landscape) {
    $planet-cluster-size: 126px;
    $ship-and-planet-size: 225px;
    $left-arch-line-size: 620px;
    $middle-arch-line-size: 350px;
    $header-font-size: 48px;
    $header-line-height: 58px;
    $message-font-size: 23px;
    $message-line-height: 23px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-six / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: -10px;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-three / span 3;
      grid-row: r-nine / span 2;
      margin-bottom: 40px;
      margin-left: 30px;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-six / span 3;
      margin-left: 10px;
      align-self: end;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  // ipad portrait
  @media only screen
  and (min-device-width : 768px)
  and (max-device-height : 1024px)
  and (orientation : portrait)  {
    $planet-cluster-size: 126px;
    $ship-and-planet-size: 225px;
    $left-arch-line-size: 465px;
    $middle-arch-line-size: 255px;
    $header-font-size: 48px;
    $header-line-height: 58px;
    $message-font-size: 23px;
    $message-line-height: 23px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-six / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: -10px;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-three / span 3;
      grid-row: r-nine / span 2;
      margin-bottom: 40px;
      margin-left: 30px;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-six / span 3;
      margin-left: 10px;
      align-self: end;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  @media only screen and (max-width: 823px) and (orientation: landscape) {
    $planet-cluster-size: 126px;
    $ship-and-planet-size: 225px;
    $left-arch-line-size: 495px;
    $middle-arch-line-size: 270px;
    $header-font-size: 26px;
    $header-line-height: 32px;
    $message-font-size: 18px;
    $message-line-height: 22px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-six / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: -10px;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-three / span 3;
      grid-row: r-nine / span 2;
      margin-bottom: 40px;
      margin-left: 30px;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-six / span 3;
      margin-left: 10px;
      align-self: end;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  @media only screen and (max-width: 736px) and (orientation: landscape) {
    $planet-cluster-size: 106px;
    $ship-and-planet-size: 175px;
    $left-arch-line-size: 450px;
    $middle-arch-line-size: 220px;
    $header-font-size: 26px;
    $header-line-height: 32px;
    $message-font-size: 18px;
    $message-line-height: 22px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-six / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: -10px;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-three / span 3;
      grid-row: r-nine / span 2;
      margin-bottom: 40px;
      margin-left: 30px;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-six / span 3;
      margin-left: 10px;
      align-self: end;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  @media only screen and (max-width: 667px) and (orientation: landscape) {
    $planet-cluster-size: 106px;
    $ship-and-planet-size: 175px;
    $left-arch-line-size: 410px;
    $middle-arch-line-size: 220px;
    $header-font-size: 22px;
    $header-line-height: 32px;
    $message-font-size: 16px;
    $message-line-height: 16px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-six / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: -10px;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-three / span 3;
      grid-row: r-nine / span 2;
      margin-bottom: 40px;
      margin-left: 30px;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-six / span 3;
      margin-left: 10px;
      align-self: end;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  @media only screen and (max-width: 568px) and (orientation: landscape){
    $planet-cluster-size: 100px;
    $ship-and-planet-size: 155px;
    $left-arch-line-size: 340px;
    $middle-arch-line-size: 170px;
    $header-font-size: 22px;
    $header-line-height: 26px;
    $message-font-size: 12px;
    $message-line-height: 16px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-six / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: 0;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-four / span 3;
      grid-row: r-nine / span 2;
      margin-left: -30px;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-six / span 3;
      margin-left: 10px;
      align-self: end;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  @media only screen and (max-width: 414px) {
    $planet-cluster-size: 96px;
    $ship-and-planet-size: 135px;
    $left-arch-line-size: 200px;
    $middle-arch-line-size: 120px;
    $header-font-size: 22px;
    $header-line-height: 26px;
    $message-font-size: 12px;
    $message-line-height: 16px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-five / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: -3px;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-four / span 3;
      grid-row: r-nine / span 2;
      align-self: start;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-seven / span 3;
      margin-left: 10px;
      align-self: end;
      margin-bottom: 60px;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  @media only screen and (max-width: 375px) {
    $planet-cluster-size: 86px;
    $ship-and-planet-size: 125px;
    $left-arch-line-size: 180px;
    $middle-arch-line-size: 120px;
    $header-font-size: 22px;
    $header-line-height: 26px;
    $message-font-size: 12px;
    $message-line-height: 16px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-five / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: -3px;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-four / span 3;
      grid-row: r-nine / span 2;
      align-self: start;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-seven / span 3;
      margin-left: 10px;
      align-self: end;
      margin-bottom: 60px;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }

  @media only screen and (max-width: 320px) {
    $planet-cluster-size: 76px;
    $ship-and-planet-size: 115px;
    $left-arch-line-size: 150px;
    $middle-arch-line-size: 120px;
    $header-font-size: 22px;
    $header-line-height: 26px;
    $message-font-size: 12px;
    $message-line-height: 16px;

    #Marketing-middle-section {
      background-color: #FFFFFF;
      grid-area: middlesection;
      color: #000000;
    }

    .middle-container {
      padding: 50px 0 50px 0;
    }

    .message > p {
      font-size: $message-font-size;
      line-height: $message-line-height;
      text-align: left;
    }

    .message .header {
      font-size: $header-font-size;
      line-height: $header-line-height;
      text-align: left;
    }

    .top-message {
      grid-column: c-two / span 3;
      grid-row: r-two / span 2;

    }

    .middle-message {
      grid-column: c-five / span 3;
      grid-row: r-seven / span 2;
      margin-bottom: 30px;
    }

    .bottom-message {
      grid-column: c-two / span 3;
      grid-row: r-ten / span 2;
    }

    .left-arch-line {
      grid-column: c-one / span 6;
      grid-row: r-four / span 2;
      width: $left-arch-line-size;
    }

    .ship-and-planet {
      grid-column: c-five / span 3;
      grid-row: r-one / span 4;
      align-self: end;
      margin-bottom: 0;
      width: $ship-and-planet-size;
    }

    .middle-arch-line {
      grid-column: c-four / span 3;
      grid-row: r-nine / span 2;
      align-self: start;
      width: $middle-arch-line-size;
    }

    .planet-cluster {
      grid-column: c-two / span 3;
      grid-row: r-seven / span 3;
      margin-left: 10px;
      align-self: end;
      margin-bottom: 60px;
      width: $planet-cluster-size;
    }

    .play-now {
      grid-column: c-six / span 2;
      grid-row: r-ten / span 1;
    }
  }
</style>
