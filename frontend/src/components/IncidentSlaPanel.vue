<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchIncidentSlaViolations } from '../services/slaApi'

const props = defineProps({
  incidentId: { type: String, required: true },
  permissions: { type: Array, default: () => [] }
})

const violations = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const canRead = computed(() => props.permissions.includes('SLA_READ'))

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : 'N/A'
}

async function load() {
  if (!canRead.value) return
  isLoading.value = true
  try {
    violations.value = await fetchIncidentSlaViolations(props.incidentId)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load SLA violations.'
  } finally {
    isLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section v-if="canRead" class="workflow-card">
    <div class="workflow-card__header">
      <div>
        <p class="workflow-card__eyebrow">Service levels</p>
        <h3 class="section-title">SLA violations</h3>
      </div>
      <span class="workflow-card__hint">{{ violations.length }} detected</span>
    </div>
    <p v-if="isLoading" class="empty-note">Loading SLA information...</p>
    <p v-else-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>
    <p v-else-if="!violations.length" class="empty-note">No SLA violations detected for this incident.</p>
    <div v-else class="violation-list">
      <article v-for="violation in violations" :key="violation.id" class="violation-item">
        <div>
          <strong>{{ violation.violationType }}</strong>
          <p>Detected {{ formatDate(violation.detectedAt) }}</p>
        </div>
        <span class="status-badge" :class="violation.penaltyApplied ? 'status-badge--approved' : ''">
          {{ violation.penaltyApplied ? 'PENALTY APPLIED' : `PENALTY ${violation.penalty}` }}
        </span>
      </article>
    </div>
  </section>
</template>
