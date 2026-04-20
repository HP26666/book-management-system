export function formatDate(input) {
  if (!input) {
    return '-'
  }

  const date = new Date(input)
  if (Number.isNaN(date.getTime())) {
    return input
  }

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function formatDateTime(input) {
  if (!input) {
    return '-'
  }

  const date = new Date(input)
  if (Number.isNaN(date.getTime())) {
    return input
  }

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

export function formatMoney(value) {
  if (value === null || value === undefined || value === '') {
    return '0.00'
  }

  return Number(value).toFixed(2)
}

export function compactText(value, max = 80) {
  if (!value) {
    return ''
  }
  return value.length > max ? `${value.slice(0, max)}...` : value
}