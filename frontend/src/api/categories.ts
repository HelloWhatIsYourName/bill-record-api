import { apiClient } from './index'
import type { Category, CategoryType } from './types'

export type CategoryPayload = {
  name: string
  type: CategoryType
  color?: string
  icon?: string
}

export const categoriesApi = {
  list(type?: CategoryType) {
    return apiClient.get<Category[]>(type ? `/categories?type=${type}` : '/categories')
  },

  create(payload: CategoryPayload) {
    return apiClient.post<Category>('/categories', payload)
  },

  update(id: string, payload: Partial<CategoryPayload>) {
    return apiClient.patch<Category>(`/categories/${id}`, payload)
  },

  archive(id: string) {
    return apiClient.delete<void>(`/categories/${id}`)
  },
}

