<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ApiError } from '../services/apiClient'
import {
  approvePostMortem,
  createPostMortem,
  fetchPostMortem,
  updatePostMortem
} from '../services/postMortemApi'

const props = defineProps({
  incident: { type: Object, required: true },
  user: { type: Object, required: true }
})

const emit = defineEmits(['changed'])

const report = ref(null)
const isLoading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const form = reactive({ rootCause: '', timeline: '', impact: '', actionItems: '' })

const permissions = computed(() => new Set(props.user.permissions || []))
const canRead = computed(() => permissions.value.has('POSTMORTEM_READ'))
const canWrite = computed(() => permissions.value.has('POSTMORTEM_WRITE'))
const canApprove = computed(() => (
  permissions.value.has('POSTMORTEM_APPROVE') &&
  report.value &&
  report.value.status !== 'APPROVED'
))
const showPanel = computed(() => (
  canRead.value && (props.incident.priority === 'CRITICAL' || report.value)
))
const formComplete = computed(() => Object.values(form).every(value => value.trim()))

function fillForm(value) {
  form.rootCause = value?.rootCause || ''
  form.timeline = value?.timeline || ''
  form.impact = value?.impact || ''
  form.actionItems = value?.actionItems || ''
}

async function load() {
  if (!canRead.value) return
  isLoading.value = true
  errorMessage.value = ''
  try {
    report.value = await fetchPostMortem(props.incident.id)
    fillForm(report.value)
    emit('changed', report.value)
  } catch (error) {
    if (!(error instanceof ApiError && error.status === 404)) {
      errorMessage.value = error instanceof Error ? error.message : 'Failed to load post-mortem.'
    }
  } finally {
    isLoading.value = false
  }
}

async function save() {
  if (!formComplete.value) return
  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    if (!report.value) {
      await createPostMortem(props.incident.id)
    }
    report.value = await updatePostMortem(props.incident.id, {
      rootCause: form.rootCause.trim(),
      timeline: form.timeline.trim(),
      impact: form.impact.trim(),
      actionItems: form.actionItems.trim()
    })
    successMessage.value = 'Post-mortem saved.'
    emit('changed', report.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to save post-mortem.'
  } finally {
    isSubmitting.value = false
  }
}

async function approve() {
  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    report.value = await approvePostMortem(props.incident.id)
    fillForm(report.value)
    successMessage.value = 'Post-mortem approved. The incident can now be closed.'
    emit('changed', report.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Approval failed.'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <section v-if="showPanel" class="workflow-card">
    <div class="workflow-card__header">
      <div>
        <p class="workflow-card__eyebrow">Critical incident review</p>
        <h3 class="section-title">Post-mortem</h3>
      </div>
      <span class="status-badge" :class="`status-badge--${(report?.status || 'missing').toLowerCase()}`">
        {{ report?.status || 'NOT CREATED' }}
      </span>
    </div>

    <p v-if="isLoading" class="empty-note">Loading post-mortem...</p>

    <div v-else-if="canWrite && report?.status !== 'APPROVED'" class="postmortem-form">
      <label class="form-field">
        <span>Root cause</span>
        <textarea v-model="form.rootCause" rows="3" placeholder="What caused the incident?" />
      </label>
      <label class="form-field">
        <span>Timeline</span>
        <textarea v-model="form.timeline" rows="3" placeholder="Key events and timestamps" />
      </label>
      <label class="form-field">
        <span>Impact</span>
        <textarea v-model="form.impact" rows="3" placeholder="Users, services, and business impact" />
      </label>
      <label class="form-field">
        <span>Action items</span>
        <textarea v-model="form.actionItems" rows="3" placeholder="Follow-up work and owners" />
      </label>
    </div>

    <dl v-else-if="report" class="postmortem-summary">
      <div><dt>Root cause</dt><dd>{{ report.rootCause }}</dd></div>
      <div><dt>Timeline</dt><dd>{{ report.timeline }}</dd></div>
      <div><dt>Impact</dt><dd>{{ report.impact }}</dd></div>
      <div><dt>Action items</dt><dd>{{ report.actionItems }}</dd></div>
    </dl>

    <p v-else class="empty-note">
      A completed and approved post-mortem is required before a critical incident can be closed.
    </p>

    <div class="workflow-actions">
      <button
        v-if="canWrite && report?.status !== 'APPROVED'"
        class="btn btn--primary"
        type="button"
        :disabled="isSubmitting || !formComplete"
        @click="save"
      >
        {{ isSubmitting ? 'Saving...' : report ? 'Update post-mortem' : 'Create post-mortem' }}
      </button>
      <button
        v-if="canApprove"
        class="btn btn--ghost"
        type="button"
        :disabled="isSubmitting"
        @click="approve"
      >
        Approve post-mortem
      </button>
    </div>

    <p v-if="successMessage" class="alert alert--success">{{ successMessage }}</p>
    <p v-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>
  </section>
</template>
