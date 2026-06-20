<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchProjects } from '../services/projectsApi'
import {
  applySlaPenalty,
  createSlaPolicy,
  fetchSlaPolicies,
  fetchSlaViolations,
  runSlaCheck,
  updateSlaPolicy
} from '../services/slaApi'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const priorities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']
const policies = ref([])
const violations = ref([])
const projects = ref([])
const projectFilter = ref('')
const editingId = ref(null)
const isLoading = ref(true)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const form = reactive({
  projectId: '',
  priority: 'MEDIUM',
  responseTimeMinutes: 30,
  resolutionTimeMinutes: 240,
  penaltyAmount: 0
})

const permissions = computed(() => new Set(auth.user.value?.permissions || []))
const canWrite = computed(() => permissions.value.has('SLA_WRITE'))

function projectLabel(key) {
  const project = projects.value.find(item => item.key === key)
  return project ? `${project.name} (${project.key})` : key
}

function resetForm() {
  editingId.value = null
  form.projectId = projects.value[0]?.key || ''
  form.priority = 'MEDIUM'
  form.responseTimeMinutes = 30
  form.resolutionTimeMinutes = 240
  form.penaltyAmount = 0
}

function editPolicy(policy) {
  editingId.value = policy.id
  form.projectId = policy.projectId
  form.priority = policy.priority
  form.responseTimeMinutes = policy.responseTimeMinutes
  form.resolutionTimeMinutes = policy.resolutionTimeMinutes
  form.penaltyAmount = policy.penaltyAmount
}

async function load() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const [policyData, violationData, projectData] = await Promise.all([
      fetchSlaPolicies(),
      fetchSlaViolations(projectFilter.value),
      fetchProjects()
    ])
    policies.value = policyData
    violations.value = violationData
    projects.value = projectData
    if (!form.projectId) form.projectId = projectData[0]?.key || ''
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load SLA data.'
  } finally {
    isLoading.value = false
  }
}

