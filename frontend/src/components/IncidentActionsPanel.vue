<script setup>
import { computed, ref } from 'vue'
import { closeIncident, escalateIncident, resolveIncident } from '../services/incidentsApi'

const props = defineProps({
  incident: { type: Object, required: true },
  user: { type: Object, required: true },
  postMortemApproved: { type: Boolean, default: false }
})

const emit = defineEmits(['updated'])

const note = ref('')
const errorMessage = ref('')
const isSubmitting = ref(false)

const permissions = computed(() => new Set(props.user.permissions || []))
const isAssignedAgent = computed(() => (
  props.user.role === 'AGENT' && props.incident.assignedTo === props.user.username
))
const canAct = computed(() => props.user.role === 'MANAGER' || isAssignedAgent.value)
const canEscalate = computed(() => (
  canAct.value &&
  permissions.value.has('INCIDENT_ESCALATE') &&
  ['NEW', 'IN_PROGRESS'].includes(props.incident.status)
))
const canResolve = computed(() => (
  canAct.value &&
  permissions.value.has('INCIDENT_RESOLVE') &&
  ['IN_PROGRESS', 'ESCALATED'].includes(props.incident.status)
))
const canClose = computed(() => (
  props.user.role === 'MANAGER' &&
  permissions.value.has('INCIDENT_CLOSE') &&
  props.incident.status === 'RESOLVED'
))
const criticalClosureBlocked = computed(() => (
  canClose.value &&
  props.incident.priority === 'CRITICAL' &&
  !props.postMortemApproved
))
const isVisible = computed(() => canEscalate.value || canResolve.value || canClose.value)

async function run(action) {
  if (action === 'close' && !window.confirm('Close this incident? This lifecycle action cannot be reversed.')) {
    return
  }
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    let updated
    if (action === 'escalate') updated = await escalateIncident(props.incident.id, note.value.trim())
    if (action === 'resolve') updated = await resolveIncident(props.incident.id, note.value.trim())
    if (action === 'close') updated = await closeIncident(props.incident.id)
    note.value = ''
    emit('updated', updated)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Incident action failed.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section v-if="isVisible" class="assignment-card">
    <h3 class="section-title">Incident actions</h3>
    <textarea
      v-if="canEscalate || canResolve"
      v-model="note"
      class="action-note"
      rows="3"
      placeholder="Add escalation reason or resolution details"
    />
    <div class="assignment-row">
      <button
        v-if="canEscalate"
        class="btn btn--ghost"
        type="button"
        :disabled="isSubmitting || !note.trim()"
        @click="run('escalate')"
      >
        Escalate
      </button>
      <button
        v-if="canResolve"
        class="btn btn--primary"
        type="button"
        :disabled="isSubmitting || !note.trim()"
        @click="run('resolve')"
      >
        Resolve
      </button>
      <button
        v-if="canClose"
        class="btn btn--primary"
        type="button"
        :disabled="isSubmitting || criticalClosureBlocked"
        @click="run('close')"
      >
        Close incident
      </button>
    </div>
    <p v-if="criticalClosureBlocked" class="workflow-note">
      An approved post-mortem is required before closing a critical incident.
    </p>
    <p v-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>
  </section>
</template>
