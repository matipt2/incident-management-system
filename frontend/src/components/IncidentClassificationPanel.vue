<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { classifyIncident } from '../services/incidentsApi'

const props = defineProps({
  incident: { type: Object, required: true },
  user: { type: Object, required: true }
})

const emit = defineEmits(['updated'])

const priorities = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']
const categories = ['NETWORK', 'APPLICATION', 'DATABASE', 'SECURITY', 'HARDWARE', 'OTHER']
const form = reactive({ priority: '', category: '' })
const isSubmitting = ref(false)
const errorMessage = ref('')

const permissions = computed(() => new Set(props.user.permissions || []))
const isManager = computed(() => props.user.role === 'MANAGER')
const isAssignedAgent = computed(() => (
  props.user.role === 'AGENT' && props.incident.assignedTo === props.user.username
))
const canClassify = computed(() => (
  permissions.value.has('INCIDENT_CLASSIFY') &&
  (isManager.value || isAssignedAgent.value) &&
  ['NEW', 'IN_PROGRESS', 'ESCALATED'].includes(props.incident.status)
))

watch(
  () => [props.incident.priority, props.incident.category],
  ([priority, category]) => {
    form.priority = priority || ''
    form.category = category || ''
  },
  { immediate: true }
)

async function submit() {
  if (!form.priority || !form.category) return
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    const updated = await classifyIncident(props.incident.id, {
      priority: form.priority,
      category: form.category
    })
    emit('updated', updated)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Classification failed.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section v-if="canClassify" class="workflow-card">
    <div class="workflow-card__header">
      <div>
        <p class="workflow-card__eyebrow">Triage</p>
        <h3 class="section-title">Classification</h3>
      </div>
      <span class="workflow-card__hint">Assigned agent or manager</span>
    </div>

    <div class="workflow-grid">
      <label class="form-field">
        <span>Priority</span>
        <select v-model="form.priority" class="form-select">
          <option value="" disabled>Select priority</option>
          <option v-for="priority in priorities" :key="priority" :value="priority">{{ priority }}</option>
        </select>
      </label>
      <label class="form-field">
        <span>Category</span>
        <select v-model="form.category" class="form-select">
          <option value="" disabled>Select category</option>
          <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
        </select>
      </label>
    </div>

    <div class="workflow-actions">
      <button
        class="btn btn--primary"
        type="button"
        :disabled="isSubmitting || !form.priority || !form.category"
        @click="submit"
      >
        {{ isSubmitting ? 'Saving...' : 'Apply classification' }}
      </button>
    </div>
    <p v-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>
  </section>
</template>
