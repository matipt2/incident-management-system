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