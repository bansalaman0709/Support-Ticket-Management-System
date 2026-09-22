import { apiRequest } from './client'
import type {
  Comment,
  CreateTicketInput,
  Ticket,
  TicketDetails,
  TicketStatus,
  UpdateTicketInput,
} from '../types'

export async function listTickets(params: {
  q?: string
  status?: TicketStatus | ''
}): Promise<Ticket[]> {
  const search = new URLSearchParams()
  const q = params.q?.trim()
  if (q) {
    search.set('q', q)
  }
  if (params.status) {
    search.set('status', params.status)
  }
  const query = search.toString()
  return apiRequest<Ticket[]>(`/api/tickets${query ? `?${query}` : ''}`)
}

export async function getTicket(id: number): Promise<TicketDetails> {
  return apiRequest<TicketDetails>(`/api/tickets/${id}`)
}

export async function createTicket(input: CreateTicketInput): Promise<Ticket> {
  const body: CreateTicketInput = {
    title: input.title.trim(),
  }
  if (input.description?.trim()) {
    body.description = input.description.trim()
  }
  if (input.priority?.trim()) {
    body.priority = input.priority.trim()
  }
  if (input.assignee?.trim()) {
    body.assignee = input.assignee.trim()
  }
  return apiRequest<Ticket>('/api/tickets', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function updateTicket(
  id: number,
  input: UpdateTicketInput,
): Promise<Ticket> {
  return apiRequest<Ticket>(`/api/tickets/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(input),
  })
}

export async function changeTicketStatus(
  id: number,
  status: TicketStatus,
): Promise<Ticket> {
  return apiRequest<Ticket>(`/api/tickets/${id}/status`, {
    method: 'POST',
    body: JSON.stringify({ status }),
  })
}

export async function addComment(id: number, text: string): Promise<Comment> {
  return apiRequest<Comment>(`/api/tickets/${id}/comments`, {
    method: 'POST',
    body: JSON.stringify({ text: text.trim() }),
  })
}
