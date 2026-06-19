const manager = {
  userId: 'manager-1',
  username: 'manager',
  role: 'MANAGER',
  permissions: ['INCIDENT_READ', 'SLA_READ', 'SLA_WRITE', 'PROJECT_READ']
}

describe('SLA administration', () => {
  beforeEach(() => {
    cy.clearLocalStorage()
    cy.intercept('GET', '/api/me/resources', manager).as('fetchMe')
    cy.intercept('GET', '/api/projects', [
      { key: 'OPS', name: 'Operations', active: true }
    ]).as('projects')
  })

  function visitSla() {
    cy.visit('/sla', {
      onBeforeLoad(win) {
        win.localStorage.setItem('ims.auth.token', 'manager-token')
        win.localStorage.setItem('ims.auth.user', JSON.stringify(manager))
      }
    })
  }

  it('creates a policy and applies a violation penalty', () => {
    let policies = []
    let violations = [{
      id: 8,
      incidentId: 'INC-SLA-1',
      projectId: 'OPS',
      violationType: 'RESOLUTION_TIME',
      detectedAt: '2026-06-20T08:00:00Z',
      penalty: 250,
      penaltyApplied: false
    }]

    cy.intercept('GET', '/api/sla/policies', (req) => req.reply(policies)).as('policies')
    cy.intercept('GET', '/api/sla/violations*', (req) => req.reply(violations)).as('violations')
    cy.intercept('POST', '/api/sla/policies', (req) => {
      expect(req.body).to.deep.equal({
        projectId: 'OPS',
        priority: 'HIGH',
        responseTimeMinutes: 15,
        resolutionTimeMinutes: 60,
        penaltyAmount: 250
      })
      policies = [{ id: 1, ...req.body }]
      req.reply({ statusCode: 201, body: policies[0] })
    }).as('createPolicy')
    cy.intercept('POST', '/api/sla/check', []).as('checkSla')
    cy.intercept('POST', '/api/sla/violations/8/apply-penalty', (req) => {
      violations = violations.map(item => ({ ...item, penaltyApplied: true }))
      req.reply(violations[0])
    }).as('applyPenalty')

    visitSla()
    cy.wait('@fetchMe')
    cy.wait('@policies')
    cy.wait('@violations')
    cy.wait('@projects')

    cy.get('.admin-form').within(() => {
      cy.get('select').eq(1).select('HIGH')
      cy.get('input').eq(0).clear().type('15')
      cy.get('input').eq(1).clear().type('60')
      cy.get('input').eq(2).clear().type('250')
      cy.contains('button', 'Save policy').click()
    })
    cy.wait('@createPolicy')
    cy.contains('.priority-badge', 'HIGH').should('be.visible')

    cy.contains('button', 'Run SLA check').click()
    cy.wait('@checkSla')

    cy.on('window:confirm', () => true)
    cy.contains('button', 'Apply penalty').click()
    cy.wait('@applyPenalty')
    cy.contains('APPLIED').should('be.visible')
  })
})
