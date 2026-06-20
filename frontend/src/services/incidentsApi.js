import { apiFetch } from './apiClient'

const USE_MOCK = false

export async function createIncident(payload) {
    if (USE_MOCK) {
        return {
            id: `INC-${Math.floor(Math.random() * 9000 + 1000)}`,
            title: payload.title,
            status: 'NEW'
        }
    }

    return apiFetch('/api/incidents', {
        method: 'POST',
        body: JSON.stringify(payload)
    })
}

export async function fetchVisibleIncidents(filters = {}) {
    const params = new URLSearchParams()

    for (const [key, value] of Object.entries(filters)) {
        if (value !== null && value !== undefined && String(value).trim() !== '') {
            params.set(key, value)
        }
    }

    const query = params.toString()
    return apiFetch(`/api/my/incidents${query ? `?${query}` : ''}`)
}

export async function fetchVisibleIncidentById(id) {
    return apiFetch(`/api/my/incidents/${id}`)
}

export async function fetchIncidentHistory(id) {
    return apiFetch(`/api/my/incidents/${id}/history`)
}

export async function fetchAgents() {
    return apiFetch('/api/management/agents')
}

export async function assignIncidentToAgent(id, agentId) {
    return apiFetch(`/api/management/incidents/${id}/assignment`, {
        method: 'POST',
        body: JSON.stringify({ agentId })
    })
}

export async function escalateIncident(id, reason) {
    const params = new URLSearchParams({ reason })
    return apiFetch(`/api/incidents/${id}/escalate?${params}`, { method: 'POST' })
}

export async function resolveIncident(id, resolution) {
    const params = new URLSearchParams({ resolution })
    return apiFetch(`/api/incidents/${id}/resolve?${params}`, { method: 'POST' })
}

export async function closeIncident(id) {
    return apiFetch(`/api/incidents/${id}/close`, { method: 'POST' })
}

export async function classifyIncident(id, classification) {
    return apiFetch(`/api/incidents/${id}/classify`, {
        method: 'POST',
        body: JSON.stringify(classification)
    })
}
