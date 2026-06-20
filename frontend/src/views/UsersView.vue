<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { fetchUsers, updateUserRole } from '../services/usersApi'

const auth = useAuthStore()
const users = ref([])
const errorMessage = ref('')
const isLoading = ref(true)
const roles = ['VIEWER', 'REPORTER', 'AGENT', 'MANAGER']

async function loadUsers() {
  isLoading.value = true
  try {
    users.value = await fetchUsers()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load users.'
  } finally {
    isLoading.value = false
  }
}

async function changeRole(user, role) {
  errorMessage.value = ''
  try {
    const updated = await updateUserRole(user.userId, role)
    users.value = users.value.map(item => item.userId === updated.userId ? updated : item)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to update role.'
    await loadUsers()
  }
}

onMounted(loadUsers)
</script>

<template>
  <section class="list-page">
    <div class="panel">
      <div class="panel__header"><h2 class="panel__title">User roles</h2></div>
      <p v-if="isLoading" class="empty-note">Loading users...</p>
      <div v-else class="table-wrap">
        <table class="incidents-table">
          <thead><tr><th>Username</th><th>Email</th><th>Role</th></tr></thead>
          <tbody>
            <tr v-for="user in users" :key="user.userId">
              <td>{{ user.username }}</td>
              <td>{{ user.email }}</td>
              <td>
                <select
                  class="form-select compact-select"
                  :value="user.role"
                  :disabled="user.userId === auth.user.value?.userId"
                  @change="changeRole(user, $event.target.value)"
                >
                  <option v-for="role in roles" :key="role" :value="role">{{ role }}</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>
    </div>
  </section>
</template>
