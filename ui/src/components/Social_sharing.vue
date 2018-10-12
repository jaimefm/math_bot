<template>
<div class="social-sharing">
  <social-sharing url="https://mathbot.com"
                  :title="message"
                  twitter-user="JWWeatherman_"
                  inline-template>
    <div class="social-links">
      <network class="share-button share-twitter" network="twitter">
        <i class="fa fa-twitter"></i>
      </network>
      <network class="share-button share-facebook" network="facebook">
        <i class="fa fa-facebook"></i>
      </network>
      <network class="share-button share-linkedin" network="linkedin">
        <i class="fa fa-linkedin"></i>
      </network>
      <network class="share-button share-reddit" network="reddit">
        <i class="fa fa-reddit"></i>
      </network>
    </div>
  </social-sharing>
</div>
</template>

<script>
export default {
  name: 'Social_sharing',
  data () {
    return {
      message: `
        I just found this cool site: https://mathbot.com. It's a fun little game, but I guess it teaches you programming & math too. I'm enjoying it. It's very satisfying when you get that little robot to finally bend to your will! **World Domination Face**
      `
    }
  }
}
</script>

<style lang="scss">
$twitter-color: #1da1f2;
$facebook-color: #3b5998;
$linkedin-color: #00a0dc;
$reddit-color: #ff4500;
$btn-font-size: 2.5vmin;
$btn-size: 3.3vmin;
.social-sharing {
  .social-links {
    .share-button {
      display: inline-flex;
      justify-content: space-evenly;
      align-items: center;
      border-radius: 50%;
      cursor: pointer;
      height: $btn-size;
      width: $btn-size;

      i {
        color: #ffffff;
        font-size: $btn-font-size;
      }
    }
    .share-twitter {
      background-color: $twitter-color;
    }
    .share-facebook {
      background-color: $facebook-color;
    }
    .share-linkedin {
      background-color: $linkedin-color;
    }
    .share-reddit {
      background-color: $reddit-color;
    }
  }
}
</style>