async function loadViolations() {
  try {
    violations.value = await fetchSlaViolations(projectFilter.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to filter violations.'
  }
}

async function savePolicy() {
  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''
  const payload = {
    projectId: form.projectId,
    priority: form.priority,
    responseTimeMinutes: Number(form.responseTimeMinutes),
    resolutionTimeMinutes: Number(form.resolutionTimeMinutes),
    penaltyAmount: Number(form.penaltyAmount)
  }
  try {
    if (editingId.value) await updateSlaPolicy(editingId.value, payload)
    else await createSlaPolicy(payload)
    successMessage.value = editingId.value ? 'SLA policy updated.' : 'SLA policy created.'
    resetForm()
    await load()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to save SLA policy.'
  } finally {
    isSubmitting.value = false
  }
}

async function checkSla() {
  isSubmitting.value = true
  try {
    await runSlaCheck()
    successMessage.value = 'SLA check completed.'
    await loadViolations()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'SLA check failed.'
  } finally {
    isSubmitting.value = false
  }
}

async function applyPenalty(violation) {
  if (!window.confirm(`Apply penalty for ${violation.incidentId}?`)) return
  isSubmitting.value = true
  try {
    await applySlaPenalty(violation.id)
    successMessage.value = 'Penalty applied.'
    await loadViolations()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to apply penalty.'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="list-page sla-page">
    <p v-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>
    <p v-if="successMessage" class="alert alert--success">{{ successMessage }}</p>
    <p v-if="isLoading" class="empty-note">Loading SLA configuration...</p>

    <template v-else>
      <div class="panel">
        <div class="panel__header panel__header--actions">
          <div>
            <p class="workflow-card__eyebrow">Configuration</p>
            <h2 class="panel__title">SLA policies</h2>
          </div>
        </div>

        <div class="sla-layout" :class="{ 'sla-layout--readonly': !canWrite }">
          <div class="table-wrap">
            <table class="incidents-table">
              <thead>
                <tr><th>Project</th><th>Priority</th><th>Response</th><th>Resolution</th><th>Penalty</th><th v-if="canWrite">Action</th></tr>
              </thead>
              <tbody>
                <tr v-for="policy in policies" :key="policy.id">
                  <td>{{ projectLabel(policy.projectId) }}</td>
                  <td><span class="priority-badge" :class="`priority-badge--${policy.priority.toLowerCase()}`">{{ policy.priority }}</span></td>
                  <td>{{ policy.responseTimeMinutes }} min</td>
                  <td>{{ policy.resolutionTimeMinutes }} min</td>
                  <td>{{ policy.penaltyAmount }}</td>
                  <td v-if="canWrite"><button class="btn btn--ghost" type="button" @click="editPolicy(policy)">Edit</button></td>
                </tr>
              </tbody>
            </table>
          </div>

          <form v-if="canWrite" class="admin-form" @submit.prevent="savePolicy">
            <h3 class="section-title">{{ editingId ? 'Edit policy' : 'Create policy' }}</h3>
            <label class="form-field">
              <span>Project</span>
              <select v-model="form.projectId" class="form-select" required>
                <option v-for="project in projects" :key="project.key" :value="project.key">
                  {{ project.name }} ({{ project.key }})
                </option>
              </select>
            </label>
            <label class="form-field">
              <span>Priority</span>
              <select v-model="form.priority" class="form-select">
                <option v-for="priority in priorities" :key="priority" :value="priority">{{ priority }}</option>
              </select>
            </label>
            <label class="form-field"><span>Response time (minutes)</span><input v-model="form.responseTimeMinutes" type="number" min="1" required /></label>
            <label class="form-field"><span>Resolution time (minutes)</span><input v-model="form.resolutionTimeMinutes" type="number" min="1" required /></label>
            <label class="form-field"><span>Penalty amount</span><input v-model="form.penaltyAmount" type="number" min="0" step="0.01" required /></label>
            <div class="workflow-actions">
              <button class="btn btn--primary" type="submit" :disabled="isSubmitting || !form.projectId">Save policy</button>
              <button v-if="editingId" class="btn btn--ghost" type="button" @click="resetForm">Cancel</button>
            </div>
          </form>
        </div>
      </div>

      <div class="panel">
        <div class="panel__header panel__header--actions">
          <div>
            <p class="workflow-card__eyebrow">Monitoring</p>
            <h2 class="panel__title">SLA violations</h2>
          </div>
          <button v-if="canWrite" class="btn btn--ghost" type="button" :disabled="isSubmitting" @click="checkSla">Run SLA check</button>
        </div>

        <div class="filter-row">
          <select v-model="projectFilter" class="form-select" @change="loadViolations">
            <option value="">All projects</option>
            <option v-for="project in projects" :key="project.key" :value="project.key">{{ project.name }} ({{ project.key }})</option>
          </select>
        </div>

        <p v-if="!violations.length" class="empty-note">No visible SLA violations.</p>
        <div v-else class="table-wrap">
          <table class="incidents-table">
            <thead><tr><th>Incident</th><th>Project</th><th>Type</th><th>Detected</th><th>Penalty</th><th>Status</th><th v-if="canWrite">Action</th></tr></thead>
            <tbody>
              <tr v-for="violation in violations" :key="violation.id">
                <td><RouterLink class="table-link" :to="`/incidents/${violation.incidentId}`">{{ violation.incidentId }}</RouterLink></td>
                <td>{{ projectLabel(violation.projectId) }}</td>
                <td>{{ violation.violationType }}</td>
                <td>{{ new Date(violation.detectedAt).toLocaleString() }}</td>
                <td>{{ violation.penalty }}</td>
                <td>{{ violation.penaltyApplied ? 'APPLIED' : 'PENDING' }}</td>
                <td v-if="canWrite">
                  <button
                    class="btn btn--ghost"
                    type="button"
                    :disabled="violation.penaltyApplied || isSubmitting"
                    @click="applyPenalty(violation)"
                  >
                    Apply penalty
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </section>
</template>
