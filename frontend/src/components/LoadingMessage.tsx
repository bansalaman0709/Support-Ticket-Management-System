interface Props {
  label?: string
}

export function LoadingMessage({ label = 'Loading…' }: Props) {
  return (
    <p className="loading" role="status">
      {label}
    </p>
  )
}
