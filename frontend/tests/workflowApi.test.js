import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createProject, fetchProjects, setProjectStatus, updateProject } from '../src/services/projectsApi'
import { fetchUsers, updateUserRole } from '../src/services/usersApi'
import { classifyIncident, closeIncident, escalateIncident, resolveIncident } from '../src/services/incidentsApi'
import {
    approvePostMortem,
    createPostMortem,
    fetchPostMortem,
    updatePostMortem
} from '../src/services/postMortemApi'
import {
    applySlaPenalty,
    createSlaPolicy,
    fetchIncidentSlaViolations,
    fetchSlaPolicies,
    fetchSlaViolations,
    runSlaCheck,
    updateSlaPolicy
} from '../src/services/slaApi'

function jsonResponse(payload) {
    return new Response(JSON.stringify(payload), {
        status: 200,
        headers: { 'Content-Type': 'application/json' }
    })
}

describe('project, user, and lifecycle APIs', () => {
    beforeEach(() => {
        localStorage.clear()
        vi.restoreAllMocks()
        vi.spyOn(globalThis, 'fetch').mockImplementation(
            async () => jsonResponse({ ok: true })
        )
    })

    it('uses project management endpoints', async () => {
        await fetchProjects(true)
        await createProject({ key: 'OPS', name: 'Operations' })
        await updateProject('OPS', { name: 'Core Operations' })
        await setProjectStatus('OPS', false)

        expect(fetch.mock.calls[0][0]).toBe('/api/projects?includeInactive=true')
        expect(fetch.mock.calls[1][0]).toBe('/api/projects')
        expect(fetch.mock.calls[1][1].method).toBe('POST')
        expect(fetch.mock.calls[2][0]).toBe('/api/projects/OPS')
        expect(fetch.mock.calls[2][1].method).toBe('PUT')
        expect(fetch.mock.calls[3][0]).toBe('/api/projects/OPS/status')
        expect(JSON.parse(fetch.mock.calls[3][1].body)).toEqual({ active: false })
    })

    it('uses protected user role endpoints', async () => {
        await fetchUsers()
        await updateUserRole('user-1', 'AGENT')

        expect(fetch.mock.calls[0][0]).toBe('/api/management/users')
        expect(fetch.mock.calls[1][0]).toBe('/api/management/users/user-1/role')
        expect(JSON.parse(fetch.mock.calls[1][1].body)).toEqual({ role: 'AGENT' })
    })

    it('encodes lifecycle action details', async () => {
        await classifyIncident('INC-1', { priority: 'HIGH', category: 'APPLICATION' })
        await escalateIncident('INC-1', 'Needs attention')
        await resolveIncident('INC-1', 'Restarted service')
        await closeIncident('INC-1')

        expect(fetch.mock.calls[0][0]).toBe('/api/incidents/INC-1/classify')
        expect(JSON.parse(fetch.mock.calls[0][1].body)).toEqual({
            priority: 'HIGH',
            category: 'APPLICATION'
        })
        expect(fetch.mock.calls[1][0]).toBe('/api/incidents/INC-1/escalate?reason=Needs+attention')
        expect(fetch.mock.calls[2][0]).toBe('/api/incidents/INC-1/resolve?resolution=Restarted+service')
        expect(fetch.mock.calls[3][0]).toBe('/api/incidents/INC-1/close')
    })

    it('uses post-mortem workflow endpoints', async () => {
        const payload = {
            rootCause: 'Configuration error',
            timeline: '12:00 outage',
            impact: 'Checkout unavailable',
            actionItems: 'Add validation'
        }

        await fetchPostMortem('INC-2')
        await createPostMortem('INC-2')
        await updatePostMortem('INC-2', payload)
        await approvePostMortem('INC-2')

        expect(fetch.mock.calls.map(call => call[0])).toEqual([
            '/api/incidents/INC-2/post-mortem',
            '/api/incidents/INC-2/post-mortem',
            '/api/incidents/INC-2/post-mortem',
            '/api/incidents/INC-2/post-mortem/approve'
        ])
        expect(fetch.mock.calls[1][1].method).toBe('POST')
        expect(fetch.mock.calls[2][1].method).toBe('PUT')
        expect(JSON.parse(fetch.mock.calls[2][1].body)).toEqual(payload)
    })

    it('uses SLA policy and violation endpoints', async () => {
        const policy = {
            projectId: 'OPS',
            priority: 'HIGH',
            responseTimeMinutes: 15,
            resolutionTimeMinutes: 60,
            penaltyAmount: 100
        }

        await fetchSlaPolicies()
        await createSlaPolicy(policy)
        await updateSlaPolicy(4, policy)
        await fetchSlaViolations('OPS')
        await fetchIncidentSlaViolations('INC-3')
        await runSlaCheck()
        await applySlaPenalty(8)

        expect(fetch.mock.calls.map(call => call[0])).toEqual([
            '/api/sla/policies',
            '/api/sla/policies',
            '/api/sla/policies/4',
            '/api/sla/violations?projectId=OPS',
            '/api/sla/violations/INC-3',
            '/api/sla/check',
            '/api/sla/violations/8/apply-penalty'
        ])
    })
})
