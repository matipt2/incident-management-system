import { clearSession, getStoredToken } from './session'

export class ApiError extends Error {
    constructor(message, status, payload = null) {
        super(message)
        this.name = 'ApiError'
        this.status = status
        this.payload = payload
    }
}

export async function apiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {})
    const token = getStoredToken()

    if (token) {
        headers.set('Authorization', `Bearer ${token}`)
    }

    if (options.body && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json')
    }

    const response = await fetch(path, { ...options, headers })
    const contentType = response.headers.get('content-type') || ''
    const isJson = contentType.includes('application/json')
    const payload = isJson ? await response.json() : null

    if (!response.ok) {
        if (response.status === 401) {
            clearSession()
        }

        const fallbackMessage = 'Wystapil blad podczas komunikacji z serwerem.'
        const message = payload?.message || payload?.error || fallbackMessage
        throw new ApiError(message, response.status, payload)
    }

    return payload
}
