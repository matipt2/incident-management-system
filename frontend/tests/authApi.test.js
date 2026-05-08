import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fetchMe, login, register } from '../src/services/authApi'

function jsonResponse(payload) {
    return new Response(JSON.stringify(payload), {
        status: 200,
        headers: {
            'Content-Type': 'application/json'
        }
    })
}

describe('authApi', () => {
    beforeEach(() => {
        localStorage.clear()
        vi.restoreAllMocks()
    })

    it('sends login request to the backend auth endpoint', async () => {
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ token: 'jwt-token' }))

        await login({
            username: 'jan.kowalski',
            password: 'password123'
        })

        const [path, options] = fetch.mock.calls[0]

        expect(path).toBe('/api/auth/login')
        expect(options.method).toBe('POST')
        expect(JSON.parse(options.body)).toEqual({
            username: 'jan.kowalski',
            password: 'password123'
        })
        expect(options.headers.get('Content-Type')).toBe('application/json')
    })

    it('sends registration request to the backend auth endpoint', async () => {
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({}))

        await register({
            username: 'jan.kowalski',
            email: 'jan@example.com',
            password: 'password123',
            role: 'REPORTER'
        })

        const [path, options] = fetch.mock.calls[0]

        expect(path).toBe('/api/auth/register')
        expect(options.method).toBe('POST')
        expect(JSON.parse(options.body)).toEqual({
            username: 'jan.kowalski',
            email: 'jan@example.com',
            password: 'password123',
            role: 'REPORTER'
        })
    })

    it('fetches current user resources from the backend', async () => {
        vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ username: 'jan.kowalski' }))

        const result = await fetchMe()
        const [path, options] = fetch.mock.calls[0]

        expect(result).toEqual({ username: 'jan.kowalski' })
        expect(path).toBe('/api/me/resources')
        expect(options.headers.has('Content-Type')).toBe(false)
    })
})
