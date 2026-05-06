import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiFetch } from '../src/services/apiClient'
import { getStoredToken, persistSession } from '../src/services/session'

function jsonResponse(payload, status = 200) {
    return new Response(JSON.stringify(payload), {
        status,
        headers: {
            'Content-Type': 'application/json'
        }
    })
}

describe('apiFetch', () => {
    beforeEach(() => {
        localStorage.clear()
        vi.restoreAllMocks()
    })

    it('adds bearer token and json content type to authenticated requests', async () => {
        persistSession('jwt-token', { username: 'jan.kowalski' })
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ id: 'INC-1' }))

        const result = await apiFetch('/api/incidents', {
            method: 'POST',
            body: JSON.stringify({ title: 'Broken service' })
        })

        const [, options] = fetch.mock.calls[0]

        expect(result).toEqual({ id: 'INC-1' })
        expect(options.headers.get('Authorization')).toBe('Bearer jwt-token')
        expect(options.headers.get('Content-Type')).toBe('application/json')
    })

    it('does not overwrite an explicit content type', async () => {
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ ok: true }))

        await apiFetch('/api/upload', {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain'
            },
            body: 'raw-payload'
        })

        const [, options] = fetch.mock.calls[0]

        expect(options.headers.get('Content-Type')).toBe('text/plain')
    })

    it('clears session and throws ApiError on 401 responses', async () => {
        persistSession('expired-token', { username: 'jan.kowalski' })
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ message: 'Token expired' }, 401))

        await expect(apiFetch('/api/me/resources')).rejects.toMatchObject({
            name: 'ApiError',
            status: 401,
            message: 'Token expired'
        })
        expect(getStoredToken()).toBeNull()
    })

    it('throws ApiError with fallback message for non-json errors', async () => {
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 403 }))

        await expect(apiFetch('/api/incidents')).rejects.toMatchObject({
            name: 'ApiError',
            status: 403,
            message: 'Wystapil blad podczas komunikacji z serwerem.'
        })
    })
})
