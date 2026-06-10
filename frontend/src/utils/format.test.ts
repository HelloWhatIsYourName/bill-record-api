import { describe, expect, it } from 'vitest'

import { formatMoney, formatPercent, toMonthInputValue } from './format'

describe('format helpers', () => {
  it('formats money values without floating point drift', () => {
    expect(formatMoney('88.8', 'CNY')).toBe('CNY 88.80')
    expect(formatMoney(-12.345, 'USD')).toBe('USD -12.35')
  })

  it('formats percentages and month inputs for controls', () => {
    expect(formatPercent(88.8, 500)).toBe('17.8%')
    expect(formatPercent(0, 0)).toBe('0.0%')
    expect(toMonthInputValue(new Date('2026-06-10T01:00:00Z'))).toBe('2026-06')
  })
})
