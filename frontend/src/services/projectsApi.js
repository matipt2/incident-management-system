import { apiFetch } from './apiClient'

export function fetchProjects(includeInactive = false) {
    return apiFetch(`/api/projects${includeInactive ? '?includeInactive=true' : ''}`)
}

export function createProject(payload) {
    return apiFetch('/api/projects', {
        method: 'POST',
        body: JSON.stringify(payload)
    })
}

export function updateProject(key, payload) {
    return apiFetch(`/api/projects/${encodeURIComponent(key)}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
    })
}

export function setProjectStatus(key, active) {
    return apiFetch(`/api/projects/${encodeURIComponent(key)}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ active })
    })
}
