import { Span, StartSpanBody } from "./span/span";
import { Trace, StartTraceBody } from "./trace/trace";

export interface LuminaSDK {
    session: <T>(name: string, callback: () => Promise<T>) => Promise<T>
    trace: <T>(startTraceBody: StartTraceBody<T>) => Promise<Trace>
    span: <T>(startSpanBody: StartSpanBody<T>) => Promise<T>
    addAttribute: (key: string, value: unknown) => void
    recordError: (message: string) => void
    shutdown: () => Promise<void>
}