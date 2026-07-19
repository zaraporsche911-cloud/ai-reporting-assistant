import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { adminApi } from '../api'
import { getProblemMessage } from '../api/client'
import { useToast } from '../components/Toast'
import { Button, ErrorState, LoadingState, PageHeader, Panel, StatusBadge, inputClass } from '../components/ui'
import type { PromptTemplate } from '../types/api'

export function SystemStatusPage() {
  const client = useQueryClient(); const toast = useToast()
  const status = useQuery({ queryKey: ['system-status'], queryFn: adminApi.status })
  const prompts = useQuery({ queryKey: ['prompts'], queryFn: adminApi.prompts })
  const change = useMutation({ mutationFn: adminApi.changeProvider, onSuccess: async () => { await client.invalidateQueries({ queryKey: ['system-status'] }); toast.success('Active AI provider changed') }, onError: (error) => toast.error(getProblemMessage(error)) })
  if (status.isLoading) return <LoadingState label="Checking system status" />
  if (status.isError || !status.data) return <ErrorState message={getProblemMessage(status.error)} />
  const data = status.data
  return <><PageHeader eyebrow="Administration" title="System status" description="A sanitized operational view. Secrets never cross this API boundary." />
    <div className="grid gap-4 md:grid-cols-3"><Health label="PostgreSQL" available={data.databaseAvailable} detail="Application-owned reporting schema" /><Health label="Fleet integration" available={data.fleetAvailable} detail={`${data.fleetMode}: ${data.fleetMessage}`} /><Health label="AI routing" available detail={`${data.activeAiProvider} / ${data.configuredModel}`} /></div>
    <Panel className="mt-6"><h2 className="text-lg font-bold">AI providers</h2><p className="mt-1 text-sm text-ink-600 dark:text-slate-400">Switching changes interpretation and narrative generation. Reporting calculations remain provider-independent.</p><div className="mt-5 grid gap-3 md:grid-cols-2">{data.providers.map((provider) => <div key={provider.id} className="flex items-center justify-between gap-4 rounded-xl border border-line p-4 dark:border-slate-700"><div><div className="flex gap-2"><p className="font-semibold uppercase">{provider.id}</p>{provider.active && <StatusBadge tone="ai">Active</StatusBadge>}</div><p className="mt-1 text-xs text-ink-600 dark:text-slate-400">{provider.model} · {provider.message}</p></div><Button variant="secondary" disabled={!provider.available || provider.active || change.isPending} onClick={() => change.mutate(provider.id)}>Activate</Button></div>)}</div></Panel>
    <Panel className="mt-6"><h2 className="text-lg font-bold">Prompt templates</h2><p className="mt-1 text-sm text-ink-600 dark:text-slate-400">Versioned application prompts are editable; provider credentials remain environment-only.</p><div className="mt-5 space-y-4">{prompts.data?.map((prompt) => <PromptEditor key={prompt.id} prompt={prompt} />)}</div></Panel>
  </>
}

function Health({ label, available, detail }: { label: string; available: boolean; detail: string }) { return <Panel><div className="flex items-center justify-between"><h2 className="font-bold">{label}</h2><StatusBadge tone={available ? 'success' : 'danger'}>{available ? 'Healthy' : 'Unavailable'}</StatusBadge></div><p className="mt-3 text-sm text-ink-600 dark:text-slate-400">{detail}</p></Panel> }
function PromptEditor({ prompt }: { prompt: PromptTemplate }) { const [draft, setDraft] = useState(prompt); const toast = useToast(); const client = useQueryClient(); const mutation = useMutation({ mutationFn: () => adminApi.updatePrompt(draft), onSuccess: async () => { await client.invalidateQueries({ queryKey: ['prompts'] }); toast.success(`${draft.displayName} updated`) }, onError: (error) => toast.error(getProblemMessage(error)) }); return <details className="rounded-xl border border-line p-4 dark:border-slate-700"><summary className="cursor-pointer font-semibold">{prompt.displayName} <span className="ml-2 font-mono text-xs text-ink-600">{prompt.key}</span></summary><div className="mt-4 space-y-3"><input aria-label="Prompt display name" className={inputClass} value={draft.displayName} onChange={(event) => setDraft({ ...draft, displayName: event.target.value })} /><textarea aria-label="Prompt content" className={`${inputClass} min-h-48 font-mono text-xs`} value={draft.content} onChange={(event) => setDraft({ ...draft, content: event.target.value })} /><label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={draft.enabled} onChange={(event) => setDraft({ ...draft, enabled: event.target.checked })} />Enabled</label><Button disabled={mutation.isPending} onClick={() => mutation.mutate()}>Save template</Button></div></details> }
