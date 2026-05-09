<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { ApiError } from '../services/apiClient'
import { fetchVisibleIncidents } from '../services/incidentsApi'

const router = useRouter()

const filters = reactive({
  status: '',
  priority: '',
  category: '',
  projectId: '',
  search: ''
})

const incidents = ref([])
const isLoading = ref(true)
const errorMessage = ref('')

async function loadIncidents() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    incidents.value = await fetchVisibleIncidents(filters)
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      await router.push('/login')
      return
    }
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load incidents.'
  } finally {
    isLoading.value = false
  }
}

function resetFilters() {
  filters.status = ''
  filters.priority = ''
  filters.category = ''
  filters.projectId = ''
  filters.search = ''
  loadIncidents()
}

onMounted(loadIncidents)
</script>

<template>
  <section class="list-page">
    <div class="panel">
      <div class="panel__header">
        <h2 class="panel__title">Incidents</h2>
      </div>

      <form class="filters" @submit.prevent="loadIncidents">
        <select v-model="filters.status" class="form-select">
          <option value="">All statuses</option>
          <option value="NEW">NEW</option>
          <option value="IN_PROGRESS">IN_PROGRESS</option>
          <option value="WAITING_FOR_CUSTOMER">WAITING_FOR_CUSTOMER</option>
          <option value="ESCALATED">ESCALATED</option>
          <option value="RESOLVED">RESOLVED</option>
          <option value="CLOSED">CLOSED</option>
        </select>

        <select v-model="filters.priority" class="form-select">
          <option value="">All priorities</option>
          <option value="CRITICAL">CRITICAL</option>
          <option value="HIGH">HIGH</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="LOW">LOW</option>
        </select>

        <select v-model="filters.category" class="form-select">
          <option value="">All categories</option>
          <option value="NETWORK">NETWORK</option>
          <option value="APPLICATION">APPLICATION</option>
          <option value="DATABASE">DATABASE</option>
          <option value="SECURITY">SECURITY</option>
          <option value="HARDWARE">HARDWARE</option>
          <option value="OTHER">OTHER</option>
        </select>

        <input v-model="filters.projectId" type="text" placeholder="Project ID" class="filters__input" />
        <input v-model="filters.search" type="text" placeholder="Search by ID, title, description..." class="filters__input" />

        <div class="filters__actions">
          <button class="btn btn--primary" type="submit">Apply filters</button>
          <button class="btn btn--ghost" type="button" @click="resetFilters">Reset</button>
        </div>
      </form>

      <p v-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>
      <p v-else-if="isLoading" class="empty-note">Loading incidents...</p>
      <p v-else-if="!incidents.length" class="empty-note">No incidents found for current filters.</p>

      <div v-else class="table-wrap">
        <table class="incidents-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Status</th>
              <th>Priority</th>
              <th>Category</th>
              <th>Assigned to</th>
              <th>Updated</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="incident in incidents" :key="incident.id">
              <td>
                <RouterLink :to="`/incidents/${incident.id}`" class="table-link">{{ incident.id }}</RouterLink>
              </td>
              <td>{{ incident.title }}</td>
              <td>{{ incident.status }}</td>
              <td>{{ incident.priority || 'N/A' }}</td>
              <td>{{ incident.category || 'N/A' }}</td>
              <td>{{ incident.assignedTo || 'Unassigned' }}</td>
              <td>{{ incident.updatedAt ? new Date(incident.updatedAt).toLocaleString() : 'N/A' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>
