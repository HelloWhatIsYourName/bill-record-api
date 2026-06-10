import { ApiError } from '@/api/client'

export function toErrorMessage(error: unknown) {
  if (error instanceof ApiError) {
    const fieldMessage = error.fieldErrors.map((field) => `${field.field}: ${field.message}`).join('；')
    return fieldMessage ? `${error.message}：${fieldMessage}` : error.message
  }

  if (error instanceof Error) {
    return error.message
  }

  return '请求失败'
}

