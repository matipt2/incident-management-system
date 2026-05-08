<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { ApiError } from '../services/apiClient'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const form = reactive({
    username: '',
    password: ''
})

const isSubmitting = ref(false)
const errorMessage = ref('')

const isFormValid = computed(() => form.username.trim() && form.password.trim())

async function handleSubmit() {
    if (!isFormValid.value) return

    isSubmitting.value = true
    errorMessage.value = ''

    try {
        await auth.loginUser({
            username: form.username.trim(),
            password: form.password
        })

        const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
        await router.replace(redirect)
    } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
            errorMessage.value = 'Invalid username or password.'
        } else {
            errorMessage.value = error instanceof Error ? error.message : 'Login failed.'
        }
    } finally {
        isSubmitting.value = false
    }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-card">
      <h2 class="form-card__title">Login</h2>

      <form class="incident-form" @submit.prevent="handleSubmit">
        <div class="form-field form-field--full">
          <label for="username">Username</label>
          <input id="username" v-model="form.username" type="text" autocomplete="username" required />
        </div>

        <div class="form-field form-field--full">
          <label for="password">Password</label>
          <input id="password" v-model="form.password" type="password" autocomplete="current-password" required />
        </div>

        <div v-if="errorMessage" class="alert alert--error form-field--full">
          <strong>Login failed.</strong>
          <span>{{ errorMessage }}</span>
        </div>

        <div class="form-actions">
          <button class="btn btn--primary" type="submit" :disabled="isSubmitting || !isFormValid">
            {{ isSubmitting ? 'Signing in...' : 'Sign in' }}
          </button>
          <p class="form-actions__hint">
            No account yet?
            <RouterLink class="text-link" to="/register">Go to registration</RouterLink>
          </p>
        </div>
      </form>
    </div>
  </section>
</template>
