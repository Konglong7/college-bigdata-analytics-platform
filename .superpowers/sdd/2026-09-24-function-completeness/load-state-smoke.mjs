import { createLoadState } from '../../../frontend/src/utils/loadState.ts'

const controller = createLoadState()
if (controller.state.value !== 'idle') throw new Error('initial state must be idle')
controller.start()
if (controller.state.value !== 'loading') throw new Error('start must enter loading')
controller.succeed(true)
if (controller.state.value !== 'empty') throw new Error('succeed(true) must enter empty')
controller.fail('接口不可用')
if (controller.state.value !== 'error' || controller.errorMessage.value !== '接口不可用') {
  throw new Error('fail must preserve error state and message')
}

console.log('load-state smoke passed')
