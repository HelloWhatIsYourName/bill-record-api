import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import ProgressBar from './ProgressBar.vue'

describe('ProgressBar', () => {
  it('caps the visual width while exposing exact percentage text', () => {
    const wrapper = mount(ProgressBar, {
      props: {
        value: 640,
        max: 500,
        label: 'Food',
      },
    })

    expect(wrapper.text()).toContain('Food')
    expect(wrapper.text()).toContain('128.0%')
    expect(wrapper.find('.progress-bar__fill').attributes('style')).toContain('width: 100%')
  })
})
