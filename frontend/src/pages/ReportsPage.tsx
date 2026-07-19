import { useQuery } from '@tanstack/react-query'
import { BarChart3, Bot, CheckCircle2, LockKeyhole } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { reportApi } from '../api'
import { getProblemMessage } from '../api/client'
import { Button, ErrorState, LoadingState, PageHeader, Panel, StatusBadge } from '../components/ui'

export function ReportsPage() {
  const navigate = useNavigate()
  const query = useQuery({ queryKey: ['catalogue'], queryFn: reportApi.catalogue })
  if (query.isLoading) return <LoadingState label="Loading report catalogue" />
  if (query.isError) return <ErrorState message={getProblemMessage(query.error)} />
  return <><PageHeader eyebrow="Report catalogue" title="Operational questions we can answer" description="Each template maps to typed intent, capability validation, deterministic calculations, and an appropriate visualization." />
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">{query.data!.map((report) => <Panel key={report.type} className="flex flex-col"><div className="flex items-start justify-between gap-3"><div className="grid size-11 place-items-center rounded-xl bg-brand-100 text-brand-700 dark:bg-emerald-950 dark:text-emerald-300"><BarChart3 /></div><StatusBadge tone={report.available ? 'success' : 'warning'}>{report.available ? 'Available' : 'Unavailable in current mode'}</StatusBadge></div><h2 className="mt-5 text-lg font-bold">{report.displayName}</h2><p className="mt-2 flex-1 text-sm leading-6 text-ink-600 dark:text-slate-400">{report.description}</p><div className="mt-4 flex flex-wrap gap-2">{report.requiredCapabilities.map((capability) => <StatusBadge key={capability}>{capability.replaceAll('_', ' ')}</StatusBadge>)}</div><p className="mt-4 flex items-center gap-2 text-xs text-ink-600 dark:text-slate-400">{report.available ? <CheckCircle2 size={15} /> : <LockKeyhole size={15} />}{report.availabilityMessage}</p><Button className="mt-5" disabled={!report.available} onClick={() => navigate('/assistant', { state: { question: `Generate a ${report.displayName.toLowerCase()} report` } })}><Bot size={17} />Ask assistant</Button></Panel>)}</div>
  </>
}
