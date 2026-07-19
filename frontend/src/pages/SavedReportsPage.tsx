import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Copy, Pin, Star, Trash2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { savedReportApi } from '../api'
import { getProblemMessage } from '../api/client'
import { useToast } from '../components/Toast'
import { Button, EmptyState, ErrorState, formatDate, LoadingState, PageHeader, Panel, StatusBadge } from '../components/ui'
import type { SavedReport } from '../types/api'

export function SavedReportsPage() {
  const client = useQueryClient(); const toast = useToast()
  const query = useQuery({ queryKey: ['saved-reports'], queryFn: savedReportApi.list })
  const refresh = () => client.invalidateQueries({ queryKey: ['saved-reports'] })
  const update = useMutation({ mutationFn: savedReportApi.update, onSuccess: async () => { await refresh(); toast.success('Saved report updated') }, onError: (error) => toast.error(getProblemMessage(error)) })
  const duplicate = useMutation({ mutationFn: savedReportApi.duplicate, onSuccess: async () => { await refresh(); toast.success('Report duplicated') } })
  const remove = useMutation({ mutationFn: savedReportApi.remove, onSuccess: async () => { await refresh(); toast.success('Saved report deleted') } })
  if (query.isLoading) return <LoadingState label="Loading saved reports" />
  if (query.isError) return <ErrorState message={getProblemMessage(query.error)} />
  return <><PageHeader eyebrow="Personal library" title="Saved reports" description="Pin important views, mark favorites, and duplicate a report before tailoring its metadata." />{query.data!.content.length === 0 ? <EmptyState title="Nothing saved yet" description="Open a generated report and save it to build your reporting library." /> : <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">{query.data!.content.map((item) => <SavedCard key={item.id} item={item} update={update.mutate} duplicate={duplicate.mutate} remove={remove.mutate} />)}</div>}</>
}

function SavedCard({ item, update, duplicate, remove }: { item: SavedReport; update: (value: SavedReport) => void; duplicate: (id: number) => void; remove: (id: number) => void }) { return <Panel className="flex flex-col"><div className="flex items-start justify-between gap-3"><StatusBadge>{item.report.reportType.replaceAll('_', ' ')}</StatusBadge><div className="flex gap-1"><button aria-label="Toggle favorite" className={item.favorite ? 'text-amber-500' : 'text-slate-400'} onClick={() => update({ ...item, favorite: !item.favorite })}><Star size={19} fill={item.favorite ? 'currentColor' : 'none'} /></button><button aria-label="Toggle pinned" className={item.pinned ? 'text-brand-700' : 'text-slate-400'} onClick={() => update({ ...item, pinned: !item.pinned })}><Pin size={19} fill={item.pinned ? 'currentColor' : 'none'} /></button></div></div><Link to={`/reports/${item.report.id}`} className="mt-4 text-lg font-bold hover:text-brand-700">{item.title}</Link><p className="mt-2 flex-1 text-sm leading-6 text-ink-600 dark:text-slate-400">{item.description ?? item.report.summary ?? item.report.question}</p><p className="mt-5 text-xs text-ink-600 dark:text-slate-500">Updated {formatDate(item.updatedAt)}</p><div className="mt-4 flex gap-2"><Button variant="secondary" className="px-3" onClick={() => duplicate(item.id)}><Copy size={16} />Duplicate</Button><Button variant="ghost" className="ml-auto px-3 text-red-600" onClick={() => { if (window.confirm('Delete this saved report?')) remove(item.id) }}><Trash2 size={16} /></Button></div></Panel> }
