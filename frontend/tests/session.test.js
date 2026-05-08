import { beforeEach, describe, expect, it } from 'vitest'
import { clearSession, getStoredToken, getStoredUser, persistSession } from '../src/services/session'

describe('session storage', () => {
    beforeEach(() => {
        localStorage.clear()
    })

    it('persists and reads auth token with user profile', () => {
        const user = {
            userId: 'user-1',
            username: 'jan.kowalski',
            role: 'REPORTER',
            permissions: ['INCIDENT_REPORT']
        }

        persistSession('jwt-token', user)

        expect(getStoredToken()).toBe('jwt-token')
        expect(getStoredUser()).toEqual(user)
    })

    it('clears persisted auth data', () => {
        persistSession('jwt-token', { username: 'jan.kowalski' })

        clearSession()

        expect(getStoredToken()).toBeNull()
        expect(getStoredUser()).toBeNull()
    })

    it('returns null for malformed stored user data', () => {
        localStorage.setItem('ims.auth.user', '{invalid-json')

        expect(getStoredUser()).toBeNull()
    })
})
