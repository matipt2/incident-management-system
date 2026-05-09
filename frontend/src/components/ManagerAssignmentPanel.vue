<script setup>
import { onMounted, ref } from 'vue'
import { ApiError } from '../services/apiClient'
import { assignIncidentToAgent, fetchAgents } from '../services/incidentsApi'

const props = defineProps({
  incidentId: {
    type: String,
    required: true
  },
  currentAssignedTo: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['assigned'])

const agents = ref([])
const selectedAgent = ref('')
const isSubmitting = ref(false)
const isLoading = ref(true)
const errorMessage = ref('')

onMounted(async () => {
  try {
    agents.value = await fetchAgents()
    selectedAgent.value = props.currentAssignedTo || agents.value[0]?.username || ''
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load agents.'
  } finally {
    isLoading.value = false
  }
})

async function assign() {
  if (!selectedAgent.value) return

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const updated = await assignIncidentToAgent(props.incidentId, selectedAgent.value)
    emit('assigned', updated)
  } catch (error) {
    if (error instanceof ApiError) {
      errorMessage.value = error.message
    } else {
      errorMessage.value = 'Failed to assign incident.'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="assignment-card">
    <h3 class="section-title">Manager assignment</h3>

    <p v-if="isLoading" class="empty-note">Loading agents...</p>

    <template v-else>
      <div class="assignment-row">
        <select v-model="selectedAgent" class="form-select" :disabled="isSubmitting || !agents.length">
          <option value="" disabled>Select agent</option>
          <option v-for="agent in agents" :key="agent.userId" :value="agent.username">
            {{ agent.username }} ({{ agent.email }})
          </option>
        </select>

        <button
          type="button"
          class="btn btn--primary"
          :disabled="isSubmitting || !selectedAgent"
          @click="assign"
        >
          {{ isSubmitting ? 'Assigning...' : 'Assign incident' }}
        </button>
      </div>

      <p v-if="errorMessage" class="alert alert--error">
        {{ errorMessage }}
      </p>
    </template>
  </section>
</template>
