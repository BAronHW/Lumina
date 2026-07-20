import { Span, StartSpanBody } from "./span/span";
import { Trace, StartTraceBody } from "./trace/trace";

export interface LuminaSDK {
    trace: <T, K>(startTraceBody: StartTraceBody<T, K>) => Promise<K>
    span: <T, K>(startSpanBody: StartSpanBody<T, K>) => Promise<K>
    addAttribute: (key: string, value: unknown) => void
    recordError: (message: string) => void
    shutdown: () => Promise<void>
}