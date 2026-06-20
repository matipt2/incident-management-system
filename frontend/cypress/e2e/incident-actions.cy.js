const baseIncident = {
  id: 'INC-3003',
  title: 'Checkout unavailable',
  description: 'Checkout requests fail.',
  projectId: 'OPS',
  priority: 'HIGH',
  category: 'APPLICATION',
  reportedBy: 'reporter',
  channel: 'FORM',
  updatedAt: '2026-06-19T12:00:00Z'
}

function visitAs(user, incident) {
  cy.intercept('GET', '/api/me/resources', user).as('fetchMe')
  cy.intercept('GET', `/api/my/incidents/${incident.id}`, (req) => req.reply(incident)).as('incident')
  cy.intercept('GET', `/api/my/incidents/${incident.id}/history`, { events: [] }).as('history')

  cy.visit(`/incidents/${incident.id}`, {
    onBeforeLoad(win) {
      win.localStorage.setItem('ims.auth.token', `${user.role.toLowerCase()}-token`)
      win.localStorage.setItem('ims.auth.user', JSON.stringify(user))
    }
  })

  cy.wait('@fetchMe')
  cy.wait('@incident')
  cy.wait('@history')
}

describe('role-aware incident actions', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
  })

  it('allows a manager to assign an incident', () => {
    const manager = {
      userId: 'manager-1',
      username: 'manager',
      role: 'MANAGER',
      permissions: ['INCIDENT_READ', 'INCIDENT_ASSIGN', 'INCIDENT_CLOSE']
    }
    const incident = { ...baseIncident, status: 'NEW', assignedTo: null }

    cy.intercept('GET', '/api/management/agents', [
      { userId: 'agent-1', username: 'support', email: 'support@example.com' }
    ]).as('agents')
    cy.intercept('POST', `/api/management/incidents/${incident.id}/assignment`, (req) => {
      expect(req.body).to.deep.equal({ agentId: 'support' })
      Object.assign(incident, { status: 'IN_PROGRESS', assignedTo: 'support' })
      req.reply(incident)
    }).as('assign')

    visitAs(manager, incident)
    cy.wait('@agents')
    cy.contains('button', 'Assign incident').click()
    cy.wait('@assign')
    cy.contains('IN_PROGRESS').should('be.visible')
    cy.contains('support').should('be.visible')
  })

  it('allows the assigned agent to resolve an incident', () => {
    const agent = {
      userId: 'agent-1',
      username: 'support',
      role: 'AGENT',
      permissions: ['INCIDENT_READ', 'INCIDENT_ESCALATE', 'INCIDENT_RESOLVE']
    }
    const incident = { ...baseIncident, status: 'IN_PROGRESS', assignedTo: 'support' }

    cy.intercept('POST', `/api/incidents/${incident.id}/resolve?*`, (req) => {
      expect(req.url).to.include('resolution=Restarted+checkout')
      Object.assign(incident, { status: 'RESOLVED' })
      req.reply(incident)
    }).as('resolve')

    visitAs(agent, incident)
    cy.get('textarea[placeholder*="resolution"]').type('Restarted checkout')
    cy.contains('button', 'Resolve').click()
    cy.wait('@resolve')
    cy.contains('RESOLVED').should('be.visible')
  })

  it('allows the assigned agent to classify an incident', () => {
    const agent = {
      userId: 'agent-1',
      username: 'support',
      role: 'AGENT',
      permissions: ['INCIDENT_READ', 'INCIDENT_CLASSIFY']
    }
    const incident = { ...baseIncident, priority: null, category: null, status: 'IN_PROGRESS', assignedTo: 'support' }

    cy.intercept('POST', `/api/incidents/${incident.id}/classify`, (req) => {
      expect(req.body).to.deep.equal({ priority: 'CRITICAL', category: 'SECURITY' })
      Object.assign(incident, req.body)
      req.reply(incident)
    }).as('classify')

    visitAs(agent, incident)
    cy.contains('Classification').parents('.workflow-card').within(() => {
      cy.get('select').eq(0).select('CRITICAL')
      cy.get('select').eq(1).select('SECURITY')
      cy.contains('button', 'Apply classification').click()
    })
    cy.wait('@classify')
    cy.contains('.priority-badge', 'CRITICAL').should('be.visible')
    cy.contains('SECURITY').should('be.visible')
  })

  it('keeps viewer access read-only', () => {
    const viewer = {
      userId: 'viewer-1',
      username: 'auditor',
      role: 'VIEWER',
      permissions: ['INCIDENT_READ', 'PROJECT_READ']
    }
    const incident = { ...baseIncident, status: 'IN_PROGRESS', assignedTo: 'support' }

    visitAs(viewer, incident)
    cy.contains('Checkout unavailable').should('be.visible')
    cy.contains('Report incident').should('not.exist')
    cy.contains('Incident actions').should('not.exist')
    cy.contains('Manager assignment').should('not.exist')
  })

  it('allows a manager to close a resolved incident', () => {
    const manager = {
      userId: 'manager-1',
      username: 'manager',
      role: 'MANAGER',
      permissions: ['INCIDENT_READ', 'INCIDENT_ASSIGN', 'INCIDENT_CLOSE']
    }
    const incident = { ...baseIncident, status: 'RESOLVED', assignedTo: 'support' }

    cy.intercept('POST', `/api/incidents/${incident.id}/close`, (req) => {
      Object.assign(incident, { status: 'CLOSED' })
      req.reply(incident)
    }).as('close')

    visitAs(manager, incident)
    cy.on('window:confirm', () => true)
    cy.contains('button', 'Close incident').click()
    cy.wait('@close')
    cy.contains('CLOSED').should('be.visible')
  })

  it('requires and approves a post-mortem before critical closure', () => {
    const manager = {
      userId: 'manager-1',
      username: 'manager',
      role: 'MANAGER',
      permissions: [
        'INCIDENT_READ', 'INCIDENT_CLOSE', 'POSTMORTEM_READ',
        'POSTMORTEM_WRITE', 'POSTMORTEM_APPROVE'
      ]
    }
    const incident = { ...baseIncident, priority: 'CRITICAL', status: 'RESOLVED', assignedTo: 'support' }
    let report = null

    cy.intercept('GET', `/api/incidents/${incident.id}/post-mortem`, (req) => {
      if (report) req.reply(report)
      else req.reply({ statusCode: 404, body: { message: 'Not found' } })
    }).as('postMortem')
    cy.intercept('POST', `/api/incidents/${incident.id}/post-mortem`, (req) => {
      report = { id: 'PM-1', incidentId: incident.id, status: 'DRAFT' }
      req.reply({ statusCode: 201, body: report })
    }).as('createPostMortem')
    cy.intercept('PUT', `/api/incidents/${incident.id}/post-mortem`, (req) => {
      report = { ...report, ...req.body, status: 'DRAFT' }
      req.reply(report)
    }).as('updatePostMortem')
    cy.intercept('POST', `/api/incidents/${incident.id}/post-mortem/approve`, (req) => {
      report = { ...report, status: 'APPROVED' }
      req.reply(report)
    }).as('approvePostMortem')
    cy.intercept('POST', `/api/incidents/${incident.id}/close`, (req) => {
      Object.assign(incident, { status: 'CLOSED' })
      req.reply(incident)
    }).as('close')

    visitAs(manager, incident)
    cy.wait('@postMortem')
    cy.contains('button', 'Close incident').should('be.disabled')
    cy.contains('Post-mortem').parents('.workflow-card').within(() => {
      cy.get('textarea').eq(0).type('Invalid production configuration')
      cy.get('textarea').eq(1).type('12:00 deployment, 12:05 outage')
      cy.get('textarea').eq(2).type('Checkout unavailable')
      cy.get('textarea').eq(3).type('Add deployment validation')
      cy.contains('button', 'Create post-mortem').click()
    })
    cy.wait('@createPostMortem')
    cy.wait('@updatePostMortem')
    cy.contains('button', 'Approve post-mortem').click()
    cy.wait('@approvePostMortem')
    cy.on('window:confirm', () => true)
    cy.contains('button', 'Close incident').should('not.be.disabled').click()
    cy.wait('@close')
    cy.contains('CLOSED').should('be.visible')
  })
})
