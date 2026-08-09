import { create } from "zustand";
import type { Trace } from "@lumina/sdk";

interface TraceStore {
  traces: Trace[]
}

const useTrace = create<TraceStore>((set, get) => ({
  traces: [],
  removeTraces: (tracesToRemove: Trace[]) => set({ traces: get().traces.filter(trace => !tracesToRemove.includes(trace)) }),
  updateTraces: (newTraces: Trace[]) => set({ traces: newTraces }),
  removeAllTraces: () => set({ traces: [] })
}))

export default useTrace