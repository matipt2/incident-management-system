<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createProject, fetchProjects, setProjectStatus, updateProject } from '../services/projectsApi'

const projects = ref([])
const isLoading = ref(true)
const isSubmitting = ref(false)
const errorMessage = ref('')
const editingKey = ref('')
const form = reactive({ key: '', name: '', description: '' })

async function loadProjects() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    projects.value = await fetchProjects(true)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load projects.'
  } finally {
    isLoading.value = false
  }
}

function edit(project) {
  editingKey.value = project.key
  form.key = project.key
  form.name = project.name
  form.description = project.description || ''
}

function reset() {
  editingKey.value = ''
  form.key = ''
  form.name = ''
  form.description = ''
}

async function save() {
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    if (editingKey.value) {
      await updateProject(editingKey.value, {
        name: form.name.trim(),
        description: form.description.trim()
      })
    } else {
      await createProject({
        key: form.key.trim(),
        name: form.name.trim(),
        description: form.description.trim()
      })
    }
    reset()
    await loadProjects()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to save project.'
  } finally {
    isSubmitting.value = false
  }
}

async function toggle(project) {
  if (project.active && !window.confirm(`Deactivate project ${project.key}? Historical data will remain available.`)) {
    return
  }
  try {
    await setProjectStatus(project.key, !project.active)
    await loadProjects()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to update project.'
  }
}

onMounted(loadProjects)
</script>

<template>
  <section class="list-page">
    <div class="panel admin-grid">
      <div>
        <div class="panel__header"><h2 class="panel__title">Projects</h2></div>
        <p v-if="isLoading" class="empty-note">Loading projects...</p>
        <p v-else-if="!projects.length" class="empty-note">No projects available.</p>
        <div v-else class="table-wrap">
          <table class="incidents-table">
            <thead><tr><th>Key</th><th>Name</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>
              <tr v-for="project in projects" :key="project.key">
                <td>{{ project.key }}</td>
                <td>{{ project.name }}</td>
                <td>{{ project.active ? 'ACTIVE' : 'INACTIVE' }}</td>
                <td class="table-actions">
                  <button class="btn btn--ghost" type="button" @click="edit(project)">Edit</button>
                  <button class="btn btn--ghost" type="button" @click="toggle(project)">
                    {{ project.active ? 'Deactivate' : 'Activate' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <form class="admin-form" @submit.prevent="save">
        <h3 class="section-title">{{ editingKey ? 'Edit project' : 'Create project' }}</h3>
        <input v-model="form.key" :disabled="Boolean(editingKey)" placeholder="PROJECT-KEY" required />
        <input v-model="form.name" placeholder="Project name" required />
        <textarea v-model="form.description" rows="4" placeholder="Description" />
        <div class="assignment-row">
          <button class="btn btn--primary" :disabled="isSubmitting" type="submit">Save</button>
          <button v-if="editingKey" class="btn btn--ghost" type="button" @click="reset">Cancel</button>
        </div>
      </form>
      <p v-if="errorMessage" class="alert alert--error">{{ errorMessage }}</p>
    </div>
  </section>
</template>
