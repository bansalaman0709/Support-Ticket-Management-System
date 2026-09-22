import type { TicketStatus } from './types'

/** Status values from spec/state-machine.md */
export const TICKET_STATUSES: TicketStatus[] = [
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
  'CANCELLED',
]

/** Allowed edges from spec/state-machine.md / api-contract.md */
const ALLOWED_TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
  RESOLVED: ['CLOSED'],
  CLOSED: [],
  CANCELLED: [],
}

export function allowedNextStatuses(current: TicketStatus): TicketStatus[] {
  return ALLOWED_TRANSITIONS[current] ?? []
}
