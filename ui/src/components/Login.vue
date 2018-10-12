<template>
 <div class="login">
   <vue-form :state="formstate" @submit.prevent="onSubmit">
     <validate auto-label class="form-group required-field" :class="fieldClassName(formstate.email)">
       <div class="input-group-prepend">
         <span class="input-group-text input-icon" id="login-email-icon-sm"><i class="fa fa-at"></i></span>
       </div>

       <input
         aria-describedby="login-email-icon-sm"
         type="email"
         name="email"
         class="form-control required-field"
         placeholder="yours@example.com"
         required
         v-model.lazy="loginForm.email"
       >

       <field-messages name="email" show="$touched || $submitted" class="form-control-feedback">
         <div slot="required">Required field</div>
         <div slot="email">Email is invalid</div>
       </field-messages>

     </validate>

     <validate auto-label class="form-group required-field" :class="fieldClassName(formstate.password)">
       <div class="input-group-prepend">
         <span class="input-group-text input-icon" id="login-password-1-icon-sm"><i class="fa fa-lock"></i></span>
       </div>

       <input
         aria-describedby="login-password-icon-sm"
         type="password"
         name="password"
         class="form-control"
         password-strength
         placeholder="your password"
         required
         v-model.lazy="loginForm.password"
       >

       <field-messages auto-label name="password" show="$touched || $submitted" class="form-control-feedback">
         <div slot="required">Required field</div>
         <div slot="password-strength">Min length is 8 characters</div>
       </field-messages>

     </validate>

     <div class="password-recovery" @click="showRecover()">Don't remember your password?</div>

     <div class="py-2 text-center">
       <button class="btn btn-primary" type="submit">LOG IN</button>
     </div>
   </vue-form>
 </div>
</template>

<script>
export default {
  name: 'Login',
  mounted () {
    this.auth.clearErrors()
  },
  computed: {
    auth () {
      return this.$store.getters.getAuth
    }
  },
  data () {
    return {
      formstate: {},
      loginForm: {
        email: '',
        password: ''
      }
    }
  },
  methods: {
    fieldClassName (field) {
      // for bootstrap classes
      if (!field) {
        return ''
      }
      if ((field.$touched || field.$submitted) && field.$valid) {
        return 'has-success'
      }

      if ((field.$touched || field.$submitted) && field.$invalid) {
        return 'has-danger'
      }
    },
    onSubmit () {
      if (this.formstate.$valid) {
        this.auth.authorizeMathbot(this.loginForm)
      }
    }
  },
  props: ['showRecover']
}
</script>

<style scoped lang="scss">
  $btn-height: 3rem;
  $danger-color: #F25C5C;
  $success-color: #50E3C2;
  $danger-background: rgba(242,92,92,0.2);
  $success-background: rgba(80,227,194, 0.2);
  .form-group {
    display: flex;
    justify-content: center;
    width: 90%;
    height: $btn-height;
    margin: 1em auto;
    position: relative;
    .form-control {
      font-size: 0.75rem;
      height: 100%;
      border-radius: 0 0.25rem 0.25rem 0;
    }
    .form-control-feedback {
      position: absolute;
      font-size: 0.75em;
      top: 100%;
    }

    .input-group-prepend {
      .input-icon {
        width: $btn-height;
        border-radius: 0.25rem 0 0 0.25rem;
        * {
          margin: 0 auto;
        }
      }
    }
    .input-success, .input-failure {
      top: -1px;
      right: -1px;
      bottom: -1px;
      left: -1px;
    }
  }
  .text-center {
    height: 4em;
    padding: 0!important;
    .btn {
      font-size: 1em;
      width: 100%;
      margin: 0;
      height: 100%;
      border-radius: 0 0 0.25rem 0.25rem;
      background-color: rgb(0, 170, 228);
    }
  }
  .required-field {
    border-radius: 0.25rem;
  }
  .required-field.has-danger {
    border: 1px solid $danger-color;
    input {
      background-color: $danger-background;
    }
  }
  .required-field.has-success {
    border: 1px solid $success-color;
    input {
      color: black;
      font-weight: bold;
      font-size: 0.9em;
      background-color: $success-background;
    }
  }
  .has-danger .form-control-feedback {
    color: $danger-color;
  }
  .has-success .form-control-feedback {
    color: $success-color;
  }
  .password-recovery {
    font-size: 0.85em;
    cursor: pointer;
    margin-bottom: 2vmin;
  }
</style>
