export function formatMoney(value: string | number, currency = 'CNY') {
  const amount = Number(value)
  if (!Number.isFinite(amount)) {
    return `${currency} 0.00`
  }
  return `${currency} ${amount.toFixed(2)}`
}

export function formatPercent(value: string | number, total: string | number) {
  const current = Number(value)
  const max = Number(total)
  if (!Number.isFinite(current) || !Number.isFinite(max) || max <= 0) {
    return '0.0%'
  }
  return `${((current / max) * 100).toFixed(1)}%`
}

export function toMonthInputValue(date = new Date()) {
  const year = date.getUTCFullYear()
  const month = `${date.getUTCMonth() + 1}`.padStart(2, '0')
  return `${year}-${month}`
}

export function toDateTimeInputValue(date = new Date()) {
  return date.toISOString().slice(0, 16)
}

export function fromDateTimeInputValue(value: string) {
  return new Date(value).toISOString()
}

