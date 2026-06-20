<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import IncidentTimeline from '../components/IncidentTimeline.vue'
import ManagerAssignmentPanel from '../components/ManagerAssignmentPanel.vue'
import IncidentActionsPanel from '../components/IncidentActionsPanel.vue'
import IncidentClassificationPanel from '../components/IncidentClassificationPanel.vue'
import IncidentSlaPanel from '../components/IncidentSlaPanel.vue'
import PostMortemPanel from '../components/PostMortemPanel.vue'
import { ApiError } from '../services/apiClient'
import { fetchIncidentHistory, fetchVisibleIncidentById } from '../services/incidentsApi'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const incident = ref(null)
const history = ref([])
const isLoading = ref(true)
const errorMessage = ref('')
const postMortemStatus = ref('')

const incidentId = computed(() => route.params.id)
const isManager = computed(() => auth.user.value?.role === 'MANAGER')

async function loadDetails() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const [incidentData, historyData] = await Promise.all([
      fetchVisibleIncidentById(incidentId.value),
      fetchIncidentHistory(incidentId.value)
    ])

    incident.value = incidentData
    history.value = historyData.events || []
  } catch (error) {
    if (error instanceof ApiError && (error.status === 401 || error.status === 403)) {
      await router.push('/incidents')
      return
    }
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load incident details.'
  } finally {
    isLoading.value = false
  }
}

function handleAssigned(updatedIncident) {
  incident.value = updatedIncident
  loadDetails()
}

async function handleUpdated(updatedIncident) {
  incident.value = updatedIncident
  await loadDetails()
}

function handlePostMortemChanged(report) {
  postMortemStatus.value = report?.status || ''
}

onMounted(loadDetails)
</script>

<template>
  <section class="details-page">
    <div class="panel">
      <p v-if="isLoading" class="empty-note">Loading incident details...</p>
      <p v-else-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>

      <template v-else-if="incident">
        <div class="panel__header">
          <div>
            <p class="details-id">{{ incident.id }}</p>
            <h2 class="panel__title">{{ incident.title }}</h2>
          </div>
        </div>

        <div class="details-grid">
          <div class="summary-item">
            <span class="summary-item__label">Status</span>
            <strong class="status-badge" :class="`status-badge--${incident.status.toLowerCase()}`">{{ incident.status }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-item__label">Priority</span>
            <strong
              v-if="incident.priority"
              class="priority-badge"
              :class="`priority-badge--${incident.priority.toLowerCase()}`"
            >
              {{ incident.priority }}
            </strong>
            <strong v-else class="summary-item__value">N/A</strong>
          </div>
          <div class="summary-item">
            <span class="summary-item__label">Category</span>
            <strong class="summary-item__value">{{ incident.category || 'N/A' }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-item__label">Assigned to</span>
            <strong class="summary-item__value">{{ incident.assignedTo || 'Unassigned' }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-item__label">Project</span>
            <strong class="summary-item__value">{{ incident.projectId }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-item__label">Reported by</span>
            <strong class="summary-item__value">{{ incident.reportedBy }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-item__label">Channel</span>
            <strong class="summary-item__value">{{ incident.channel }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-item__label">Last updated</span>
            <strong class="summary-item__value">
              {{ incident.updatedAt ? new Date(incident.updatedAt).toLocaleString() : 'N/A' }}
            </strong>
          </div>
          <div class="summary-item summary-item--full">
            <span class="summary-item__label">Description</span>
            <strong class="summary-item__value">{{ incident.description }}</strong>
          </div>
        </div>

        <IncidentClassificationPanel
          :incident="incident"
          :user="auth.user.value"
          @updated="handleUpdated"
        />

        <ManagerAssignmentPanel
          v-if="isManager && ['NEW', 'IN_PROGRESS', 'ESCALATED'].includes(incident.status)"
          :incident-id="incident.id"
          :current-assigned-to="incident.assignedTo || ''"
          @assigned="handleAssigned"
        />

        <IncidentActionsPanel
          :incident="incident"
          :user="auth.user.value"
          :post-mortem-approved="postMortemStatus === 'APPROVED'"
          @updated="handleUpdated"
        />

        <IncidentSlaPanel
          :incident-id="incident.id"
          :permissions="auth.user.value?.permissions || []"
        />

        <PostMortemPanel
          :incident="incident"
          :user="auth.user.value"
          @changed="handlePostMortemChanged"
        />

        <IncidentTimeline :events="history" />
      </template>
    </div>
  </section>
</template>
