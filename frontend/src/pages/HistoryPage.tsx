import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { reportApi } from '../api'
import { getProblemMessage } from '../api/client'
import { EmptyState, ErrorState, formatDate, LoadingState, PageHeader, Panel, StatusBadge } from '../components/ui'

export function HistoryPage() {
  const query = useQuery({ queryKey: ['reports'], queryFn: () => reportApi.list('size=100') })
  if (query.isLoading) return <LoadingState label="Loading report history" />
  if (query.isError) return <ErrorState message={getProblemMessage(query.error)} />
  return <><PageHeader eyebrow="Traceability" title="Report history" description="Every execution records the question, interpreted intent, provider metadata, duration, and outcome." />{query.data!.content.length === 0 ? <EmptyState title="No report history" description="Generated reports will appear here." /> : <Panel className="overflow-hidden p-0"><div className="overflow-x-auto"><table className="w-full min-w-200 text-left text-sm"><thead className="bg-slate-50 text-xs uppercase text-ink-600 dark:bg-slate-800 dark:text-slate-400"><tr><th className="px-5 py-4">Report</th><th className="px-5 py-4">Type</th><th className="px-5 py-4">Status</th><th className="px-5 py-4">AI provider</th><th className="px-5 py-4">Duration</th><th className="px-5 py-4">Created</th></tr></thead><tbody className="divide-y divide-line dark:divide-slate-800">{query.data!.content.map((report) => <tr key={report.id} className="hover:bg-slate-50 dark:hover:bg-slate-800"><td className="max-w-md px-5 py-4"><Link className="font-semibold text-brand-700 dark:text-emerald-400" to={`/reports/${report.id}`}>{report.result?.title ?? report.question}</Link><p className="mt-1 truncate text-xs text-ink-600 dark:text-slate-400">{report.question}</p></td><td className="px-5 py-4">{report.reportType.replaceAll('_', ' ')}</td><td className="px-5 py-4"><StatusBadge tone={report.status === 'SUCCEEDED' ? 'success' : 'danger'}>{report.status}</StatusBadge></td><td className="px-5 py-4">{report.aiProvider ?? 'Fallback'}</td><td className="px-5 py-4">{report.executionTimeMs} ms</td><td className="px-5 py-4">{formatDate(report.createdAt)}</td></tr>)}</tbody></table></div></Panel>}</>
}
