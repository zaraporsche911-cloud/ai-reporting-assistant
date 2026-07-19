import { LoaderCircle, TriangleAlert } from 'lucide-react'
import type { ButtonHTMLAttributes, ReactNode } from 'react'
import clsx from 'clsx'

export function Panel({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <section className={clsx('rounded-2xl border border-line bg-panel p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900', className)}>{children}</section>
}

export function PageHeader({ eyebrow, title, description, actions }: { eyebrow?: string; title: string; description?: string; actions?: ReactNode }) {
  return <header className="mb-7 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
    <div>
      {eyebrow && <p className="mb-2 text-xs font-bold uppercase tracking-[.18em] text-brand-700 dark:text-emerald-400">{eyebrow}</p>}
      <h1 className="text-3xl font-bold tracking-tight text-ink-950 dark:text-white">{title}</h1>
      {description && <p className="mt-2 max-w-3xl text-sm leading-6 text-ink-600 dark:text-slate-400">{description}</p>}
    </div>
    {actions && <div className="flex shrink-0 flex-wrap gap-2">{actions}</div>}
  </header>
}

export function Button({ className, variant = 'primary', ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' | 'danger' | 'ghost' }) {
  const styles = {
    primary: 'bg-brand-700 text-white hover:bg-brand-800 disabled:bg-slate-400',
    secondary: 'border border-line bg-white text-ink-900 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800',
    danger: 'bg-red-600 text-white hover:bg-red-700',
    ghost: 'text-ink-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800',
  }
  return <button className={clsx('focus-ring inline-flex min-h-10 items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-70', styles[variant], className)} {...props} />
}

export function Field({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return <label className="block text-sm font-semibold text-ink-900 dark:text-slate-200">
    {label}
    <span className="mt-2 block">{children}</span>
    {error && <span className="mt-1 block text-xs font-medium text-red-600">{error}</span>}
  </label>
}

export const inputClass = 'focus-ring w-full rounded-xl border px-3.5 py-2.5 text-sm outline-none transition focus:border-brand-600'

export function LoadingState({ label = 'Loading' }: { label?: string }) {
  return <div className="flex min-h-48 items-center justify-center gap-3 text-sm text-ink-600 dark:text-slate-400"><LoaderCircle className="animate-spin" size={20} />{label}…</div>
}

export function ErrorState({ message, retry }: { message: string; retry?: () => void }) {
  return <div role="alert" className="rounded-2xl border border-red-200 bg-red-50 p-5 text-red-800 dark:border-red-900 dark:bg-red-950/40 dark:text-red-200">
    <div className="flex gap-3"><TriangleAlert className="shrink-0" size={20} /><div><p className="font-semibold">We couldn't complete that request</p><p className="mt-1 text-sm">{message}</p>{retry && <Button className="mt-4" variant="secondary" onClick={retry}>Try again</Button>}</div></div>
  </div>
}

export function EmptyState({ title, description, action }: { title: string; description: string; action?: ReactNode }) {
  return <div className="rounded-2xl border border-dashed border-line p-10 text-center dark:border-slate-700"><p className="font-semibold">{title}</p><p className="mx-auto mt-2 max-w-lg text-sm text-ink-600 dark:text-slate-400">{description}</p>{action && <div className="mt-5">{action}</div>}</div>
}

export function StatusBadge({ tone = 'neutral', children }: { tone?: 'success' | 'warning' | 'danger' | 'ai' | 'neutral'; children: ReactNode }) {
  const styles = { success: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300', warning: 'bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300', danger: 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-300', ai: 'bg-ai-100 text-ai-700 dark:bg-violet-950 dark:text-violet-300', neutral: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300' }
  return <span className={clsx('inline-flex rounded-full px-2.5 py-1 text-xs font-bold', styles[tone])}>{children}</span>
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}
