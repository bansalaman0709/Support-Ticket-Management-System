import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createTicket } from '../api/tickets'
import { ErrorBanner } from '../components/ErrorBanner'

export function CreateTicketPage() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState('')
  const [assignee, setAssignee] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<unknown>(null)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)

    if (!title.trim()) {
      setError(new Error('Title is required.'))
      return
    }
    if (title.trim().length > 200) {
      setError(new Error('Title must be at most 200 characters.'))
      return
    }

    setSubmitting(true)
    try {
      const created = await createTicket({
        title,
        description,
        priority,
        assignee,
      })
      navigate(`/tickets/${created.id}`)
    } catch (err) {
      setError(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="page">
      <header className="page-header">
        <h1>Create ticket</h1>
        <Link to="/">Back to list</Link>
      </header>

      <ErrorBanner error={error} title="Could not create ticket" />

      <form className="stack-form" onSubmit={onSubmit}>
        <label>
          Title <span className="required">*</span>
          <input
            name="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            maxLength={200}
            required
            aria-required="true"
          />
        </label>
        <label>
          Description
          <textarea
            name="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            maxLength={5000}
            rows={5}
          />
        </label>
        <label>
          Priority
          <input
            name="priority"
            value={priority}
            onChange={(e) => setPriority(e.target.value)}
            maxLength={50}
          />
        </label>
        <label>
          Assignee
          <input
            name="assignee"
            value={assignee}
            onChange={(e) => setAssignee(e.target.value)}
            maxLength={100}
          />
        </label>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Creating…' : 'Create ticket'}
        </button>
      </form>
    </section>
  )
}
