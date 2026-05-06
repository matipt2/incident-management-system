<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createIncident } from '../services/incidentsApi'
import { ApiError } from '../services/apiClient'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({
  title: '',
  description: '',
  reportedBy: auth.user.value?.username || '',
  channel: 'FORM',
  projectId: ''
})

const isSubmitting = ref(false)
const errorMessage = ref('')

const isFormValid = computed(() => {
  return (
      form.title.trim() &&
      form.description.trim() &&
      reportedByUsername.value.trim() &&
      form.channel.trim() &&
      form.projectId.trim()
  )
})

const reportedByUsername = computed(() => auth.user.value?.username || '')

async function handleSubmit() {
  if (!isFormValid.value) return

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const incident = await createIncident({
      title: form.title.trim(),
      description: form.description.trim(),
      reportedBy: reportedByUsername.value,
      channel: form.channel.trim(),
      projectId: form.projectId.trim()
    })

    router.push({
      name: 'incident-submitted',
      query: {
        id: incident.id,
        title: incident.title,
        status: incident.status
      }
    })
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      auth.logoutUser()
      await router.push('/login')
      return
    }

    if (error instanceof ApiError && error.status === 403) {
      errorMessage.value = 'You do not have permission to create incidents.'
      return
    }

    errorMessage.value = error instanceof Error ? error.message : 'Something went wrong.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="form-page">
    <div class="form-card">
      <div class="form-card__header">
        <div>
          <p class="form-card__eyebrow">Form</p>
          <h2 class="form-card__title">Incident details</h2>
        </div>

        <span class="form-card__badge">Incident Report</span>
      </div>

      <form class="incident-form" @submit.prevent="handleSubmit">
        <div class="form-field form-field--full">
          <label for="title">Incident title</label>
          <input
              id="title"
              v-model="form.title"
              type="text"
              placeholder="E.g. Unable to access the admin panel"
              required
          />
        </div>

        <div class="form-field form-field--full">
          <label for="description">Description</label>
          <textarea
              id="description"
              v-model="form.description"
              rows="6"
              placeholder="Describe symptoms, user impact, and incident context..."
              required
          />
        </div>

        <div class="form-field">
          <label for="reportedBy">Reported by</label>
          <input
              id="reportedBy"
              :value="reportedByUsername"
              type="text"
              readonly
              disabled
          />
        </div>

        <div class="form-field">
          <label for="channel">Reporting channel</label>
          <input
              id="channel"
              v-model="form.channel"
              type="text"
              placeholder="E.g. FORM / EMAIL / SLACK"
              required
          />
        </div>

        <div class="form-field form-field--full">
          <label for="projectId">Project ID</label>
          <input
              id="projectId"
              v-model="form.projectId"
              type="text"
              placeholder="E.g. payments-platform"
              required
          />
        </div>

        <div v-if="errorMessage" class="alert alert--error">
          <strong>Submission failed.</strong>
          <span>{{ errorMessage }}</span>
        </div>

        <div class="form-actions">
          <button
              type="submit"
              class="btn btn--primary"
              :disabled="isSubmitting || !isFormValid"
          >
            {{ isSubmitting ? 'Submitting...' : 'Create incident' }}
          </button>

          <p class="form-actions__hint">
            After submission, you will be redirected to the confirmation view.
          </p>
        </div>
      </form>
    </div>
  </section>
</template>