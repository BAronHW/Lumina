export interface GenericBuffer<T> {
  add: (elem: T) => void;
  flush: () => Promise<void>;
  shutdown: () => Promise<void>;
}
