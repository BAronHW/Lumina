import { Span } from "./span/span";
import { Trace } from "./trace/trace";

export interface LuminaTrace {
    startTrace: () => Trace
    startSpan: () => Span
    startTelemetry: () => void | Error
    endTelemetry: () => void | Error
}