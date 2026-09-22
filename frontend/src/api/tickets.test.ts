import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  addComment,
  changeTicketStatus,
  createTicket,
  getTicket,
  listTickets,
  updateTicket,
} from './tickets'

describe('tickets API client', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('builds list query params for keyword and status AND semantics', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify([]), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )

    await listTickets({ q: '  printer ', status: 'OPEN' })

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/tickets?q=printer&status=OPEN',
      expect.objectContaining({
        headers: expect.objectContaining({ Accept: 'application/json' }),
      }),
    )
  })

  it('omits blank keyword and blank status from the list URL', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify([]), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )

    await listTickets({ q: '   ', status: '' })

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/tickets',
      expect.any(Object),
    )
  })

  it('calls create, details, update, status, and comment endpoints', async () => {
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            id: 1,
            title: 'A',
            description: null,
            priority: null,
            assignee: null,
            status: 'OPEN',
          }),
          { status: 201, headers: { 'Content-Type': 'application/json' } },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            id: 1,
            title: 'A',
            description: null,
            priority: null,
            assignee: null,
            status: 'OPEN',
            comments: [],
          }),
          { status: 200, headers: { 'Content-Type': 'application/json' } },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            id: 1,
            title: 'B',
            description: null,
            priority: null,
            assignee: 'bob',
            status: 'OPEN',
          }),
          { status: 200, headers: { 'Content-Type': 'application/json' } },
        ),
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            id: 1,
            title: 'B',
            description: null,
            priority: null,
            assignee: 'bob',
            status: 'IN_PROGRESS',
          }),
          { status: 200, headers: { 'Content-Type': 'application/json' } },
        ),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ id: 9, text: 'Hello' }), {
          status: 201,
          headers: { 'Content-Type': 'application/json' },
        }),
      )

    await createTicket({ title: ' A ', description: '', priority: '', assignee: '' })
    await getTicket(1)
    await updateTicket(1, { title: 'B', assignee: 'bob' })
    await changeTicketStatus(1, 'IN_PROGRESS')
    await addComment(1, '  Hello  ')

    expect(fetchMock.mock.calls[0][0]).toBe('/api/tickets')
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ title: 'A' })
    expect(fetchMock.mock.calls[1][0]).toBe('/api/tickets/1')
    expect(fetchMock.mock.calls[2][0]).toBe('/api/tickets/1')
    expect(fetchMock.mock.calls[2][1]?.method).toBe('PATCH')
    expect(fetchMock.mock.calls[3][0]).toBe('/api/tickets/1/status')
    expect(JSON.parse(String(fetchMock.mock.calls[3][1]?.body))).toEqual({
      status: 'IN_PROGRESS',
    })
    expect(fetchMock.mock.calls[4][0]).toBe('/api/tickets/1/comments')
    expect(JSON.parse(String(fetchMock.mock.calls[4][1]?.body))).toEqual({ text: 'Hello' })
  })
})
