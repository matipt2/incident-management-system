import { apiFetch } from './apiClient'

export function fetchSlaPolicies() {
    return apiFetch('/api/sla/policies')
}

export function createSlaPolicy(payload) {
    return apiFetch('/api/sla/policies', {
        method: 'POST',
        body: JSON.stringify(payload)
    })
}

export function updateSlaPolicy(id, payload) {
    return apiFetch(`/api/sla/policies/${id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
    })
}

export function fetchSlaViolations(projectId = '') {
    const query = projectId ? `?projectId=${encodeURIComponent(projectId)}` : ''
    return apiFetch(`/api/sla/violations${query}`)
}

export function fetchIncidentSlaViolations(incidentId) {
    return apiFetch(`/api/sla/violations/${incidentId}`)
}

export function runSlaCheck() {
    return apiFetch('/api/sla/check', { method: 'POST' })
}

export function applySlaPenalty(id) {
    return apiFetch(`/api/sla/violations/${id}/apply-penalty`, { method: 'POST' })
}
