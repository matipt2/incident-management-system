import { apiFetch } from './apiClient'

function path(incidentId, suffix = '') {
    return `/api/incidents/${incidentId}/post-mortem${suffix}`
}

export function fetchPostMortem(incidentId) {
    return apiFetch(path(incidentId))
}

export function createPostMortem(incidentId) {
    return apiFetch(path(incidentId), { method: 'POST' })
}

export function updatePostMortem(incidentId, payload) {
    return apiFetch(path(incidentId), {
        method: 'PUT',
        body: JSON.stringify(payload)
    })
}

export function approvePostMortem(incidentId) {
    return apiFetch(path(incidentId, '/approve'), { method: 'POST' })
}
