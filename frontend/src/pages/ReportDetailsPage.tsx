import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Download, Play, RefreshCcw, Save } from 'lucide-react'
import { useParams } from 'react-router-dom'
import { reportApi } from '../api'
import { downloadFile, getProblemMessage } from '../api/client'
import { ReportView } from '../components/ReportView'
import { useToast } from '../components/Toast'
import { Button, ErrorState, formatDate, LoadingState, PageHeader, Panel, StatusBadge } from '../components/ui'

export function ReportDetailsPage() {
  const id = Number(useParams().id)
  const client = useQueryClient(); const toast = useToast()
  const query = useQuery({ queryKey: ['report', id], queryFn: () => reportApi.get(id), enabled: Number.isFinite(id) })
  const rerun = useMutation({ mutationFn: () => reportApi.rerun(id), onSuccess: (report) => { client.setQueryData(['report', id], report); toast.success('Report rerun completed') }, onError: (error) => toast.error(getProblemMessage(error)) })
  const regenerate = useMutation({ mutationFn: () => reportApi.regenerate(id), onSuccess: (report) => { client.setQueryData(['report', id], report); toast.success('AI explanation regenerated') }, onError: (error) => toast.error(getProblemMessage(error)) })
  if (query.isLoading) return <LoadingState label="Loading report" />
  if (query.isError || !query.data) return <ErrorState message={getProblemMessage(query.error)} />
  const report = query.data
  return <><PageHeader eyebrow="Generated report" title={report.result?.title ?? report.question} description={`Created ${formatDate(report.createdAt)} · ${report.executionTimeMs} ms`} actions={<><Button variant="secondary" onClick={() => rerun.mutate()} disabled={rerun.isPending}><Play size={16} />Rerun</Button><Button variant="secondary" onClick={() => regenerate.mutate()} disabled={regenerate.isPending}><RefreshCcw size={16} />Regenerate summary</Button><Button onClick={async () => { await reportApi.save(id, report.result?.title ?? 'Saved report'); toast.success('Report saved') }}><Save size={16} />Save</Button></>} />
    <Panel className="mb-5"><div className="flex flex-wrap gap-2"><StatusBadge tone={report.status === 'SUCCEEDED' ? 'success' : 'danger'}>{report.status}</StatusBadge><StatusBadge>{report.reportType.replaceAll('_', ' ')}</StatusBadge><StatusBadge tone="ai">{report.aiProvider ?? 'Deterministic fallback'} / {report.aiModel ?? 'local'}</StatusBadge></div><p className="mt-4 text-sm"><strong>Question:</strong> {report.question}</p><details className="mt-4"><summary className="cursor-pointer text-sm font-semibold text-brand-700">View interpreted intent</summary><pre className="mt-3 overflow-x-auto rounded-xl bg-slate-950 p-4 text-xs text-emerald-200">{JSON.stringify(report.intent, null, 2)}</pre></details><div className="mt-4 flex gap-2"><Button variant="secondary" onClick={() => void downloadFile(`/reports/${id}/export/csv`)}><Download size={16} />CSV</Button><Button variant="secondary" onClick={() => void downloadFile(`/reports/${id}/export/pdf`)}><Download size={16} />PDF</Button></div></Panel><ReportView report={report} />
  </>
}
