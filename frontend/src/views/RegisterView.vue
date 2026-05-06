<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { ApiError } from '../services/apiClient'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const roles = ['REPORTER', 'AGENT', 'MANAGER']

const form = reactive({
    username: '',
    email: '',
    password: '',
    role: 'REPORTER'
})

const isSubmitting = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const isFormValid = computed(() => {
    return (
        form.username.trim() &&
        form.email.trim() &&
        form.password.trim().length >= 8 &&
        roles.includes(form.role)
    )
})

async function handleSubmit() {
    if (!isFormValid.value) return

    isSubmitting.value = true
    errorMessage.value = ''
    successMessage.value = ''

    try {
        await auth.registerUser({
            username: form.username.trim(),
            email: form.email.trim(),
            password: form.password,
            role: form.role
        })

        successMessage.value = 'Account created. You can now sign in.'
        setTimeout(() => {
            router.push('/login')
        }, 600)
    } catch (error) {
        if (error instanceof ApiError && error.status === 409) {
            errorMessage.value = 'Username or email is already taken.'
        } else {
            errorMessage.value = error instanceof Error ? error.message : 'Registration failed.'
        }
    } finally {
        isSubmitting.value = false
    }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-card">
      <h2 class="form-card__title">Registration</h2>

      <form class="incident-form" @submit.prevent="handleSubmit">
        <div class="form-field form-field--full">
          <label for="username">Username</label>
          <input id="username" v-model="form.username" type="text" autocomplete="username" required />
        </div>

        <div class="form-field form-field--full">
          <label for="email">Email</label>
          <input id="email" v-model="form.email" type="email" autocomplete="email" required />
        </div>

        <div class="form-field">
          <label for="password">Password (min. 8 characters)</label>
          <input id="password" v-model="form.password" type="password" autocomplete="new-password" required minlength="8" />
        </div>

        <div class="form-field">
          <label for="role">Role</label>
          <select id="role" v-model="form.role" class="form-select">
            <option v-for="role in roles" :key="role" :value="role">{{ role }}</option>
          </select>
        </div>

        <div v-if="errorMessage" class="alert alert--error form-field--full">
          <strong>Registration failed.</strong>
          <span>{{ errorMessage }}</span>
        </div>

        <div v-if="successMessage" class="alert alert--success form-field--full">
          <strong>Success.</strong>
          <span>{{ successMessage }}</span>
        </div>

        <div class="form-actions">
          <button class="btn btn--primary" type="submit" :disabled="isSubmitting || !isFormValid">
            {{ isSubmitting ? 'Creating account...' : 'Create account' }}
          </button>
          <p class="form-actions__hint">
            Already have an account?
            <RouterLink class="text-link" to="/login">Go to login</RouterLink>
          </p>
        </div>
      </form>
    </div>
  </section>
</template>
