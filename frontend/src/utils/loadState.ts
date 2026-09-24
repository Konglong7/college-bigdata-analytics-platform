import { ref, type Ref } from 'vue'

export type LoadState = 'idle' | 'loading' | 'success' | 'empty' | 'error'

export interface LoadStateController {
  state: Ref<LoadState>
  errorMessage: Ref<string>
  start: () => void
  succeed: (empty?: boolean) => void
  fail: (message: string) => void
}

export function createLoadState(): LoadStateController {
  const state = ref<LoadState>('idle')
  const errorMessage = ref('')

  return {
    state,
    errorMessage,
    start: () => {
      state.value = 'loading'
      errorMessage.value = ''
    },
    succeed: (empty = false) => {
      state.value = empty ? 'empty' : 'success'
    },
    fail: (message: string) => {
      state.value = 'error'
      errorMessage.value = message
    }
  }
}
