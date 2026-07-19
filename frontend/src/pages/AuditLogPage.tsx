import { useQuery } from '@tanstack/react-query'
import { adminApi } from '../api'
import { getProblemMessage } from '../api/client'
import { ErrorState, formatDate, LoadingState, PageHeader, Panel, StatusBadge } from '../components/ui'

export function AuditLogPage() {
  const query = useQuery({ queryKey: ['audit'], queryFn: adminApi.audit })
  if (query.isLoading) return <LoadingState label="Loading audit log" />
  if (query.isError) return <ErrorState message={getProblemMessage(query.error)} />
  return <><PageHeader eyebrow="Administration" title="Audit log" description="Security- and configuration-sensitive actions are retained with actor, resource, timestamp, and correlation context." /><Panel className="overflow-hidden p-0"><div className="overflow-x-auto"><table className="w-full min-w-240 text-left text-sm"><thead className="bg-slate-50 text-xs uppercase text-ink-600 dark:bg-slate-800 dark:text-slate-400"><tr><th className="px-5 py-4">Time</th><th className="px-5 py-4">Actor</th><th className="px-5 py-4">Action</th><th className="px-5 py-4">Resource</th><th className="px-5 py-4">Detail</th><th className="px-5 py-4">Correlation</th></tr></thead><tbody className="divide-y divide-line dark:divide-slate-800">{query.data!.content.map((entry) => <tr key={entry.id}><td className="whitespace-nowrap px-5 py-4">{formatDate(entry.createdAt)}</td><td className="px-5 py-4">{entry.actorEmail ?? 'System'}</td><td className="px-5 py-4"><StatusBadge>{entry.action.replaceAll('_', ' ')}</StatusBadge></td><td className="px-5 py-4">{entry.resourceType}{entry.resourceId ? ` #${entry.resourceId}` : ''}</td><td className="max-w-lg px-5 py-4 text-ink-600 dark:text-slate-400">{entry.detail ?? '—'}</td><td className="px-5 py-4 font-mono text-xs">{entry.correlationId ?? '—'}</td></tr>)}</tbody></table></div></Panel></>
}
