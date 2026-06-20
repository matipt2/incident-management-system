import { apiFetch } from './apiClient'

export function fetchUsers() {
    return apiFetch('/api/management/users')
}

export function updateUserRole(userId, role) {
    return apiFetch(`/api/management/users/${userId}/role`, {
        method: 'PATCH',
        body: JSON.stringify({ role })
    })
}
