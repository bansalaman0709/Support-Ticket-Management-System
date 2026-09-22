export type TicketStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED'

export interface Comment {
  id: number
  text: string
}

export interface Ticket {
  id: number
  title: string
  description: string | null
  priority: string | null
  assignee: string | null
  status: TicketStatus
}

export interface TicketDetails extends Ticket {
  comments: Comment[]
}

export interface FieldError {
  field?: string
  message: string
}

export interface ApiErrorBody {
  message: string
  errors?: FieldError[]
}

export class ApiError extends Error {
  readonly status: number
  readonly body: ApiErrorBody

  constructor(status: number, body: ApiErrorBody) {
    super(body.message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export interface CreateTicketInput {
  title: string
  description?: string
  priority?: string
  assignee?: string
}

export interface UpdateTicketInput {
  title?: string
  description?: string
  priority?: string
  assignee?: string
}
