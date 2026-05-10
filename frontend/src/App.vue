<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import AppSidebar from './components/AppSidebar.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const pageTitle = computed(() => {
  if (route.name === 'login') return 'Login'
  if (route.name === 'register') return 'Registration'
  if (route.name === 'incidents-list') return 'Incidents'
  if (route.name === 'incident-details') return 'Incident details'
  if (route.name === 'incident-submitted') return 'Incident submitted'
  return 'Report Incident'
})

const showSidebar = computed(() => {
  return auth.isAuthenticated.value && route.name !== 'incident-submitted'
})

async function handleLogout() {
  auth.logoutUser()
  await router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header" :class="{ 'app-header--centered': !auth.isAuthenticated.value }">
      <div class="app-header__main">
        <p class="app-header__eyebrow">Incident Management</p>
        <h1 class="app-header__title">{{ pageTitle }}</h1>
      </div>

      <div v-if="auth.isAuthenticated.value" class="app-header__session">
        <span class="app-header__user">
          {{ auth.user.value?.username }} ({{ auth.user.value?.role }})
        </span>
        <button class="btn btn--ghost" type="button" @click="handleLogout">Logout</button>
      </div>
    </header>

    <div class="workspace" :class="{ 'workspace--with-sidebar': showSidebar }">
      <AppSidebar v-if="showSidebar" :role="auth.user.value?.role" />

      <main class="app-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>